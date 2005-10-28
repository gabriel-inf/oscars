package AAAS::Client::SOAPClient;

use SOAP::Lite;
use Data::Dumper;

use Exporter;

our @ISA = qw(Exporter);

our @EXPORT = qw( aaas_dispatcher );


######################################
# All calls are made to AAAS front end
######################################

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/Dispatcher')
  -> proxy ('http://localhost:2000/AAAS_server.pl');



##############################################################################
#
sub aaas_dispatcher
{
    my ($params) = @_;
    return($AAAS_server->dispatch($params));
}
######

#####
1;
