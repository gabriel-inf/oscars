#!/usr/bin/perl
# min testing client

   use SOAP::Lite;
   my $client = SOAP::Lite->new();
   #$client->uri('urn:BSS'); # this will work
   $client->uri('http://localhost:8001/BSS');
   $client->proxy('http://localhost:8001');

   my $som = $client->create_reservation(
	"www.mcs.anl.gov",
	#"www.bnl.gov",  # bad host, doesn't ping,tr hangs...
	"www.sdsc.edu",
	"1113522927",
	"600",
	"4m",
	"testing",
	"5222",
	"6666",
	'DN=/DC=org/DC=doegrids/OU=People/CN=Jason R Lee 402729');

   my @res = $som->paramsout;
   my $output = $som->result;

   print "Result is [$output], outparams are:\n";
   for $it (@res) {
       print "=> $it\n";
   }
