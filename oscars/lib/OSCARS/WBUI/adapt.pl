#!/usr/bin/perl -w

# adapt.pl: Script called by all CGI forms.  SOAPAdapter
#           (called by Runner) does all the work.
# Last modified:  December 7, 2005
# David Robertson (dwrobertson@lbl.gov)

use strict;
use OSCARS::WBUI::Runner;

OSCARS::WBUI::Runner::run();

######
1;
