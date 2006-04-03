#==============================================================================
package OSCARS::MethodFactory;

use strict;
use Data::Dumper;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #___________________________________________________________________________ 


###############################################################################
#
sub instantiate {
    my( $self, $user, $params, $logger ) = @_;

    my( $location, $class_name );

    $location = 'OSCARS/' . $params->{server} . '/Method/' .
                    $params->{method} . '.pm';
    $class_name = 'OSCARS::' . $params->{server} . '::Method::' .
                    $params->{method};
    require $location;
    return $class_name->new( 'user'   => $user,
                             'params' => $params,
		             'logger' => $logger );
} #___________________________________________________________________________ 


#==============================================================================
package OSCARS::Method;

=head1 NAME

OSCARS::Method - Superclass for all SOAP methods.

=head1 SYNOPSIS

  use OSCARS::Method;

=head1 DESCRIPTION

Superclass for all SOAP methods.  Contains methods for all phases of
an OSCARS request.  Assumes that authentication has already been performed.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 3, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

use SOAP::Lite;

use OSCARS::Mail;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{mailer} = OSCARS::Mail->new();
    $self->{param_tests} = {};
} #____________________________________________________________________________


###############################################################################
# authorized:  Check whether user calling this method has the proper 
#     authorizations, including viewing and setting parameters.  If not 
#     overriden, a noop.
#
sub authorized {
    my( $self ) = @_;

    return 1;
} #____________________________________________________________________________


###############################################################################
# validate:  validate incoming parameters
#
sub validate {
    my( $self ) = @_;

    my( $test );

    my $op = $self->{params}->{op};
    if ( !$op ) { return; }

    # for all tests 
    for my $test_name (keys(%{$self->{param_tests}->{$op}})) {
        $test = $self->{param_tests}->{op}->{$test_name};
        if (!$self->{params}->{$test_name}) {
            throw Error::Simple(
                "Cannot validate $self->{params}->{method}, test $test_name failed");
        }
        if ($self->{params}->{$test_name} !~ $test->{regexp}) {
            throw Error::Simple( $test->{error} );
        }
    }
} #____________________________________________________________________________


###############################################################################
sub numeric_compare {
    my( $self, $val, $lesser_val, $greater_val ) = @_;

    if ($lesser_val > $val) { return 0; }
    if ($greater_val < $val) { return 0; }
    return 1;
} #____________________________________________________________________________


###############################################################################
# dispatch
#
sub dispatch {
    my( $self ) = @_;

    return 1;
} #___________________________________________________________________________ 


###############################################################################
# post_process:  Perform any operations necessary after making SOAP call
#
sub post_process {
    my( $self, $results ) = @_;

    my $messages = $self->generate_messages($results);
    if ($messages) {
        $self->{mailer}->send_message($messages);
    }
} #___________________________________________________________________________ 


###############################################################################
# generate_messages:  overriden if anything to mail
#
sub generate_messages {
    my( $self, $results ) = @_;

    return undef;
} #___________________________________________________________________________ 


######
1;
