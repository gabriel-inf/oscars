###############################################################################
package Client::AAAS::Login;

# Handles user login.
#
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;
use CGI qw{:cgi};

use Client::UserSession;
use Client::NavigationBar;
use Client::GetInfo;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#_____________________________________________________________________________ 


###############################################################################
# Overrides super-class call to avoid trying to verify a non-existent session.
# In this case, the SOAP call is used to authenticate.
#
sub authenticate {
    my( $self ) = @_;

    return 1;
} #____________________________________________________________________________


###############################################################################
# Overrides super-class call to avoid trying to authorize a non-existent 
# session.  In this case, the SOAP call is used to authorize.
#
sub authorize {
    my( $self ) = @_;

    return 1;
} #____________________________________________________________________________


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $params->{server_name} = 'AAAS';
    $params->{user_level} = 2;   # TODO:  FIX user level
    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# overrides super-class call
#
sub post_process {
    my( $self, $results ) = @_;

    $self->{user_dn} = $results->{user_dn};
    $self->{user_level} = $results->{user_level};
    my $sid = $self->{session}->start_session($self->{cgi}, $results);
    print $self->{cgi}->header(
         -type=>'text/xml',
         -cookie=>$self->{cgi}->cookie(CGISESSID => $sid));
} #____________________________________________________________________________


###############################################################################
sub output {
    my( $self, $results ) = @_;

    my $info = Client::GetInfo->new();
    my $navigation_bar = Client::NavigationBar->new();
    print "<xml>\n";
    print "<msg>User $self->{user_dn} signed in.</msg>\n";
    if ($self->{session}->authorized($results->{user_level}, 'admin')) {
        $navigation_bar->initialize('admin');
    }
    else { $navigation_bar->initialize('user'); }
    $info->output($results);
    print "</xml>\n";
} #____________________________________________________________________________

######
1;
