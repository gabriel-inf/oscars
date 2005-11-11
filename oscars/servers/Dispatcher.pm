# Dispatcher.pm:  SOAP::Lite meta-dispatcher for all servers
# Last modified:  November 10, 2005
# David Robertson (dwrobertson@lbl.gov)

package Dispatcher;

use FindBin::Real;
use Error qw(:try);
use Data::Dumper;

# TODO:  these should be dynamically determined instead
use lib qw(/usr/local/esnet/servers/prod);
use AAAS::SOAP::Dispatcher;
use BSS::SOAP::Dispatcher;

sub dispatch {
    my ( $class_name, $inref ) = @_;

    # TODO:  get URI and extract prod or test directory name
    #        build hash of dispatchers, dispatch to right version
    #        by manipulating %INC (somehow) with directory name
    if ( $inref->{server_name} eq 'AAAS' ) {
        $results = AAAS::SOAP::Dispatcher->dispatch($inref);
    }
    else {
        $results = BSS::SOAP::Dispatcher->dispatch($inref);
    }
    return $results;
}
######

######
1;
