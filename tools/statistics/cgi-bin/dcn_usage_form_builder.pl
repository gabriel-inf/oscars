#!/usr/bin/perl

use strict;
use CGI;
use DBI;
use JSON;
use DCN::Usage::ListBuilder;
use YAML qw 'LoadFile';

use constant CONFIG_FILE => 'conf/config.yaml';
use constant LIST_TYPES => { 'users'=> 1, 'links' => 1, 'links-users' => 1 };
use constant PARAM_LIST_TYPE => 'listType';

main();

sub main(){
    my $cgi = new CGI();
    my %json_map = ();
    my $list_builder = new DCN::Usage::ListBuilder();
    my $yaml_props = LoadFile(CONFIG_FILE);
    my $DB_USER = $yaml_props->{'db_user'};
    my $DB_PASS = $yaml_props->{'db_pass'};
    
    print $cgi->header("application/json");
    my $dbh = DBI->connect('DBI:mysql:bss', $DB_USER, $DB_PASS);
    
    my $list_type = $cgi->param(PARAM_LIST_TYPE);
    if(!LIST_TYPES->{$list_type}){
        $json_map{"error"} = "Invalid list type specified";
        output_json(\%json_map);
        return;
    }
    
    if($list_type =~ 'users'){
        $json_map{"users"} = $list_builder->listUsers($dbh);
    }
    if($list_type =~ 'links'){
        ($json_map{"links"},  $json_map{"edges"}) = $list_builder->listLinks($dbh);
    }
    
    output_json(\%json_map);
}

#################
sub output_json(){
    my $json_map = shift @_;
    my $json = to_json($json_map, {pretty => 1});
   print "{}&&". $json;
}