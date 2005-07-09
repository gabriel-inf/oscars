# Scheduler.pm:  Database handling for BSS/Scheduler/SchedulerThread.pm
# Last modified: July 8, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

package BSS::Frontend::Scheduler;

use strict;

use DBI;
use Data::Dumper;

use BSS::Frontend::Database;

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
}
######

###############################################################################
sub find_pending_reservations  { 
    my ( $self, $user_dn, $stime, $status ) = @_;

    my ( $sth, $data, $query );

    # FIX:  make SCHEDULER a bona fide db user
    # user dn in this case is the scheduler thread pseudo user
    #$self->{dbconn}->enforce_connection($user_dn);

    $query = qq{ SELECT * FROM reservations WHERE reservation_status = ? and
                 reservation_start_time < ?};
    $sth = $self->{dbconn}->do_query($user_dn, $query, $status, $stime);
    $data = $sth->fetchall_arrayref({});
    $sth->finish();
    return( "", $data );
}
######

###############################################################################
sub find_expired_reservations {
    my ( $self, $user_dn, $stime, $status ) = @_;

    my ( $sth, $data, $query );

    # FIX:  make SCHEDULER a bona fide db user
    #$self->{dbconn}->enforce_connection($user_dn);

    #print "expired: Looking at time == " . $stime . "\n";

    $query = qq{ SELECT * FROM reservations WHERE (reservation_status = ? and
                 reservation_end_time < ?) or (reservation_status = ?)};
    $sth = $self->{dbconn}->do_query($user_dn, $query, $status, $stime,
                                     $self->{configs}->{PENDING_CANCEL});
    # get all the data
    $data = $sth->fetchall_arrayref({});
    $sth->finish();
    return( $data );
}
######

1;
# vim: et ts=4 sw=4
