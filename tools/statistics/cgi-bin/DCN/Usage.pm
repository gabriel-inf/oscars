#!/usr/bin/perl

package DCN::Usage;

use strict;
use DBI;
use DateTime;
use DCN::Usage::ListBuilder;

use constant Y_AXIS_TYPE => {
    "resvCount"=>"COUNT(*)", 
    "bwSum" => "SUM(bandwidth)/1000000", 
    "bwAvg" => "AVG(bandwidth)/1000000", 
    "bwMax" => "MAX(bandwidth)/1000000", 
    "bwMin" => "MIN(bandwidth)/1000000", 
    "bwStd" => "STD(bandwidth)/1000000", 
    "durSum" => "SUM(duration)/60", 
    "durAvg" => "AVG(duration)/60", 
    "durMax" => "MAX(duration)/60", 
    "durMin" => "MIN(duration)/60",
    "durStd" => "STD(duration)/60",
    "hopCountAvg" => "AVG(hopCount)", 
    "hopCountMax" => "MAX(hopCount)", 
    "hopCountMin" => "MIN(hopCount)",
    "hopCountStd" => "STD(hopCount)",
};
    
sub new {
    my $self = {};
    bless($self);
    return $self;
}

sub get_y_types(){
    return Y_AXIS_TYPE;
}
    
sub createQuery(){
    my($self, $start, $end, $user_list, $link_list, $edge_list, $x_axis_type, $y_axis_type, $dbh) = @_;
    my @bind_list = ();
    
    #put the item to graph first then add the rest
    my $select_fields = Y_AXIS_TYPE->{$y_axis_type};
    my $y_axis_type_var = Y_AXIS_TYPE;
    foreach my $key(sort(keys %$y_axis_type_var)){
        $select_fields .= ", " . Y_AXIS_TYPE->{$key};
    }
    
    my $query = "SELECT " . $select_fields . " FROM resvPathReport ";
    if($x_axis_type eq 'links' || ($link_list && (scalar @{$link_list}) > 0)){
        $query .= 'INNER JOIN pathElems ON pathElems.pathId=resvPathReport.pathId ';
    }
    $query .= "WHERE createdTime >= ? AND createdTime < ?";
    #below will be overwritten if x_axis_type is time
    push @bind_list, $start;
    push @bind_list, $end;
    
    #add axis params first
    if($x_axis_type eq 'users'){
        $query .= " AND (login=? OR payloadSender=?)";
        push @bind_list, "";
        push @bind_list, "";
    }elsif($x_axis_type eq 'links'){
        $query .= " AND (pathElems.urn=?)";
        push @bind_list, "";
    }elsif($x_axis_type eq 'circuits'){
        $query .= " AND ((ingress=? AND egress=?) OR (ingress=? AND egress=?))";
        push @bind_list, "";
        push @bind_list, "";
        push @bind_list, "";
        push @bind_list, "";
    }
    
    #add fixed params
    if($x_axis_type ne 'users' && $user_list && (scalar @{$user_list}) > 0){
        $query .= " AND (";
        for(my $i = 0; $i < (scalar @{$user_list}); $i++){
            $query .= " OR " if($i!=0);
            $query .= "(login=? OR payloadSender=?)";
            push @bind_list, $user_list->[$i];
            push @bind_list, $user_list->[$i];
        }
        $query .= ")";
    }
    if($x_axis_type ne 'links' && $link_list && (scalar @{$link_list}) > 0){
        $query .= " AND (";
        for(my $i = 0; $i < (scalar @{$link_list}); $i++){
            $query .= " OR " if($i!=0);
            $query .= "pathElems.urn=?";
            my($label, $urn) = split('=>', $link_list->[$i]);
            push @bind_list, $urn;
        }
        $query .= ")";
    }
    if($x_axis_type eq 'links' && $edge_list && (scalar @{$edge_list}) > 0){
        $query .= " AND (";
        for(my $i = 0; $i < (scalar @{$edge_list}); $i++){
            $query .= " OR " if($i!=0);
            $query .= "ingress=? OR egress=?";
            my($label, $urn) = split('=>', $edge_list->[$i]);
            push @bind_list, $urn;
            push @bind_list, $urn;
        }
        $query .= ")";
    }
    
    #set bind params
    my $resv_data = $dbh->prepare($query);
    my $bindIndex = 1;
    foreach my $bind_param(@bind_list){
        $resv_data->bind_param($bindIndex, $bind_param);
        $bindIndex++;
    }
    
    return $resv_data;
}

