###############################################################################
package OSCARS::BSS::Methods;

# SOAP methods for BSS.
#
# Note that all authentication and authorization handling is assumed
# to have been previously done by AAAS.  Use caution if running the
# BSS on a separate machine from the one running the AAAS.
#
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use Error qw(:try);
use Data::Dumper;

use strict;

use OSCARS::BSS::Database;
use OSCARS::BSS::Policy;
use OSCARS::BSS::RouteHandler;
use OSCARS::BSS::UpdateRouterTables;
use OSCARS::PSS::JnxLSP;

# until can get MySQL and views going
my $user_fields =
    'reservation_id, user_dn, ' .
    'reservation_start_time, reservation_end_time, reservation_status, ' .
    'src_hostaddr_id, dst_hostaddr_id, reservation_tag';


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{policy} = OSCARS::BSS::Policy->new( 'dbconn' => $self->{dbconn});
    $self->{route_setup} = OSCARS::BSS::RouteHandler->new(
                      'dbconn' => $self->{dbconn});
    $self->{router_updater} = OSCARS::BSS::UpdateRouterTables->new(
                      'dbconn' => $self->{dbconn});
    # PSS setup
    $self->{LSP_SETUP} = 1;
    $self->{LSP_TEARDOWN} = 0;
    $self->{pss_configs} = $self->{dbconn}->get_pss_configs();

} #____________________________________________________________________________ 


