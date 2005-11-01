# general.pl
#
# library for general cgi script usage
# Last modified: October 31, 2005
# David Robertson (dwrobertson@lbl.gov)


use CGI;
use CGI::Session;
use Data::Dumper;
use SOAP::Lite;

######################################
# SOAP client section
######################################

######################################
# All calls are made to AAAS front end
######################################

my $AAAS_server = SOAP::Lite
  -> uri('http://localhost:2000/Dispatcher')
  -> proxy ('http://localhost:2000/AAAS_server.pl');



##############################################################################
#
sub aaas_dispatcher
{
    my ($params) = @_;
    return($AAAS_server->dispatch($params));
}
######

######################################
# session verification section
######################################

##############################################################################
# start_session: Sets cookie containing session id to be used in granting
#   access.  Note that this does not handle checking whether the user is in 
#   the database; that is handled by a method in the AAAS.
#
# In:   ref to CGI instance
# Out:  None
#
sub start_session
{
    my ($cgi, $login_results) = @_;
    my ($session, $sid, $cookie);

    $session = CGI::Session->new("driver:File", undef, {Directory => "/tmp"});
    $sid = $session->id();
    $cookie = $cgi->cookie(CGISESSID => $sid);
    $session->param("user_dn", $cgi->param('user_dn'));
    $session->param("user_level", $login_results->{'user_level'});
    $session->param("timezone_offset", $cgi->param('timezone_offset'));
    return( $cgi->param('user_dn'), $login_results->{'user_level'}, $sid );
}
######

##############################################################################
# verify_session:  Checks to see that a cookie containing a valid session
# id is set before granting access.
#
# In:  ref to CGI instance
# Out: 1 (logged in)/0 (not logged in)
#
sub verify_session
{
    my ($cgi) = @_;
    my ($session, $stored_dn, $user_level, $timezone_offset);

    $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});

    # Unauthorized user may know to set CGISESSID cookie. However,
    # an entirely new session (without the dn param) will be 
    # created if there is no valid session with that id.
    $stored_dn = $session->param("user_dn");
    $user_level = $session->param("user_level");
    $timezone_offset = $session->param("timezone_offset");
    if (!$stored_dn)  {
        return( undef, undef, undef, '..' );
    }
    else {
       $cgi->param(-name=>'user_dn',-value=>$stored_dn);
       $cgi->param(-name=>'user_level',-value=>$user_level);
       $cgi->param(-name=>'timezone_offset',-value=>$timezone_offset);
       return( $stored_dn, $user_level, $timezone_offset, '..' );
    }
}
######


##############################################################################
sub end_session
{
    my( $cgi ) = @_;
    my ($session, $stored_dn);
  
    $session = CGI::Session->new(undef, $cgi, {Directory => "/tmp"});
    $session->clear(["user_dn"]);
    $session->clear(["user_level"]);
    $session->clear(["timezone_offset"]);
}
######

##############################################################################
# generate_random_string: Takes care of generating a random string for all
#     functions
#
# In: $string_length
# Out: $random_string
#
sub generate_random_string
{
    my $string_length = $_[0] + 0;	# make it a numeric value

    my @alphanumeric = ('a'..'z', 'A'..'Z', 0..9);
    my $random_string = join( '', map( $alphanumeric[ rand @alphanumeric ], 0 .. $string_Length ) );

    return $random_string;
}
######

##############################################################################
# authorized:  Given the user level string, see if the user has the required
#              privilege 
#
sub authorized {
    my( $user_level, $required_priv ) = @_;
 
    for my $priv (split(' ', $user_level)) {
        if ($priv eq $required_priv) {
            return( 1 );
        }
    }
    return( 0 );
}
######


######################################
# utilities section
######################################

##############################################################################
# get_params:  Material common to almost all scripts; has to verify
#              user is logged in, and copy over form params
#
sub get_params {

    my( %form_params, $tz, $starting_page );

    my $cgi = CGI->new();
    ($form_params{user_dn}, $form_params{user_level}, $tz, $starting_page) =
                                                          verify_session($cgi);
    print $cgi->header( -type=>'text/xml' );
    if (!$form_params{user_level}) {
        print "Location:  " . $starting_page . "\n\n";
        return (undef, undef);
    }
    for $_ ($cgi->param) {
        $form_params{$_} = $cgi->param($_);
    }
    return( \%form_params, $starting_page );
}
######

##############################################################################
# get_results:  Material common to almost all scripts;
#               make the SOAP call, and get the results.
#
sub get_results {
    my( $form_params) = @_;

    my $som = aaas_dispatcher($form_params);
    if ($som->faultstring) {
        update_page($som->faultstring);
        return undef;
    }
    my $results = $som->result;
    return $results;
}
######

##############################################################################
# update_page:  If output_func is null, an error has occurred and only the
#               error message is printed in the status div on the OSCARS
#               page.
#
sub update_page {
    my( $msg, $output_func, $user_dn, $user_level) = @_;

    print "<xml>\n";
    print "<msg>\n";
    print "$msg\n";
    print "</msg>\n";
    if ($output_func) {
        print "<user_level>\n";
        print "$user_level\n";
        print "</user_level>\n";
        print "<div>\n";
        $output_func->($user_dn, $user_level);
        print "</div>\n";
    }
    print "</xml>\n";
}
######

##############################################################################
sub output_info {
    my ($unused1, $unused2) = @_;

    print "<div id=\"info_form\"><p>With the advent of service sensitive applications (such as remote",
	  " controlled experiments, time constrained massive data",
        " transfers video-conferencing, etc.), it has become apparent",
        " that there is a need to augment the services present in",
        " today's ESnet infrastructure.</p>\n",

        "<p>Two DOE Office of Science workshops in the past two years have",
        " clearly identified both science discipline driven network",
        " requirements and a roadmap for meeting these requirements.",
        " This project begins to addresses one element of the",
        " roadmap: dynamically provisioned, QoS paths.</p>\n",

        "<p>The focus of the ESnet On-Demand Secure Circuits and",
        " Advance Reservation System (OSCARS) is to develop and",
        " deploy a prototype service that enables on-demand provisioning",
        " of guaranteed bandwidth secure circuits within ESnet.</p>\n",

        "<p>To begin using OSCARS, click on one of the notebook tabs.</p></div>\n";
}
######

##############################################################################
# start_row:  util to print out tr with class depending on input counter
#
# In:  counter
# Out: incremented counter
#
sub start_row {
    my ($ctr) = @_;
   
    if (($ctr % 2) == 0) { print "<tr class=\"even\">"; }
    else { print "<tr class=\"odd\">"; }
    return ($ctr + 1);
}
######


# Don't touch the line below
1;
