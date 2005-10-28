package BSS::Client::SOAPClient;

use SOAP::Lite;
use Data::Dumper;

use Exporter;

our @ISA = qw(Exporter);

our @EXPORT = qw( bss_dispatcher );
 

my $BSS_server = SOAP::Lite
-> uri('http://localhost:3000/Dispatcher')
  -> proxy ('http://localhost:3000/BSS_server.pl');


##############################################################################
# bss_dispatcher:  calls BSS SOAP server
#
sub bss_dispatcher {
    my ($params) = @_;

    return $BSS_server->dispatch($params);
}
######

######
1;
