# SOAPMethods.pm:  SOAP methods for BSS.
# BSS::Traceroute::RouteHandler is called to set up route before
# inserting info in database
#
# Note that all authentication and authorization handling is assumed
# to have been previously done by AAAS.  Use caution if running the
# BSS on a separate machine from the one running the AAAS.
#
# Last modified:  November 9, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

package BSS::Frontend::SOAPMethods;

use strict;

use Error qw(:try);
use Data::Dumper;

use Common::Exception;
use BSS::Frontend::Database;
use BSS::Frontend::Policy;
use BSS::Frontend::Stats;
use BSS::Traceroute::RouteHandler;

# until can get MySQL and views going
my @user_fields = ( 'reservation_id',
                    'user_dn',
                    'reservation_start_time',
                    'reservation_end_time',
                    'reservation_status',
                    'src_hostaddr_id',
                    'dst_hostaddr_id',
                    'reservation_tag');

my @detail_fields = ( 'reservation_id',
                    'reservation_start_time',
                    'reservation_end_time',
                    'reservation_created_time',
                    'reservation_time_zone',
                    'reservation_bandwidth',
                    'reservation_burst_limit',
                    'reservation_status',
                    'src_hostaddr_id',
                    'dst_hostaddr_id',
                    'reservation_description',
                    'reservation_src_port',
                    'reservation_dst_port',
                    'reservation_dscp',
                    'reservation_protocol',
                    'reservation_tag');

my @detail_admin_fields = ( 'ingress_interface_id',
                    'egress_interface_id',
                    'reservation_path',
                    'reservation_class');


###############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

sub initialize {
    my ($self) = @_;

    $self->{policy} = BSS::Frontend::Policy->new(
                       'dbconn' => $self->{dbconn});
    $self->{route_setup} = BSS::Traceroute::RouteHandler->new(
                                               'dbconn' => $self->{dbconn});
}
######


