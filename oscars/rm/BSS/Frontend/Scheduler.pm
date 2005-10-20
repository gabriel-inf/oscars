# Scheduler.pm:  Database handling for BSS/Scheduler/SchedulerThread.pm
# Last modified: October 18, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Frontend::Scheduler;

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

    $self->{stats} = BSS::Frontend::Stats->new();
}
######

###############################################################################
sub find_pending_reservations  { 
    my ( $self, $user_dn, $status, $time_interval ) = @_;

    my ( $sth, $data, $query );

    # FIX:  make SCHEDULER a bona fide db user
    # user dn in this case is the scheduler thread pseudo user
    #$self->{dbconn}->login_user($user_dn);
    $query = 'SELECT @@global.time_zone';
    $sth = $self->{dbconn}->do_query( $user_dn, $query );
    my $timezone = $sth->fetchrow_arrayref()->[0];
    $sth->finish();

    $query = "SELECT CONVERT_TZ(now() + INTERVAL ? SECOND, ?, '+00:00')";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                            $time_interval, $timezone );
    my $timeslot = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    $query = qq{ SELECT * FROM reservations WHERE reservation_status = ? and
                 reservation_start_time < ?};
    $sth = $self->{dbconn}->do_query($user_dn, $query, $status, $timeslot);
    $data = $sth->fetchall_arrayref({});
    $sth->finish();
    return( "", $data );
}
######

###############################################################################
sub find_expired_reservations {
    my ( $self, $user_dn, $status, $time_interval ) = @_;

    my ( $sth, $data, $query );

    # FIX:  make SCHEDULER a bona fide db user
    #$self->{dbconn}->login_user($user_dn);
    $query = 'SELECT @@global.time_zone';
    $sth = $self->{dbconn}->do_query( $user_dn, $query );
    my $timezone = $sth->fetchrow_arrayref()->[0];
    $sth->finish();

    $query = "SELECT CONVERT_TZ(now() + INTERVAL ? SECOND, ?, '+00:00')";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                            $time_interval, $timezone );
    my $timeslot = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    $query = qq{ SELECT * FROM reservations WHERE (reservation_status = ? and
                 reservation_end_time < ?) or (reservation_status = ?)};
    $sth = $self->{dbconn}->do_query($user_dn, $query, $status, $timeslot,
                                     'precancel' );
    # get all the data
    $data = $sth->fetchall_arrayref({});
    $sth->finish();
    return( $data );
}
######

###############################################################################
#
sub get_time_intervals {
    my( $self, $user_dn ) = @_;

    my( $sth, $query );

        # just use defaults for now
    $query = "SELECT server_db_poll_time, server_time_interval" .
             " FROM servers WHERE server_id = 1";
    $sth = $self->{dbconn}->do_query( $user_dn, $query );
    my $ref = $sth->fetchrow_hashref();
    $sth->finish();
    return( $ref->{server_db_poll_time}, $ref->{server_time_interval} );
}
######

###############################################################################
#
sub get_lsp_stats {
    my( $self, $user_dn, $resv, $status ) = @_;

    my( $query, $sth, $config_time );

    $query = "SELECT CONVERT_TZ(now(), '+00:00', ?)";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $resv->{reservation_time_zone});
    $config_time = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    # convert to seconds before sending back
    $query = "SELECT CONVERT_TZ(?, '+00:00', ?)";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $resv->{reservation_start_time},
                                      $resv->{reservation_time_zone} );
    $resv->{reservation_start_time} = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $resv->{reservation_end_time},
                                      $resv->{reservation_time_zone} );
    $resv->{reservation_end_time} = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $resv->{reservation_created_time},
                                      $resv->{reservation_time_zone} );
    $resv->{reservation_created_time} = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    my $results = $self->{stats}->get_lsp_stats($resv, $status, $config_time);
    return $results;
}
######

1;
# vim: et ts=4 sw=4
