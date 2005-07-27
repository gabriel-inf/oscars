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
    my ( $class_name, $inref ) = @_;

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
        die SOAP::Fault->faultcode('Server')
                 ->faultstring($ex->{-text});
    }
    return $results;
}
######

sub validate {
    my ( $inref ) = @_;

    if ($inref->{method} eq 'verify_login') {
        if (!($inref->{user_dn})) {
            throw Common::Exception("Please enter your login name.");
        }
        if (!($inref->{user_password})) {
            throw Common::Exception("Please enter the current password.");
        }
    }
    elsif ($inref->{method} eq 'set_profile') {
        if (!($inref->{user_password})) {
            throw Common::Exception("Please enter the current password.");
        }
        if ($inref->{password_new_once}) {
            if (!$inref->{password_new_twice}) {
                throw Common::Exception("Please enter the same new password twice for verification");
            }
            if ($inref->{password_new_once} ne
                $inref->{password_new_twice}) {
                throw Common::Exception("Please enter the same new password twice for verification");
            }
        }
        if (!$inref->{user_first_name}) {
            throw Common::Exception("Please enter the first name.");
        }
        if (!$inref->{user_last_name}) {
            throw Common::Exception("Please enter the last name.");
        }
        if (!$inref->{user_institution}) {
            throw Common::Exception("Please enter the user's organization.");
        }
        if (!$inref->{user_email_primary}) {
            throw Common::Exception("Please enter the primary email address.");
        }
        if (!$inref->{user_phone_primary}) {
            throw Common::Exception("Please enter the primary phone.");
        }
    }
}

######
1;
