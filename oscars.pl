#!/usr/bin/perl -w

# If it doesn't work:
# perl -MCPAN -e "install Proc::Background"

use strict;
use Proc::Background;

# set up a signal handler
$SIG{TERM} = \&sig_handler;
$SIG{KILL} = \&sig_handler;


# spawn the aaa service in the background
my $aaa = Proc::Background->new("./oscars_aaa.sh");

# wait a bit
sleep(5);

# spawn the core service in the background
my $core = Proc::Background->new("./oscars_core.sh");

while (1) {
# do nothing and only wait for a signal
}
exit(0);

# handle signals by killing the processes and exiting
sub sig_handler {
    $core->die;
    sleep(5);
    $aaa->die;
    exit 0;
}
