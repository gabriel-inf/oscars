###############################################################################
package OSCARS::AAAS::Mail;

# Handles all notification email messages.
# 
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use Data::Dumper;

use OSCARS::AAAS::Notifications;


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
    $self->{notifier} = OSCARS::AAAS::Notifications->new(
                                              'dbconn' => $self->{dbconn});
    $self->{method_mail} = {
        'create_reservation' =>  1,
        'cancel_reservation' =>  1,
        'find_pending_reservations' =>  1,
        'find_expired_reservations' =>  1,
    };
} #____________________________________________________________________________ 


###############################################################################
# send message:  send mail message, if appropriate, from method results
#
sub send_message {
    my( $self, $user_dn, $method_name, $results ) = @_;

    my( $err_msg );

    if ( $self->{method_mail}->{$method_name} ) {
         my $messages = 
            $self->{notifier}->$method_name($user_dn, $results);
         for my $msg (@$messages) {
             $err_msg = $self->send_mailings($msg);
             if ($err_msg) { return $err_msg; }
         }
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
# send_mail:  Mails message.  Used by both AAAS and BSS for notifications
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
