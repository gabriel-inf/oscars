###############################################################################
package Client::AAAS::AddUser;

# Handles adding a user to the database.
#
# Last modified:  November 28, 2005
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

    $params->{server_name} = 'AAAS';
    $self->SUPER::modify_params($params);
} #____________________________________________________________________________


###############################################################################
# make_call:  make SOAP calls to add user, and get user list
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    # First make call to add user (any exceptions are handled there)
    my $results = $self->SUPER::make_call($soap_server, $soap_params);

    # and then get back list of users, including new user
    $self->{id} = $results->{id};
    $soap_params->{method} = 'view_users';
    my $som = $soap_server->dispatch($soap_params);
    if ($som->faultstring) {
        $self->update_status_msg($som->faultstring);
        return undef;
    }
    return $som->result;
} #____________________________________________________________________________


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
    <msg>$self->{user_dn} successfully added user $self->{id}</msg>
    };
    Client::AAAS::Users::output_users( $results, $self->{session});
    print "</xml>\n";
} #____________________________________________________________________________


######
1;
