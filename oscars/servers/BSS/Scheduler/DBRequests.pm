###############################################################################
package BSS::Scheduler::DBRequests;

# Database request handling for BSS scheduler
# Last modified:  November 21, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use DBI;
use Data::Dumper;

use BSS::Frontend::DBRequests;


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
} #____________________________________________________________________________ 


###############################################################################
#
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
} #____________________________________________________________________________ 


###############################################################################
#
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
} #____________________________________________________________________________ 


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
} #____________________________________________________________________________ 


######
1;
# vim: et ts=4 sw=4
