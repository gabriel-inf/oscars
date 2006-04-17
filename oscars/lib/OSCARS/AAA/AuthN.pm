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
    my $req_host = $daemon->{_request}->{_headers}{host};
    if (0) {
	    #if ($req_host !~ /localhost/ ){
        my( $user_login, $error_msg ) = 
	        $self->verify_signature($de, $envelope);
	# throw type of exception that main try in oscars script understands
#	if ( $error_msg ) { throw Error::Simple($error_msg); }
	if ( !$error_msg ) {
	   $user = $self->get_user($user_login); 
	   return $user;
	
	} else { print STDERR "return from verify_signature is $error_msg \n";}
    }
    # otherwise, message came via web interface, use login name and password
    # in params for authentication
    # for now drop thru if message is not signed. Assume  message carries password.
    $user = $self->verify_login($params);
    return $user;
} #____________________________________________________________________________


###############################################################################
# verify_signature:  authenticates user via their XML signature
#
# In:  WSRF::Deserializer instance, SOAP envelope
# Out: error message, if any
#
sub verify_signature {
    my( $self, $de, $envelope ) = @_;

    my ( $user_login, $ex );
    my %verify_results;
    my $msg_som = $de->deserialize($envelope);
    eval { %verify_results = WSRF::WSS::verify($msg_som); };
    if ($@) {
	$ex = $@;
	return ($user_login,$ex);
    }
    print STDERR "received signed message\n";
    # print "$verify_results(X509) \n";
    my $x509_pem = $verify_results{X509};
    my $X509 = Crypt::OpenSSL::X509->new_from_string($x509_pem);
    my $subject = $X509->subject();
    my $issuer =$X509->issuer();
    print STDERR "$issuer \n$subject\n";
    my $query = "SELECT user_login FROM users " .
                    "WHERE user_cert_subject = '$subject'";
    my $row = $self->{db}->get_row($query);
    $user_login = $row->{user_login};
    print STDERR "user is $user_login\n";
    return( $user_login, $ex );
} #____________________________________________________________________________


###############################################################################
# verify_login:  authenticates user via login name and password
#
# In:  SOAP parameters
# Out: OSCARS::User instance
#
sub verify_login {
    my( $self, $params ) = @_;

    my $user = $self->get_user($params->{user_login});
    if ($user->authenticated()) { return $user; }
    if (!$params->{user_password}) {
	throw Error::Simple('Attempting to access a SOAP method before authenticating.');
    }
    # Get the password and privilege level from the database.
    my $statement = 'SELECT user_password FROM users WHERE user_login = ?';
    my $results = $self->{db}->get_row($statement, $user->{login});
    # Make sure user exists.
    if ( !$results ) {
        throw Error::Simple('Please check your login name and try again.');
    }
    # compare passwords
    my $encoded_password = crypt($params->{user_password}, 'oscars');
    if ( $results->{user_password} ne $encoded_password ) {
        # see if password already encrypted
        if ( $results->{user_password} ne $params->{user_password} ) {
            throw Error::Simple('Please check your password and try again.');
        }
    }
    $user->set_authenticated(1);
    return $user;
} #____________________________________________________________________________


###############################################################################
# Gets user instance from user list if it exists; otherwise create an instance
# associated with the component and distinguished name given. 
#
sub get_user {
    my( $self, $login ) = @_;

    if (!$self->{users}->{$login}) {
        $self->{users}->{$login} = OSCARS::User->new('login' => $login);
    }
    return $self->{users}->{$login};
} #____________________________________________________________________________


###############################################################################
# Removes given user from the user list.
#
sub remove_user {
    my( $self, $login ) = @_;

    # close all cached db connection handles
    if ( $self->{users}->{$login} ) {
        $self->{users}->{$login}->close_handles();
    }
    $self->{users}->{$login} = undef;
} #____________________________________________________________________________


###############################################################################
# Currently only used by installation tests.
#
sub get_credentials {
    my( $self, $user_login, $credential_type ) = @_;

    if ($credential_type eq 'password') {
        my $statement = 'SELECT user_password FROM users WHERE user_login = ?';
        my $results = $self->{db}->get_row($statement, $user_login);
        return $results->{user_password};
    }
    return undef;
} #____________________________________________________________________________

######
1;
