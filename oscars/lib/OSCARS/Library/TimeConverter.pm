#==============================================================================
package OSCARS::Library::TimeConverter;

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

=head1 NAME

OSCARS::Library::TimeConverter- Functionality for timezone conversions.

=head1 SYNOPSIS

  use OSCARS::Library::TimeConverter;

=head1 DESCRIPTION

Common functionality for timezone conversions between xsd:datetime in the client's current
time zone and epoch seconds (int).  SOAP time parameters and results are in xsd:datetime format. 
Database time fields are in int format.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

July 3, 2006

=cut


use strict;

use DateTime;
use DateTime::TimeZone;
use DateTime::Format::W3CDTF;

use Data::Dumper;
use Error qw(:try);

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
# datetimeToSeconds:  Convert from incoming times in xsd:datetime format to
#     seconds since epoch.
#
sub datetimeToSeconds {
    my( $self, $datetime ) = @_;

    # DateTime::Format::W3CDTF is deprecated; supposedly
    # DateTime::Format::Builder supposedly handles fractional seconds correctly,
    # but documentation on how to use W3CDTF with it is non-existent.  The
    # example in its distribution is, shall we say, not clear.

    # strip the decimal fraction digits (from perl.datetime (6248) )
    $datetime =~ s/([\d-T:]+)(\.[0-9]+)([-\d:Z]+)/$1$3/;
    my $f = DateTime::Format::W3CDTF->new();
    my $dt = $f->parse_datetime( $datetime );
    my $epochSeconds = $dt->epoch();
    return $epochSeconds;
} #____________________________________________________________________________


###############################################################################
# secondsToDatetime:  Convert from epoch seconds to xsd:datetime format
#
sub secondsToDatetime {
    my( $self, $epochSeconds, $origTimeZone ) = @_;

    my $f = DateTime::Format::W3CDTF->new();
    my $dt = DateTime->from_epoch( epoch => $epochSeconds );
    my $offsetStr = $origTimeZone;
    # strip out semicolon
    $offsetStr =~ s/://;
    my $timezone = DateTime::TimeZone->new( name => $offsetStr );
    $dt->set_time_zone($timezone);
    my $datetime = $f->format_datetime($dt);
    return $datetime;
} #____________________________________________________________________________


###############################################################################
# getYMD:  Convert from epoch seconds to YYYY-MM-DD string format
#
sub getYMD {
    my( $self, $epochSeconds ) = @_;

    my( $month, $day );

    my $dt = DateTime->from_epoch( epoch => $epochSeconds );
    if ( $dt->month < 9 ) { $month = '0' . $dt->month; }
    else { $month = $dt->month; }
    if ( $dt->day < 9 ) { $day = '0' . $dt->day; }
    else { $day = $dt->day; }
    my $ymd = $dt->year . '-' . $month . '-' . $day;
    return $ymd;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
