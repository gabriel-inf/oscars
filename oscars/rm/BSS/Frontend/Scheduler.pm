# Scheduler.pm:  Database handling for BSS/Scheduler/SchedulerThread.pm
# Last modified: June 29, 2005
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

    my ( $sth, $data, $query, $error_msg );
    my $results = {};

    # user dn in this case is the scheduler thread pseudo user
    $results->{error_msg} = $self->{dbconn}->enforce_connection($user_dn);
    if ($results->{error_msg}) { return( 1, $results); }

    $query = qq{ SELECT * FROM reservations WHERE reservation_status = ? and
                 reservation_start_time < ?};
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query, $status,
                                                            $stime);
    if ( $results->{error_msg} ) { return( 1, $results ); }

    # get all the data
    $data = $sth->fetchall_arrayref({});
    # close it up
    $sth->finish();

    return( "", $data );
}
######

###############################################################################
sub find_expired_reservations {
    my ( $self, $user_dn, $stime, $status ) = @_;

    my ( $sth, $data, $query, $error_msg);
    my $results = {};

    $results->{error_msg} = $self->{dbconn}->enforce_connection($user_dn);
    if ($results->{error_msg}) { return( 1, $results); }

    #print "expired: Looking at time == " . $stime . "\n";

    $query = qq{ SELECT * FROM reservations WHERE (reservation_status = ? and
                 reservation_end_time < ?) or (reservation_status = ?)};
    ($sth, $results->{error_msg}) = $self->{dbconn}->do_query($user_dn, $query, $status,
                                        $stime,
                                        $self->{configs}->{PENDING_CANCEL});
    if ( $results->{error_msg} ) { return( 1, $results ); }

    # get all the data
    $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish();

    # return the answer
    return( "", $data );
}
######

1;
# vim: et ts=4 sw=4
