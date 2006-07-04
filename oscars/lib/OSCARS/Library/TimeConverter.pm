#==============================================================================
package OSCARS::Library::TimeConverter;

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
