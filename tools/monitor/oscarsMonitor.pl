#!/usr/bin/perl

# OSCARS monitoring perl script
# Contact: Evangelos Chaniotakis, haniotak@es.net
#
# - What it does:
# It checks a few things: 
# 1. Are OSCARS-related processes running locally? PathScheduler and mysql 
# for now, tomcat is only visible as [java] in the process table, which is
# not very clear.
# 2. Can we hit the web server over HTTP? We look for both the Axis service
# and the webapp.
# 3. Have certain log files been touched lately?
#
# - How do I configure it for my server?
# See the Settings section below.
#
# - How should I run it?
# This is meant to run as a cron job. No output except through email.
#
# Please provide feedback!


# Constants
my $curl_command = '/usr/local/bin/curl -k -s -o /dev/null -w "%{http_code}" ';
my $host = `/bin/hostname`;
my $dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,$ctime,$blksize,$blocks;
my $time = time();
my $alert_message = "";
chomp $host;


# Settings

# Email-related
my $alert_email = 'haniotak\@es.net';
my $alert_subject = "OSCARS ALERT : $host";
my $email_command = "/usr/bin/mailx -s '$alert_subject' $alert_email > /dev/null";

# A list of services to look for in the process table with pgrep
my @services = (  'mysqld_safe',  'PathScheduler');

# Curl will try and hit both of these URLs
my $war_url = 'https://oscars.es.net/OSCARS/';
my $aar_url = 'https://oscars.es.net/axis2/services/OSCARS/';

# We'll look for all these files and let you know if they get too stale
# the staleness is in seconds.
my $tomcat_log = '/usr/local/tomcat/logs/catalina.out';
my $tomcat_stale = 24*3600;
my $scheduler_log = '/usr/local/tomcat/logs/scheduler.log';
my $scheduler_stale = 24*3600;
my $war_log = '/usr/local/tomcat/logs/oscars.log';
my $war_stale = 24*3600;
my $aar_log = '/usr/local/tomcat/logs/oscars-aar.log';
my $aar_stale = 24*3600;
my $heartbeat_log = '/tmp/oscars.heartbeat';
my $heartbeat_stale = 3600;


# Settings end here

# 1. Check if service processes are running
foreach my $service (@services) {
  my $status = `/bin/pgrep -fl $service`;
   if (!$status) {
     $alert_message .= "Service $service stopped\n";
  }
}

# 2. Check if we can hit server over the web
my $aar = `$curl_command $aar_url`;
if ($aar == "500") {
  # we expect a 500 because we are not signing the message
} elsif ($aar == "000") {
  $alert_message .= "AAR: Connection error, tomcat not running?\n";
} elsif ($aar == "404") {
  $alert_message .= "AAR: File not found error, AAR undeployed?\n";
} else {
  $alert_message .= "AAR: Connection error, error code: [$aar] \n";
}

my $war = `$curl_command $war_url`;
if ($war == "200") {
} elsif ($war == "000") {
  $alert_message .= "WAR: Connection error, tomcat not running?\n";
} elsif ($war == "404") {
  $alert_message .= "WAR: File not found error, WAR undeployed?\n";
} else {
  $alert_message .= "WAR: Connection error, error code: [$war] \n";
}


# 3. Check if files are stale
($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,$ctime,$blksize, $blocks) = stat($tomcat_log);
if ($mtime < $time - $tomcat_stale) {
  my $delta = $time - $mtime;
  $alert_message .= "No updates at file $tomcat_log for $delta seconds!\n";
}
($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,$ctime,$blksize, $blocks) = stat($scheduler_log);
if ($mtime < $time - $scheduler_stale) {
  my $delta = $time - $mtime;
  $alert_message .= "No updates at file $scheduler_log for $delta seconds!\n";
}
($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,$ctime,$blksize, $blocks) = stat($war_log);
if ($mtime < $time - $war_stale) {
  my $delta = $time - $mtime;
  $alert_message .= "No updates at file $war_log for $delta seconds!\n";
}
($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,$ctime,$blksize, $blocks) = stat($aar_log);
if ($mtime < $time - $aar_stale) {
  my $delta = $time - $mtime;
  $alert_message .= "No updates at file $aar_log for $delta seconds!\n";
}
($dev,$ino,$mode,$nlink,$uid,$gid,$rdev,$size,$atime,$mtime,$ctime,$blksize, $blocks) = stat($heartbeat_log);
if ($mtime < $time - $heartbeat_stale) {
  my $delta = $time - $mtime;
  $alert_message .= "No updates at file $heartbeat_log for $delta seconds!\n";
}


# 4. Send message out if something went wrong
if ($alert_message) {
    open MAIL, "|$email_command";
    print MAIL $alert_message;
    close MAIL;
}
