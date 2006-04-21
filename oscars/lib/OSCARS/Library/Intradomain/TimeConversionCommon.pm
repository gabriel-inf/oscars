#==============================================================================
package OSCARS::Intradomain::TimeConversionCommon;

=head1 NAME

OSCARS::Intradomain::TimeConversionCommon - Common functionality for timezone 
conversions.

=head1 SYNOPSIS

  use OSCARS::Intradomain::TimeConversionCommon;

=head1 DESCRIPTION

Common functionality for timezone conversions.  Times are stored in the 
database in UTC, and methods in this class convert to/from that time zone to 
the client's time zone.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

April 18, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________


###############################################################################
#
sub convertTimes {
    my( $self, $resv ) = @_;
 
    # convert to time zone reservation was created in
    my $statement = "SELECT CONVERT_TZ(?, '+00:00', ?) AS newtime";
    my $row = $self->{db}->getRow( $statement, $resv->{startTime},
                             $resv->{origTimeZone} );
    $resv->{startTime} = $row->{newtime};
    $row = $self->{db}->getRow( $statement, $resv->{endTime},
                             $resv->{origTimeZone} );
    $resv->{endTime} = $row->{newtime};
    $row = $self->{db}->getRow( $statement, $resv->{createdTime},
                             $resv->{origTimeZone} );
    $resv->{createdTime} = $row->{newtime};
} #____________________________________________________________________________


###############################################################################
#
sub convertLspTimes {
    my( $self, $resv ) = @_;

    my $statement = "SELECT now() AS nowtime";
    my $row = $self->{db}->getRow( $statement );
    my $nowtime = $row->{nowtime};

    $statement = "SELECT CONVERT_TZ(?, '+00:00', ?) AS nowtime";
    $row = $self->{db}->getRow( $statement, $nowtime,
                                        $resv->{origTimeZone});
    $resv->{lspConfigTime} = $row->{nowtime};
    $self->convertTimes($resv);
} #____________________________________________________________________________


###############################################################################
# setupTimes:  
#
sub setupTimes {
    my( $self, $startTime, $endTime ) = @_;

    # Expects strings in second since epoch; converts to date in UTC time
    my $statement = 'SELECT from_unixtime(?) AS startTime';
    my $row = $self->{db}->getRow( $statement, $startTime);
    $startTime = $row->{startTime};

    my $statement = 'SELECT from_unixtime(?) AS endTime';
    my $row = $self->{db}->getRow( $statement, $endTime);
    $endTime = $row->{endTime};

    $statement = 'SELECT now() AS createdTime';
    $row = $self->{db}->getRow( $statement );
    my $currentTime = $row->{createdTime};
    return( $startTime, $endTime, $currentTime );
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
