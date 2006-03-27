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

March 24, 2006

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
    return( $self );
} #___________________________________________________________________________ 


###############################################################################
# handle_request:  handles all phases of the request; may be nested to
#     handle multiple requests
#
sub handle_request {
    my( $self, $soap_server ) = @_;

    my $results;
    my( $user_login, $authorized ) = $self->authenticate();
    if ( !$user_login ) { return; }
    my $soap_params = $self->modify_params();  # adapts from form params
    my $som = $self->make_call($soap_server, $soap_params);
    if (!$som) { my $results = {} ; }
    else { $results = $som->result; }
    $self->post_process($results);
    if (!$authorized) { $authorized = $results->{authorized}; }
    $self->{tabs} = OSCARS::WBUI::NavigationBar->new();
    print "<xml>\n";
    $self->{tabs}->output( $soap_params->{method}, $authorized );
    $self->output($som, $soap_params, $authorized);
    print "</xml>\n";
} #___________________________________________________________________________ 


###############################################################################
# authenticate
#
sub authenticate {
    my( $self ) = @_;

    my $session = OSCARS::WBUI::UserSession->new();
    return $session->verify($self->{cgi});
} #___________________________________________________________________________ 


###############################################################################
# modify_params:  Do any modification of CGI params to transform them
#                 into the arguments that the SOAP call expects.
#
sub modify_params {
    my( $self ) = @_;

    my $params = {};
    for $_ ($self->{cgi}->param) {
	# TODO:  Fix when figure out Apache2::Request
        if ($_ ne 'permissions') {
            $params->{$_} = $self->{cgi}->param($_);
        }
        else {
            @{$params->{$_}} = $self->{cgi}->param($_);
        }
    }
    return $params;
} #___________________________________________________________________________ 


###############################################################################
# make_call:  make SOAP call, and get results
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    my $som = $soap_server->dispatch($soap_params);
    return $som;
} #___________________________________________________________________________ 


###############################################################################
# post_process:  Perform any operations necessary after making SOAP call.  Take
#   care that any overriding method calls SUPER or prints the header itself.
#
sub post_process {
    my( $self, $results ) = @_;

    print $self->{cgi}->header( -type => 'text/xml');
} #___________________________________________________________________________ 


###############################################################################
# output:  formats and prints results to send back to browser
#
sub output {
    my( $self, $som, $params, $authorized ) = @_;

    my $msg;

    if (!$som) { $msg = "SOAP call $params->{method} failed"; }
    elsif ($som->faultstring) { $msg = $som->faultstring; }
    else {
	my $results = $som->result;
        $msg = $self->output_div($results, $authorized);
    }
    print "<msg>$msg</msg>\n";
} #___________________________________________________________________________ 


######
1;
