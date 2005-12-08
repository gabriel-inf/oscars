##############################################################################
package Dispatcher;

# SOAP::Lite meta-dispatcher for all servers
# Last modified:  Dedember 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use FindBin::Real;
use Error qw(:try);
use Data::Dumper;

# TODO:  these should be dynamically determined instead
use lib qw(/usr/local/esnet/servers/prod);
use OSCARS::AAAS::Dispatcher;


##############################################################################
sub dispatch {
    my ( $class_name, $params ) = @_;

    # TODO:  get URI and extract prod or test directory name
    #        build hash of dispatchers, dispatch to right version
    #        by manipulating %INC (somehow) with directory name
    $results = OSCARS::AAAS::Dispatcher->dispatch($params);
    return $results;
} #___________________________________________________________________________

######
1;
