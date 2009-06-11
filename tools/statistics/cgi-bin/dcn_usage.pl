#!/usr/bin/perl

use strict;
use CGI;
use DBI;
use JSON;
use Date::Parse;
use DCN::Usage;
use YAML qw 'LoadFile';

#constants
use constant CONFIG_FILE => 'conf/config.yaml';
##request parameters
use constant X_AXIS_TYPE => {"users"=>1, "links" => 1, "time" => 1};
use constant PARAM_X_AXIS_TYPE => "xType";
use constant PARAM_Y_AXIS_TYPE => "yType";
use constant PARAM_X_USER => "xUser";
use constant PARAM_X_LINK => "xLink";
use constant PARAM_X_EDGE => "xEdge";
use constant PARAM_X_TIME_INCR => "xTimeIncrement";
use constant PARAM_TIME_START => "timeStart";
use constant PARAM_TIME_END => "timeEnd";

# execute main function
main();

##### MAIN #####
sub main(){
    my $cgi = new CGI();
    my %json_map = ();
    my $dcn_usage = new DCN::Usage();
    my $yaml_props = LoadFile(CONFIG_FILE);
    my $DB_USER = $yaml_props->{'db_user'};
    my $DB_PASS = $yaml_props->{'db_pass'};
    
    print $cgi->header("application/json");
    my $dbh = DBI->connect('DBI:mysql:bss', $DB_USER, $DB_PASS);
    
    my $x_axis_type = $cgi->param(PARAM_X_AXIS_TYPE);
    if(!X_AXIS_TYPE->{$x_axis_type}){
        $json_map{"error"} = "Invalid X Axis type specified";
        output_json(\%json_map);
        $dbh->close();
        return;
    }
    
    my $y_axis_type = $cgi->param(PARAM_Y_AXIS_TYPE);
    if(!$dcn_usage->get_y_types()->{$y_axis_type}){
        $json_map{"error"} = "Invalid Y Axis type specified";
        output_json(\%json_map);
        $dbh->close();
        return;
    }
    
    if(!$cgi->param(PARAM_TIME_START)){
        $json_map{"error"} = "Invalid start time specified";
        output_json(\%json_map);
        $dbh->close();
        return;
    }
    my $time_start = str2time($cgi->param(PARAM_TIME_START), 'GMT');
    
    if(!$cgi->param(PARAM_TIME_END)){
        $json_map{"error"} = "Invalid end time specified";
        output_json(\%json_map);
        $dbh->close();
        return;
    }
    my $time_end = str2time($cgi->param(PARAM_TIME_END), 'GMT');
    
    #get query params
    my $incr_type = $cgi->param(PARAM_X_TIME_INCR);
    if(!$incr_type){
        $incr_type = "month";
    }
    my @user_list = $cgi->param(PARAM_X_USER);
    my @link_list = $cgi->param(PARAM_X_LINK);
    my @edge_list = $cgi->param(PARAM_X_EDGE);
    
    my $resv_data = $dcn_usage->createQuery($time_start, $time_end, \@user_list, \@link_list,
            \@edge_list, $x_axis_type, $y_axis_type, $dbh);
            
    if($x_axis_type eq 'time'){
        ($json_map{"graph_data"}, $json_map{"labels"}, $json_map{"report_data"}) = 
            $dcn_usage->queryResvsByTime($time_start, $time_end, $incr_type, $resv_data, $dbh);
    }elsif($x_axis_type eq 'users'){
        ($json_map{"graph_data"}, $json_map{"labels"}, $json_map{"report_data"}) = 
            $dcn_usage->queryResvsByUsers(\@user_list, $resv_data, $dbh);
    }elsif($x_axis_type eq 'links'){
        ($json_map{"graph_data"}, $json_map{"labels"}, $json_map{"report_data"}) =
            $dcn_usage->queryResvsByLinks(\@link_list, $resv_data, $dbh);
    }
    
    
    output_json(\%json_map);
    $dbh->close();
}
#################

sub output_json(){
    my $json_map = shift @_;
    my $json = to_json($json_map, {pretty => 1});
    print "{}&&". $json;
}