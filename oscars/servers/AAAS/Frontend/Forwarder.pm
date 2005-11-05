package AAAS::Frontend::Forwarder;

use SOAP::Lite;
use Data::Dumper;

use Exporter;

our @ISA = qw(Exporter);

our @EXPORT = qw( forward );
 

# TODO:  FIX
my $target = SOAP::Lite
  -> uri('http://localhost/Dispatcher')
  -> proxy ('http://localhost/Dispatcher.pm');


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
