###############################################################################
# SOAPAdapter packages
# Last modified:  November 21, 2005
# David Robertson (dwrobertson@lbl.gov)


###############################################################################
package Client::SOAPAdapterFactory;

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
    my( $self ) = @_;

    # TODO:  Fix when have virtual hosts for AAAS and BSS
    $self->{class_mapping} = {
        'login' => 'Client::AAAS::Login',
        'get_info' => 'Client::GetInfo',
        'get_profile' => 'Client::AAAS::GetProfile',
        'set_profile' => 'Client::AAAS::SetProfile',
        'logout' => 'Client::AAAS::Logout',
        'view_users' => 'Client::AAAS::ViewUsers',
        'add_user' => 'Client::AAAS::AddUser',
        'create_reservation' => 'Client::BSS::CreateReservation',
        'delete_reservation' => 'Client::BSS::DeleteReservation',
        'view_reservations' => 'Client::BSS::ViewReservations',
    };
    $self->{location_mapping} = {
        'login' => 'Client/AAAS/Login',
        'get_info' => 'Client/GetInfo',
        'get_profile' => 'Client/AAAS/GetProfile',
        'set_profile' => 'Client/AAAS/SetProfile',
        'logout' => 'Client/AAAS/Logout',
        'view_users' => 'Client/AAAS/ViewUsers',
        'add_user' => 'Client/AAAS/AddUser',
        'create_reservation' => 'Client/BSS/CreateReservation',
        'delete_reservation' => 'Client/BSS/DeleteReservation',
        'view_reservations' => 'Client/BSS/ViewReservations',
    };
} #___________________________________________________________________________                                         

###############################################################################
#
sub instantiate {
    my( $self, $cgi ) = @_;

    my $method_name = $cgi->param('method'); 
    my $location = $self->{location_mapping}->{$method_name} . ".pm";
    require $location;
    return $self->{class_mapping}->{$method_name}->new('cgi' => $cgi);
} #___________________________________________________________________________                                         


###############################################################################
package Client::SOAPAdapter;
#

use strict;

use Data::Dumper;
use SOAP::Lite;
use CGI;

use Client::UserSession;


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


###############################################################################
# authenticate
#
sub authenticate {
    my( $self ) = @_;

    $self->{user_dn} = $self->{session}->verify_session($self->{cgi});
    return $self->{user_dn};
} #___________________________________________________________________________                                         


###############################################################################
# authorize:  TODO
#
sub authorize {
    my( $self, $user_dn ) = @_;

    return 1;
} #___________________________________________________________________________                                         


###############################################################################
# modify_params:  Do any modification of CGI params to transform them
#                 into the arguments that the SOAP call expects.
#
sub modify_params {
    my( $self, $params ) = @_;

    for $_ ($self->{cgi}->param) {
        $params->{$_} = $self->{cgi}->param($_);
    }
} #___________________________________________________________________________                                         


###############################################################################
# make_call:  make SOAP call, and get results
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    my $som = $soap_server->dispatch($soap_params);
    if ($som->faultstring) {
        # TODO:  return error in status
        #$self->update_page($som->faultstring);
        return undef;
    }
    return $som->result;
} #___________________________________________________________________________                                         


###############################################################################
# post_process:  Perform any operations necessary after making SOAP call
#
sub post_process {
    my( $self, $results ) = @_;

} #___________________________________________________________________________                                         


###############################################################################
# output:  formats and prints results to send back to browser
#
sub output {
    my( $self, $results ) = @_;

} #___________________________________________________________________________                                         

######
1;
