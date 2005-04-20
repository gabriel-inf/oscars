package SOAP::Transport::HTTP::Daemon::ThreadOnAccept;

use threads;
use threads::shared;

use strict;
use vars qw(@ISA);

# based on SOAP::ransport::HTTP::Daemon
use SOAP::Transport::HTTP;

# Based on ForkOnAccept.pm by Michael Douglass

@ISA = qw(SOAP::Transport::HTTP::Daemon);

my $c;

sub handle {
  my $self = shift->new;

  while ( $c = $self->accept) {
    my $handler_thread = threads->create( "callback", $self );
    $handler_thread->detach();
    $c->close;
    next;
  }
  
}

sub callback {
  my $self = shift;
 
  # why does this crash it?  
  #$self->close;  # Close the listening socket (always done in children)

  # Handle requests as they come in
  while (my $r = $c->get_request) {
    $self->request($r);
    $self->SOAP::Transport::HTTP::Server::handle;
    $c->send_response($self->response);
  }
  $c->close;
  #try this here
  $self->close;  # Close the listening socket (always done in children)
  return;
}


1;
