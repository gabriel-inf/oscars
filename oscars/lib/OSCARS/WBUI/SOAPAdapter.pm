#==============================================================================
package OSCARS::WBUI::SOAPAdapterFactory;

use strict;

use CGI;
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
    my( $self ) = @_;

    my( $location, $className );

    my $cgi = CGI->new();
    my $method = $cgi->param('method'); 
    $location = 'OSCARS/WBUI/Method/' . $method . '.pm';
    $className = 'OSCARS::WBUI::Method::' . $method;
    require $location;
    return $className->new('method' => $method );
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

July 19, 2006

=cut

use strict;

use SOAP::Lite;
use CGI::Session;

use Data::Dumper;


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

    my $verified = $self->authenticate();
    if ( !$verified ) { return; }
    my $request = $self->modifyParams();  # adapts from form params
    if ( !$request->{login} ) { $request->{login} = $self->{login}; }
    my $som = $self->makeCall( $request );
    if (!$som) { $response = {} ; }
    else { $response = $som->result; }

    $self->postProcess( $request, $response );
    $self->output( $som, $request );
} #___________________________________________________________________________ 


###############################################################################
# authenticate:  Authenticates user on pages after user login.
#
sub authenticate {
    my( $self, $request ) = @_;

    # Attempt to authenticate by loading existing session
    $self->{session} = CGI::Session->load();
    if ( CGI::Session->errstr ) { return 0; }
    $self->{login} = $self->{session}->param("login");
    return 1;
} #___________________________________________________________________________ 


###############################################################################
# modifyParams:  Do any modification of CGI params to transform them
#                 into the arguments that the SOAP call expects.
#
sub modifyParams {
    my( $self ) = @_;

    my $cgi = $self->{session}->query();
    my $params = {};
    for $_ ($cgi->param) {
	# TODO:  Fix when figure out Apache2::Request
        if ($_ ne 'method') { $params->{$_} = $cgi->param($_); }
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
    my( $self, $som, $request ) = @_;

    my( $response, $msg, $activeTab, $previousTab );

    if (!$som) { $msg = "SOAP call $self->{method} failed"; }
    elsif ($som->faultstring) { $msg = $som->faultstring; }
    else {
	$response = $som->result;
    }
    # stores new active tab name in cookie
    if ( $response ) {
        $activeTab = $self->getTab();
        $previousTab = $self->{session}->param("tab");
        $self->{session}->param("tab", $activeTab );
    }

    print $self->{session}->header( -type => 'text/xml' );
    print "<xml>\n";
    if ( $response ) {
	print "<content>\n";
        $msg = $self->outputContent( $request, $response );
	print "</content>\n";
        print "<tabs>\n";
        print "<active>$activeTab</active><previous>$previousTab</previous>\n";
        print "</tabs>\n";
    }
    print "<status>$msg</status>\n";
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
