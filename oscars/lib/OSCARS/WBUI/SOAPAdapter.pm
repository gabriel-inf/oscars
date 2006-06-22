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
    return $className->new('cgi' => $cgi,
                           'method' => $method );
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

May 17, 2006

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
    my( $self ) = @_;

    my $response;
    my( $login, $authorizations ) = $self->authenticate();
    if ( !$login ) { return; }
    my $request = $self->modifyParams();  # adapts from form params
    my $som = $self->makeCall( $request );
    if (!$som) { $response = {} ; }
    else {
        $response = $som->result;
    }
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
        if ($_ ne 'method') { $params->{$_} = $self->{cgi}->param($_); }
    }
    return $params;
} #___________________________________________________________________________ 


###############################################################################
# makeCall:  make SOAP call, and get response
#
sub makeCall {
    my( $self, $params ) = @_;

    my $methodName = $self->{method};
    return $self->docLiteralRequest( $methodName, $params );
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
    $self->{tabs}->output( $self->{method}, $authorizations );
    if (!$som) { $msg = "SOAP call $self->{method} failed"; }
    elsif ($som->faultstring) { $msg = $som->faultstring; }
    else {
	my $response = $som->result;
        $msg = $self->outputDiv($request, $response, $authorizations);
    }
    print "<msg>$msg</msg>\n";
    print "</xml>\n";
} #___________________________________________________________________________ 


###############################################################################
# docLiteralRequest:  makes a SOAP request using document/literal
#
sub docLiteralRequest {
    my( $self, $methodName, $params ) = @_;

    # convert first letter to lowercase
    $methodName =~ s/(\w)/\l$1/;
    my $soapURI = 'http://localhost:2000/OSCARS/Dispatcher';
    my $soapProxy = 'http://localhost:2000/OSCARS/Server';
    my $soapAction = "http://oscars.es.net/OSCARS/Dispatcher/$methodName";
    my $client = SOAP::Lite
        -> uri( $soapURI )
        -> proxy( $soapProxy )
	-> on_action ( sub { return "$soapAction" } );
    my $method = SOAP::Data -> name($methodName)
        -> attr ({'xmlns' => 'http://oscars.es.net/OSCARS/Dispatcher'});
    my $request = SOAP::Data -> name($methodName . "Request" => $params );
    return $client->call($method => $request);
} #___________________________________________________________________________ 


######
1;
