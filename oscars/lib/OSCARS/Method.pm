#==============================================================================
package OSCARS::MethodFactory;

use strict;
use Data::Dumper;

use OSCARS::PluginManager;
use OSCARS::Mail;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
}

###############################################################################
#
sub instantiate {
    my( $self, $user, $method ) = @_;

    return $self->{pluginMgr}->usePlugin($method, $user);
} #___________________________________________________________________________ 


#==============================================================================
package OSCARS::Method;

=head1 NAME

OSCARS::Method - Superclass for all SOAP methods.

=head1 SYNOPSIS

  use OSCARS::Method;

=head1 DESCRIPTION

Superclass for all SOAP methods.  Contains methods for all phases of
a SOAP request.  Assumes that authentication has already been performed.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

May 24, 2006

=cut

use strict;

use Data::Dumper;
use Error qw(:try);

use OSCARS::Mail;
use OSCARS::Library::Reservation::ClientForward;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{forwarder} = OSCARS::Library::Reservation::ClientForward->new();
    $self->{paramTests} = {};
    $self->{db} = $self->{user}->getDbHandle($self->{database});
    my $configuration = $self->{pluginMgr}->getConfiguration();
    $self->{mailer} = OSCARS::Mail->new(
                         'configuration' => $configuration->{notification} );
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
    my( $self, $params ) = @_;

    my( $test );

    my $method = $params->{method};
    if ( !$method ) { return; }

    # for all tests 
    for my $testName (keys(%{$self->{paramTests}->{$method}})) {
        $test = $self->{paramTests}->{method}->{$testName};
        if (!$params->{$testName}) {
            throw Error::Simple(
                "Cannot validate $method, test $testName failed");
        }
        if ($params->{$testName} !~ $test->{regexp}) {
            throw Error::Simple( $test->{error} );
        }
    }
} #____________________________________________________________________________


###############################################################################
sub numericCompare {
    my( $self, $val, $lesser, $greater ) = @_;

    if ($lesser > $val) { return 0; }
    if ($greater < $val) { return 0; }
    return 1;
} #____________________________________________________________________________


###############################################################################
# postProcess:  Perform any operations necessary after making SOAP call
#
sub postProcess {
    my( $self, $params, $results ) = @_;

    # must be notification entry in the XML configuration file for this
    # method, for any message to be sent
    $self->{mailer}->sendMessage($self->{user}->{login}, $params->{method},
	                         $results);
} #___________________________________________________________________________ 


######
1;
