###############################################################################
package BSS::SOAP::Dispatcher;

# SOAP::Lite dispatcher for BSS.  Note well:  calls to the BSS are currently
# assumed to have already been validated and authorized through the AAAS.
#
# Last modified:  November 21, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee       (jrlee@lbl.gov)

use Data::Dumper;
use Error qw(:try);

use BSS::Frontend::SOAPMethods;
use BSS::Frontend::DBRequests;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = BSS::Frontend::DBRequests->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";

my $request_handler = BSS::Frontend::SOAPMethods->new('dbconn' => $dbconn);

#______________________________________________________________________________


###############################################################################
sub dispatch {
    my ( $class_name, $params ) = @_;

    my ( $ex );

    my $results = {};
    try {
        my $m = $params->{method};
        $results = $request_handler->$m($params) ;
    }
    catch Error::Simple with {
        $ex = shift;
    }
    otherwise {
        $ex = shift;
    }
    finally {
        if ($ex) {
            print STDERR "BSS EXCEPTION:\n";
            print STDERR "BSS: $ex->{-text}\n";
        }
    };
    # caught by SOAP to indicate fault
    if ($ex) {
        die SOAP::Fault->faultcode('Server')
                 ->faultstring($ex->{-text});
    }
    return $results;
} #____________________________________________________________________________ 


######
1;
