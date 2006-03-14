#==============================================================================
package OSCARS::WBUI::SOAPAdapterFactory;


use strict;
use Data::Dumper;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #___________________________________________________________________________                                         

###############################################################################
#
sub instantiate {
    my( $self, $cgi ) = @_;

    my( $location, $class_name );

    my $method_name = $cgi->param('method'); 
    my $server = $cgi->param('server');
    if ($server) {
        $location = 'OSCARS/WBUI/' . $server . '/' . $method_name . '.pm';
        $class_name = 'OSCARS::WBUI::' . $server . '::' . $method_name;
    }
    else {
        $location = 'OSCARS/WBUI/' . $method_name . '.pm';
        $class_name = 'OSCARS::WBUI::' . $method_name;
    }
    require $location;
    return $class_name->new('cgi' => $cgi);
} #___________________________________________________________________________                                         


#==============================================================================
package OSCARS::WBUI::SOAPAdapter;

=head1 NAME

OSCARS::WBUI::SOAPAdapter - Superclass for all SOAP methods.

=head1 SYNOPSIS

  use OSCARS::WBUI::SOAPAdapter;

=head1 DESCRIPTION

OSCARS::WBUI::SOAPAdapterFactory is called to create a SOAP method instance which
inherits from SOAPAdapter.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 28, 2006

=cut


use strict;

use Data::Dumper;
use SOAP::Lite;
use CGI;

use OSCARS::WBUI::UserSession;
use OSCARS::WBUI::NavigationBar;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{session} = OSCARS::WBUI::UserSession->new();
} #____________________________________________________________________________


###############################################################################
# handle_request:  handles all phases of the request; may be nested to
#     handle multiple requests
#
sub handle_request {
    my( $self, $soap_server ) = @_;

    my( %soap_params );

    my $user_login = $self->authenticate();
    if (!$user_login) { return; }
    $self->modify_params(\%soap_params);  # adapts from CGI params
    $self->{tabs} = OSCARS::WBUI::NavigationBar->new();
    my $results = $self->make_call($soap_server, \%soap_params);
    if (!$results) {
        return;
    }
    $self->post_process($results);
    $self->output($results);
} #___________________________________________________________________________ 


###############################################################################
# authenticate
#
sub authenticate {
    my( $self ) = @_;

    $self->{user_login} = $self->{session}->verify_session($self->{cgi});
    return $self->{user_login};
} #___________________________________________________________________________ 


###############################################################################
# modify_params:  Do any modification of CGI params to transform them
#                 into the arguments that the SOAP call expects.
#
sub modify_params {
    my( $self, $params ) = @_;

    for $_ ($self->{cgi}->param) {
        # NOTE that param is a method call 
        # kludge for now
        if ($_ ne 'permissions') {
            $params->{$_} = $self->{cgi}->param($_);
        }
        else {
            @{$params->{$_}} = $self->{cgi}->param($_);
        }
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
