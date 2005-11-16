package Dispatcher;

# Dispatcher.pm:  SOAP::Lite meta-dispatcher for all servers
# Last modified:  November 10, 2005
# David Robertson (dwrobertson@lbl.gov)

use FindBin::Real;
use Error qw(:try);
use Data::Dumper;

# TODO:  these should be dynamically determined instead
use lib qw(/usr/local/esnet/servers/prod);
use AAAS::SOAP::Dispatcher;

sub dispatch {
    my ( $class_name, $params ) = @_;

    # TODO:  get URI and extract prod or test directory name
    #        build hash of dispatchers, dispatch to right version
    #        by manipulating %INC (somehow) with directory name
    $results = AAAS::SOAP::Dispatcher->dispatch($params);
    return $results;
}
######

######
1;
