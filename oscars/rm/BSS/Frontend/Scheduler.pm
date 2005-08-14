# Scheduler.pm:  Database handling for BSS/Scheduler/SchedulerThread.pm
# Last modified: July 22, 2005
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

    $self->{dbconn} = BSS::Frontend::Database->new(
                       'database' => $self->{configs}->{use_BSS_database},
                       'login' => $self->{configs}->{BSS_login_name},
                       'password' => $self->{configs}->{BSS_login_passwd},
                       'configs' => $self->{configs})
                        or die "FATAL:  could not connect to database";
    $self->{stats} = BSS::Frontend::Stats->new();
}
######

###############################################################################
sub find_pending_reservations  { 
    my ( $self, $user_dn, $status ) = @_;

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
                            $self->{configs}->{reservation_time_interval},
                            $timezone );
    my $timeslot = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    if ($self->{configs}->{debug}) {
        print STDERR "pending: $timeslot\n";
    }
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
    my ( $self, $user_dn, $status ) = @_;

    my ( $sth, $data, $query );

    # FIX:  make SCHEDULER a bona fide db user
    #$self->{dbconn}->login_user($user_dn);
    $query = 'SELECT @@global.time_zone';
    $sth = $self->{dbconn}->do_query( $user_dn, $query );
    my $timezone = $sth->fetchrow_arrayref()->[0];
    $sth->finish();

    $query = "SELECT CONVERT_TZ(now() + INTERVAL ? SECOND, ?, '+00:00')";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                            $self->{configs}->{reservation_time_interval},
                            $timezone );
    my $timeslot = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    if ($self->{configs}->{debug}) {
        print STDERR "pending: $timeslot\n";
    }
    $query = qq{ SELECT * FROM reservations WHERE (reservation_status = ? and
                 reservation_end_time < ?) or (reservation_status = ?)};
    $sth = $self->{dbconn}->do_query($user_dn, $query, $status, $timeslot,
                                     $self->{configs}->{PENDING_CANCEL});
    # get all the data
    $data = $sth->fetchall_arrayref({});
    $sth->finish();
    return( $data );
}
######

###############################################################################
sub get_lsp_stats {
    my( $self, $user_dn, $lsp_info, $inref, $status ) = @_;

    my( $query, $sth, $config_time );

    $query = "SELECT CONVERT_TZ(now(), '+00:00', ?)";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $inref->{reservation_time_zone});
    $config_time = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    # convert to seconds before sending back
    $query = "SELECT CONVERT_TZ(?, '+00:00', ?)";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $inref->{reservation_start_time},
                                      $inref->{reservation_time_zone} );
    $inref->{reservation_start_time} = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $inref->{reservation_end_time},
                                      $inref->{reservation_time_zone} );
    $inref->{reservation_end_time} = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                                      $inref->{reservation_created_time},
                                      $inref->{reservation_time_zone} );
    $inref->{reservation_created_time} = $sth->fetchrow_arrayref()->[0];
    $sth->finish();
    my $results = $self->{stats}->get_lsp_stats($lsp_info, $inref,
                                         $status, $config_time);
    return $results;
}
######

1;
# vim: et ts=4 sw=4
