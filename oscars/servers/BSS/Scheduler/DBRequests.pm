package BSS::Scheduler::DBRequests;

# Database request handling for BSS scheduler
# Last modified:  November 15, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use DBI;
use Data::Dumper;

use BSS::Frontend::DBRequests;
use BSS::Frontend::Stats;

###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    my $db_login = 'oscars';
    my $password = 'ritazza6';

    $self->{dbconn} = BSS::Frontend::DBRequests->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";

    print STDERR "Scheduler running\n";
    $self->{debug} = $self->{dbconn}->get_debug_level();
    $self->{stats} = BSS::Frontend::Stats->new();
}
######

###############################################################################
sub find_pending_reservations  { 
    my ( $self, $time_interval ) = @_;

    my $status = 'pending';
    my $statement = "SELECT now() + INTERVAL ? SECOND AS newtime";
    my $row = $self->{dbconn}->get_row( $statement, $time_interval );
    my $timeslot = $row->{new_time};
    $statement = qq{ SELECT * FROM reservations WHERE reservation_status = ? and
                 reservation_start_time < ?};
    my $rows = $self->{dbconn}->do_query($statement, $status, $timeslot);
    return $rows;
}
######

###############################################################################
sub find_expired_reservations {
    my ( $self, $time_interval ) = @_;

    my $status = 'active';
    my $statement = "SELECT now() + INTERVAL ? SECOND AS newtime";
    my $row = $self->{dbconn}->get_row( $statement, $time_interval );
    my $timeslot = $row->{new_time};
    $statement = qq{ SELECT * FROM reservations WHERE (reservation_status = ? and
                 reservation_end_time < ?) or (reservation_status = ?)};
    my $rows = $self->{dbconn}->do_query($statement, $status, $timeslot,
                                        'precancel' );
    return $rows;
}
######

###############################################################################
#
sub get_time_intervals {
    my( $self ) = @_;

        # just use defaults for now
    my $statement = "SELECT server_db_poll_time, server_time_interval" .
             " FROM servers WHERE server_id = 1";
    my $row = $self->{dbconn}->get_row( $statement );
    return( $row->{server_db_poll_time},
            $row->{server_time_interval} );
}
######

###############################################################################
#
sub get_lsp_stats {
    my( $self, $resv, $status ) = @_;

    my $statement = "SELECT CONVERT_TZ(now(), '+00:00', ?) AS newtime";
    my $row = $self->{dbconn}->get_row( $statement,
                                        $resv->{reservation_time_zone});
    my $config_time = $row->{newtime};
    # convert to seconds before sending back
    $statement = "SELECT CONVERT_TZ(?, '+00:00', ?) AS newtime";
    $row = $self->{dbconn}->get_row( $statement, $resv->{reservation_start_time},
                                     $resv->{reservation_time_zone} );
    $resv->{reservation_start_time} = $row->{newtime};
    $row = $self->{dbconn}->get_row( $statement, $resv->{reservation_end_time},
                                     $resv->{reservation_time_zone} );
    $resv->{reservation_end_time} = $row->{newtime};
    $row = $self->{dbconn}->get_row( $statement,
                                     $resv->{reservation_created_time},
                                     $resv->{reservation_time_zone} );
    $resv->{reservation_created_time} = $row->{newtime};
    my $results = $self->{stats}->get_lsp_stats($resv, $status, $config_time);
    return $results;
}
######

1;
# vim: et ts=4 sw=4
