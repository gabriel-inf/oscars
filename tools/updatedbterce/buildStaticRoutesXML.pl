#!/usr/bin/perl -W
#
# buildStaticRoutesXML.pl
#
# (C) 2008 by Mid-Atlantic Crossroads (MAX).  All rights reserved.
#
# Time-stamp: <2008-05-29 15:00:07 EDT>
# $Id$
#
# Authors: 
#
# Chris Tracy <chris@maxgigapop.net>
# Andrew Lake <alake@internet2.edu>
#
# Description:
#
# This script is intended to be used to create the static-routes.xml file
# used by the TERCE Web Service package.  The XML topology file is used as
# input, as well as path computation results from the DRAGON NARB running
# in the local domain.
#
# Some assumptions are made about the format/interpretation of the Link IDs
# specified in the XML topology file.
#
# Please let us know if it does not work in your environment.
# 
#
use strict;
use vars qw($ORIGINAL_SCRIPT $P $VERSION $VERBOSE $OPTS $USAGE $DESCR $AUTHOR
            $COPYRIGHT $ARGS_DESC $LOG_STDERR $LOG_FILE $LOG_FP $LOG_TSTAMP_FMT
            $DEFAULTS);
BEGIN {
    $ORIGINAL_SCRIPT = $0;
    my(@P) = split("/", $0);
    $P = pop(@P);
    my $dir = join('/', @P);
    unshift(@INC, $dir);
    ## If we're being run out of a bin/ directory and there is ../lib, then
    ## add it to @INC
    if ($P[$#P] eq 'bin') {
      my @tmp = @P;
      pop(@tmp);
      my $tmp = join("/",@tmp)."/lib";
      unshift(@INC, $tmp) if (-d $tmp);
      $tmp .= "/perl";
      unshift(@INC, $tmp) if (-d $tmp);
    }
    my $ndir = "$dir/../lib/perl5/site_perl";
    unshift(@INC, $ndir) if (-d $ndir);
}
##
use POSIX;
use Getopt::Long;
use IO::File;
##
$DEFAULTS =
  {
    XMLTOPO    => 'tedb-intra.xml',
    OUTFILE    => undef,
    OVERWRITE  => 0,
    NARB       => '127.0.0.1',
    LOG        => undef,
    USELOCALID => 0,
    USESUBNET  => 0,
    NARB_TEST  => '/usr/local/dragon/sbin/narb_test',
    BANDWIDTH  => 100,
  };
$VERSION = '0.2';
$DESCR = 'builds static EROs XML file from XML topo file, for use with IDC';
$AUTHOR = 'Chris Tracy <chris@maxgigapop.net>';
$VERBOSE = 0;
$COPYRIGHT = '(C) 2008 by Mid-Atlantic Crossroads (MAX).  All rights reserved.';
$LOG_STDERR = 1;
$LOG_FILE = undef;
$LOG_FP = undef;
$LOG_TSTAMP_FMT = '%Y-%m-%d %H:%M:%S';
$USAGE = <<__UsAGe__;
 options:
           --xmltopo=[filename]   filename for XML topology (default=$DEFAULTS->{XMLTOPO})
           --outfile=[filename]   output filename for static EROs (no default)
           --overwrite            overwrite outfile if exists (default=no overwrite)
           --narb=[IP]            IP address of NARB (default=$DEFAULTS->{NARB})
           --use-localid          pass local-ids to narb_test
           --use-subnet           use subnet ERO as path in XML file
           --bandwidth=[x]        bandwidth (Mbps) passed to narb_test (default=$DEFAULTS->{BANDWIDTH})
           --narb_test=[filename] location of narb_test binary
                                   (default=$DEFAULTS->{NARB_TEST})
           --log=[filename]       log output to filename (default=only log to STDERR)
           -v                     be verbose
           -v=[x]                 set verbosity to x (-v is the same as -v 1)
           -h                     print this message and exit
__UsAGe__
##

my %opts = (
            'xmltopo'     => $DEFAULTS->{XMLTOPO},
            'outfile'     => $DEFAULTS->{OUTFILE},
            'overwrite'   => $DEFAULTS->{OVERWRITE},
            'narb'        => $DEFAULTS->{NARB},
            'log'         => $DEFAULTS->{LOG},
            'use-localid' => $DEFAULTS->{USELOCALID},
            'use-subnet'  => $DEFAULTS->{USESUBNET},
            'narb_test'   => $DEFAULTS->{NARB_TEST},
            'bandwidth'   => $DEFAULTS->{BANDWIDTH},
            );

usage() unless scalar(@ARGV);
usage() unless 
    GetOptions (\%opts, 
                "h",         # help
                "v:i",       # verbosity level
                "xmltopo=s",
                "outfile=s",
                "overwrite",
                "narb=s",
                "log=s",
                "use-localid",
                "use-subnet",
                "narb_test=s",
                "bandwidth=i",
                );
usage(undef, 1) if $opts{h};
if (defined($opts{v})) {
  if ($opts{v} == 0) {
    $VERBOSE = 1;
  } else  {
    $VERBOSE = $opts{v};
  }
}
$LOG_FILE = $opts{log};
# log_msg(1,qq{our args: }.join(' // ',@ARGV));

# edge ports array - list of fully-qualified link IDs that look like edge ports
# key = link ID, value = 1
my %edge_ports;

# local id array - list of local ids on each VLSR
# key1 = VLSR node address, value = local-id array
my %localids;

# key = TE address (e.g. 140.173.97.97)
# value = fully-qualified link ID
my %TE_mapping;

# key = node name (e.g. MAX), value = loopback address (e.g. 140.173.2.232)
my %VLSRs;

# $EROs{srcVLSR}{dstVLSR} = @array of hops
my %EROs;

if (!defined($opts{outfile})) {
  log_msg(0,"Output file was not specified, please provide parameter to --outfile");
  exit(255); 
}
if (-e $opts{outfile}) {
  if ($opts{overwrite}) {
    log_msg(1,"Output file '$opts{outfile}' already exists, will overwrite since --overwrite was used");
  } else {
    log_msg(0,"Output file '$opts{outfile}' already exists, will not overwrite");
    log_msg(0,"Use --overwrite option if you would like the existing file to be overwritten");
    exit(255);
  }
}

# this is where everything really starts to happen:
&parse_xmltopo();
&compute_all_EROs();
&create_IDC_EROs();

sub parse_xmltopo {
  if (!-e $opts{xmltopo}) {
    log_msg(0,"Input file '$opts{xmltopo}' does not exist");
    exit(255); 
  }
  open I, $opts{xmltopo};
  my $domainid;
  my $nodename;
  my $linkid;
  my $teaddr;
  
  while (<I>) {
    if (/<link id="(\S+)">/) {
      $linkid = $1;
      $teaddr = $1 if(/:link=(\d+\.\d+\.\d+\.\d+)/);
    } elsif (/<\/link>/) {
      $linkid = "";
    } elsif (/<remoteLinkId>(.*)<\/remoteLinkId>/){
      my $remotelinkid = $1;
      if ($remotelinkid =~ /:domain=$domainid:node=[^\*]+:port=[^\*]+/) {
        log_msg(2,"TE link -> $linkid");
        $TE_mapping{$teaddr} = $linkid;
      }else {
        log_msg(2,"edge port -> $linkid");
        $edge_ports{$linkid} = 1;
        my $port_id = $1 if($linkid =~ /:port=S?(.+):link/);
        push(@{$localids{$nodename}}, $port_id);
      }
    } elsif (/<node id="(\S+)">/) {
      my $nodeid = $1;
      if ($nodeid =~ /:node=(\S+)/) {
        $nodename = $1;
      }
    } elsif (/<address>(\d+\.\d+\.\d+\.\d+)<\/address>/) {
      my $address = $1;
      log_msg(1,"address for node $nodename is $address");
      $VLSRs{$nodename} = $address;
    } elsif (/<domain id="urn:ogf:network:domain=(\S+)">/) {
       $domainid = $1;
       log_msg(1,"domain is $domainid");
    }
  }
  close I;
}

sub compute_all_EROs {
  foreach my $a (keys %VLSRs) {
    foreach my $z (keys %VLSRs) {
      #skip id same node name. 
      #NOTE: skipping based on address does not work in subnet case
      next if $a eq $z;
      
      if($opts{'use-localid'}){
        foreach my $local_id_a (@{$localids{$a}}) {
          foreach my $local_id_z (@{$localids{$z}}) {
            &get_ERO_from_narb($VLSRs{$a}, $VLSRs{$z}, $local_id_a, $local_id_z);
          }
        }
        next;
      }
      
      &get_ERO_from_narb($VLSRs{$a}, $VLSRs{$z});
    }
  }
}

sub get_ERO_from_narb {
  my $a = shift;
  my $z = shift;
  my $local_id_a = shift;
  my $local_id_z = shift;
  my $local_id_opts = "";
  my $subnet_found = 0;
  my $aKey = $a;
  my $zKey = $z;
  
  if($local_id_a && $local_id_z){
    $aKey .= ':' . $local_id_a;
    $zKey .= ':' . $local_id_z;
    $local_id_opts = "-l 5/$local_id_a:5/$local_id_z -s";
  }
  
  open I, "$opts{narb_test} -H $opts{narb} -S $a -D $z -b$opts{bandwidth} $local_id_opts 2>&1 |";
  my @lines = <I>;
  close I;
  foreach (@lines) {
    chomp;
    
    if(/Subnet ERO hops/){
      $subnet_found = 1;
    }
    
    if($opts{'use-subnet'} && (!$subnet_found)){
      next;
    }
    
    if (/HOP\-TYPE\s+\[\w+\]\:\s+(\d+\.\d+\.\d+\.\d+)/i) {
      my $hop = $1;
      push @{ $EROs{$aKey}{$zKey} }, $hop;
    }
  }
  
  if (grep(/request\s+successful/i, @lines)) {
    log_msg(1,"Successfully queried NARB for src=$a, dst=$z");
  } else {
    log_msg(0,"Query NARB for src=$a, dst=$z failed!");
    log_msg(0,"Could not get a path via the NARB, there may be a problem with the underlying network!");
    log_msg(0,"Please verify that the NARB is reachable at IP address $opts{narb}");
    exit(255);
  }
}

sub create_IDC_EROs {
  open O, "> $opts{outfile}" or die "problem opening output file: $!";

  print O <<END;
<?xml version="1.0" encoding="UTF-8"?>
<staticPathDatabase xmlns="http://dragon.maxgigapop.net/DRAGON/TERCE/RCE/STATICPATHS" id="dragon-paths">

END

  foreach my $a (keys %edge_ports) {
    foreach my $z (keys %edge_ports) {
      next if $a eq $z;

      # figure out ingress VLSR loopback address
      $a =~ /node=([^:]+):/;
      my $ingress_vlsr = $VLSRs{$1};
      my $ingress_key = $ingress_vlsr;
      
      # figure out egress VLSR loopback address
      $z =~ /node=([^:]+):/;
      my $egress_vlsr = $VLSRs{$1};
      my $egress_key = $egress_vlsr;
      
      if($opts{'use-localid'}){
        $ingress_key .= ':' . $1 if($a =~ /port=S?([^:]+):/);
        $egress_key .= ':' . $1 if($z =~ /port=S?([^:]+):/);
      }
  
      if (!defined($ingress_vlsr)) {
        log_msg(0,"Could not determine ingress VLSR for $a");
        exit(255);
      }
      if (!defined($egress_vlsr)) {
        log_msg(0,"Could not determine egress VLSR for $z");
        exit(255);
      }

      my $name;
      $a =~ /node=([^:]+):port=([^:]+)/;
      $name .= "$1:$2-";
      $z =~ /node=([^:]+):port=([^:]+)/;
      $name .= "-$1:$2";

      log_msg(1,"Writing out ERO for path '$name'");
      log_msg(2," - A: $a");
      log_msg(2," - Z: $z");
      
      print O <<END;
    <staticPathEntry id="$name">
        <srcEndpoint>$a</srcEndpoint>
        <destEndpoint>$z</destEndpoint>
        <path id="$name">
END
 
      my $hop = 1;

      # first hop is the edge port itself
      log_msg(3,"   - hop $hop: $a");
      print O <<END;
            <hop id="$hop">
                <linkIdRef>$a</linkIdRef>
            </hop>
END

      $hop++;
      foreach my $i ( 0 .. $#{ $EROs{$ingress_key}{$egress_key} }) {
        log_msg(3,"   - hop $hop: $TE_mapping{ $EROs{$ingress_key}{$egress_key}[$i] }");
        print O <<END;
            <hop id="$hop">
                <linkIdRef>$TE_mapping{ $EROs{$ingress_key}{$egress_key}[$i] }</linkIdRef>
            </hop>
END
        $hop++;
      }

      # last hop is the edge port itself
      log_msg(3,"   - hop $hop: $z");
      print O <<END;
            <hop id="$hop">
                <linkIdRef>$z</linkIdRef>
            </hop>
END

      print O <<END;
        </path>
        <availableVtags></availableVtags>  <!-- deprecated: leave blank -->
    </staticPathEntry>

END
    }
  }

  print O <<END;
</staticPathDatabase>
END
  close O;
}

exit(0);

##
sub usage {
  my $msg = shift(@_);
  print STDERR sprintf("%9s: %s\n", "ERROR", $msg) if $msg;
  print STDERR sprintf("%9s: %s\n", $P, $DESCR);
  print STDERR sprintf("%9s: %s\n", "Version", $VERSION);
  print STDERR sprintf("%9s: %s\n", "Copyright", $COPYRIGHT);
  print STDERR sprintf("%9s: %s\n", "Author", $AUTHOR);
  print $USAGE;
  if (scalar(@_)) {
    my $nope = 0;
    open(ME, "<$0") || ($nope=1);
    unless ($nope) {
      my $in_history = 0;
      while (<ME>) {
        next unless ($in_history || /^=head1\s+VERSION/);
        if (/^=head1\s+VERSION/) {
          $in_history = 1;
          print STDERR "\n  ","-" x 20, "[ VERSION HISTORY ]", "-" x 20,"\n\n";
          print STDERR sprintf("  %-7s   %-9s   %-7s %s\n",
                               "VERS","WHEN","WHO","WHAT");
          next;
        } elsif ($in_history && /^=cut/) {
          last;
        } elsif ($in_history && ($_ !~ /^\s*$/)) {
          print STDERR $_;
        }
      }
      close(ME);
    }
  }
  exit(defined($msg));
}
##
sub ts {
  my $fmt = $LOG_TSTAMP_FMT || "%Y-%m-%d %H:%M:%S";
  return POSIX::strftime($fmt, localtime(time));
}
##
sub log_msg {
  my $lvl = shift(@_);
  return unless $VERBOSE >= $lvl;
  my $logmsg = "$P: " . ts() . " [$lvl] @_\n";
  print STDERR $logmsg if $LOG_STDERR;
  if ($LOG_FILE && !$LOG_FP) {
    $LOG_FP = new IO::File(">> $LOG_FILE")
        or die "$P: could not create log file $LOG_FILE: $!\n";
  }
  print $LOG_FP $logmsg if $LOG_FP;
}
__END__

=head1 VERSION HISTORY
  
  0.1.0   29 May 08     ctracy     started
  0.2.0   12 Jun 08     ctracy     Andrew Lake <alake@internet2.edu> added support
                                     for DRAGON configurations that utilize the
                                     subnet local-id options
                                   Other minor improvements incorporated:
                                     removed hardcoded path to narb_test
                                     allow user to specify bandwidth value passed to narb_test
                                     logging/verbosity changes
                                     etc..

=cut

# Local variables:
# tab-width: 2
# perl-indent-level: 2
# indent-tabs-mode: nil
# comment-column: 40
# End:
