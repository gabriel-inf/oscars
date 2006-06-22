#==============================================================================
package OSCARS::WBUI::Method::ReservationArchiveForm;

=head1 NAME

OSCARS::WBUI::Method::ReservationArchiveForm - outputs archive reservations form

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::ReservationArchiveForm;

=head1 DESCRIPTION

Handles CGI request to display the archive reservations form.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

June 22, 2006

=cut

use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# Currently a noop.
#
sub makeCall {
    my( $self, $request ) = @_;

    return {};
} #____________________________________________________________________________


###############################################################################
# outputDiv:  Prints out the archive reservations form.  Not functional yet. 
#
# In:   response from SOAP call
# Out:  None
#
sub outputDiv {
    my( $self, $request, $response, $authorizations ) = @_;

    my $msg = "Reservation archiving form";
    print( qq{
    <div id='reservation-ui'>
    <form method='post' action='' onsubmit="return submit_form(this, 
	     'method=ReservationArchive;');">

     <p>Required inputs are bordered in green.  Ranges or types of valid 
     entries are given in parentheses below the input fields.</p>
    </form>
    </div>
    } );
    return $msg;
} #____________________________________________________________________________


######
1;
