#==============================================================================
package OSCARS::Logger; 

=head1 NAME

OSCARS::Logger - Logger for OSCARS.

=head1 SYNOPSIS

  use OSCARS::Logger;

=head1 DESCRIPTION

This package contains methods for writing log messages to a file
(currently there is a log file for each SOAP method access).

Log files are in HTML format.  There are cases where
the file name won't be ready until results are returned (for example, file
names depending on a reservation tag), so output is buffered until the
instance is closed.

=head1 AUTHOR

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

January 9, 2006

=cut

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

    @{$self->{buf_strings}} = ( );
} #____________________________________________________________________________


###############################################################################
#
sub add_string {
    my( $self, $buf ) = @_;

    push(@{$self->{buf_strings}}, "$buf<br/>");
} #____________________________________________________________________________


##############################################################################
#
sub add_hash {
    my( $self, $hashref ) = @_;

    my( $key, $value );

    my $buf = '';
    foreach $key(sort keys %{$hashref} ) {
        if (($key ne 'status_msg') &&
            defined($hashref->{$key})) {
            $value = $hashref->{$key};
            if ($value) {
                if (ref($value) eq 'ARRAY') {
                    $buf .= "$key -> ";
                    for my $row (@$value) {
                        $buf .= "$row ";
                    }
                    $buf .= "\n";
                }
                else { $buf .= "$key -> $value<br/>\n"; }
            }
            else {
                $buf .= "$key -> <br/>\n";
            }
        }
    }
    push(@{$self->{buf_strings}}, "$buf<br/>");
} #___________________________________________________________________________



###############################################################################
#
sub write_file {
    my( $self, $user, $id, $exception_flag ) = @_;

    if ( !$self->{dir} ) {
        $self->{dir} = '/home/oscars/logs';
    }
    my( $sec, $min, $hour, $monthday, $month, $year, $weekday, $yearday,
        $isdaylight ) = localtime();
    $year += 1900;
    $month += 1;
    if ($month < 10) { $month = '0' . $month; }
    if ($monthday < 10) { $monthday = '0' . $monthday; }
    $user =~ s/@/./;
    # TODO:  non-portable
    my $fname = "$self->{dir}/$user";
    if (!(-d $fname)) {
        mkdir($fname, 0755) || die "Cannot mkdir: $fname";
    }
    my $ctr = 1;
    if (!$exception_flag) {
        $fname = "$self->{dir}/$user/$id.$year.$month.$monthday.$ctr.html";
    }
    else {
        $fname = "$self->{dir}/$user/EX.$id.$year.$month.$monthday.$ctr.html";
    }
    while (-e $fname) {
        $ctr += 1;
        $fname = "$self->{dir}/$user/$id.$year.$month.$monthday.$ctr.html";
    }
    open (LOGFILE, ">$fname") ||
            die "Can't open log file $fname.\n";
    print LOGFILE "<html><head><title>$id</title></head><body>\n";
    print LOGFILE "<h2>$id</h2>\n";
    print LOGFILE "<h2>Time:  $hour:$min:$sec</h2>\n";
    print LOGFILE "<h3>Output</h3>\n";
    print LOGFILE join("\n", @{$self->{buf_strings}});
    print LOGFILE "</body></html>\n";
    close(LOGFILE);
    # clear string buffers
    @{$self->{buf_strings}} = ( );
} #____________________________________________________________________________


######
1;
