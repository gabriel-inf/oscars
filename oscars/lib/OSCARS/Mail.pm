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
            'OSCARS:  Admin notice.  ' . $notification->{subject},
	    $msg);
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
