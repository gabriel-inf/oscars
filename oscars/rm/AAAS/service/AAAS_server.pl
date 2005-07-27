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

my $daemon = SOAP::Transport::HTTP::Daemon
  -> new (LocalPort => $config->{'AAAS_server_port'}, Listen => 5, Reuse => 1)
  -> dispatch_to('Dispatcher')
  -> handle;

######

package Dispatcher;

use Error qw(:try);
use Common::Exception;

my $aaas_user;

sub dispatch {
    my ( $self, $inref ) = @_;

    my( $ex );
    my $results = {};
    if (!$aaas_user) {
        $aaas_user = AAAS::Frontend::User->new('configs' => $config);
    }

    try {
        validate($inref);
        my $m = $inref->{method};
        $results = $aaas_user->$m($inref);
    }
    catch Common::Exception with {
        $ex = shift;
    }
    otherwise {
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
        print STDERR $ex->{-text}, "\n";
        die SOAP::Fault->faultcode('Server')
                 ->faultstring($ex->{-text});
    }
    return $results;
}
######

sub validate {
    my ( $self, $inref ) = @_;

    
    return( 1 );
}

######
1;
