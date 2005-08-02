# Validator.pm:  Input validation
# Last modified: July 31, 2005
# David Robertson (dwrobertson@lbl.gov)

package AAAS::Frontend::Validator;

use strict;

use Data::Dumper;

my $form_variables = {
    'verify_login' => {
        # must be valid email address
        'user_dn' => (
            {'regexp' => '.+',
             'error' => "Please enter your login name."
            }
        ),
        'user_password' => (
            {'regexp' => '.+',
             'error' => "Please enter your password."
            }
        )
    },
    # currently no tests
    'get_profile' => {},
    'set_profile' => {
        'user_password' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's current password."
            }
        ),
        'user_last_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's last name."
            }
        ),
        'user_first_name' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's first name."
            }
        ),
        'user_institution' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's organization."
            }
        ),
        'user_email_primary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary email address."
            }
        ),
        'user_phone_primary' => (
            {'regexp' => '.+',
            'error' => "Please enter the user's primary phone number."
            }
        )
    },
    # currently no tests
    'check_login_status' => {},
    'get_userlist' => {},
    'logout' => {}
};

###############################################################################
sub new {
    my ($_class, %_args) = @_;
    my ($_self) = {%_args};
  
    # Bless $_self into designated class.
    bless($_self, $_class);
  
    # Initialize.
    $_self->initialize();
  
    return($_self);
}

sub initialize {
    my ($self) = @_;
}
######

###############################################################################
## validate:  input validation
#
sub validate {
    my( $self, $inref ) = @_;

    my( $m, $k, $r );

    $m = $form_variables->{$inref->{method}};
    if (!$m) {
        return "Cannot validate $inref->{method}";
    }
    # validate form variables for that method
    for $k (keys %{$m}) {
        # set to blank string if not defined
        if (!defined($inref->{$k})) {
            $inref->{$k} = '';
        }
        # for all tests 
        for $r ($m->{$k}) {
            if ($inref->{$k} !~ $r->{regexp}) {
                return $r->{error};
            }
        }
    }
    return "";
}
######

sub numeric_compare {
    my( $self, $val, $lesser_val, $greater_val ) = @_;

    if ($lesser_val > $val) { return 0; }
    if ($greater_val < $val) { return 0; }
    return 1;
}
######

######
1;
