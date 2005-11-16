# Dispatcher.pm:  SOAP::Lite dispatcher for BSS
# Last modified:  November 9, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee       (jrlee@lbl.gov)

package BSS::SOAP::Dispatcher;

use Data::Dumper;
use Error qw(:try);

use BSS::Frontend::SOAPMethods;
use BSS::Frontend::DBRequests;
use BSS::Frontend::Validator;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = BSS::Frontend::DBRequests->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";

my $request_handler = BSS::Frontend::SOAPMethods->new('dbconn' => $dbconn);



sub dispatch {
    my ( $class_name, $inref ) = @_;

    my ( $ex );

    my $results = {};
    try {
        $v = BSS::Frontend::Validator->new();
        $v->validate($inref);
        my $m = $inref->{method};
        my $results = $request_handler->$m($inref) ;
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
}
######

######
1;
