###############################################################################
package AAAS::Frontend::Validator;

# Called by AAAS::SOAP::Dispatcher to determine whether parameters are
# valid before handling them by the AAAS or forwarding them to the BSS.
# 
# Last modified:  November 21, 2005
# David Robertson (dwrobertson@lbl.gov)


use strict;

use Data::Dumper;

my $tests = {
     # AAAS
    'login' => {
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
        'institution' => (
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
    'add_user' => {
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
        'institution' => (
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

    # BSS
    'create_reservation' => {
        'reservation_start_time' => (
            {'regexp' => '.+',
             'error' => "Please enter the reservation starting time."
            }
        ),
        'duration_hour' => (
            {'regexp' => '.+',
             'error' => "Please enter the duration in hours."
            }
        ),
        'source_host' => (
            {'regexp' => '.+',
             'error' => "Please enter starting host name or IP address."
            }
        ),
        'destination_host' => (
            {'regexp' => '.+',
             'error' => "Please enter destination host name or IP address."
            }
        ),
        'reservation_bandwidth' => (
            {'regexp' => '.+',
             'error' => "Please enter the bandwidth you wish to reserve."
            }
        ),
        'reservation_description' => (
            {'regexp' => '.+',
             'error' => "Please enter a description of the purpose for this reservation."
            }
        ),
    },
    # no tests yet
    # AAAS
    'get_profile' => {},
    'view_users' => {},
    # BSS
    'delete_reservation' => {},
    'view_reservations' => {},
    'find_pending_reservations' => {},
    'find_expired_reservations' => {}
};


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________ 


###############################################################################
## validate:  input validation
#
sub validate {
    my( $self, $params ) = @_;

    for $_ (keys(%$params)) {
        print STDERR "param: $_, value: $params->{$_}\n";
    }
    my $pmethod = $tests->{$params->{method}};
    if ( !$pmethod ) {
        return "Cannot validate $params->{method}";
    }
    # validate parameters for that method
    for my $ptest (keys %{$pmethod}) {
        # set to blank string if not defined
        if (!defined($params->{$ptest})) {
            $params->{$ptest} = '';
        }
        # for all tests 
        for my $t ($pmethod->{$ptest}) {
            if ($params->{$ptest} !~ $t->{regexp}) {
                return $t->{error};
            }
            if ($params->{ptest}) {
                if ($t->{test_function} && !($t->{test_function}())) {
                    return $t->{error};
                }
            }

        }
    }
    return "";
} #____________________________________________________________________________ 


###############################################################################
sub numeric_compare {
    my( $self, $val, $lesser_val, $greater_val ) = @_;

    if ($lesser_val > $val) { return 0; }
    if ($greater_val < $val) { return 0; }
    return 1;
} #____________________________________________________________________________ 


######
1;
