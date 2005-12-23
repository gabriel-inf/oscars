###############################################################################
package Client::AAAS::DeleteUser;

# Handles deleting a user from the database.
#
# Last modified:  December 13, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Data::Dumper;

use Client::AAAS::Users;

use Client::SOAPAdapter;
our @ISA = qw{Client::SOAPAdapter};

#______________________________________________________________________________


###############################################################################
sub modify_params {
    my( $self, $params ) = @_;

    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# make_call:  make SOAP calls to delete user and get back user list
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    # First make call to delete user (any exceptions are handled there)
    my $results = $self->SUPER::make_call($soap_server, $soap_params);

    # and then get back list of users, minus deleted user
    $self->{id} = $results->{id};
    $soap_params->{method} = 'ViewUsers';
    my $som = $soap_server->dispatch($soap_params);
    if ($som->faultstring) {
        $self->update_status_msg($som->faultstring);
        return undef;
    }
    return $som->result;
} #____________________________________________________________________________


##############################################################################
# output:  prints the resulting list after user deletion
#
sub output {
    my( $self, $results ) = @_;

    print $self->{cgi}->header(
        -type=>'text/xml');
    print "<xml>\n";
    print qq{
    <msg>$self->{user_dn} successfully deleted user $self->{id}</msg>
    };
    Client::AAAS::Users::output_users( $results, $self->{session});
    print "</xml>\n";
} #____________________________________________________________________________


######
1;
