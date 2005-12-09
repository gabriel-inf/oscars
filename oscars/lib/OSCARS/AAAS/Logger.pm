###############################################################################
package OSCARS::AAAS::Logger; 

# Outputs logging information to HTML formatted file.  There are cases where
# the file name won't be ready until results are returned (for example, file
# names depending on a reservation tag).  Output is buffered until the
# instance is closed.
#
# Last modified:  December 7, 2005
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
    my( $self ) = @_;

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
    my( $self ) = @_;

    if (!$self->{output_written} && 
        ($self->{recurrent_methods}->{$self->{params}->{method}})) {
        return;
    }
    my $fname = "$self->{dir}/$self->{params}->{method}.html";
    open (LOGFILE, ">$fname") ||
            die "Can't open log file $fname.\n";
    print LOGFILE "<html><body>\n";
    print LOGFILE join('\n', @{$self->{buf_strings}});
    print LOGFILE "</body></html>\n";
    close(LOGFILE);
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
