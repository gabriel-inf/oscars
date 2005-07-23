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
    #$self->{dbconn}->enforce_connection($user_dn);
    $query = "SELECT now() + INTERVAL ? SECOND";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                            $self->{configs}->{reservation_time_interval} );
    my $timeslot = $sth->fetchrow_arrayref()->[0];
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
    #$self->{dbconn}->enforce_connection($user_dn);
    $query = "SELECT now() + INTERVAL ? SECOND";
    $sth = $self->{dbconn}->do_query( $user_dn, $query,
                            $self->{configs}->{reservation_time_interval} );
    my $timeslot = $sth->fetchrow_arrayref()->[0];
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
    my( $self, $user_dn, $lsp_info, $inref, $status, $config_time) = @_;

    my( $query, $sth, $config_time );

    $query = "SELECT now()";
    $sth = $self->{dbconn}->do_query( $user_dn, $query );
    $config_time = $sth->fetchrow_arrayref()->[0];
    return $self->{stats}->get_lsp_stats($lsp_info, $inref,
                                         $status, $config_time);
}
######

1;
# vim: et ts=4 sw=4