###############################################################################
# CreateReservation:  SOAP call to insert a row into the reservations table. 
#     OSCARS::BSS::RouteHandler is called to set up the route before
#     inserting a reservation in the database
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: ref to results hash.
#
sub CreateReservation {
    my( $self, $params ) = @_;
    my( $duration_seconds );

    # params fields having to do with traceroute modified in find_interface_ids
    $self->{route_setup}->find_interface_ids($params);
    $self->{dbconn}->setup_times($params, $self->get_infinite_time());
    ( $params->{reservation_class}, $params->{reservation_burst_limit} ) =
                    $self->{route_setup}->get_pss_fields();

    # convert requested bandwidth to bps
    $params->{reservation_bandwidth} *= 1000000;
    $self->{policy}->check_oversubscribe($params);

    # Get ipaddrs table id from source's and destination's host name or ip
    # address.
    $params->{src_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($params->{source_ip}); 
    $params->{dst_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($params->{destination_ip}); 

    my $results = $self->get_results($params);
    return $results;
} #____________________________________________________________________________ 


###############################################################################
# CancelReservation:  Given the reservation id, leave the reservation in the
#     db, but mark status as cancelled, and set the ending time to 0 so that 
#     find_expired_reservations will tear down the LSP if the reservation is
#     active.
#
sub CancelReservation {
    my( $self, $params ) = @_;

    my $status =  $self->{dbconn}->update_status( $params, 'precancel' );
    return $self->ViewDetails($params);
} #____________________________________________________________________________ 


###############################################################################
# ViewReservations:  get reservations from the database.  If the user has
#     engr privileges, they can view all reservations.  Otherwise they can
#     only view their own.
#
# In:  reference to hash of parameters
# Out: reference to array of hashes
#
sub ViewReservations {
    my( $self, $params ) = @_;

    my( $statement, $rows );

    if ( $params->{engr_permission} ) {
        $statement = 'SELECT * FROM reservations';
    }
    else {
        $statement = "SELECT $user_fields FROM reservations" .
                     ' WHERE user_dn = ?';
    }
    $statement .= ' ORDER BY reservation_start_time';
    if ( $params->{engr_permission} ) {
        $rows = $self->{dbconn}->do_query($statement);
    }
    else {
        $rows = $self->{dbconn}->do_query($statement, $params->{user_dn});
    }
    
    # get additional fields if getting reservation details and user
    # has permission
    if ( $params->{engr_permission} && $params->{reservation_id} ) { 
        $self->{dbconn}->get_engr_fields($rows->[0]); 
    }
    for my $resv ( @$rows ) {
        $self->{dbconn}->convert_times($resv);
        $self->{dbconn}->get_host_info($resv);
        $self->check_nulls($resv);
    }
    return $rows;
} #____________________________________________________________________________ 


###############################################################################
# ViewDetails:  get reservation details from the database, given its
#     reservation id.  If a user has engr privileges, they can view any 
#     reservation's details.  Otherwise they can only view reservations that
#     they have made, with less of the details.
#
# In:  reference to hash of parameters
# Out: reservations if any, and status message
#
sub ViewDetails {
    my( $self, $params ) = @_;

    my( $statement, $row );

    if ( $params->{engr_permission} ) {
        $statement = 'SELECT * FROM reservations';
        $statement .= ' WHERE reservation_id = ?';
    }
    else {
        $statement = "SELECT $user_fields FROM reservations" .
                     ' WHERE user_dn = ?';
        $statement .= ' AND reservation_id = ?';
    }
    if ( $params->{engr_permission} ) {
        $row = $self->{dbconn}->get_row($statement, $params->{reservation_id});
    }
    else {
        $row = $self->{dbconn}->get_row($statement, $params->{user_dn},
                                        $params->{reservation_id});
    }
    if (!$row) { return $row; }
    
    # get additional fields if getting reservation details and user
    # has permission
    if ( $params->{engr_permission} ) { 
        $self->{dbconn}->get_engr_fields($row); 
    }
    $self->{dbconn}->convert_times($row);
    $self->{dbconn}->get_host_info($row);
    $self->check_nulls($row);
    return $row;
} #____________________________________________________________________________ 


###################
# Scheduler methods
###################


###############################################################################
# find_pending_reservations:  find reservations to run.  Find all the
#    reservatations in db that need to be setup and run in the next N minutes.
#
sub find_pending_reservations {
    my( $self, $params ) = @_;

    my( $reservations, $status );
    my( $error_msg );

    # find reservations that need to be scheduled
    $reservations = $self->{dbconn}->find_pending_reservations(
                                                      $params->{time_interval});
    if (!@$reservations) { return $reservations; }

    for my $resv (@$reservations) {
        $self->{dbconn}->map_to_ips($resv);
        # call PSS to schedule LSP
        $resv->{lsp_status} = $self->setup_pss($params, $resv);
        $self->update_reservation( $params, $resv, 'active' );
    }
    return $reservations;
} #____________________________________________________________________________ 


###############################################################################
# find_expired_reservations:  find reservations that have expired, and tear
#                             them down
#
sub find_expired_reservations {
    my ($self, $params) = @_;

    my( $reservations, $status );
    my( $error_msg );

    # find reservations whose end time is before the current time and
    # thus expired
    $reservations = $self->{dbconn}->find_expired_reservations(
                                                     $params->{time_interval});
    if (!@$reservations) { return $reservations; }

    for my $resv (@$reservations) {
        $self->{dbconn}->map_to_ips($resv);
        $resv->{lsp_status} = $self->teardown_pss($params, $resv);
        $self->update_reservation( $resv, 'finished' );
    }
    return $reservations;
} #____________________________________________________________________________ 


###############################################################################
# update_router_tables:  Update information in router, interface, and ipaddrs
#                        tables
#
sub update_router_tables {
    my ($self, $params) = @_;

    $self->{router_updater}->update_router_info($params);
    return { 'msg' => 'success' } ;
} #____________________________________________________________________________ 


#################
# Private methods
#################

###############################################################################
# get_time_str:  print formatted time string
#
sub get_time_str {
    my( $self, $dtime ) = @_;

    my @ymd = split(' ', $dtime);
    return $ymd[0];
} #____________________________________________________________________________ 


###############################################################################
# get_infinite_time:  returns "infinite" time
#
sub get_infinite_time {
    my( $self ) = @_;

    return '2039-01-01 00:00:00';
} #____________________________________________________________________________ 


###############################################################################
# get_results:  
#
sub get_results {
    my( $self, $params ) = @_;

    # build fields to insert
    my $statement = 'SHOW COLUMNS from reservations';
    my $rows = $self->{dbconn}->do_query( $statement );
    my @insertions;
    my $results = {}; 
    # TODO:  necessary to do insertions this way?
    for $_ ( @$rows ) {
       if ($params->{$_->{Field}}) {
           $results->{$_->{Field}} = $params->{$_->{Field}};
           push(@insertions, $params->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }

    # insert all fields for reservation into database
    $statement = "INSERT INTO reservations VALUES (
             " . join( ', ', ('?') x @insertions ) . " )";
    my $unused = $self->{dbconn}->do_query($statement, @insertions);
    $results->{reservation_id} = $self->{dbconn}->get_primary_id();
    # copy over non-db fields
    $results->{source_host} = $params->{source_host};
    $results->{destination_host} = $params->{destination_host};
    # clean up NULL values
    $self->check_nulls($results);
    # convert times back to user's time zone for mail message
    $self->{dbconn}->convert_times($results);

    # set user-semi-readable tag
    $results->{reservation_tag} = $params->{user_dn} . '.' .
        $self->get_time_str($params->{reservation_start_time}) .  "-" .
        $results->{reservation_id};
    $statement = "UPDATE reservations SET reservation_tag = ?,
                 reservation_status = 'pending'
                 WHERE reservation_id = ?";
    $unused = $self->{dbconn}->do_query($statement,
                      $results->{reservation_tag}, $results->{reservation_id});
    # get loopback fields if have engr privileges
    # TODO:  need authorization for these fields
    $self->{dbconn}->get_engr_fields($results); 
    $results->{reservation_tag} =~ s/@/../;
    $results->{reservation_status} = 'pending';
    return $results;
} #____________________________________________________________________________ 


###############################################################################
# check_nulls:  
#
sub check_nulls {
    my( $self, $resv ) = @_ ;

    # clean up NULL values
    if (!$resv->{reservation_protocol} ||
        ($resv->{reservation_protocol} eq 'NULL')) {
        $resv->{reservation_protocol} = 'DEFAULT';
    }
    if (!$resv->{reservation_dscp} ||
        ($resv->{reservation_dscp} eq 'NU')) {
        $resv->{reservation_dscp} = 'DEFAULT';
    }
} #____________________________________________________________________________ 


###############################################################################
# setup_pss:  format the args and call pss to do the configuration change
#
sub setup_pss {
    my( $self, $params, $resv_info ) = @_;   

    my( $error );

    $params->{logger}->write_log("Creating lsp_info...");

        # Create an LSP object.
    my $lsp_info = $self->map_fields($resv_info);
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);

    $params->{logger}->write_log("Setting up LSP...");
    $jnxLsp->configure_lsp($self->{LSP_SETUP}, $resv_info, $params->{logger});
    if ($error = $jnxLsp->get_error())  {
        return $error;
    }
    $params->{logger}->write_log("LSP setup complete");
    return "";
} #____________________________________________________________________________ 


###############################################################################
# teardown_pss:  format the args and call pss to teardown the configuraion 
#
sub teardown_pss {
    my ( $self, $params, $resv_info ) = @_;

    my ( $error );

        # Create an LSP object.
    my $lsp_info = $self->map_fields($resv_info);
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);

    $params->{logger}->write_log("Tearing down LSP...");
    $jnxLsp->configure_lsp($self->{LSP_TEARDOWN}, $resv_info, $params->{logger}); 
    if ($error = $jnxLsp->get_error())  {
        return $error;
    }
    $params->{logger}->write_log("LSP teardown complete");
    return "";
} #____________________________________________________________________________ 


###############################################################################
# update_reservation: change the status of the reservervation from pending to
#                     active
#
sub update_reservation {
    my ($self, $params, $resv, $status) = @_;

    $params->{logger}->write_log("Updating status of reservation $resv->{reservation_id} to ");
    if ( !$resv->{lsp_status} ) {
        $resv->{lsp_status} = "Successful configuration";
        $status = $self->{dbconn}->update_status($resv, $status);
    } else {
        $status = $self->{dbconn}->update_status($resv, 'failed');
    }
    $params->{logger}->write_log("$status");
} #____________________________________________________________________________ 


###############################################################################
#
sub map_fields {
    my ( $self, $resv ) = @_;

    my ( %lsp_info );

    %lsp_info = (
      'name' => "oscars_$resv->{reservation_id}",
      'lsp_from' => $resv->{ingress_ip},
      'lsp_to' => $resv->{egress_ip},
      'bandwidth' => $resv->{reservation_bandwidth},
      'lsp_class-of-service' => $resv->{reservation_class},
      'policer_burst-size-limit' =>  $resv->{reservation_burst_limit},
      'source-address' => $resv->{source_ip},
      'destination-address' => $resv->{destination_ip},
    );
    if ($resv->{reservation_src_port} &&
        ($resv->{reservation_src_port} != 'NULL')) {
        $lsp_info{'source-port'} = $resv->{reservation_src_port};
    }
    if ($resv->{reservation_dst_port} &&
      ($resv->{reservation_dst_port} != 'NULL')) {
        $lsp_info{'destination-port'} = $resv->{reservation_dst_port};
    }
    if ($resv->{reservation_dscp} &&
      ($resv->{reservation_dscp} != 'NULL')) {
        $lsp_info{dscp} = $resv->{reservation_dscp};
    }
    if ($resv->{reservation_protocol} &&
      ($resv->{reservation_protocol} != 'NULL')) {
        $lsp_info{protocol} = $resv->{reservation_protocol};
    }
    $lsp_info{configs} = $self->{pss_configs};
    return \%lsp_info;
} #____________________________________________________________________________ 


######
1;
# vim: et ts=4 sw=4
