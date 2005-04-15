#######################################################################
# DB specific calls 
#
# JRLee
#######################################################################

package BSSdb;
use DBI;

# tighten it up
use strict; 

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
    my $sth = $dbh->do($q) or print "Couldn't 'do' statement: " . $dbh->errstr . "\n"; 

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

######################################################################
# foo
######################################################################
sub foo {
    print "start test\n";
    # connect to sql (this should already be done by the BSS)
    my $dbh = DBI->connect('DBI:mysql:BSS', 'jason', 'ritazza6') or die "Couldn't connect to database: " . DBI->errstr;

    # setup some example stuff
    my $rid = 1;
    my $stime = '2005-04-04 11:11:11';
    my $etime = '2006-04-04 11:11:11';
    my $qos='Good';
    my $status='pending';
    my $desc='this is a test';
    my $ctime = '2006-04-04 11:11:11';
    my $inport = 3;
    my $outport = 4;
    my $inid = 5;
    my $outid = 6;
    my $dn = 'jason lee';

    &insert_reservation($dbh, $rid, $stime,$etime,$qos,$status,$desc,$ctime,$inport,$outport,$inid,$outid,$dn);

    $dbh->disconnect;

    print "end test\n";
}

## last line of a module
1;
# vim: et ts=4 sw=4
