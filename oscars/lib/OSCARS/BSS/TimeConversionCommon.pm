###############################################################################
package OSCARS::BSS::TimeConversionCommon;

=head1 NAME

OSCARS::BSS::TimeConversionCommon - Common functionality for timezone 
conversions.

=head1 SYNOPSIS

  use OSCARS::BSS::TimeConversionCommon;

=head1 DESCRIPTION

Common functionality for timezone conversions.  Times are stored in the 
database in UTC, and methods in this class convert to/from that time zone to 
the client's time zone.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

December 21, 2005

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

use OSCARS::User;

sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    return( $self );
} #____________________________________________________________________________ 

###############################################################################
#
sub convert_times {
    my( $self, $resv ) = @_;
 
    # convert to time zone reservation was created in
    my $statement = "SELECT CONVERT_TZ(?, '+00:00', ?) AS newtime";
    my $row = $self->{user}->get_row( $statement, $resv->{reservation_start_time},
                             $resv->{reservation_time_zone} );
    $resv->{reservation_start_time} = $row->{newtime};
    $row = $self->{user}->get_row( $statement, $resv->{reservation_end_time},
                             $resv->{reservation_time_zone} );
    $resv->{reservation_end_time} = $row->{newtime};
    $row = $self->{user}->get_row( $statement, $resv->{reservation_created_time},
                             $resv->{reservation_time_zone} );
    $resv->{reservation_created_time} = $row->{newtime};
} #____________________________________________________________________________


###############################################################################
#
sub convert_lsp_times {
    my( $self, $resv ) = @_;

    my $statement = "SELECT now() AS nowtime";
    my $row = $self->{user}->get_row( $statement );
    my $nowtime = $row->{nowtime};

    $statement = "SELECT CONVERT_TZ(?, '+00:00', ?) AS nowtime";
    $row = $self->{user}->get_row( $statement, $nowtime,
                                        $resv->{reservation_time_zone});
    $resv->{lsp_config_time} = $row->{nowtime};
    $self->convert_times($resv);
} #____________________________________________________________________________


###############################################################################
# setup_times:  
#
sub setup_times {
    my( $self, $infinite_time) = @_;

    my( $duration_seconds );

    my $infinite_time = $self->get_infinite_time();
    # Expects strings in second since epoch; converts to date in UTC time
    my $statement = 'SELECT from_unixtime(?) AS start_time';
    my $row = $self->{user}->get_row( $statement, $self->{params}->{reservation_start_time});
    $self->{params}->{reservation_start_time} = $row->{start_time};
    if ($self->{params}->{duration_hour} < (2**31 - 1)) {
        $duration_seconds = $self->{params}->{duration_hour} * 3600;
        $statement = 'SELECT DATE_ADD(?, INTERVAL ? SECOND) AS end_time';
        $row = $self->{user}->get_row( $statement, $self->{params}->{reservation_start_time},
                                $duration_seconds );
        $self->{params}->{reservation_end_time} = $row->{end_time};
    }
    else {
        $self->{params}->{reservation_end_time} = $infinite_time;
    }
    $statement = 'SELECT now() AS created_time';
    $row = $self->{user}->get_row( $statement );
    $self->{params}->{reservation_created_time} = $row->{created_time};
} #____________________________________________________________________________


###############################################################################
# get_time_str:  print formatted time string
#
sub get_time_str {
    my( $self, $dtime ) = @_;

    my @ymd = split(' ', $dtime);
    return $ymd[0];
} #____________________________________________________________________________ 


###############################################################################
# get_infinite_time:  returns "infinite" time
#
sub get_infinite_time {
    my( $self ) = @_;

    return '2039-01-01 00:00:00';
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
