package Common::Mail;

# Mail.pm:  handles sending notification mail messages
# 
# Last modified: June 27, 2005
# David Robertson (dwrobertson@lbl.gov)
# Soo-yeon Hwang (dapi@umich.edu)

use strict;

use Data::Dumper;

###############################################################################
sub new {
    my $invocant = shift;
    my $_class = ref($invocant) || $invocant;
    my ($self) = {@_};
  
    # Bless $self into designated class.
    bless($self, $_class);
  
    # Initialize.
    $self->initialize();
  
    return($self);
}

sub initialize {
    my ( $self ) = @_;
    # email text encoding
    $self->{notification_email_encoding} = 'ISO-8859-1';
    $self->{sendmail_cmd} = '/usr/sbin/sendmail -oi';
    $self->{webmaster} = 'dwrobertson@lbl.gov';
}
######

###############################################################################
# send_mail:  Mails message.  Used by both AAAS and BSS for notifications
#
sub send_mail {
    my( $self, $sender, $recipient, $subject, $msg ) = @_;

    if (!open(MAIL, "|$self->{sendmail_cmd} $recipient")) {
        return( $! );           
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

    if (!close( MAIL )) { return( $! ); }
    return( "" );
}
######

###############################################################################
#
sub get_webmaster {
    my( $self ) = @_;

    return( $self->{webmaster} );
}
######

###############################################################################
#
sub get_admins {
    my( $self ) = @_;

    # for now
    return( 'dwrobertson@lbl.gov' );
    #return( 'chin@es.net dwrobertson@lbl.gov' );
}
######

# Don't touch the line below
1;
