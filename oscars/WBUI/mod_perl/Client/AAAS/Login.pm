###############################################################################
package Client::AAAS::Login;

# Handles user login.
#
# Last modified:  November 20, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;
use CGI qw{:cgi};

use Client::UserSession;
use Client::NavigationBar;
use Client::GetInfo;

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
# Overrides super-class call to avoid trying to verify a non-existent session.
# In this case, the SOAP call is used to authenticate.
#
sub authenticate {
    my( $self ) = @_;

} #____________________________________________________________________________ 


#******************************************************************************
sub modify_params {
    my( $self, $params ) = @_;

    $params->{server_name} = 'AAAS';
    $self->SUPER::modify_params($params);
} #____________________________________________________________________________ 


#******************************************************************************
# overrides super-class call
#
sub post_process {
    my( $self, $results ) = @_;

    my( $sid );

    ($results->{user_dn}, $sid ) =
                                $self->{session}->start_session($self->{cgi});
    print $self->{cgi}->header(
         -type=>'text/xml',
         -cookie=>$self->{cgi}->cookie(CGISESSID => $sid));
} #____________________________________________________________________________ 


#******************************************************************************
sub output {
    my( $self, $results ) = @_;

    my $info = Client::GetInfo->new();
    my $navigation_bar = Client::NavigationBar->new();
    for $_ (keys(%$results)) {
        if ($results->{$_}) {
            print STDERR "result: $_, value: $results->{$_}\n";
        }
    }
    print "<xml>\n";
    print "<msg>User $results->{user_dn} signed in.</msg>\n";
    # TODO:  FIX hard-wired admin
    $navigation_bar->initialize('admin');
    $info->output();
    print "</xml>\n";
} #____________________________________________________________________________ 

######
1;
