package Client::AAASCallbacks;

# Callbacks for SOAPAdapter for various phases of OSCARS request to AAAS.
# Will be converted to something more object-oriented.
# Last modified:  November 17, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;

use Error qw(:try);
use Data::Dumper;

##############################################################################
sub login_output {
    my( $results ) = @_;

    print qq{
      <msg>User $results->{user_dn} signed in.</msg>
      <div id="info_form">
      <p>
      With the advent of service sensitive applications (such as remote-
      controlled experiments, time constrained massive data transfers,
      video-conferencing, etc.), it has become apparent that there is a need
      to augment the services present in today's ESnet infrastructure.
      </p>

      <p>
      Two DOE Office of Science workshops in the past two years have clearly 
      identified both science discipline driven network requirements and a 
      roadmap for meeting these requirements.  This project begins to 
      address one element of the roadmap: dynamically provisioned, QoS paths.
      </p>

      <p>
      The focus of the ESnet On-Demand Secure Circuits and Advance Reservation 
      System (OSCARS) is to develop and deploy a prototype service that enables 
      on-demand provisioning of guaranteed bandwidth secure circuits within 
      ESnet.
      </p>

      <p>To begin using OSCARS, click on one of the notebook tabs.</p>
      </div>
    };
}
######

###############################################################################
# get_user_list_output:  If the caller has admin privileges print a list of 
# all users returned by the SOAP call
#
# In:  results of SOAP call
# Out: None
#
sub get_user_list_output {
    my ( $results ) = @_;

    print qq{
      <msg>Successfully read user list.</msg>
    <div id="zebratable_ui">
      <p>Click on the user's last name to view detailed user information.</p>
      <table cellspacing="0" width="90%" class="sortable" id="userlist">
        <thead><tr>
          <td>Last Name</td>          <td>First Name</td>
          <td>Distinguished Name</td> <td>Level</td>
          <td>Organization</td>       <td>Status</td>
        </tr></thead>
      <tbody>
    };
    for my $row (@$results) { output_user_row( $row ); }
    print qq{
      </tbody></table>
      </div>";
    };
}
######

###############################################################################
# output_user_row:  print the information for one user
#
sub output_user_row {
    my( $row ) = @_;

    print qq{
    <tr>
      <td><a href="#" style="/styleSheets/layout.css"
        onclick="new_page(
        '/perl/adapt.pl?method=get_profile;id=$row->{user_dn}');
        return false;">$row->{user_last_name}</a></td>
      <td>$row->{user_first_name}</td> <td>$row->{user_dn}</td>
      <td>$row->{user_level}</td>      <td>$row->{institution_id}</td>
      <td>$row->{user_status}</td>
    </tr>
    };
}

######

##############################################################################
# add_user_output: print add user form, and results from SOAP call, if any
#
sub add_user_output {
    my( $results ) = @_;

    my $params_str;
    print qq{

    <msg>Successfully added user $results->{user_dn}</msg>
    <div>
    <h3>Add a new user</h3>
    <p>The <strong>Admin Password</strong> is your password for 
    <strong>$results->{user_dn}</strong>.</p>]
    <p>Required fields are outlined in green.</p>
    <form method="post" action="" onsubmit="return submit_form(this,
              '/perl/adapt.pl?method=add_user', '$params_str');">
    <table>
    <tr>
      <td>Distinguished Name</td>
      <td><input type="text" name="user_dn" size="40"</input></td>
    </tr>
    };
    output_password_fields($results);
    print qq{
    <tr>
      <td>User Level</td>
      <td><input class="required" type="text" name="user_level" size="40"
           value="$results->{user_level}"></input> </td>
    </tr>
    };
    output_user_profile_fields($results);
    print qq{
    <p><input type="submit" value="Create Profile"></input></p>
    </form>
    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
}
######

##############################################################################
# get_user_profile_output:  print user profile form, and results retrieved via
# a SOAP call, if any
#
sub get_user_profile_output {
    my( $results ) = @_;

    my $params_str;
    print qq{
    <msg>User profile</msg>
    <div>
    <h3>Editing profile for user: $results->{user_dn}</h3>
    <p>Required fields are outlined in green.</p>
    <form method="post" action=""
        onsubmit="return submit_form(this,
                                     '/perl/adapt.pl?method=set_profile',
                                     '$params_str');">
    <table>
    <tr><td>Distinguished Name</td> <td>$results->{user_dn}</td></tr>
    };
    output_password_fields($results);
    if ($results->{admin}) {
        print qq{
        <tr>
          <td>User Level</td>
          <td><input class="required" type="text" name="user_level" size="40"
               value="$results->{user_level}"></input></td>
        </tr>
        };
    }
    output_user_profile_fields($results);
    print qq{
    <p><input type="submit" value="Change Profile"></input></p>
    </form>
    <p>For inquiries, please contact the project administrator.</p>
    </div>
    };
}
######

##############################################################################
# output_user_profile_fields:  print fields of user profile
#
sub output_user_profile_fields {
    my( $row ) = @_;

    print qq{
      <tr><td>First Name</td>
      <td><input class="required" type="text" name="user_first_name" size="40"
           value="$row->{user_first_name}"></input>
      </td></tr>
      <tr><td>Last Name</td>
      <td><input class="required" type="text" name="user_last_name" size="40"
           value="$row->{user_last_name}></input>
      </td></tr>
      <tr><td>Organization</td>
      <td><input class="required" type="text" name="institution" size="40"
           value="$row->{institution}"</input>
      </td></tr>
      <tr><td valign="top">Personal Description</td>
      <td><textarea name="user_description" rows="3" cols="50">
           $row->{user_description}</textarea>
      </td></tr>
      <tr><td>E-mail (Primary)</td>
      <td><input class="required" type="text" name="user_email_primary"
           size="40" value="$row->{user_email_primary}"></input>
      </td></tr>
      <tr>
      <td>E-mail (Secondary)</td>
      <td><input type="text" name="user_email_secondary" size="40"
           value="$row->{user_email_secondary}"></input>
      </td></tr>
      <tr><td>Phone Number (Primary)</td>
      <td><input class="required" type="text" name="user_phone_primary"
           size="40" value="$row->{user_phone_primary}"></input>
      </td></tr>
      <tr><td>Phone Number (Secondary)</td>
      <td><input type="text" name="user_phone_secondary" size="40"
           value="$row->{user_phone_secondary}"></input>
      </td></tr>
      </table>
      <p>Please check your contact information carefully before submitting 
      the form.</p>
    };
}
######


##############################################################################
# output_password_fields:  print rows having to do with passwords
#
sub output_password_fields {
    my( $results ) = @_;

    my $admin_form = 0;

    # TODO:  FIX
    if ($results->{method} eq 'new_user_form') { $admin_form = 1; }

    print qq{ <td> };
    if ($admin_form) { print qq{ Admin Password }; }
    else { print qq{ Current Password  }; }
    print qq{
      </td>
      <td>
        <input class="required" type="password" name="user_password" size="40">
        </input>
      </td>
      </tr>
    };
    print qq{
      <tr>
      <td>
    };
    if ($admin_form) { print qq{ New User Password }; }
    else { print qq{ New Password (Enter twice }; }
    print qq {
      </td>
      <td>
        <input type="password" name="password_new_once" size="40"></input>
      </td>
      </tr>
    };

    print qq{
      <tr>
      <td>
    };
    if ($admin_form) { print qq{ (Enter twice) }; }
    else { print qq{ Leave blank to stay the same) }; }
    print qq {
      </td>
      <td>
        <input type="password" name="password_new_twice" size="40"></input>
      </td>
      </tr>
    };
}
######
 
1;
