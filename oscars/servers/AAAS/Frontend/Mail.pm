###############################################################################
package AAAS::Frontend::Mail;

# Handles all notification email messages.
# 
# Last modified:  November 23, 2005
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
    $self->{method_mappings} = {
        'create_reservation' =>  AAAS::Frontend::Notifications::create_reservation,
    }
} #____________________________________________________________________________ 


###############################################################################
# gen_message:  Generates mail message, if appropriate, from method results
#
sub gen_message {
    my( $self, $method_name, $results ) = @_;

    my( $subject_line, $message );

    if ( $self->{method_mappings}->{$method_name} ) {
        $message = $self->{method_mappings}->{$method_name}($results);
        # TODO:  FIX subject line
        $subject_line = 'OSCARS: ' . $method_name;
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
