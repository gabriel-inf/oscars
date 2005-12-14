###############################################################################
package OSCARS::AAAS::Dispatcher;

# SOAP::Lite dispatcher for AAAS.  Validates parameters and does authorization
# checks through calls to AAAS packages before handing them to
# either the OSCARS::AAAS::Methods package, or forwarding them to the
# BSS SOAP server.

# Last modified:  December 9, 2005
# David Robertson (dwrobertson@lbl.gov)

use Error qw(:try);
use Data::Dumper;
use SOAP::Lite;

use strict;

use OSCARS::AAAS::Logger;
use OSCARS::AAAS::Methods;
use OSCARS::AAAS::Validator;
use OSCARS::AAAS::Auth;
use OSCARS::AAAS::Database;
use OSCARS::AAAS::Mail;

my $db_login = 'oscars';
my $password = 'ritazza6';

my $dbconn = OSCARS::AAAS::Database->new(
                 'database' => 'DBI:mysql:AAAS',
                 'dblogin' => $db_login,
                 'password' => $password)
             or die "FATAL:  could not connect to database";


my $request_handler = OSCARS::AAAS::Methods->new('dbconn' => $dbconn);
my $auth = OSCARS::AAAS::Auth->new( 'dbconn' => $dbconn);

my $BSS_server = SOAP::Lite
  -> uri('http://localhost:3000/OSCARS/BSS/Dispatcher')
  -> proxy ('http://localhost:3000/bss');


###############################################################################
sub dispatch {
    my ( $class_name, $params ) = @_;

    my $results = {};
    my ( $method_name, $som, $ex );

    try {
        $method_name = $params->{method};
        $params->{logger} = OSCARS::AAAS::Logger->new(
                           'dir' => '/home/oscars/logs',
                           'method' => $method_name);
        $params->{logger}->start_log($params->{user_dn});
        if (!$auth->authorized($params, $method_name)) {
            throw Error::Simple(
                "User $params->{user_dn} not authorized to make $method_name call");
        }
        my $v = OSCARS::AAAS::Validator->new();
        my $err = $v->validate($params);
        if ($err) { throw Error::Simple($err); }
        # either AAAS handles this call
        if ( $params->{server_name} eq 'AAAS' ) {
            $results = $request_handler->$method_name($params);
        }
        # or else it is forwarded to the BSS SOAP server
        else {
            $som = $BSS_server->dispatch($params);
            $results = $som->result;
        }
    }
    catch Error::Simple with { $ex = shift; }
    otherwise { $ex = shift; }
    finally {
        if ( $ex ) {
            $params->{logger}->write_log("AAAS EXCEPTION:\n");
            $params->{logger}->write_log("AAAS: $ex->{-text}\n");
        }
        # only will happen with BSS exception
        if ( $som && $som->faultstring ) {
            $params->{logger}->write_log("BSS EXCEPTION:\n");
            $params->{logger}->write_log("BSS: $som->faultstring\n");
        }
    };
    if ( $params->{logger} ) { $params->{logger}->end_log($results); }

    my $mailer = OSCARS::AAAS::Mail->new('dbconn' => $dbconn);
    $mailer->send_message($params->{user_dn}, $method_name, $results) ;
    # caught by SOAP to indicate fault
    if ($ex) {
        die SOAP::Fault->faultcode('Server')
                 ->faultstring($ex->{-text});
    }
    # if BSS exception occurred
    if ($som && $som->faultstring) {
        die SOAP::Fault->faultcode('Server')
                 ->faultstring($som->faultstring);
    }
    return $results;
} #____________________________________________________________________________ 


######
1;
