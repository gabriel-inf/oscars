#######################################################################
# FOO
#
# JRLee
#######################################################################

package Foo;
use DBI;
use strict; 

######################################################################
sub new {
  my ($_class, %_args) = @_;
  my ($_self) = {%_args};
  
  # Bless $_self into designated class.
  bless($_self, $_class);
  
  # Initialize.
  $_self->initialize();
  
  return($_self);
}

######################################################################
sub initialize {
    my ($self) = @_;
    $self->{'dbh'} = DBI->connect('DBI:mysql:BSS', 
             $self->{db_login_name}, $self->{'db_login_passwd'})
            or die "Couldn't connect to database: " . DBI->errstr;
}

######################################################################
sub find_pending_reservations  {

    my ($self, $stime, $status) = @_;
    my ($sth);

    #print "pending: Looking at time == $stime \n";

    $sth = $self->{'dbh'}->prepare( qq{ SELECT * FROM reservations WHERE reservation_status = ? and reservation_start_time < ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;

    $sth->execute( $status, $stime );

    # get all the data
    my $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish;

    # return the answer
    return $data
}

######################################################################
sub find_expired_reservations  {

    my ($self, $stime, $status) = @_;
    my ($sth);

    #print "expired: Looking at time == " . $stime . "\n";

    $sth = $self->{'dbh'}->prepare( qq{ SELECT * FROM reservations WHERE reservation_status = ? and reservation_end_time < ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;

    $sth->execute( $status, $stime );

    # get all the data
    my $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish;

    # return the answer
    return $data
}

######################################################################
sub ipidx2ip {

    my ($self, $idx) = @_;
    my ($sth);

    $sth = $self->{'dbh'}->prepare( qq{ SELECT * FROM ipaddrs WHERE ipaddrs_id = ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;

    $sth->execute( $idx );

    # get all the data
    my $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish;

    # XXX: how do we raise an error here? die?
    if ( $#{ @$data }  == -1 ) {
        return -1;
    }
    #print "ip: " . $data->[0]{'ipaddrs_ip'} . "\n";
    # return the answer
    return $data->[0]{'ipaddrs_ip'}
}

######################################################################
sub hostidx2ip {

    my ($self, $idx) = @_;
    my ($sth);

    $sth = $self->{'dbh'}->prepare( qq{ SELECT * FROM hostaddrs WHERE hostaddrs_id = ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;

    $sth->execute( $idx );

    # get all the data
    my $data = $sth->fetchall_arrayref({});

    # close it up
    $sth->finish;

    # XXX: how do we raise an error here? die?
    if ( $#{ @$data }  == -1 ) {
        return -1;
    }
    #print "hostip: " . $data->[0]{'hostaddrs_ip'} . "\n";
    # return the answer
    return $data->[0]{'hostaddrs_ip'}
}
######################################################################
sub db_update_reservation {

    my ($self, $res_id, $status) = @_;

    my $sth = $self->{'dbh'}->prepare( qq{ UPDATE reservations SET reservation_status = ? WHERE reservation_id = ?}) or die "Couldn't prepare statement: " . $self->{'dbh'}->errstr;
    $sth->execute( $status, $res_id->{reservation_id});

    # close it up
    $sth->finish;

    return 1;
}
## last line of a module
1;
# vim: et ts=4 sw=4
