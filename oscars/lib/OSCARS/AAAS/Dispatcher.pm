###############################################################################
package OSCARS::AAAS::Dispatcher;

# SOAP::Lite dispatcher for AAAS.  Validates parameters and does authorization
# checks through calls to AAAS packages before handing them to
# either the OSCARS::AAAS::Methods package, or forwarding them to the
# BSS SOAP server.

# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use Error qw(:try);
use Data::Dumper;

use lib qw(/usr/local/esnet/servers/prod);

use strict;

use OSCARS::AAAS::Logger;
use OSCARS::AAAS::SOAPMethods;
use OSCARS::AAAS::Validator;
use OSCARS::AAAS::Auth;
use OSCARS::AAAS::Database;
use OSCARS::AAAS::Mail;

# TODO:  FIX, means BSS needs to run on same server
#        To fix, will need virtual hosts for AAAS and BSS
use OSCARS::BSS::Dispatcher;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = OSCARS::AAAS::Database->new(
                 'database' => 'DBI:mysql:AAAS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";


my $request_handler = OSCARS::AAAS::Methods->new('dbconn' => $dbconn);
my $auth = OSCARS::AAAS::Auth->new( 'dbconn' => $dbconn);

#______________________________________________________________________________ 


###############################################################################
sub dispatch {
    my ( $class_name, $params ) = @_;

    my( $ex,  );
    my $results = {};
    my $method_name;

    try {
        $method_name = $params->{method};
        $params->{logger} = OSCARS::AAAS::Logger->new(
                               'dir' => '/home/davidr/oscars/tmp',
                               'params' => $params);
        $params->{logger}->open();
        if (!$auth->authorized($params, $method_name)) {
            throw Error::Simple(
                "User $params->{user_dn} not authorized to make $method_name call");
        }
        my $v = OSCARS::AAAS::Validator->new();
        my $err = $v->validate($params);
        if ($err) { throw Error::Simple($err); }
        # AAAS handles this call
        if ( $params->{server_name} ne 'BSS' ) {
            $results = $request_handler->$method_name($params);
        }
        # forward to BSS SOAP server
        else {
            $results = OSCARS::BSS::Dispatcher::dispatch('OSCARS::BSS::Dispatcher', $params);
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
    $params->{logger}->close();
    my $mailer = OSCARS::AAAS::Mail->new('dbconn' => $dbconn);
    $mailer->send_message($params->{user_dn}, $method_name, $results) ;
    # caught by SOAP to indicate fault
    if ($ex) {
        die SOAP::Fault->faultcode('Server')
                 ->faultstring($ex->{-text});
    }
    return $results;
} #____________________________________________________________________________ 


######
1;
