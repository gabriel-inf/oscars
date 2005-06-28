#!/usr/bin/perl

use Common::Mail

$mailer = Common::Mail->new();
$subject = 'test';
$sender = $mailer->get_webmaster();
$msg = "This is a test.\n";

$err_msg = $mailer->send_mail($sender, $mailer->get_admins(), $subject, $msg); 
if ($err_msg) {
    print STDERR "ERROR: $err_msg\n";
}
