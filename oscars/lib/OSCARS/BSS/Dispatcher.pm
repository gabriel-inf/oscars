###############################################################################
package OSCARS::BSS::Dispatcher;

# SOAP::Lite dispatcher for BSS.  Note well:  calls to the BSS are currently
# assumed to have already been validated and authorized through the AAAS.
#
# Last modified:  December 13, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee       (jrlee@lbl.gov)

use Data::Dumper;
use Error qw(:try);

use OSCARS::BSS::Database;
use OSCARS::BSS::Methods;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = OSCARS::BSS::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";

my $request_handler = OSCARS::BSS::Methods->new('dbconn' => $dbconn);

#______________________________________________________________________________


###############################################################################
#
sub dispatch {
    my ( $class_name, $params ) = @_;

    my ( $ex );

    my $results = {};
    try {
        my $method_name = $params->{method};
        $results = $request_handler->$method_name($params) ;
    }
    catch Error::Simple with { $ex = shift; }
    otherwise { $ex = shift; }
    finally { ; };

    # Caught by SOAP to indicate fault.  AAAS uses faultstring to update
    # its log for this call.
    if ($ex) {
        die SOAP::Fault->faultcode('Server')
                 ->faultstring($ex->{-text});
    }
    return $results;
} #____________________________________________________________________________ 


######
1;
