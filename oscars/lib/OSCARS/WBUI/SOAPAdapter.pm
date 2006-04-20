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
    my $component = $cgi->param('component');
    if ($component) {
        $location = 'OSCARS/WBUI/' . $component . '/' . $method . '.pm';
        $className = 'OSCARS::WBUI::' . $component . '::' . $method;
    }
    else {
        $location = 'OSCARS/WBUI/' . $method . '.pm';
        $className = 'OSCARS::WBUI::' . $method;
    }
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

April 17, 2006

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

    my $results;
    my( $login, $authorizations ) = $self->authenticate();
    if ( !$login ) { return; }
    my $soapParams = $self->modifyParams();  # adapts from form params
    my $som = $self->makeCall($soapServer, $soapParams);
    if (!$som) { my $results = {} ; }
    else { $results = $som->result; }
    $self->postProcess($soapParams, $results);
    if (!$authorizations) { $authorizations = $results->{authorized}; }
    $self->{tabs} = OSCARS::WBUI::NavigationBar->new();
    $self->output($som, $soapParams, $authorizations);
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
# makeCall:  make SOAP call, and get results
#
sub makeCall {
    my( $self, $soapServer, $soapParams ) = @_;

    my $method = $soapParams->{method};
    $method =~ s/(\w)/\l$1/;
    my $som = $soapServer->$method($soapParams);
    return $som;
} #___________________________________________________________________________ 


###############################################################################
# postProcess:  Perform any operations necessary after making SOAP call.
#
sub postProcess {
    my( $self, $params, $results ) = @_;

} #___________________________________________________________________________ 


###############################################################################
# output:  formats and prints results to send back to browser
#
sub output {
    my( $self, $som, $soapParams, $authorizations ) = @_;

    my $msg;

    print $self->{cgi}->header( -type => 'text/xml');
    print "<xml>\n";
    $self->{tabs}->output( $soapParams->{method}, $authorizations );
    if (!$som) { $msg = "SOAP call $soapParams->{method} failed"; }
    elsif ($som->faultstring) { $msg = $som->faultstring; }
    else {
	my $results = $som->result;
        $msg = $self->outputDiv($results, $authorizations);
    }
    print "<msg>$msg</msg>\n";
    print "</xml>\n";
} #___________________________________________________________________________ 


######
1;
