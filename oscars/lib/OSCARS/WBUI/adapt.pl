#!/usr/bin/perl -w

# adapt.pl: Script called by all CGI forms.  Client::SOAPAdapter
#           (called by Client::Runner) does all the work.
# Last modified:  November 15, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;
use Client::Runner;

Client::Runner::run();

######
1;
