#==============================================================================
package OSCARS::WBUI::AAAS::ManageInterDomain;

=head1 NAME

OSCARS::WBUI::AAAS::ManageInterDomain - Manage inter-domain setup.

=head1 SYNOPSIS

  use OSCARS::WBUI::AAAS::ManageInterDomain;

=head1 DESCRIPTION

Manage inter-domain setup and communications.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

March 19, 2006

=cut


use strict;

use Data::Dumper;

use OSCARS::WBUI::SOAPAdapter;
our @ISA = qw{OSCARS::WBUI::SOAPAdapter};


###############################################################################
# output_div:  not functional
#
sub output_div {
    my( $self, $results ) = @_;

    my $msg = "OSCARS inter-domain setup (not functional yet)";
    print("<div>OSCARS inter-domain setup if necessary, put here</div>\n");
    return $msg;
} #____________________________________________________________________________


######
1;
