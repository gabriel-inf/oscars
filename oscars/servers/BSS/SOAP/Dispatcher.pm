# Dispatcher.pm:  SOAP::Lite dispatcher for BSS
# Last modified:  November 9, 2005
# David Robertson (dwrobertson@lbl.gov)
# Jason Lee       (jrlee@lbl.gov)

package Dispatcher;

use lib qw(/usr/local/esnet/servers/prod);

print STDERR "BSS::SOAP::Dispatcher\n";

use Data::Dumper;
use Error qw(:try);

use Common::Exception;
use BSS::Frontend::SOAPMethods;
use BSS::Frontend::Validator;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = BSS::Frontend::Database->new(
                 'database' => 'DBI:mysql:BSS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";

my $request_handler = BSS::Frontend::SOAPMethods->new('dbconn' => $dbconn);



sub dispatch {
    my ( $class_name, $inref ) = @_;

    my ( $logging_buf, $ex );

    print STDERR "BSS::SOAP::Dispatcher->dispatch() called\n";
    my $results = {};
    try {
        $v = BSS::Frontend::Validator->new();
        $v->validate($inref);
        my $m = $inref->{method};
        ($results, $logging_buf) = $request_handler->$m($inref) ;
    }
    catch Common::Exception with {
        $ex = shift;
    }
    otherwise {
        $ex = shift;
    }
    finally {
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
