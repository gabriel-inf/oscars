package AAAS::Frontend::Forwarder;

use SOAP::Lite;
use Data::Dumper;

use Exporter;

our @ISA = qw(Exporter);

our @EXPORT = qw( forward );
 

my $target = SOAP::Lite
  -> uri('http://198.128.14.164/Dispatcher')
  -> proxy ('https://198.128.14.164/BSS');


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