###############################################################################
# insert_reservation:  SOAP call to insert a row into the reservations table.  #
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: results hash.
#
sub insert_reservation {
    my( $self, $inref ) = @_;
    my( $duration_seconds );

    my $output_buf = $self->{route_setup}->find_interface_ids( $inref );
    print STDERR "past find_interface_ids\n";
    if (($inref->{ingress_interface_id} == 0) ||
        ($inref->{egress_interface_id} == 0))
    {
        throw Common::Exception("Invalid router id(s): 0.  Unable to " .
                                "do insert.");
    }
    $self->{dbconn}->setup_times($inref, $self->get_infinite_time());

    # convert requested bandwidth to bps
    $inref->{reservation_bandwidth} *= 1000000;
    $self->{policy}->check_oversubscribe($inref);

    # Get ipaddrs table id from source's and destination's host name or ip
    # address.
    $inref->{src_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($inref->{source_ip}); 
    $inref->{dst_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($inref->{destination_ip}); 

    $self->fill_fields($inref);
    my $outref = $self->insert_fields($inref);
    my $results = $self->get_results($inref, $outref);
    return( $results, $output_buf );
}
######

##############################################################################
# delete_reservation:  Given the reservation id, leave the reservation in the
#     db, but mark status as cancelled, and set the ending time to 0 so that 
#     find_expired_reservations will tear down the LSP if the reservation is
#     active.
#
sub delete_reservation {
    my( $self, $inref ) = @_;

    my $status =  $self->{dbconn}->update_status( $inref, 'precancel' );
    return( $self->get_reservation_details($inref), '');
}
######

###############################################################################
# get_all_reservations: get all reservations from the database
#
# In: reference to hash of parameters
# Out: reservations if any, and status message
#
sub get_all_reservations {
    my( $self, $inref ) = @_;

    my( $query );

    $query = "SELECT * FROM reservations" .
             " ORDER BY reservation_start_time";
    my $rows = $self->{dbconn}->do_query($query);
    return( $self->process_reservation_request($inref, $rows) );
}
######

###############################################################################
# get_user_reservations: get all user's reservations from the database
#
# In:  reference to hash of parameters
# Out: reservations if any, and status message
#
sub get_user_reservations {
    my( $self, $inref ) = @_;

    my( $query );

    # TODO:  fix authorization
    if ( $inref->{engr} ) {
        $query = "SELECT *";
    }
    else {
        $query = "SELECT " . join(', ', @user_fields);
    }
    $query .= " FROM reservations" .
              " WHERE user_dn = ?";
    $query .= " ORDER BY reservation_start_time";
    my $rows = $self->{dbconn}->do_query($query, $inref->{user_dn});
    return( $self->process_reservation_request($inref, $rows) );
}
######

###############################################################################
# get_reservation_details: get details for one reservation
#
# In: reference to hash of parameters
# Out: hash ref of results, status message
#
sub get_reservation_details {
    my( $self, $inref ) = @_;

    my( $query );

        # TODO:  Fix authorization checks
    if ( !($inref->{engr_permission}) ) {
        $query = "SELECT " . join(', ', @detail_fields);
    }
    else {
        $query = "SELECT " .
                 join(', ', (@detail_fields, @detail_admin_fields));
    }
    $query .= " FROM reservations" .
              " WHERE reservation_id = ?" .
              " ORDER BY reservation_start_time";
    my $rows = $self->{dbconn}->do_query($query, $inref->{reservation_id});
    return( $self->process_reservation_request($inref, $rows) );
}
######

###############################################################################
# process_reservation_request: handle get reservation(s) query, and
#                              reformat results before sending back
#
sub process_reservation_request {
    my( $self, $inref, $rows ) = @_;

    $self->{dbconn}->get_host_info($rows);
    $self->{dbconn}->convert_times($rows);
    # TODO:  fix authorization
    if ( $inref->{engr_permission} ) { 
        $self->{dbconn}->get_engr_fields($rows); 
    }
    $self->check_nulls($rows);
    my $results;
    $results->{rows} = $rows;
    return( $results, '' );
}
######

#################
# Private methods
#################

###############################################################################
# get_time_str:  print formatted time string
#
sub get_time_str {
    my( $self, $dtime, $gentag ) = @_;

    if ($gentag ne 'tag') {
        return $dtime;
    }
    my @ymd = split(' ', $dtime);
    return( $ymd[0] );
}
######

###############################################################################
# get_infinite_time:  returns "infinite" time
#
sub get_infinite_time {
    my( $self );

    return '2039-01-01 00:00:00';
}
######

###############################################################################
# fill_fields:  
#
sub fill_fields {
    my( $self, $inref );

    $self->{pss_configs} = $self->{dbconn}->get_pss_configs();
    $inref->{reservation_id} = 'NULL';
        # class of service
    $inref->{reservation_class} = $self->{pss_configs}->{pss_conf_CoS};
    $inref->{reservation_burst_limit} =
                                $self->{pss_configs}->{pss_conf_burst_limit};
    $inref->{reservation_status} = 'pending';
    $inref->{reservation_tag} = $inref->{user_dn} . '.' .
        $self->get_time_str($inref->{reservation_start_time}, 'tag') .  "-";

}
######

###############################################################################
# insert_fields:  
#
sub insert_fields {
    my( $self, $inref );

    my $query = "SHOW COLUMNS from reservations";
    my $rows = $self->{dbconn}->do_query( $query );
    my @insertions;
    my $outref = {}; 
    # TODO:  necessary to do insertions this way?
    for $_ ( @$rows ) {
       if ($inref->{$_->{Field}}) {
           $outref->{$_->{Field}} = $inref->{$_->{Field}};
           push(@insertions, $inref->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }

    # insert all fields for reservation into database
    $query = "INSERT INTO reservations VALUES (
             " . join( ', ', ('?') x @insertions ) . " )";
    my $unused = $self->{dbconn}->do_query($query, @insertions);
    # TODO:  FIX getting reservation id
    $outref->{reservation_id} = $self->{dbconn}->get_reservation_id();
    return( $outref );
}
######


###############################################################################
# get_results:  
#
sub get_results {
    my( $self, $inref, $outref );

    $outref->{reservation_tag} =
        $inref->{reservation_tag} . $outref->{reservation_id};
    # copy over non-db fields
    $outref->{source_host} = $inref->{source_host};
    $outref->{destination_host} = $inref->{destination_host};

    my $query = "UPDATE reservations SET reservation_tag = ?
                 WHERE reservation_id = ?";
    my $unused = $self->{dbconn}->do_query($query, $outref->{reservation_tag},
                                     $outref->{reservation_id});
    my @resv_array = ($outref);
    my $results;
    $results->{rows} = \@resv_array;
    # clean up NULL values
    $self->check_nulls($results->{rows});
    # convert times back to user's time zone for mail message
    $self->{dbconn}->convert_times($results->{rows});
    # get loopback fields if have engr privileges
    # TODO:  Fix authorization checks
    if ($self->{policy}->authorized($inref->{user_level}, "engr")) {
        $self->{dbconn}->get_engr_fields($results->{rows}); 
    }
    $results->{reservation_tag} =~ s/@/../;

}
######

###############################################################################
# check_nulls:  
#
sub check_nulls {
    my( $self, $rref ) = @_ ;

    my( $resv );

    for $resv (@$rref) {
        # clean up NULL values
        if (!$resv->{reservation_protocol} ||
            ($resv->{reservation_protocol} eq 'NULL')) {
            $resv->{reservation_protocol} = 'DEFAULT';
        }
        if (!$resv->{reservation_dscp} ||
            ($resv->{reservation_dscp} eq 'NU')) {
            $resv->{reservation_dscp} = 'DEFAULT';
        }
    }
}
######

1;
# vim: et ts=4 sw=4
