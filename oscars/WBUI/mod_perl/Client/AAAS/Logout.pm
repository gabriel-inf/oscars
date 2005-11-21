###############################################################################
package Client::AAAS::Logout;

# Handles user login.
#
# Last modified:  November 20, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;
use CGI qw{:cgi};

use Client::UserSession;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#******************************************************************************
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{session} = Client::UserSession->new();
} #____________________________________________________________________________ 


#******************************************************************************
# Currently a noop.
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    return {};
} #____________________________________________________________________________ 


#******************************************************************************
# post_process:  In this case, closes CGI session.
#
sub post_process {
    my( $self, $results ) = @_;

    $self->{session}->end_session($self->{cgi});
    return {};
} #___________________________________________________________________________                                         


#******************************************************************************
sub output {
    my( $self, $results ) = @_;

    # TODO:  FIX hard-coded URL
    print STDERR "Logout output called\n";
    print "Location:  https://oscars-test.es.net/\n\n";
} #____________________________________________________________________________ 

######
1;
