#!/usr/bin/perl -w

# AAAS_server.pl:  AAAS SOAP server.
# Last modified: July 6, 2005
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

sub dispatch {
    my ( $self, $inref ) = @_;

    my ( $results );

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
        $results = {};
        if ($inref->{method}) {
            $results->{error_msg} = "No such SOAP method: $inref->{method}\n";
        }
        else {
            $results->{error_msg} = "SOAP method not provided\n";
        }
    }
    return $results;
}
######

######
1;
