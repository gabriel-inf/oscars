package Client::AAAS::Login;

# Handles user login.
#
# Last modified:  November 18, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

##############################################################################
# Overrides super-class call to avoid trying to verify a non-existent session.
#
sub pre_call {
    my( $self, $params ) = @_;
}

##############################################################################
sub output {
    my( $self, $params ) = @_;

    print "<xml>\n";
    print qq{
      <msg>User $params->{user_dn} signed in.</msg>
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
    print "</xml>\n";
}
######

##############################################################################
# overrides super-class call
#
sub post_call {
    my( $self, $params );

    my( $sid );

    ($params->{user_dn}, $sid ) =
                                $self->{session}->start_session($self->{cgi});
    print $self->{cgi}->header(
         -type=>'text/xml',
         -cookie=>$self->{cgi}->cookie(CGISESSID => $sid));
}
######

######
1;
