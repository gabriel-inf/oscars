package Client::SOAPAdapter;

# SOAPAdapter:
#
# Takes CGI parameters, and modifies them if necessary before sending them to 
# the SOAP server.  With some form submissions, additional parameters may be
# added to have the same interface as the API.
#
# When the SOAP results are returned, they are incorporated into the XML
# output.
# 
# general.pl is the starting point.  Some methods are likely to be split off
# into other classes.

# Last modified:  November 15, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Error qw(:try);
use Data::Dumper;

use CGI;
use SOAP::Lite;

use Client::UserSession;


###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    print STDERR "initialize called\n";
    $self->{auth} = Client::UserSession->new();
    $self->{args} = $ENV{'QUERY_STRING'};
    $self->{URI} = $ENV{'REQUEST_URI'};
    $self->{cgi} = CGI->new();
    # don't make print to STDOUT before instantiating this class
    print $self->{cgi}->header( -type=>'text/xml' );
}
######

##############################################################################
# make_soap_call:  Copy CGI paramaters into SOAP parameters, and make
#                  SOAP call
#
sub make_soap_call {
    my( $self, $server ) = @_;

    my( $user_dn, $user_level ) = $self->auth->verify_session($self->{cgi});
    my $soap_params = $self->adapt_params( $user_dn, $user_level );
    my $som = $server->dispatch($soap_params);
    if ($som->faultstring) {
        $self->update_page($som->faultstring);
        return undef;
    }
    return( $som->result );
}
######

##############################################################################
# output:  Format SOAP results, and output XML.  (Javascript adds HTML
#          portion.)
#
sub output {
    my( $self, $results ) = @_;

    print "<xml>\n";
    #print "<msg>User profile</msg>\n";
    #print "<div id=\"zebratable_ui\">\n";
    print "<msg>Test</msg>\n";
    #print_profile($results, $form_params, $starting_page, 'get_profile');
    print  "</div>\n";
    print  "</xml>\n";
}
######


################################
# Private methods.
################################

##############################################################################
# adapt_params:  Adapt CGI paramaters to the expected SOAP parameters
#
sub adapt_params {

    my( $self, $user_dn, $user_permissions ) = @_;

    my( $soap_params );

    if (!$user_permissions) {
        # TODO:  fix
        print "Location:  " . "https://oscars-test.es.net/" . "\n\n";
        return (undef, undef);
    }
    for $_ ($self->{cgi}->param) {
        $soap_params->{$_} = $self->{cgi}->param($_);
    }
    $soap_params->{user_dn} = $user_dn;
    $soap_params->{user_permissions} = $user_permissions;
    # TODO:  get method from query string of URI, as well as server name
    #        from hash of methods
    $soap_params->{method} = 'foo';  
    return( $soap_params );
}
######

##############################################################################
# update_page:  If output_func is null, an error has occurred and only the
#               error message is printed in the status div on the OSCARS
#               page.
#
sub update_page {
    my( $self, $msg, $output_func, $user_dn, $user_level) = @_;

    print "<xml>\n";
    print "<msg>\n";
    print "$msg\n";
    print "</msg>\n";
    # TODO:  FIX  blanket access
    my $user_level = 'auth engr user';
    if ($output_func) {
        print qq{
          <user_level>$user_level</user_level>
          <div>
        };
        print $output_func->($user_dn, $user_level), "</div>\n";
    }
    print "</xml>\n";
}
######

# TODO: make into HTML fragment
##############################################################################
sub output_info {
    my( $self, $unused1, $unused2 ) = @_;

    print qq{
      <div id="info_form">
      <p>
      With the advent of service sensitive applications (such as remote-
      controlled experiments, time constrained massive data transfers,
      video-conferencing, etc.), it has become apparent that there is a need
      to augment the services present in today's ESnet infrastructure.
      </p>

      <p>
      Two DOE Office of Science workshops in the past two years have clearly 
      identified both science discipline driven network requirements and a 
      roadmap for meeting these requirements.  This project begins to 
      address one element of the roadmap: dynamically provisioned, QoS paths.
      </p>

      <p>
      The focus of the ESnet On-Demand Secure Circuits and Advance Reservation 
      System (OSCARS) is to develop and deploy a prototype service that enables 
      on-demand provisioning of guaranteed bandwidth secure circuits within 
      ESnet.
      </p>

      <p>To begin using OSCARS, click on one of the notebook tabs.</p>
      </div>
    };
}
######

1;
