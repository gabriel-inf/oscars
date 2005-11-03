# Reservation.pm:  SOAP methods for BSS; calls 
# BSS::Scheduler::ReservationHandler to set up reservations before
# inserting info in database
#
# Last modified: November 2, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Frontend::Reservation;

use strict;

use Error qw(:try);
use Data::Dumper;

use Common::Mail;
use Common::Exception;
use BSS::Frontend::Database;
use BSS::Frontend::Policy;
use BSS::Frontend::Stats;
use BSS::Scheduler::ReservationHandler;

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
    my $trace_configs = $self->{dbconn}->get_trace_configs();
    $self->{route_setup} = BSS::Scheduler::ReservationHandler->new(
                                               'dbconn' => $self->{dbconn},
                                               'configs' => $trace_configs);
}
######


###############################################################################
# insert_reservation:  Called from the scheduler to insert a row into the
#   reservations table.  Error checking so far is assumed to have been done by
#   scheduler and CGI script.
#
# In:  reference to hash.  Hash's keys are all the fields of the reservations
#      table except for the primary key.
# Out: results hash.
#
sub insert_reservation {
    my( $self, $inref ) = @_;
    my( $query, $sth );
    my $user_dn = $inref->{user_dn};
    my( $duration_seconds );

    my $output_buf = $self->{route_setup}->insert_reservation( $inref );
    if (($inref->{ingress_interface_id} == 0) ||
        ($inref->{egress_interface_id} == 0))
    {
        throw Common::Exception("Invalid router id(s): 0.  Unable to " .
                                "do insert.");
    }
    $self->{dbconn}->login_user($user_dn);
    my $stats = BSS::Frontend::Stats->new();
    $self->{dbconn}->setup_times($inref, $user_dn, $stats);

    # convert requested bandwidth to bps
    $inref->{reservation_bandwidth} *= 1000000;
    $self->{policy}->check_oversubscribe($inref, $user_dn);

    # Get ipaddrs table id from source's and destination's host name or ip
    # address.
    $inref->{src_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($inref->{user_dn},
                $inref->{source_ip}); 
    $inref->{dst_hostaddr_id} =
        $self->{dbconn}->hostaddrs_ip_to_id($inref->{user_dn},
                                            $inref->{destination_ip}); 

    $self->fill_fields($inref, $user_dn, $stats);
    my $outref = $self->insert_fields($inref, $user_dn);
    my $results = $self->get_results($inref, $outref, $user_dn);

    my $mailer = Common::Mail->new();
    my $mail_msg = $stats->get_stats($user_dn, $results->{rows}[0]) ;
    $mailer->send_mail($mailer->get_webmaster(), $mailer->get_admins(),
                       "Reservation made by $user_dn", $mail_msg);
    $mailer->send_mail($mailer->get_webmaster(), $user_dn,
                       "Your reservation has been accepted", $mail_msg);
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

    my $status =  $self->{dbconn}->update_reservation( $inref->{user_dn}, $inref,
                                     'precancel' );
    return($self->get_reservations($inref), '');
}
######

###############################################################################
# get_reservations: gets the reservation list from the database
#
# In: reference to hash of parameters, id if only want one reservation
# Out: success or failure, and status message
#
sub get_reservations {
    my( $self, $inref ) = @_;

    my( $sth, $query );
    my( $rref );
    my $user_dn = $inref->{user_dn};

    $self->{dbconn}->login_user($user_dn);
    # If administrator is making request, show all reservations.  Otherwise,
    # show only the user's reservations.  If id is given, show only the results
    # for that reservation.  Sort by start time in ascending order.
    if ($inref->{reservation_id}) {
        if ( !($self->{policy}->authorized($inref->{user_level}, "engr")) ) {
            $query = "SELECT " . join(', ', @detail_fields);
        }
        else {
            $query = "SELECT " .
                     join(', ', (@detail_fields, @detail_admin_fields));
        }
        $query .= " FROM reservations" .
                  " WHERE reservation_id = $inref->{reservation_id}";
    }
    elsif ($user_dn) {
        if ( $self->{policy}->authorized($inref->{user_level}, "engr") ) {
            $query = "SELECT * FROM reservations";
        }
        else {
            $query = "SELECT " . join(', ', @user_fields);
            $query .= " FROM reservations" .
                      " WHERE user_dn = '$user_dn'";
        }
    }
    $query .= " ORDER BY reservation_start_time";
    $sth = $self->{dbconn}->do_query($user_dn, $query);
    $rref = $sth->fetchall_arrayref({});
    $sth->finish();

    $self->{dbconn}->get_host_info($user_dn, $rref);
    $self->{dbconn}->convert_times($user_dn, $rref);
    if ($self->{policy}->authorized($inref->{user_level}, "engr") &&
        $inref->{reservation_id}) {
        # in this case, only one row
        $self->{dbconn}->get_engr_fields($user_dn, $rref); 
        $self->check_nulls($rref);
    }
    my $results;
    $results->{rows} = $rref;
    return( $results, '' );
}
######

###############################################################################
sub logout {
    my( $self, $inref ) = @_;

    return( $self->{dbconn}->logout($inref->{user_dn}) );
}
######

#################
# Private methods
#################

###############################################################################
# fill_fields:  
#
sub fill_fields {
    my( $self, $inref, $user_dn, $stats );

    $self->{pss_configs} = $self->{dbconn}->get_pss_configs();
    $inref->{reservation_id} = 'NULL';
        # class of service
    $inref->{reservation_class} = $self->{pss_configs}->{pss_conf_CoS};
    $inref->{reservation_burst_limit} =
                                $self->{pss_configs}->{pss_conf_burst_limit};
    $inref->{reservation_status} = 'pending';
    $inref->{reservation_tag} = $user_dn . '.' .
        $stats->get_time_str($inref->{reservation_start_time}, 'tag') .  "-";

}
######

###############################################################################
# insert_fields:  
#
sub insert_fields {
    my( $self, $inref, $user_dn );

    my( $query, $sth );

    $query = "SHOW COLUMNS from reservations";
    $sth = $self->{dbconn}->do_query( $user_dn, $query );
    my $arrayref = $sth->fetchall_arrayref({});
    my @insertions;
    my $outref = {}; 
    for $_ ( @$arrayref ) {
       if ($inref->{$_->{Field}}) {
           $outref->{$_->{Field}} = $inref->{$_->{Field}};
           push(@insertions, $inref->{$_->{Field}}); 
       }
       else{ push(@insertions, 'NULL'); }
    }
    $sth->finish();

    # insert all fields for reservation into database
    $query = "INSERT INTO reservations VALUES (
             " . join( ', ', ('?') x @insertions ) . " )";
    $sth = $self->{dbconn}->do_query($user_dn, $query, @insertions);
    $outref->{reservation_id} = $self->{dbconn}->get_reservation_id($user_dn);
    $sth->finish();
    return( $outref );
}
######


###############################################################################
# get_results:  
#
sub get_results {
    my( $self, $inref, $outref, $user_dn );

    my( $query, $sth );

    $outref->{reservation_tag} =
        $inref->{reservation_tag} . $outref->{reservation_id};
    # copy over non-db fields
    $outref->{source_host} = $inref->{source_host};
    $outref->{destination_host} = $inref->{destination_host};

    $query = "UPDATE reservations SET reservation_tag = ?
              WHERE reservation_id = ?";
    $sth = $self->{dbconn}->do_query($user_dn, $query,
        $outref->{reservation_tag}, $outref->{reservation_id});
    $sth->finish();

    my @resv_array = ($outref);
    my $results;
    $results->{rows} = \@resv_array;
    # clean up NULL values
    $self->check_nulls($results->{rows});
    # convert times back to user's time zone for mail message
    $self->{dbconn}->convert_times($user_dn, $results->{rows});
    # get loopback fields if have engr privileges
    if ($self->{policy}->authorized($inref->{user_level}, "engr")) {
        $self->{dbconn}->get_engr_fields($user_dn, $results->{rows}); 
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
