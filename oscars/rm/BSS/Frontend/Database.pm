# Database.pm:  BSS specific database settings
# Last modified: April 14, 2005
# Soo-yeon Hwang (dapi@umich.edu)
# David Robertson (dwrobertson@lbl.gov)

package BSS::Frontend::Database;

# tighten it up
use strict; 

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw($Dbname %Table %Table_field);

##### Settings Begin (Global variables) #####
# database connection info
our $Dbname = 'BSS';

# database table names
our %Table = (
  'reservations' => 'reservations',
);

# reservations field names
our %Table_field = (
  'reservations' => {
    'id' => 'reservation_id',
    'start_time' => 'reservation_start_time',
    'end_time' => 'reservation_end_time',
    'created_time' => 'reservation_created_time',
    'bandwidth' => 'reservation_bandwidth',
    'resv_class' => 'reservation_class',
    'burst_limit' => 'reservation_burst_limit',
    'status' => 'reservation_status',
    'ingress_id' => 'ingress_interface_id',
    'egress_id' => 'egress_interface_id',
    'src_id' => 'src_hostaddrs_id',
    'dst_id' => 'dst_hostaddrs_id',
    'user_dn' => 'user_dn',
    'ingress_port' => 'reservation_ingress_port',
    'egress_port' => 'reservation_egress_port',
    'dscp' => 'reservation_dscp',
    'description' => 'reservation_description',
  }
);


##### Settings End #####


#######################################################################
# BSS DB specific calls 
#
# JRLee
#######################################################################

use DBI;


################################
### convert the ipaddr idx to 
### to a router idx
### IN src & dst ip idx's
### OUT src and dst interface idx
################################

sub ip_to_interface {
    my ($src_idx, $dst_idx) = @_; 
    my ($src_iface, $dst_iface);

    $src_iface = ipaddr_to_iface_idx($src_idx);
    $dst_iface = ipaddr_to_iface_idx($dst_idx);

    if ($src_iface == 0 || $dst_iface == 0 ) {
        return (0,0);
    } 
	return ($src_iface, $dst_iface);
}


#######################################################################
# Search the db for for ipaddrs idx and convert them back to 
# interface idx.
# IN: ipaddr_idx
# OUT: interface_idx
#######################################################################
sub ipaddr_to_iface_idx  {

    my ($ip_idx) = @_;
    my ($sth, $dbh, $id);

    # XXX:
    # this should probably be done either  in a constructor or init method
    # and stached in globals and a config for the name/pass/etc
    $dbh = DBI->connect('DBI:mysql:BSS', 'jason', 'ritazza6')
            or die "Couldn't connect to database: " . DBI->errstr;

    $sth = $dbh->prepare('SELECT * FROM ipaddrs WHERE interface_id = ?')
            or die "Couldn't prepare statement: " . $dbh->errstr;

    $sth->execute( $ip_idx );

    # error on no matchs
    if ($sth->rows == 0 ) {
        print "nothing matched ($ip_idx)\n";
        $sth->finish;
        return 0;
    }

    # flatten it out
    while (my @data = $sth->fetchrow_array()) {
            $id = $data[2];
    }   

    # close it up
    $sth->finish;

    # return the answer
    return  $id;
}

#######################################################################
# Check the db ifaces for a router iface ip
# IN: ip
# OUT: router idx
#######################################################################
sub check_db_rtr {

    my ($rtr) = @_;
    my ($dbh, $sth, $id);

    # XXX:
    # this should probably be done either  in a constructor or init method
    # and stached in globals
    $dbh = DBI->connect('DBI:mysql:BSS', 'jason', 'ritazza6')
            or die "Couldn't connect to database: " . DBI->errstr;

    $sth = $dbh->prepare('SELECT * FROM ipaddrs WHERE ipaddrs_ip = ?')
            or die "Couldn't prepare statement: " . $dbh->errstr;

    $sth->execute( $rtr );

    # no match
    if ($sth->rows == 0 ) {
        #print "nothing matched ($rtr)\n";
        $sth->finish;
        return 0;
    }

    # flatten it out
    while (my @data = $sth->fetchrow_array()) {
            $id = $data[2];
    }   

    # close it up
    $sth->finish;

    # return the answer
    return  $id;

}

# setup globls?
#######################################################################
# insert a reservation into the database
# IN:  dbh,rid,stime,time,qos,status,desc,ctime,inport,outport,inid,outid,dn
# OUT: 1 on success, 0 on failure
#######################################################################
sub insert_db_reservation {

    my( $stime,$etime,$qos,$status,$desc,$ctime, 
        $inport,$outport,$inid,$outid,$dn) = @_;

    my ($dbh, $sth, $res_id);

    print "start of insert_db_reservations\n";
    # XXX:
    # this should be done either in a constructor or init method
    # and stached in globals for this package
    $dbh = DBI->connect('DBI:mysql:BSS', 'jason', 'ritazza6')
            or die "Couldn't connect to database: " . DBI->errstr;
        
    # XXX: should parse/check args
    my $q = "INSERT INTO reservations VALUES(NULL,$stime,$etime,'$qos',
            '$status','$desc',$ctime,$inport,$outport,$inid,$outid,'$dn')";
    #print "q = $q\n";
    # Execute the query  (NOTE, use do if we don't expect results)
    $sth = $dbh->do($q) or print "Couldn't 'do' statement: " . $dbh->errstr . "\n"; 

    print "Finished insert_reservation\n";

    $res_id = get_res_id( $stime,$etime,$qos,$status,$desc,$ctime, 
        $inport,$outport,$inid,$outid,$dn);

    if ( $res_id == 0 ) {
        print "Error inserting reservation\n";
    }
    return $res_id;
}

#######################################################################
# insert a reservation into the database
# IN:  dbh,rid,stime,time,qos,status,desc,ctime,inport,outport,inid,outid,dn
# OUT: 1 on success, 0 on failure
#######################################################################
sub get_res_id {

    my( $stime,$time,$qos,$status,$desc,$ctime, 
        $inport,$outport,$inid,$outid,$dn) = @_;

    my ($dbh, $sth, $id);

    $dbh = DBI->connect('DBI:mysql:BSS', 'jason', 'ritazza6')
            or die "Couldn't connect to database: " . DBI->errstr;
        
    $sth = $dbh->prepare('SELECT MAX(reservation_id) FROM reservations WHERE 
            reservation_created_time = ? and reservation_start_time = ?') or 
            die "Couldn't prepare statement: " . $dbh->errstr;

    $sth->execute( $ctime, $stime );
    # error on no matchs
    if ($sth->rows == 0 ) {
        print "nothing matched ($ctime,$stime)\n";
        $sth->finish;
        return 0;
    }

    $id = $sth->fetchrow_array();

    # close it up
    $sth->finish;

    # return the answer
    return  $id;
}

## last line of a module
1;
# vim: et ts=4 sw=4
