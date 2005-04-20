package SOAP::Transport::HTTP::Daemon::ReapForkOnAccept;

use strict;
use vars qw(@ISA);
use SOAP::Transport::HTTP;
use POSIX ":sys_wait_h";

# Idea and implementation of Michael Douglass
# Addition of reaper (waitpid) and YIKES failure message recommended 
# for perl 5.6 and up in Win32 environments by Sydd Souza

@ISA = qw(SOAP::Transport::HTTP::Daemon);

sub handle {
  my $self = shift->new;

  CLIENT:
  while (my $c = $self->accept) {

    while (waitpid(-1, &WNOHANG) != -1) {} # reap any unreaped forks
    
    my $pid = fork();

    # We are going to close the new connection on one of two conditions
    #  1. The fork failed ($pid is undefined)
    #  2. We are the parent ($pid != 0)
    unless( defined $pid && $pid == 0 ) {
      if (!defined $pid) {
        print STDERR <<YIKES;
SOAP::Transport::HTTP::Daemon::ReapForkOnAccept Failing to fork handlers.
Requests are quietly going unanswered (they are likely getting code 500).
YIKES
      }
      $c->close;
      next;
    }
    # From this point on, we are the child.

    $self->close;  # Close the listening socket (always done in children)

    # Handle requests as they come in
    while (my $r = $c->get_request) {
      $self->request($r);
      $self->SOAP::Transport::HTTP::Server::handle;
      $c->send_response($self->response);
    }
    $c->close;
    return;
  }
}

1;
