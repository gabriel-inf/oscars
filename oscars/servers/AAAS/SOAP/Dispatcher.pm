package AAAS::SOAP::Dispatcher;

# SOAP::Lite dispatcher for AAAS.  Validates parameters and does authorization
# checks through calls to AAAS::Frontend packages before handing them to
# either the AAAS::Frontend::SOAPMethods package, or forwarding them to the
# BSS SOAP server.

# Last modified:  November 15, 2005
# David Robertson (dwrobertson@lbl.gov)

use Error qw(:try);

use Data::Dumper;

use lib qw(/usr/local/esnet/servers/prod);

use AAAS::Frontend::SOAPMethods;
use AAAS::Frontend::Validator;
use AAAS::Frontend::Auth;
use AAAS::Frontend::Database;

# TODO:  FIX, means BSS needs to run on same server
#        To fix, will need virtual hosts for AAAS and BSS
use BSS::SOAP::Dispatcher;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = AAAS::Frontend::Database->new(
                 'database' => 'DBI:mysql:AAAS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";


my $request_handler = AAAS::Frontend::SOAPMethods->new('dbconn' => $dbconn);
my $auth = AAAS::Frontend::Auth->new( 'dbconn' => $self->{dbconn});

sub dispatch {
    my ( $class_name, $params ) = @_;

    my( $ex,  );
    my $results = {};

    try {
        my $v = AAAS::Frontend::Validator->new();
        my $err = $v->validate($params);
        if ($err) { throw Error::Simple($err); }
        my $m = $params->{method};
        if (!$auth->authorized($params->{user_dn}, $m)) {
            throw Error::Simple(
                "User $params->{user_dn} not authorized to make $m call");
        }
        # AAAS handles this call
        if ( $params->{server_name} ne 'BSS' ) {
            $results = $request_handler->$m($params);
        }
        # forward to BSS SOAP server
        else {
            print STDERR "forwarding\n";
            $results = BSS::SOAP::Dispatcher::dispatch('BSS::SOAP::Dispatcher', $params);
            print STDERR Dumper($results);
        }
    }
    catch Error::Simple with {
        $ex = shift;
    }
    otherwise {
        $ex = shift;
    }
    finally {
        if ($ex) {
            print STDERR "AAAS EXCEPTION:\n";
            print STDERR "AAAS: $ex->{-text}\n";
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
