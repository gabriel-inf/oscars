#==============================================================================
package OSCARS::WBUI::BSS::ArchiveReservationsForm;

=head1 NAME

OSCARS::WBUI::BSS::ArchiveReservationsForm - outputs archive reservations form

=head1 SYNOPSIS

  use OSCARS::WBUI::BSS::ArchiveReservationsForm;

=head1 DESCRIPTION

Handles CGI request to display the archive reservations form.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 28, 2006

=cut

use strict;

use Data::Dumper;

use OSCARS::WBUI::NavigationBar;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# Currently a noop.
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    return {};
} #____________________________________________________________________________


###############################################################################
# output:  Prints out the archive reservations form.  Accessed via a button on 
#          the "View/Edit Reservations" page
# In:   results of SOAP call
# Out:  None
#
sub output {
    my( $self, $results ) = @_;

    my $params_str;

    print $self->{cgi}->header( -type=>'text/xml' );
    print "<xml>\n";
    print qq{ <msg>Reservation archiving form</msg> };
    $self->{tabs}->output('ManageReservations', $results->{authorizations});
    print qq{
    <div id='reservation-ui'>
    <form method='post' action='' onsubmit="return submit_form(this, 
	     'server=BSS;method=ManageReservations;op=archiveReservations;');">

     <p>Required inputs are bordered in green.  Ranges or types of valid 
     entries are given in parentheses below the input fields.</p>
    </form>
    </div>
    };
    print "</xml>\n";
} #____________________________________________________________________________


######
1;
