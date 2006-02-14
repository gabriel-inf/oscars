#!/usr/bin/perl -w

# adapt.pl: Script called by all CGI forms.  OSCARS::WBUI::SOAPAdapter
#           (called by OSCARS::WBUI::Runner) does all the work.
# Last modified:  January 10, 2006
# David Robertson (dwrobertson@lbl.gov)

use strict;
use OSCARS::WBUI::Runner;

OSCARS::WBUI::Runner::run();

######
1;
