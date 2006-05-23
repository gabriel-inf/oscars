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
    $self->readConfiguration();
} #____________________________________________________________________________


###############################################################################
# readConfiguration:  read and parse XML configuration file.
#
sub readConfiguration {
    my( $self ) = @_;

    $self->{config} = {};
    my $parser = new XML::DOM::Parser;
    my $doc = $parser->parsefile( "$ENV{HOME}/.oscars.xml" );
    my $topLevel = $doc->getElementsByTagName ("notifications");
    my $topNode = $topLevel->item(0);
    my @children = $topNode->getChildNodes();
    # TODO:  error checking
    for my $n (@children) {
        if ( $n->getNodeType() == 1 ) {
            my $attr = $n->getAttributeNode( "name" );
	    my $msgName = $attr->getValue();
	    $self->{config}->{$msgName} = {};
	    $self->{config}->{$msgName}->{mappings} = ();
            my $nc = $n->getFirstChild();
            while ($nc) {
		if ( $nc->getNodeType() == 1) {
		    my $nodeName = $nc->getNodeName();
		    if ($nodeName eq 'subject') {
			$attr = $nc->getAttributeNode('msg');
			$self->{config}->{$msgName}->{subject} = $attr->getValue();
		    }
		    elsif ($nodeName eq 'mapping') {
			$attr = $nc->getAttributeNode('field');
			if (!$attr) { next; }
			my $descr = $nc->getAttributeNode('description');
			if (!$descr) { next; }
			my $mapping = {};
			$mapping->{$attr->getValue()} = $descr->getValue();
			push( @{$self->{config}->{$msgName}->{mappings}},
                              $mapping );
		    }
		}
	        $nc = $nc->getNextSibling();
	    }
	}
    }
} #____________________________________________________________________________


###############################################################################
# sendMessage:  send mail message(s), if appropriate, from method results
#
sub sendMessage {
    my( $self, $login, $method, $results ) = @_;

    my( $errMsg );

    if ( !$self->{config}->{$method} ) { return; }
    my @resultsArray = ();
    if ( ref($results) eq 'HASH' ) { push(@resultsArray, $results); }
    else { @resultsArray = @{$results}; }
    my $msg = $self->{config}->{$method}->{subject} . "\n\n";

    for my $result (@resultsArray) {
        for my $mapping ( @{$self->{config}->{$method}->{mappings}} ) {
	    for my $key ( keys %{$mapping} ) {
		if ( $result->{$key} ) {
		    $msg .= $mapping->{$key} . ':    ' . $result->{$key} . "\n"; 
		}
	    }
        }
	if ($login ne 'testaccount') {
	    $errMsg = $self->mailMessage($self->getWebmaster(), $login,
                'OSCARS:  ' . $self->{config}->{$method}->{subject}, $msg);
	    if ($errMsg) { throw Error::Simple( $errMsg ); }
	}
        $errMsg = $self->mailMessage($self->getWebmaster(), $self->getAdmins(),
            'OSCARS:  Admin notice.  ' . $self->{config}->{$method}->{subject},
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

    return 'oscars-admin@es.net';
    #return 'dwrobertson@lbl.gov chin@es.net';
    #return 'dwrobertson@lbl.gov';
} #____________________________________________________________________________


######
1;
