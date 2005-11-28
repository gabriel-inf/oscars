###############################################################################
package AAAS::Frontend::Mail;

# Handles all notification email messages.
# 
# Last modified:  November 28, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang  (dapi@umich.edu)

use strict;

use Data::Dumper;

use AAAS::Frontend::Notifications;


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
    $self->{webmaster} = 'dwrobertson@lbl.gov';
    $self->{notifier} = AAAS::Frontend::Notifications->new();
    $self->{method_mail} = {
        'create_reservation' =>  1,
        'cancel_reservation' =>  1,
        'find_pending_reservations' =>  1,
        'find_expired_reservations' =>  1,
    };
} #____________________________________________________________________________ 


###############################################################################
# gen_message:  Generates mail message, if appropriate, from method results
#
sub gen_message {
    my( $self, $user_dn, $method_name, $results ) = @_;

    my( $subject_line, $message );

    if ( $self->{method_mail}->{$method_name} ) {
        ( $subject_line, $message ) = 
            $self->{notifier}->$method_name($user_dn, $results);
    }
    return( $subject_line, $message );
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
    print MAIL "Subject:  OSCARS: $subject\n";
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

    return $self->{webmaster};
} #____________________________________________________________________________ 


###############################################################################
#
sub get_admins {
    my( $self ) = @_;

    #return 'oscars-admin@es.net';
    #return 'dwrobertson@lbl.gov chin@es.net';
    return 'dwrobertson@lbl.gov';
} #____________________________________________________________________________ 


######
1;
