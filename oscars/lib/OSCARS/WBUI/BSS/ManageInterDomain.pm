#==============================================================================
package OSCARS::WBUI::BSS::ManageInterDomain;

=head1 NAME

OSCARS::WBUI::BSS::ManageInterDomain - Manage inter-domain setup.

=head1 SYNOPSIS

  use OSCARS::WBUI::BSS::ManageInterDomain;

=head1 DESCRIPTION

Manage inter-domain setup and communications.

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
# output:  not functional
#
sub output {
    my( $self, $results ) = @_;

    print $self->{cgi}->header( -type=>'text/xml' );
    print "<xml>\n";
    print qq{ <msg>OSCARS inter-domain setup (not functional)</msg> };
    $self->{tabs}->output('ManageInterDomain', $results->{authorizations});
    print qq{ <div> </div>
    };
    print "</xml>\n";
} #____________________________________________________________________________


######
1;
