#==============================================================================
package OSCARS::AAAS::UserCommon;

=head1 NAME

OSCARS::AAAS::UserCommon - provides functionality common to AAAS SOAP methods.

=head1 SYNOPSIS

  use OSCARS::AAAS::UserCommon;

=head1 DESCRIPTION

This class provides functionality common to AAAS SOAP methods.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

December 21, 2005

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    # Bless $_self into designated class.
    bless($self, $class);
    $self->initialize();
    return($self);
}

sub initialize {
     my( $self ) = @_;
     # names of the fields to be displayed on the screen
     $self->{user_profile_fields} =
         'user_last_name, user_first_name, user_dn, user_password, ' .
         'user_email_primary, user_email_secondary, ' .
         'user_phone_primary, user_phone_secondary, user_description, ' .
#    'user_register_time, user_activation_key, ' .
         'institution_id';
} #____________________________________________________________________________


###############################################################################
#
sub get_institution_id {
    my( $self, $institution_name ) = @_;

    my $statement = "SELECT institution_id FROM institutions
                WHERE institution_name = ?";
    my $row = $self->{user}->get_row($statement, $institution_name);
    if ( !$row ) {
        throw Error::Simple("The organization " .
                   "$institution_name is not in the database.");
    }
    return $row->{institution_id};
} #____________________________________________________________________________ 


######
1;

