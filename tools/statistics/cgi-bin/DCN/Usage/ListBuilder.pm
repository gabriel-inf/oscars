#!/usr/bin/perl

package DCN::Usage::ListBuilder;

use strict;
use DBI;
use YAML qw 'LoadFile';

use constant CONFIG_FILE => 'conf/config.yaml';

sub new {
    my $self = {};
    bless($self);
    return $self;
}

sub listUsers(){
    my ($self, $dbh) = @_;
    
    my @users = ();
    my %duplicate_tracker = ();
    my $login_data = $dbh->prepare("SELECT DISTINCT login FROM reservations WHERE login IS NOT NULL ORDER BY login");
    $login_data->execute();
    while(my $login = $login_data->fetchrow_arrayref){
        if($login->[0]){
            push @users, $login->[0];
            $duplicate_tracker{$login->[0]} = 1;
        }
    }
    
    my $payload_sender_data = $dbh->prepare("SELECT DISTINCT payloadSender FROM reservations WHERE payloadSender IS NOT NULL ORDER BY payloadSender");
    $payload_sender_data->execute();
    while(my $payload_sender = $payload_sender_data->fetchrow_arrayref){
        if($payload_sender->[0] && (!$duplicate_tracker{$payload_sender->[0]})){
            push @users, $payload_sender->[0];
        }
    }
    
    @users = sort {lc($a) cmp lc($b)}@users;
    
    return \@users;
}

sub listLinks(){
    my ($self, $dbh) = @_;
    
    my $yaml_props = LoadFile(CONFIG_FILE);
    my $local_domain = $yaml_props->{'localdomain'};
    my $edge_ports = $yaml_props->{"edge_ports"};
    my $exclude_links = $yaml_props->{"exclude_links"};
    
    my @links = ();
    my @edges = ();
    my $link_data = $dbh->prepare('SELECT DISTINCT nodes.topologyIdent, remoteDomains.topologyIdent, remoteNodes.topologyIdent, ports.topologyIdent, links.topologyIdent FROM nodes INNER JOIN ports ON nodes.id=ports.nodeId INNER JOIN links ON links.portId=ports.id INNER JOIN links AS remoteLinks ON links.remoteLinkId=remoteLinks.id INNER JOIN ports AS remotePorts ON remoteLinks.portId=remotePorts.id INNER JOIN nodes AS remoteNodes ON remotePorts.nodeId=remoteNodes.id INNER JOIN domains AS remoteDomains ON remoteNodes.domainId=remoteDomains.id WHERE nodes.domainId=(SELECT id FROM domains WHERE local=1 AND nodes.valid=1) ORDER BY nodes.topologyIdent');
    $link_data->execute();
    while(my $link = $link_data->fetchrow_arrayref){
        my $label = $link->[0].'->';
        if($link->[1] eq $local_domain){
            $label .= $link->[2];
        }else{
            $label .= $link->[1];
        }
        if($exclude_links->{$label}){
            next;
        }
        push @links, {'label' => $label, 
                      'urn' => "urn:ogf:network:domain=$local_domain:node=".$link->[0].":port=".$link->[3].":link=".$link->[4]};
        if(!($link->[1] && $link->[1] eq $local_domain)){
            push @edges, {'label' => $label, 
                      'urn' => "urn:ogf:network:domain=$local_domain:node=".$link->[0].":port=".$link->[3].":link=".$link->[4]};
        }
    }
    
    for my $edge(@$edge_ports){
         push @links, {'label' => $edge->{'label'},
                       'urn' => $edge->{'urn'}};
         push @edges, {'label' => $edge->{'label'},
                       'urn' => $edge->{'urn'}};
    }
    
    @links = sort{lc($a->{'label'}) cmp lc($b->{'label'})} @links;
    @edges = sort{lc($a->{'label'}) cmp lc($b->{'label'})} @edges;
    
    return (\@links, \@edges);
}

1;