# DBRequests.pm:  Database handling for BSS/Scheduler/Poller.pm
# Last modified: November 5, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Scheduler::DBRequests;

use strict;

use DBI;
use Data::Dumper;

use BSS::Frontend::Database;
use BSS::Frontend::Stats;

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

    my $db_login = 'oscars';
    my $password = 'ritazza6';

    $self->{dbconn} = BSS::Frontend::Database->new(
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

    my $query = 'SELECT @@global.time_zone AS timezone';
    my $rows = $self->{dbconn}->do_query( $query );
    my $timezone = $rows->[0]->{timezone};
    my $status = 'pending';

    $query = "SELECT CONVERT_TZ(now() + INTERVAL ? SECOND, ?, '+00:00') " .
             "AS newtime";
    $rows = $self->{dbconn}->do_query( $query, $time_interval, $timezone );
    my $timeslot = $rows->[0]->{new_time};
    $query = qq{ SELECT * FROM reservations WHERE reservation_status = ? and
                 reservation_start_time < ?};
    $rows = $self->{dbconn}->do_query($query, $status, $timeslot);
    return( "", $rows );
}
######

###############################################################################
sub find_expired_reservations {
    my ( $self, $time_interval ) = @_;

    my $query = 'SELECT @@global.time_zone AS timezone';
    my $rows = $self->{dbconn}->do_query( $query );
    my $timezone = $rows->[0]->{timezone};
    my $status = 'active';

    $query = "SELECT CONVERT_TZ(now() + INTERVAL ? SECOND, ?, '+00:00')" .
             " AS newtime";
    $rows = $self->{dbconn}->do_query( $query, $time_interval, $timezone );
    my $timeslot = $rows->[0]->{new_time};
    $query = qq{ SELECT * FROM reservations WHERE (reservation_status = ? and
                 reservation_end_time < ?) or (reservation_status = ?)};
    $rows = $self->{dbconn}->do_query($query, $status, $timeslot,
                                      'precancel' );
    return( $rows );
}
######

###############################################################################
#
sub get_time_intervals {
    my( $self ) = @_;

        # just use defaults for now
    my $query = "SELECT server_db_poll_time, server_time_interval" .
             " FROM servers WHERE server_id = 1";
    my $rows = $self->{dbconn}->do_query( $query );
    return( $rows->[0]->{server_db_poll_time},
            $rows->[0]->{server_time_interval} );
}
######

###############################################################################
#
sub get_lsp_stats {
    my( $self, $resv, $status ) = @_;

    my $query = "SELECT CONVERT_TZ(now(), '+00:00', ?) AS newtime";
    my $rows = $self->{dbconn}->do_query( $query,
                                          $resv->{reservation_time_zone});
    my $config_time = $rows->[0]->{newtime};
    # convert to seconds before sending back
    $query = "SELECT CONVERT_TZ(?, '+00:00', ?) AS newtime";
    $rows = $self->{dbconn}->do_query( $query, $resv->{reservation_start_time},
                                       $resv->{reservation_time_zone} );
    $resv->{reservation_start_time} = $rows->[0]->{newtime};
    $rows = $self->{dbconn}->do_query( $query, $resv->{reservation_end_time},
                                       $resv->{reservation_time_zone} );
    $resv->{reservation_end_time} = $rows->[0]->{newtime};
    $rows = $self->{dbconn}->do_query( $query,
                                       $resv->{reservation_created_time},
                                       $resv->{reservation_time_zone} );
    $resv->{reservation_created_time} = $rows->[0]->{newtime};
    my $results = $self->{stats}->get_lsp_stats($resv, $status, $config_time);
    return $results;
}
######

1;
# vim: et ts=4 sw=4
