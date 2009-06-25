#/usr/bin/perl

use strict;
use DBI;

my $DB_USER = "MYUSER";
my $DB_PASS = "MYPASS";

my $start = time();
my $dbh = DBI->connect('DBI:mysql:bss', $DB_USER, $DB_PASS);
my $insert_cmd = $dbh->prepare("INSERT INTO resvPathReport SELECT NULL, r1.id, pe1.pathId, r1.createdTime, (r1.endTime - r1.startTime), r1.bandwidth, r1.login, r1.payloadSender, COUNT(*)/2, (SELECT urn FROM pathElems AS pe2 WHERE pe2.pathId=? AND pe2.seqNumber=(SELECT MIN(pe3.seqNumber) FROM pathElems AS pe3 WHERE pe3.pathId=?)), (SELECT urn FROM pathElems AS pe2 WHERE pe2.pathId=? AND pe2.seqNumber=(SELECT MAX(pe3.seqNumber) FROM pathElems AS pe3 WHERE pe3.pathId=?)) FROM pathElems AS pe1, paths AS p1, reservations AS r1 WHERE pe1.pathId=p1.id AND p1.id=? AND r1.id = p1.reservationId GROUP BY pe1.pathId");
my $path_data = $dbh->prepare("SELECT paths.id FROM paths,reservations WHERE paths.reservationId=reservations.id AND paths.pathType='local' AND (reservations.status='FINISHED' OR reservations.status='CANCELLED') AND reservations.id > ?");

#find starting point
my $max_command = $dbh->prepare("SELECT MAX(reservationId) FROM resvPathReport");
$max_command->execute();
my $max_resv = $max_command->fetchrow_arrayref->[0];
if(!$max_resv){
    $max_resv = 0;
}

# get all paths
$path_data->bind_param(1, $max_resv);
$path_data->execute();

#iterate through paths generating rows
while(my $path_id = $path_data->fetchrow_arrayref){
    $insert_cmd->bind_param(1, $path_id->[0]);
    $insert_cmd->bind_param(2, $path_id->[0]);
    $insert_cmd->bind_param(3, $path_id->[0]);
    $insert_cmd->bind_param(4, $path_id->[0]);
    $insert_cmd->bind_param(5, $path_id->[0]);
    $insert_cmd->execute() or die("MySQL error occurred");
}

print "Report finished in " . (time()-$start) . " seconds\n";
