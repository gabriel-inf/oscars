# Validator.pm:  Input validation
# Last modified: August 2, 2005
# David Robertson (dwrobertson@lbl.gov)

package BSS::Frontend::Validator;

use strict;

use Data::Dumper;

my $form_variables = {
    'insert_reservation' => {
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
    'delete_reservation' => {},
    'get_reservations' => {}
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
            if ($r->{regexp} && $inref->{$k} !~ $r->{regexp}) {
                return $r->{error};
            }
            if ($inref->{k}) {
                if ($r->{test_function} && !($r->{test_function}())) {
                    return $r->{error};
                }
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
