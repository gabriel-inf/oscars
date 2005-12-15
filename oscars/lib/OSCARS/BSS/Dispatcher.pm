###############################################################################
package OSCARS::BSS::Dispatcher;

# SOAP::Lite dispatcher for BSS.  Note well:  calls to the BSS are currently
# assumed to have already been validated and authorized through the AAAS.
#
# Last modified:  December 14, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee       (jrlee@lbl.gov)

use Data::Dumper;
use Error qw(:try);

use OSCARS::AAAS::Logger;
use OSCARS::BSS::Database;
use OSCARS::BSS::Methods;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = OSCARS::BSS::Database->new(
                 'database' => 'DBI:mysql:BSSTest',
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
        $dbconn->reconnect();  # TODO:  FIX reconnecting every time
        my $method_name = $params->{method};
        $params->{logger} =
                OSCARS::AAAS::Logger->new( 'dir' => '/home/oscars/logs',
                                           'method' => $method_name);
        $params->{logger}->start_log( $params->{user_dn} );
        $results = $request_handler->$method_name($params) ;
    }
    catch Error::Simple with {
        $ex = shift;
    }
    otherwise {
        $ex = shift;
    }
    finally {
        if ($ex) {
            $params->{logger}->write_log("BSS EXCEPTION:");
            $params->{logger}->write_log("BSS: $ex->{-text}");
        }
    };
    $params->{logger}->end_log( $results );
    # caught by SOAP to indicate fault
    if ($ex) {
        die SOAP::Fault->faultcode('Server')
                 ->faultstring($ex->{-text});
    }
    return $results;
} #____________________________________________________________________________ 


######
1;
