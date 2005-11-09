# Dispatcher.pm:  SOAP::Lite dispatcher for AAAS
# Last modified:  November 8, 2005
# David Robertson (dwrobertson@lbl.gov)

package Dispatcher;

use Error qw(:try);

use lib qw(/usr/local/esnet/servers/prod);

use Common::Exception;
use AAAS::Frontend::Validator;
use AAAS::Frontend::Database;
use AAAS::Frontend::User;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = AAAS::Frontend::Database->new(
                 'database' => 'DBI:mysql:AAAS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";


my $request_handler = AAAS::Frontend::User->new('dbconn' => $dbconn);

sub dispatch {
    my ( $class_name, $inref ) = @_;

    my( $ex,  );
    my $results = {};

    try {
        my $v = AAAS::Frontend::Validator->new();
        my $err = $v->validate($inref);
        if ($err) { throw Common::Exception($err); }
        my $m = $inref->{method};
        $results = $request_handler->$m($inref);
    }
    catch Common::Exception with {
        $ex = shift;
    }
    otherwise {
        $ex = shift;
    }
    finally {};
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
