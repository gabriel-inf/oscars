#==============================================================================
package OSCARS::WBUI::Method::ReservationArchiveForm;

=head1 NAME

OSCARS::WBUI::Method::ReservationArchiveForm - outputs archive reservations form

=head1 SYNOPSIS

  use OSCARS::WBUI::Method::ReservationArchiveForm;

=head1 DESCRIPTION

Handles request to display the archive reservations form.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 19, 2006

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
# getTab:  Gets navigation tab to set if this method returned successfully.
#
# In:  None
# Out: Tab name
#
sub getTab {
    my( $self ) = @_;

    return 'ListReservations';
} #___________________________________________________________________________ 


###############################################################################
# outputContent:  Prints out new reservations list after archiving.
#       Not functional yet. 
#
# In:   response from SOAP call
# Out:  None
#
sub outputContent {
    my( $self, $request, $response ) = @_;

    my $msg = "Reservation archiving form";
    print( qq{
    <form method='post' action='' onsubmit="return submitForm(this, 
	     'method=ReservationArchive;');">

     <p>Required inputs are bordered in green.  Ranges or types of valid 
     entries are given in parentheses below the input fields.</p>
    </form>
    } );
    return $msg;
} #____________________________________________________________________________


######
1;
