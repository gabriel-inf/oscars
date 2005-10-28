package Common::Exception;

use base qw(Error);
use overload ('""' => 'stringify');

sub new {
    my $self = shift;
    my $text = "" . shift;
    my @args = ();

    $self->SUPER::new(-text => $text, @args);
}
1;
