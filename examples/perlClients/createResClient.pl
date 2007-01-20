#!/usr/local/bin/perl -w

# client to test createReservation

use strict;

use WSRF::Lite;
use Data::Dumper;
use DateTime::Format::W3CDTF;
use MyDeserializer;


#Points to the public key of the X509 certificate
#$ENV{HTTPS_CERT_FILE} = $ENV{HOME}."/.globus/usercert.pem";
$ENV{HTTPS_CERT_FILE} = $ENV{HOME}."/.globus/alicecert.pem";
#Points to the private key of the cert - must be unencrypted
#ENV{HTTPS_KEY_FILE}  = $ENV{HOME}."/.globus/pkey.pem";
$ENV{HTTPS_KEY_FILE}  = $ENV{HOME}."/.globus/alicekey.pem";
#Tells WSRF::Lite to sign the message with the above cert
$ENV{WSS_SIGN} = 'true';


# Test the message passing
my $epoch = time();
my $f = DateTime::Format::W3CDTF->new;
my $st = DateTime->from_epoch( epoch => $epoch );
# end time is 4 minutes later
$epoch += 240;
my $et = DateTime->from_epoch( epoch => $epoch );

my $soap = WSRF::Lite
   -> uri('http://oscars.es.net/OSCARS')     # target namespace
   -> proxy ('http://localhost:9191/axis2/services/OSCARS')    # endpoint address
   -> deserializer(MyDeserializer->new)
   -> on_action ( sub {return '"http://oscars-dev.es.net/axis2/services/OSCARS/createReservation"'});  # soapaction

my $method = SOAP::Data -> name ('createReservation')
#	-> encodingStyle ('http://xml.apache.org/xml-soap/literalxml')
	-> attr ({xmlns => 'http://oscars.es.net/OSCARS'});

# define the xml structure to be passed a literal
my $query= SOAP::Data-> value(
		  SOAP::Data->name(srcHost => 'bosshog'),
		  SOAP::Data->name(destHost => 'pabst'),
		  SOAP::Data->name(startTime => $f->format_datetime($st)),
		  SOAP::Data->name(endTime =>  $f->format_datetime($et)),
		  SOAP::Data->name(origTimeZone => '-07:00'),
		  SOAP::Data->name(bandwidth => 10),
		  SOAP::Data->name(createRouteDirection => 'FORWARD'),
		  SOAP::Data->name(protocol => 'TCP'),
		  SOAP::Data->name(description => 'mrt reservation'));

# make the call

my $result = $soap->call($method => $query);
#print Dumper($result);

if ($result->fault) {
  print STDERR "returned with error \n";
    print STDERR $result->faultstring, "\n\n";
    exit;
}
my $resTag = $result->valueof('//createReservationResponse/tag');
my $status = $result->valueof('//createReservationResponse/status');

print "tag is $resTag:  status is $status\n";
