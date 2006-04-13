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
    $self->{notification_email_encoding} = 'ISO-8859-1';
    $self->{sendmail_cmd} = '/usr/sbin/sendmail -oi';
} #____________________________________________________________________________ 


###############################################################################
# send message:  send mail message, if appropriate, from method results
#
sub send_message {
    my( $self, $messages ) = @_;

    my( $err_msg );

    for my $msg (@$messages) {
         $err_msg = $self->send_mailings($msg);
         if ($err_msg) { return $err_msg; }
    }
    return '';
} #____________________________________________________________________________ 


###############################################################################
# send_mailings:  Send mail to user and administrator(s).
#
sub send_mailings {
    my( $self, $msg ) = @_;

    my $err_msg = $self->send_mail($self->get_webmaster(), $msg->{user},
                     'OSCARS:  ' . $msg->{subject_line}, $msg->{msg});
    if ($err_msg) { return $err_msg; }
    $err_msg = $self->send_mail($self->get_webmaster(), $self->get_admins(),
                     'OSCARS:  Admin notice.  ' . $msg->{subject_line}, $msg->{msg});
    if ($err_msg) { return $err_msg; }
    return '';
} #____________________________________________________________________________ 


###############################################################################
# send_mail:  Mails message.  Used by AAA, Intradomain, and Interdomain
#     components for notifications
#
sub send_mail {
    my( $self, $sender, $recipient, $subject, $msg ) = @_;

    if (!open(MAIL, "|$self->{sendmail_cmd} $recipient")) {
        return $!;           
    }
    print MAIL "From: $sender\n";
    print MAIL "To:   $recipient\n";
    print MAIL "Subject:  $subject\n";
    print MAIL 'Content-Type: text/plain; charset="' .
                   $self->{notification_email_encoding} . '"' . "\n\n";
			
    print MAIL $msg;
    print MAIL "\n";
    print MAIL "---------------------------------------------------\n";
    print MAIL "=== This is an auto-generated e-mail ===\n";

    if (!close( MAIL )) { return $!; }
    return "";
} #____________________________________________________________________________


###############################################################################
#
sub get_webmaster {
    my( $self ) = @_;

    return 'dwrobertson@lbl.gov';
} #____________________________________________________________________________


###############################################################################
#
sub get_admins {
    my( $self ) = @_;

    #return 'oscars-admin@es.net';
    return 'dwrobertson@lbl.gov chin@es.net';
    #return 'dwrobertson@lbl.gov';
} #____________________________________________________________________________


######
1;
