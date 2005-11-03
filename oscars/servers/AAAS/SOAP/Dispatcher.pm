#!/usr/bin/perl

package AAAS::Frontend::Dispatcher;

####
# Dispatcher.pm:  Soap lite dispatcher for AAAS
# Last modified:  November 2, 2005
# David Robertson (dwrobertson@lbl.gov)
###

use Error qw(:try);
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
    finally {
        my $logfile_name = "$ENV{HOME}/logs/AAAS.err";

        open (LOGFILE, ">$logfile_name") ||
                die "Can't open log file $logfile_name.\n";
        print LOGFILE "********************\n";
        if ($ex) {
            print LOGFILE "EXCEPTION:\n";
            print LOGFILE $ex->{-text};
            print LOGFILE "\n";
        }
        close(LOGFILE);
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
