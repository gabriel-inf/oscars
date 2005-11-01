package AAAS::Frontend::Forwarder;

use SOAP::Lite;
use Data::Dumper;

use Exporter;

our @ISA = qw(Exporter);

our @EXPORT = qw( forward );
 

my $target = SOAP::Lite
  -> uri('http://localhost:3000/Dispatcher')
  -> proxy ('http://localhost:3000/BSS_server.pl');


##############################################################################
# forward:  forwards calls to BSS SOAP server
#
sub forward {
    my ($params) = @_;

    return $target->dispatch($params);
}
######

######
1;
