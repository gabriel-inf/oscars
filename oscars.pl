#!/usr/bin/perl -w

# If it doesn't work:
# perl -MCPAN -e "install Proc::Background"


use strict;
use Proc::Background;

$SIG{TERM} = \&sig_handler;
$SIG{KILL} = \&sig_handler;

my $opts  = {'die_upon_destroy' => 1};
my $aaa = Proc::Background->new($opts, "./oscars_aaa.sh");
sleep(5);
my $core = Proc::Background->new($opts, "./oscars_core.sh");

while (1) {
# do nothing and only wait for a signal
}

exit(0);


sub sig_handler {
    $core->die;
    $aaa->die;
    exit(0);
}
