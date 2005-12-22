###############################################################################
package OSCARS::AAAS::Logger; 

# Outputs logging information to HTML formatted file.  There are cases where
# the file name won't be ready until results are returned (for example, file
# names depending on a reservation tag).  Output is buffered until the
# instance is closed.
#
# Last modified:  December 15, 2005
# David Robertson (dwrobertson@lbl.gov)

use Data::Dumper;
use Error qw(:try);

use strict;


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
  
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my( $self ) = @_;

    $self->{output_written} = 0;
    $self->{recurrent_methods} = {
        'find_pending_reservations' => 1,
        'find_expired_reservations' => 1,
    };
} #____________________________________________________________________________


###############################################################################
#
sub start_log {
    my( $self, $user_dn ) = @_;

    $self->{user_dn} = $user_dn;
    @{$self->{buf_strings}} = ( );
} #____________________________________________________________________________


###############################################################################
#
sub write_log {
    my( $self, $buf ) = @_;

    push(@{$self->{buf_strings}}, "$buf<br/>");
    $self->{output_written} = 1;
} #____________________________________________________________________________


###############################################################################
#
sub end_log {
    my( $self, $results ) = @_;

    if ((!$self->{output_written}) && 
        ($self->{recurrent_methods}->{$self->{method}})) {
        return;
    }
    my( $sec, $min, $hour, $monthday, $month, $year, $weekday, $yearday,
        $isdaylight ) = localtime();
    $year += 1900;
    $month += 1;
    if ($month < 10) { $month = '0' . $month; }
    if ($monthday < 10) { $monthday = '0' . $monthday; }
    $self->{user_dn} =~ s/@/./;
    # TODO:  non-portable
    my $fname = "$self->{dir}/$self->{user_dn}";
    if (!(-d $fname)) {
        mkdir($fname, 0755) || die "Cannot mkdir: $fname";
    }
    my $ctr = 1;
    $fname = "$self->{dir}/$self->{user_dn}/$self->{method}.$year.$month.$monthday.$ctr.html";
    while (-e $fname) {
        $ctr += 1;
        $fname = "$self->{dir}/$self->{user_dn}/$self->{method}.$year.$month.$monthday.$ctr.html";
    }
    open (LOGFILE, ">$fname") ||
            die "Can't open log file $fname.\n";
    print LOGFILE "<html><head><title>$self->{method}</title></head><body>\n";
    print LOGFILE "<h2>$self->{method}</h2>\n";
    print LOGFILE "<h3>Output</h3>\n";
    print LOGFILE join("\n", @{$self->{buf_strings}});
    if ($results) {
        print LOGFILE "\n<h3>Results</h3>\n";
        if (ref($results) ne "ARRAY") {
            print LOGFILE to_string($results);
        }
        else {
            for my $row (@$results) {
                print LOGFILE to_string($row);
            }
        }
        print LOGFILE "\n";
    }
    print LOGFILE "</body></html>\n";
    close(LOGFILE);
} #____________________________________________________________________________


##############################################################################
#
sub to_string {
    my( $results ) = @_;

    my( $key, $value );

    my $msg = '';
    foreach $key(sort keys %{$results} ) {
        if (($key ne 'status_msg') &&
            defined($results->{$key})) {
            $value = $results->{$key};
            if ($value) {
                if (ref($value) eq 'ARRAY') {
                    $msg .= "$key -> ";
                    for my $row (@$value) {
                        $msg .= "$row ";
                    }
                    $msg .= "\n";
                }
                else { $msg .= "$key -> $value<br/>\n"; }
            }
            else {
                $msg .= "$key -> <br/>\n";
            }
        }
    }
    return $msg;
} #___________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
