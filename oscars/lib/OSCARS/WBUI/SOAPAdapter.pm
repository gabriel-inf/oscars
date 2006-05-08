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

    my( $location, $className );

    my $method = $cgi->param('method'); 
    $location = 'OSCARS/WBUI/Method/' . $method . '.pm';
    $className = 'OSCARS::WBUI::Method::' . $method;
    require $location;
    return $className->new('cgi' => $cgi);
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

May 5, 2006

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
# handleRequest:  handles all phases of the request; may be nested to
#     handle multiple requests
#
sub handleRequest {
    my( $self, $soapServer ) = @_;

    my $response;
    my( $login, $authorizations ) = $self->authenticate();
    if ( !$login ) { return; }
    my $request = $self->modifyParams();  # adapts from form params
    my $som = $self->makeCall($soapServer, $request);
    if (!$som) { my $response = {} ; }
    else { $response = $som->result; }
    $self->postProcess($request, $response);
    if (!$authorizations) { $authorizations = $response->{authorized}; }
    $self->{tabs} = OSCARS::WBUI::NavigationBar->new();
    $self->output($som, $request, $authorizations);
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
# modifyParams:  Do any modification of CGI params to transform them
#                 into the arguments that the SOAP call expects.
#
sub modifyParams {
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
# makeCall:  make SOAP call, and get response
#
sub makeCall {
    my( $self, $soapServer, $request ) = @_;

    $request->{method} =~ s/(\w)/\l$1/;
    my $method = $request->{method};
    my $som = $soapServer->$method($request);
    $request->{method} =~ s/(\w)/\U$1/;
    return $som;
} #___________________________________________________________________________ 


###############################################################################
# postProcess:  Perform any operations necessary after making SOAP call.
#
sub postProcess {
    my( $self, $request, $response ) = @_;

} #___________________________________________________________________________ 


###############################################################################
# output:  formats and prints response to send back to browser
#
sub output {
    my( $self, $som, $request, $authorizations ) = @_;

    my $msg;

    print $self->{cgi}->header( -type => 'text/xml');
    print "<xml>\n";
    $self->{tabs}->output( $request->{method}, $authorizations );
    if (!$som) { $msg = "SOAP call $request->{method} failed"; }
    elsif ($som->faultstring) { $msg = $som->faultstring; }
    else {
	my $response = $som->result;
        $msg = $self->outputDiv($response, $authorizations);
    }
    print "<msg>$msg</msg>\n";
    print "</xml>\n";
} #___________________________________________________________________________ 


######
1;
