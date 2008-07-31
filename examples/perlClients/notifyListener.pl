#!/usr/bin/perl

use HTTP::Daemon;
use Getopt::Std;

my %Options;
getopt('p', \%Options);

$port = $Options{'p'};
$port=8070 if(!$port); #default port 8070

my $d = HTTP::Daemon->new(LocalPort => $port) || die("Unable to start daemon: " . $@);
print "Server is running at URL " . $d->url . "\n\n";

while(my $c = $d->accept){
    my $r = $c->get_request;
    print "Got notifcation: \n" . $r->content . "\n\n"; 
    $c->send_status_line(200)
}