#==============================================================================
package OSCARS::Library::AAA::AuthN;

=head1 NAME

OSCARS::Library::AAA::AuthN - performs authenticatication

=head1 SYNOPSIS

  use OSCARS::Library::AAA::AuthN;

=head1 DESCRIPTION

Performs authentication required for access.

=head1 AUTHORS

Mary Thompson (mrthompson@lbl.gov)
David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

June 15, 2006

=cut

use WSRF::Lite;
use Crypt::OpenSSL::X509;
use OSCARS::Database;
use OSCARS::User;

use strict;

use Data::Dumper;
use Error qw(:try);

sub new {
    my ($class, %args) = @_;
    my ($self) = {%args};
  
    bless($self, $class);
    $self->initialize();
    return($self);
}

sub initialize {
    my( $self ) = @_;

    $self->{users} = {};
    $self->{db} = OSCARS::Database->new();
} #____________________________________________________________________________


###############################################################################
# authenticate:  authenticates user
#
# In:  OSCARS::User instance, hash of parameters
# Out: None
#
sub authenticate {
    my( $self, $daemon, $request ) = @_;

    my( $user, $errorMsg );

    $self->{db}->connect($self->{database});
    # check to see if message should be signed
    my $envelope = $daemon->{_request}->{_content};
    my $de = WSRF::Deserializer->new();
    my $reqHost = $daemon->{_request}->{_headers}{host};
    # Special case for BNL forwarding.  Use password for authentication.
    if ( $request->{method} eq 'testForward' ) {
        ( $user, $errorMsg ) = $self->verifyLogin($request);
    }
    # Otherwise, if request did not come from localhost, require signature.
    elsif ($reqHost !~ /localhost/ ){
        ( $user, $errorMsg ) = $self->verifySignature($de, $envelope);
    }
    else {
        # If from localhost, check for password first (occurs if arrived
	# via Web interface).  Done in this order to avoid useless
        # signature verification for requests via browser.
        ( $user, $errorMsg ) = $self->verifyLogin($request);

        # If no password present, try signature (present in scheduler and test
	# suite).
        if ( $errorMsg ) {
	    my $verifyErrorMsg;
	    # More important to have correct error message for browser, than
	    # for scheduler or tests.
            ( $user, $verifyErrorMsg ) = $self->verifySignature($de, $envelope);
	    if ( !$verifyErrorMsg ) { $errorMsg = undef; }
        }
    }
    $self->{db}->disconnect();

    # If an occur occurred, throw the type of exception that the main try in 
    # the oscars script understands.
    if ( $errorMsg ) { throw Error::Simple($errorMsg); }

    return $user;
} #____________________________________________________________________________


###############################################################################
# verifySignature:  authenticates user via their XML signature
#
# In:  WSRF::Deserializer instance, SOAP envelope
# Out: error message, if any
#
sub verifySignature {
    my( $self, $de, $envelope ) = @_;

    my $ex;
    my %verifyResults;
    my $msgSom = $de->deserialize($envelope);
    eval { %verifyResults = WSRF::WSS::verify($msgSom); };
    if ($@) {
	$ex = $@;
	return( undef, $ex );
    }
    # print "$verifyResults(X509) \n";
    my $x509_pem = $verifyResults{X509};
    my $X509 = Crypt::OpenSSL::X509->new_from_string($x509_pem);
    my $subject = $X509->subject();
    my $issuer =$X509->issuer();
    my $query = "SELECT login FROM users WHERE certSubject = '$subject'";
    my $row = $self->{db}->getRow($query);
    my $user = $self->getUser($row->{login}); 
    return( $user, undef );
} #____________________________________________________________________________


###############################################################################
# verifyLogin:  authenticates user via login name and password
#
# In:  SOAP parameters
# Out: OSCARS::User instance
#
sub verifyLogin {
    my( $self, $request ) = @_;

    my( $login, $password );

    # will only happen with testForward method (for BNL)
    if ( $request->{userLogin} ) {
        $login = $request->{userLogin}->{userName};
        $password = $request->{userLogin}->{password};
    }
    else {
        $login = $request->{login};
	$password = $request->{password};
    }
    my $user = $self->getUser($login);
    if ($user->authenticated()) { return( $user, undef ); }
    if ( !$password ) {
	return( undef, "Server may have been restarted.  Please try logging in again.");
    }
    # Get the password and privilege level from the database.
    my $statement = 'SELECT password FROM users WHERE login = ?';
    my $results = $self->{db}->getRow($statement, $login);
    # Make sure user exists.
    if ( !$results ) { 
        return( undef, "Login $login does not exist.");
    }

    # compare passwords
    my $encodedPassword = crypt($password, 'oscars');
    if ( $results->{password} ne $encodedPassword ) {
        # see if password already encrypted
        if ( $results->{password} ne $password ) {
            return( undef,
                    "Password for $login is incorrect.");
        }
    }
    $user->setAuthenticated(1);
    return( $user, undef );
} #____________________________________________________________________________


###############################################################################
# Gets user instance from user list if it exists; otherwise create an instance
# associated with the component and distinguished name given. 
#
sub getUser {
    my( $self, $login ) = @_;

    if (!$self->{users}->{$login}) {
        $self->{users}->{$login} = OSCARS::User->new(
                         'login' => $login,
		         'pluginMgr' => $self->{pluginMgr});
    }
    return $self->{users}->{$login};
} #____________________________________________________________________________


###############################################################################
# Removes given user from the user list.
#
sub removeUser {
    my( $self, $login ) = @_;

    # close all cached db connection handles
    if ( $self->{users}->{$login} ) {
        $self->{users}->{$login}->closeHandles();
    }
    $self->{users}->{$login} = undef;
} #____________________________________________________________________________


###############################################################################
# Currently only used by installation tests.
#
sub getCredentials {
    my( $self, $login, $credentialType ) = @_;

    my $statement;

    $self->{db}->connect($self->{database});
    if ($credentialType eq 'password') {
        $statement = 
	    'SELECT password AS credential FROM users WHERE login = ?';
    }
    else {
        $statement = 
	    'SELECT certificate AS credential FROM users WHERE login = ?';
    }
    my $results = $self->{db}->getRow($statement, $login);
    $self->{db}->disconnect();
    return $results->{credential};
} #____________________________________________________________________________


######
1;
