#==============================================================================
package OSCARS::WBUI::Info;

=head1 NAME

OSCARS::WBUI::Info - Prints information page.

=head1 SYNOPSIS

  use OSCARS::WBUI::Info;

=head1 DESCRIPTION

Prints information page.  This is the first page that comes up after login.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 24, 2006

=cut


use strict;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};

#______________________________________________________________________________ 


###############################################################################
# Currently a noop.
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    return {};
} #____________________________________________________________________________ 


###############################################################################
# post_process:  Perform any operations necessary after making SOAP call
#
sub post_process {
    my( $self, $results ) = @_;

    $results->{use_xml_tag} = 1;
} #____________________________________________________________________________ 


###############################################################################
# Outputs information section.
sub output {
    my( $self, $results ) = @_;

    if ($results->{use_xml_tag}) {
        print $self->{cgi}->header(
             -type=>'text/xml');
        print "<xml><msg>Information page</msg>\n";
    }
    print qq{
      <div id="get-info">
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
    if ($results->{use_xml_tag}) {
        print "</xml>\n";
    }
} #____________________________________________________________________________ 


######
1;
