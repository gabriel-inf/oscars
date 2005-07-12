#!/usr/bin/perl -w

# AAAS_server.pl:  AAAS SOAP server.
# Last modified: July 11, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Config::Auto;
use SOAP::Transport::HTTP;
use Data::Dumper;

use AAAS::Frontend::User;

my $config = Config::Auto::parse($ENV{'OSCARS_HOME'} . '/oscars.cfg');

my $user = AAAS::Frontend::User->new('configs' => $config);

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => $config->{'AAAS_server_port'})
  -> dispatch_to('Dispatcher')
  -> handle;

######

package Dispatcher;

use Error qw(:try);
use Common::Exception;

sub dispatch {
    my ( $self, $inref ) = @_;

    my( $ex );
    my $results = {};
    try {
        if ($inref->{method} eq 'soap_verify_login') {
            $results = $user->verify_login($inref) ;
        }
        elsif ($inref->{method} eq 'soap_check_login') {
            $results = $user->check_login_status($inref) ;
        }
        elsif ($inref->{method} eq 'soap_get_profile') {
            $results = $user->get_profile($inref) ;
        }
        elsif ($inref->{method} eq 'soap_set_profile') {
            $results = $user->set_profile($inref) ;
        }
        elsif ($inref->{method} eq 'soap_logout') {
            $results = $user->logout($inref) ;
        }
        elsif ($inref->{method} eq 'soap_get_userlist') {
            $results = $user->get_userlist($inref) ;
        }
        else {
            if ($inref->{method}) {
                die SOAP::Fault->faultcode('Server')
                         ->faultstring("No such SOAP method: $inref->{method}\n");
            }
            else {
                die SOAP::Fault->faultcode('Server')
                         ->faultstring("SOAP method not provided\n");
            }
        }
    }
    catch Common::Exception with {
        $ex = shift;
    }
    finally {
        my $logfile_name = "$ENV{OSCARS_HOME}/logs/AAAS.err";

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