sub queryResvsByTime(){
    my($self, $start, $end, $incr_type, $resv_data, $dbh) = @_;
    
    my @graph_data = ();
    my @graph_labels = ();
    my %report_data =();
    my $cur_time = $start;
    my $value = 1;
    
    while($cur_time <= $end){
        my ($cur_end, $label) = time_increment($cur_time,$incr_type);
        $resv_data->bind_param(1, $cur_time);
        $resv_data->bind_param(2, $cur_end);
        $resv_data->execute();
        normalizeRow($resv_data, \@graph_data, \@graph_labels, \%report_data, $value, $label);
        $cur_time = $cur_end;
        $value++;
    }
    
    return (\@graph_data, \@graph_labels, \%report_data);
}

sub queryResvsByUsers(){
    my($self, $user_list, $resv_data, $dbh) = @_;
    my @graph_data = ();
    my @graph_labels = ();
    my %report_data =();
    my $value = 1;
    
    #get all users if user list empty
    if(scalar(@$user_list) == 0){
        my $list_builder = new DCN::Usage::ListBuilder;
        $user_list = $list_builder->listUsers($dbh);
    }
    
    foreach my $user(@$user_list){
        $resv_data->bind_param(3, $user);
        $resv_data->bind_param(4, $user);
        $resv_data->execute();
        normalizeRow($resv_data, \@graph_data, \@graph_labels, \%report_data, $value, $user);
        $value++;
    }

    return (\@graph_data, \@graph_labels, \%report_data);
}

sub queryResvsByLinks(){
    my($self, $link_list, $resv_data, $dbh) = @_;
    my @graph_data = ();
    my @graph_labels = ();
    my %report_data =();
    my $value = 1;
    
    #get all links if user list empty
    if(scalar(@$link_list) == 0){
        my $list_builder = new DCN::Usage::ListBuilder;
        my ($link_data, $edge_data) = $list_builder->listLinks($dbh);
        foreach my $link_datum(@$link_data){
            push @$link_list, ($link_datum->{'label'}.'=>'.$link_datum->{'urn'});
        }
    }
    
    foreach my $link(@$link_list){
        my($label, $urn) = split('=>', $link);
        $resv_data->bind_param(3, $urn);
        $resv_data->execute();
        #Note: Make sure perl does not quote the numbers
        normalizeRow($resv_data, \@graph_data, \@graph_labels, \%report_data, $value, $label);
        $value++;
    }
    
    return (\@graph_data, \@graph_labels, \%report_data);
}

####### private subroutines ####### 
sub normalizeRow(){
    my($resv_data, $graph_data, $graph_labels, $report_data, $value, $label) = @_;
    
    my $row = $resv_data->fetchrow_arrayref;
    #save graph data
    ##Note: Make sure perl does not quote the numbers
    push @{$graph_data}, sprintf("%.2f", $row->[0])+0;
    push @{$graph_labels},  {'value' => $value, 'text' => $label};
    
    #save rest of data for report
    my $y_axis_type_var = Y_AXIS_TYPE;
    my $i = 1;
    foreach my $key(sort(keys %$y_axis_type_var)){
        push @{$report_data->{$key}}, sprintf("%.2f", $row->[$i])+0;
        $i++;
    }
}
sub time_increment(){
    my($timestamp, $incr_type) = @_;
    my $dt = DateTime->from_epoch(epoch => $timestamp);
    my $label = "";
    
    if($incr_type eq "minute"){
        $label = $dt->minute;
        $dt->add(minutes => 1);
    }elsif($incr_type eq "hour"){
        $label = $dt->hour;
        $dt->add(hours => 1);      
    }elsif($incr_type eq "day"){
        $label = $dt->month . '/' . $dt->day;
        $dt->add(days => 1);
    }elsif($incr_type eq "week"){
        $label = $dt->month_abbr . '/' . $dt->day;
        $dt->add(weeks => 1);
        $label .= '-' . $dt->month_abbr . '/' . $dt->day;
    }elsif($incr_type eq "month"){
        $label = $dt->month_abbr;
        $dt->add(months => 1);
    }elsif($incr_type eq "year"){
        $label = $dt->year;
        $dt->add(years => 1); 
    }

    return ($dt->epoch(), $label);
}

1;