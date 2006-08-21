#==============================================================================
package OSCARS::WBUI::SOAPAdapterFactory;

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

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
