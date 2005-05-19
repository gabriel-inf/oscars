# Database.pm:  BSS specific database settings and routines
# Last modified: May 18, 2005
# Jason Lee (jrlee@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

package BSS::Frontend::Database;

use strict; 

use DBI;

######################################################################
sub new {
    my $invocant = shift;
    my $_class = ref($invocant) || $invocant;
    my ($_self) = {@_};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}


######################################################################
sub initialize {
    my ( $_self ) = @_;
    my %attr = (
        RaiseError => 0,
        PrintError => 0
    );
    $_self->{'dbh'} = DBI->connect($_self->{'configs'}->{'use_BSS_database'}, 
             $_self->{'configs'}->{'BSS_login_name'}, $_self->{'configs'}->{'BSS_login_passwd'})
            or die "Couldn't connect to database: " . $DBI::errstr;
}


our @insert_fields = ('reservation_id', 'reservation_start_time', 'reservation_end_time',
    'reservation_created_time', 'reservation_bandwidth', 'reservation_class',
    'reservation_burst_limit', 'reservation_status', 'ingress_interface_id',
    'egress_interface_id', 'src_hostaddrs_id', 'dst_hostaddrs_id', 'user_dn',
    'reservation_ingress_port', 'reservation_egress_port', 'reservation_dscp',
    'reservation_description');


######################################################################
sub get_fields_to_insert {
    my ($self) = @_;
    return (@insert_fields)
}

    
#######################################################################
## Get the db iface id from an ip address.  Called from the scheduler
## to see if a router is an edge router (will not be in ipaddrs table
## if it is not).
# IN:  interface ip address
# OUT: interface id
#######################################################################
sub ip_to_xface_id {
    my ($self, $ipaddr) = @_;
    my ($query, $sth, $interface_id, $error_msg);

    $query = 'SELECT interface_id FROM ipaddrs WHERE ipaddrs_ip = ?';
    ($sth, $error_msg) = $self->do_query($query, $ipaddr);
    if ( $error_msg ) {
        $sth->finish();
        return( 0, $error_msg );
    }
    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return (0, "No match in database for $ipaddr");
    }
    my @data = $sth->fetchrow_array();
    $interface_id = $data[0];
    $sth->finish();
    return ($interface_id, "");
}


#######################################################################
## Get the rooter loopback ip from the interface database id.
# IN:  interface primary key id
# OUT: loopback ip address
#######################################################################
sub xface_id_to_loopback {
    my ($self, $interface_id) = @_;
    my ($query, $sth, $loopback_addr, $error_msg);

    $query = 'SELECT router_name FROM routers WHERE router_id = (SELECT router_id from interfaces WHERE interface_id = ?)';
    ($sth, $error_msg) = $self->do_query($query, $interface_id);
    if ( $error_msg ) {
        $sth->finish();
        return( '', $error_msg );
    }

    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return ('', "No match in database for $interface_id");
    }
    my @data = $sth->fetchrow_array();
    $loopback_addr = $data[0];
    $sth->finish();
    return ($loopback_addr, "");
}


#######################################################################
### Get the primary key in the hostaddrs table, given a host ip address
### A row is created if that ip address is not present.
# IN:  hostaddrs_ip
# OUT: hostaddrs_id
#######################################################################
sub hostaddrs_ip_to_id
{
    my ($self, $ipaddr) = @_;
    my ($query, $error_msg, $sth);
    my ($id);

    # TODO:  make hostaddrs_ip field UNIQUE in hostaddrs?
    $query = 'SELECT hostaddrs_id FROM hostaddrs WHERE hostaddrs_ip = ?';
    ($sth, $error_msg) = $self->do_query($query, $ipaddr);
    if ( $error_msg ) {
        $sth->finish();
        return( 0, $error_msg );
    }

    # if no matches, insert a row in hostaddrs
    if ($sth->rows == 0 ) {
        $query = "INSERT INTO hostaddrs VALUES ( '', '$ipaddr'  )";
        ($sth, $error_msg) = $self->do_query($query);
        if ( $error_msg ) {
            $sth->finish();
            return( 0, $error_msg );
        }
        $id = $self->{'dbh'}->{'mysql_insertid'};
    }
    else {
        my @data = $sth->fetchrow_array();
        $id = $data[0];
    }

    $sth->finish();
    return ($id);
}


#######################################################################
### Get the ip address from the row in the hostaddrs table identified
### by the id.
# IN:  hostaddrs_id
# OUT: hostaddrs_ip
#######################################################################
sub hostaddrs_id_to_ip {
    my ($self, $id) = @_;
    my ($query, $sth, $ipaddr, $error_msg);

    $query = 'SELECT hostaddrs_ip FROM hostaddrs WHERE hostaddrs_id = ?';
    ($sth, $error_msg) = $self->do_query($query, $id);
    if ( $error_msg ) {
        $sth->finish();
        return( 1, $error_msg );
    }

    # no match
    if ($sth->rows == 0 ) {
        $sth->finish();
        return (0, "No match in database for $id");
    }
    my @data = $sth->fetchrow_array();
    $ipaddr = $data[0];
    $sth->finish();
    return ($ipaddr, "");
}


sub do_query
{
    my( $self, $query, @args ) = @_;
    my( $sth, $error_msg );

    $sth = $self->{'dbh'}->prepare( $query );
    if ($DBI::err) {
        $error_msg = "[DBERROR] Preparing $query:  $DBI::errstr";
        return (undef, $error_msg);
    }
    $sth->execute( @args );
    if ( $DBI::err ) {
        $error_msg = "[DBERROR] Executing $query:  $DBI::errstr";
        $sth->finish();
        return(undef, $error_msg);
    }
    return( $sth, '');
}


## last line of a module
1;
# vim: et ts=4 sw=4
