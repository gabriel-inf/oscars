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

