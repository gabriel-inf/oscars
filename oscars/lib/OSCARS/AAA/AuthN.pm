#==============================================================================
package OSCARS::AAA::AuthN;

=head1 NAME

OSCARS::AAA::AuthN - performs authenticatication

=head1 SYNOPSIS

  use OSCARS::AAA::AuthN;

=head1 DESCRIPTION

Performs authentication required for access.

=head1 AUTHORS

Mary Thompson (mrthompson@lbl.gov)
David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 17, 2006

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
    $self->{db}->connect($self->{database});
} #____________________________________________________________________________


###############################################################################
# authenticate:  authenticates user
#
# In:  OSCARS::User instance, hash of parameters
# Out: None
#
sub authenticate {
    my( $self, $daemon, $params ) = @_;

    my $user;

    # check to see if message should be signed
    my $envelope = $daemon->{_request}->{_content};
    my $de = WSRF::Deserializer->new();
    my $reqHost = $daemon->{_request}->{_headers}{host};
    if (0) {
	    #if ($reqHost !~ /localhost/ ){
        my( $login, $errorMsg ) = $self->verifySignature($de, $envelope);
	# throw type of exception that main try in oscars script understands
#	if ( $errorMsg ) { throw Error::Simple($errorMsg); }
	if ( !$errorMsg ) {
	   $user = $self->getUser($login); 
	   return $user;
	
	} else { print STDERR "return from verifySignature is $errorMsg \n";}
    }
    # otherwise, message came via web interface, use login name and password
    # in params for authentication
    # for now drop thru if message is not signed. Assume  message carries password.
    $user = $self->verifyLogin($params);
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

    my ( $login, $ex );
    my %verifyResults;
    my $msgSom = $de->deserialize($envelope);
    eval { %verifyResults = WSRF::WSS::verify($msgSom); };
    if ($@) {
	$ex = $@;
	return ($login,$ex);
    }
    print STDERR "received signed message\n";
    # print "$verifyResults(X509) \n";
    my $x509_pem = $verifyResults{X509};
    my $X509 = Crypt::OpenSSL::X509->new_from_string($x509_pem);
    my $subject = $X509->subject();
    my $issuer =$X509->issuer();
    print STDERR "$issuer \n$subject\n";
    my $query = "SELECT login FROM users WHERE certSubject = '$subject'";
    my $row = $self->{db}->getRow($query);
    $login = $row->{login};
    print STDERR "user is $login\n";
    return( $login, $ex );
} #____________________________________________________________________________


###############################################################################
# verifyLogin:  authenticates user via login name and password
#
# In:  SOAP parameters
# Out: OSCARS::User instance
#
sub verifyLogin {
    my( $self, $params ) = @_;

    my $user = $self->getUser($params->{login});
    if ($user->authenticated()) { return $user; }
    if (!$params->{password}) {
	throw Error::Simple('Attempting to access a SOAP method before authenticating.');
    }
    # Get the password and privilege level from the database.
    my $statement = 'SELECT password FROM users WHERE login = ?';
    my $results = $self->{db}->getRow($statement, $user->{login});
    # Make sure user exists.
    if ( !$results ) {
        throw Error::Simple('Please check your login name and try again.');
    }
    # compare passwords
    my $encodedPassword = crypt($params->{password}, 'oscars');
    if ( $results->{password} ne $encodedPassword ) {
        # see if password already encrypted
        if ( $results->{password} ne $params->{password} ) {
            throw Error::Simple('Please check your password and try again.');
        }
    }
    $user->setAuthenticated(1);
    return $user;
} #____________________________________________________________________________


###############################################################################
# Gets user instance from user list if it exists; otherwise create an instance
# associated with the component and distinguished name given. 
#
sub getUser {
    my( $self, $login ) = @_;

    if (!$self->{users}->{$login}) {
        $self->{users}->{$login} = OSCARS::User->new('login' => $login);
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

    if ($credentialType eq 'password') {
        my $statement = 'SELECT password FROM users WHERE login = ?';
        my $results = $self->{db}->getRow($statement, $login);
        return $results->{password};
    }
    return undef;
} #____________________________________________________________________________


######
1;
