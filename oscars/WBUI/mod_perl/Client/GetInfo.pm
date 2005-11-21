###############################################################################
package Client::GetInfo;

# Handles printing initial navigation page.
#
# Last modified:  November 20, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#******************************************************************************
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________ 


#******************************************************************************
sub output {
    my( $self ) = @_;

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
} #____________________________________________________________________________ 


######
1;
