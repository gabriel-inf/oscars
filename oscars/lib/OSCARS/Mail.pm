# =============================================================================
package OSCARS::Mail;

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

=head1 NAME

OSCARS::Mail - handles email notifications for OSCARS.

=head1 SYNOPSIS

  use OSCARS::Mail;

=head1 DESCRIPTION

This module contains methods to use sendmail to send notifications to
administators and users containing the results and/or status of an
OSCARS SOAP request.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 17, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use XML::DOM;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ( $self ) = @_;
    # email text encoding
    $self->{emailEncoding} = 'ISO-8859-1';
    $self->{sendmailCmd} = '/usr/sbin/sendmail -oi';
} #____________________________________________________________________________


###############################################################################
# sendMessage:  send mail message(s), if appropriate, from method results
#
sub sendMessage {
    my( $self, $login, $method, $results ) = @_;

    my $errMsg;

    my $notification = $self->{configuration}->{$method};
    if ( !$notification ) { return; }
    my @resultsArray = ();
    if ( ref($results) eq 'HASH' ) { push(@resultsArray, $results); }
    else { @resultsArray = @{$results}; }
    my $msg = $notification->{subject} . "\n\n";

    for my $result (@resultsArray) {
        for my $key ( keys %{$notification} ) {
            if ( $result->{$key} ) {
                $msg .= $key . ':    ' . $result->{$key} . "\n"; 
            }
        }
        if ($login ne 'testaccount') {
            $errMsg = $self->mailMessage($self->getWebmaster(), $login,
                'OSCARS:  ' . $notification->{subject}, $msg);
            if ($errMsg) { throw Error::Simple( $errMsg ); }
        }
        $errMsg = $self->mailMessage($self->getWebmaster(), $self->getAdmins(),
            'OSCARS:  Admin notice.  ' . $notification->{subject}, $msg);
        if ($errMsg) { throw Error::Simple( $errMsg ); }
    }
    return;
} #____________________________________________________________________________ 


###############################################################################
# mailMessage:  Mails message.
#
sub mailMessage {
    my( $self, $sender, $recipient, $subject, $msg ) = @_;

    if (!open(MAIL, "|$self->{sendmailCmd} $recipient")) {
        return $!;           
    }
    print MAIL "From: $sender\n";
    print MAIL "To:   $recipient\n";
    print MAIL "Subject:  $subject\n";
    print MAIL 'Content-Type: text/plain; charset="' .
                   $self->{emailEncoding} . '"' . "\n\n";
    print MAIL $msg;
    print MAIL "\n";
    print MAIL "---------------------------------------------------\n";
    print MAIL "=== This is an auto-generated e-mail ===\n";

    if (!close( MAIL )) { return $!; }
    return "";
} #____________________________________________________________________________


###############################################################################
#
sub getWebmaster {
    my( $self ) = @_;

    return 'dwrobertson@lbl.gov';
} #____________________________________________________________________________


###############################################################################
#
sub getAdmins {
    my( $self ) = @_;

    #return 'oscars-admin@es.net';
    return 'dwrobertson@lbl.gov chin@es.net';
    #return 'dwrobertson@lbl.gov';
} #____________________________________________________________________________


######
1;
