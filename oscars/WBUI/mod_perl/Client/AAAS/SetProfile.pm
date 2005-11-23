###############################################################################
package Client::AAAS::SetProfile;

# Handles modifying a user's profile.
#
# Last modified:  November 21, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#______________________________________________________________________________


##############################################################################
# output:  print user profile form, and results retrieved via
# a SOAP call, if any
#
sub output {
    my( $self, $results ) = @_;

    print $self->{cgi}->header(
        -type=>'text/xml');
    print "<xml>\n";
    print qq{
    <msg>Successfully updated user $self->{user_dn}'s profile</msg>
    };
    print "</xml>\n";
} #____________________________________________________________________________


######
1;
