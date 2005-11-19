# Last modified:  November 18, 2005
# David Robertson (dwrobertson@lbl.gov)


package Client::SOAPAdapterFactory;
    use strict;
    use Data::Dumper;

###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
}

sub instantiate {
    my( $self, $requested_type, $args ) = @_;

    my @loc_array      = split('::', $requested_type); 
    shift(@loc_array);  # remove parent directory
    my $location       = join('/', @loc_array) . ".pm";
    my $subclass       = "$requested_type";

    require $location;
    return $subclass->new(%$args);
}

#############################################################################

package Client::SOAPAdapter;
    use strict;

    use Data::Dumper;
    use CGI;
    use SOAP::Lite;

    use Client::UserSession;

###############################################################################
#
sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my ($self) = @_;

    $self->{cgi} = CGI->new();
    $self->{session} = Client::UserSession->new();
}
######

##############################################################################
# before_call:  Perform operations necessary before making SOAP call
#
sub before_call {
    my( $self ) = @_;

    my( $params );

    # Note that CGI param is a method call, not a hash lookup
    for $_ ($self->{cgi}->param) {
        $params->{$_} = $self->{cgi}->param($_);
    }
    $params->{signed_on} = $self->{session}->verify_session($self->{cgi});
    return( $params );
}
######

##############################################################################
# make_call:  make SOAP call, and get results
#
sub make_call {
    my( $self, $soap_server, $soap_params ) = @_;

    my $som = $soap_server->dispatch($soap_params);
    if ($som->faultstring) {
        # TODO:  return error in status
        #$self->update_page($som->faultstring);
        return undef;
    }
    return $som->result;
}
######

##############################################################################
# output:  formats and prints results to send back to browser
#
sub output {
    my( $self, $results ) = @_;

}
######

##############################################################################
# after_call:  Perform any operations necessary after making SOAP call
#
sub after_call {
    my( $self, $results ) = @_;

}
######

######
1;
