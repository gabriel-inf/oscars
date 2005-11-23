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
        'create_reservation_form' => 'Client::BSS::CreateReservationForm',
        'create_reservation' => 'Client::BSS::CreateReservation',
        'cancel_reservation' => 'Client::BSS::CancelReservation',
        'view_reservations' => 'Client::BSS::ViewReservations',
        'view_details' => 'Client::BSS::ViewDetails',
    };
    $self->{location_mapping} = {
        'login' => 'Client/AAAS/Login',
        'get_info' => 'Client/GetInfo',
        'get_profile' => 'Client/AAAS/GetProfile',
        'set_profile' => 'Client/AAAS/SetProfile',
        'logout' => 'Client/AAAS/Logout',
        'view_users' => 'Client/AAAS/ViewUsers',
        'add_user' => 'Client/AAAS/AddUser',
        'create_reservation_form' => 'Client/BSS/CreateReservationForm',
        'create_reservation' => 'Client/BSS/CreateReservation',
        'cancel_reservation' => 'Client/BSS/CancelReservation',
        'view_reservations' => 'Client/BSS/ViewReservations',
        'view_details' => 'Client/BSS/ViewDetails',
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
# handle_request:  handles all phases of the request; may be nested to
#     handle multiple requests
#
sub handle_request {
    my( $self, $soap_server ) = @_;

    my( %soap_params );

    my $user_dn = $self->authenticate();
        # TODO:  handle cleanly when not authenticated or authorized
    if (!$user_dn) { return; }
    my $user_level = $self->authorize($user_dn);
    if (!$user_level) { return; }
    $self->modify_params(\%soap_params);  # adapts from CGI params
    my $results = $self->make_call($soap_server, \%soap_params);
    if (!$results) {
        return;
    }
    $self->post_process($results);
    $self->output($results);
}

###############################################################################
# authenticate
#
sub authenticate {
    my( $self ) = @_;

    $self->{user_dn} = $self->{session}->verify_session($self->{cgi});
    return $self->{user_dn};
} #___________________________________________________________________________                                         


###############################################################################
# authorize:  TODO with real authorization
#
sub authorize {
    my( $self ) = @_;

    $self->{user_level} = $self->{session}->authorize_session($self->{cgi});
    return $self->{user_level};
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
        $self->update_status_msg($som->faultstring);
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

###############################################################################
# update_status_msg:  Currently called on if there has been a SOAP fault
#
sub update_status_msg {
    my( $self, $msg ) = @_;

    print $self->{cgi}->header(
        -type=>'text/xml');
    print "<xml>\n";
    print qq{
    <msg>$msg</msg>
    };
    print "</xml>\n";
} #___________________________________________________________________________                                         

######
1;
