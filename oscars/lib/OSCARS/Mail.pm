# =============================================================================
package OSCARS::Mail;

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

January 4, 2006

=cut


use strict;

use Data::Dumper;


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
# sendMessage:  send mail message, if appropriate, from method results
#
sub sendMessage {
    my( $self, $messages ) = @_;

    my( $errMsg );

    for my $msg (@$messages) {
         $errMsg = $self->sendMailings($msg);
         if ($errMsg) { return $errMsg; }
    }
    return '';
} #____________________________________________________________________________ 


###############################################################################
# sendMailings:  Send mail to user and administrator(s).
#
sub sendMailings {
    my( $self, $msg ) = @_;

    my $errMsg = $self->sendMail($self->getWebmaster(), $msg->{user},
                     'OSCARS:  ' . $msg->{subject}, $msg->{msg});
    if ($errMsg) { return $errMsg; }
    $errMsg = $self->sendMail($self->getWebmaster(), $self->getAdmins(),
                     'OSCARS:  Admin notice.  ' . $msg->{subject}, $msg->{msg});
    if ($errMsg) { return $errMsg; }
    return '';
} #____________________________________________________________________________ 


###############################################################################
# sendMail:  Mails message.  Used by AAA, Intradomain, and Interdomain
#     components for notifications
#
sub sendMail {
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
