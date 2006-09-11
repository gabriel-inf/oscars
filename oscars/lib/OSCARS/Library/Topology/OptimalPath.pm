# Kilroy was here... 
package OSCARS::Library::Topology::OptimalPath;

use strict;
#use library_interface_addresses;
########################################################################################################################################
#File      :  library.pm 
#Developer : Neena Kaushik (PhD Candidate, Santa Clara University), Summer Intern, ESnet
#Supervisor: Chin Guok, ESnet
#######################################################################################################################################

use XML::DOM;
use XML::Writer;

################################################### Subroutine description  ################################################################
#The following sub routines are present in this file
#
# sub pretty_printer($source, $dest, $paths, $debug)  
# - Prints the paths in easily readable form
#
# sub pretty_printer_with_latency($source, $dest, $pathlist, $latency_doc, $topo_doc, $debug);
# - Prints the paths in easily readable form along witht their latencies and also returns the scalar pathlist with latencies
#
# sub get_latency - Returns the per hop latency 
#
# sub sort_paths($paths, $debug)  
# - Sorts the paths separted by a colon delimiter, by length 
#
# sub by_length  
# - Does a comparison by length of subpath to be used by the sort subroutine 
#
# sub get_length_of_current_path($path, $debug)
# - Returns the length of the current path
#
# sub is_router($topo_doc, $ipaddress, $debug)       
# - Check for the address passed being the loopback address of a router
#
# sub is_network($topo_doc, $ipaddress, $debug)      
# - Check for the address passed being the address of a network
#
# sub get_other_end_of_link($topo_doc, $current_router_loopback_address, $source, $counter, $debug) 
# - Get the other end of the point to point link
#
# sub get_other_transit_link($topo_doc, $current_router_loopback_address, $network, $debug) 
# - Get the other end of transit link
#
# sub remove_tag($string, $debug)     
# - Remove the xml tag from the given string
#
# sub path($topo_doc, $source, $dest, $current_path, $level, $traversed_routers, $r_length, $debug)    
# - Find the paths from a source to a destination by calling itself recursively
#
# sub path_hash($pathlist, $debug)  
# - Returns the pathlist in a hash format 
#
# sub create_path_xml_file($src, $dst,$xml_output_file, $debug, %pl_latency_hash)
# - Create an xml file with name $xml_output_file giving the path latency information
#
# sub hash_path_print($source, $dest, $debug, %pl_latency_hash)
# - Provides a sample of how to print the hash data structure containing the path and latency
#   information 
#########################################################################################################################################


sub new {
    my( $class, %args ) = @_;
    my( $self ) = { %args };
    bless( $self, $class );
    $self->initialize();
    return( $self );
}

sub initialize {
    my($self) = @_;
    $self->{lookup_struct} = {};
}


#########################################################################################################################################
# sub hash_path_print($source, $dest, %pl_latency_hash, $debug)
# - Provides a sample of how to print the hash data structure containing the path and latency
#   information 
#########################################################################################################################################
sub hash_path_print($$$%)
{
    my($self, $source,$dest,$debug,%pl_latency_hash) = @_;
    my $count = 0;

    foreach my $key (keys % pl_latency_hash)
    {
        $count = $count + 1;
    }
    $count = $count/3;
 
    print "\n %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
    print "\n Path characteristics from source $source to dest $dest from the hash data structure are";
    print "\n %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
    
    for (my $i = 0; $i < $count; $i++)
    {
         print "\n -------------------Path $i---------------------------------------------------------------"; 
         my $path_id = "path_" . $i;
         print "\n Path is $pl_latency_hash{$path_id}";
         my $latency_id = "lat_" . $i;
         print "\n Latency is $pl_latency_hash{$latency_id}";

         my $comment_id = "comment_" . $i;
         my $k = 0;
         print "\n The following hops have undetermined latencies";
         while (1)
         {
            if ( $pl_latency_hash{$comment_id}{$k} eq "done" )
             {
                last;
             }
             print "\n Hop $k: $pl_latency_hash{$comment_id}{$k}";
             $k = $k + 1;
         }

    }
}

##########################################################################
#sub create_path_xml_file($src, $dst, $xml_output_file, $debug, %pl_latency_hash)  
# Create an xml file with name $xml_output_file giving the path latency
# information
##########################################################################
sub create_path_xml_file($$$$%)
{
    my($self, $src, $dst, $xml_output_file, $debug, %pl_latency_hash) = @_;
    my $j = 0;
    my $id = 0;
    my $count = 0;
    my $data_id = "null";

    my $output = new IO::File(">$xml_output_file");
    my $writer = new XML::Writer(OUTPUT => $output);

    $writer->xmlDecl('UTF-8');
    $writer->doctype('xml');

    $writer->startTag('xml');
    $writer->startTag('nmwg:message', 'type' => 'path_from_src_to_dst', 'xmlns:nmwg' => 'http://ggf.org/ns/nmwg/2.0/',
                                                                      'xmlns:nmwgt' => 'http://ggf.org/ns/nmwg/topology/2.0/');
 

     ## Start of the metadata part in forward direction

        $writer->startTag('nmwg:metadata', 'id' => "meta1");
        $writer->startTag('nmwgt:endPointPair');
        $writer->startTag('nmwgt:src', 'type' => 'ipv4', 'value' => $src);
        $writer->endTag();
        $writer->startTag('nmwgt:dst', 'type' => 'ipv4', 'value' => $dst);
        $writer->endTag();
        $writer->endTag();
        $writer->endTag();

    ## End of the metadata part in forward direction


 
    foreach my $key (keys % pl_latency_hash)
    {
        $count = $count + 1;
    }

    $count = $count/3;

    for (my $i = 0; $i < $count; $i++)
    {
         $id = $i + 1;
         $data_id = "data_" . $id;
         $writer->startTag('nmwg:data', 'id' => $data_id , 'metadataIdRef' => "meta1");
         $writer->dataElement("nmwg:rank", "$i");
         my $path_id = "path_" . $i;
         $writer->dataElement("nmwg:path", "$pl_latency_hash{$path_id}");
         my $latency_id = "lat_" . $i;

         my $lat_temp = $pl_latency_hash{$latency_id};
         my @temp = split(/[ ]/, $lat_temp);

         $writer->startTag('nmwgt:latency', 'value' => $temp[4], 'valueUnits' => $temp[5]);
         $writer->endTag();

         my $comment_id = "comment_" . $i;
         my $k = 0;
         while (1)
         {
             if ($pl_latency_hash{$comment_id}{$k} eq "done")
             {
                 last;
             }
             $writer->dataElement("nmwg:undetermined_latencies_hop_$k", "$pl_latency_hash{$comment_id}{$k}");
             $k = $k + 1;
         }

         $writer->endTag();
    }

    $writer->endTag();
    $writer->endTag();
    $writer->end();

}
##########################################################################
#sub sort_paths($paths) - Sorts the paths by length
##########################################################################
sub sort_paths($$)
{
    my($p, $debug) = @_;
    my @paths = split(/:/, $p);
  
    ## Sort paths by number of links 
    @paths = sort by_length @paths;  

    $p = join(":", @paths);
    return $p;
}
##########################################################################
#sub pretty_printer - Prints the paths in easily readable form, with
#                     each path on a separate line by removing the 
#                     colon separator  
##########################################################################
sub pretty_printer($$$$)
{
    my($self, $s, $d, $p, $debug) = @_;
    my $count = 0;

    $p = &sort_paths($p, $debug);
    my @paths = split(/:/,$p);
    my $length = @paths;

    print "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
    print "\n Paths from source $s to destination $d are";
    print "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
    
    for (my $i = 0; $i < $length; $i++)
    {
        if (($paths[$i] eq "") || ($paths[$i] eq -1))
        {
            next;
        }
        $count = $count + 1;
        $paths[$i] = $s . $paths[$i];
        print "\n $count).  $paths[$i]";
    }
    print "\n \n";
}
##############################################################################
#sub pretty_printer_with_latency - Prints the paths in easily readable form, 
#                                  with their latencies
#                                  each path on a separate line by removing the 
#                                  colon separator and also returns them
#                                  as a scalar. If any of the per hop latencies
#                                  are undetermined, it returns them as
#                                  a comment. 
#############################################################################
sub pretty_printer_with_latency($$$$$$)
{
    my ($self, $s, $d, $pl, $lt_doc, $topo_doc, $debug) = @_;
    my $count = 0;
    my $i = 0;
    my $j = 0;
    my $k = 0;
    my $flag = 0;

    $pl = &sort_paths($pl, $debug);
    my @paths = split(/:/, $pl);
    my $length = @paths;
 
    my $total_latency = 0;
    my $hop_latency = 0;
    my $comment = "";

    print "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
    print "\n Paths from source $s to destination $d  with latency information are";
    print "\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";

    for ($i = 0; $i < $length; $i++)
    {
        ## Check if this condition can be removed 

        if ( ($paths[$i] eq "") || ($paths[$i] eq "-1") )
        {
            next;
        }

        ## Check if this condition can be removed 

        $count = $count + 1;
        $paths[$i] =  " " . $s . $paths[$i]; ## space is to ensure the proper format while printing undetermined latencies
        
        my @lt_paths = split(/,/, $paths[$i]);
        my $lt_paths_length = @lt_paths;
         
        $j = 0;
        $k = 0;
        $total_latency = 0;
        $comment = "";

        while($j < ($lt_paths_length -1))
        {
            $k = $j + 1;
            if (($lt_paths[$j]) && ($lt_paths[$k]))
            {
                $hop_latency = &get_latency($lt_doc, $topo_doc, $lt_paths[$j], $lt_paths[$k], $debug);
            }

            if ($hop_latency == -1)
            {
                $comment = $comment . "\n" . $lt_paths[$j] . ", " . $lt_paths[$k];
            }
            else
            {
                $total_latency = $total_latency + $hop_latency;
            }
            $j = $j+1;
        }
        if ($comment eq "")
        {
            $paths[$i] = $paths[$i] . "; total latency = $total_latency ms"; 
        }
        else
        {
            $paths[$i] = $paths[$i] . "; total latency = $total_latency" . "+" .  " ms ; undetermined per hop latencies are" . $comment; 
        }

        print "\n $count). $paths[$i] ";
   
    }
    print "\n";

    $pl = join(":", @paths);
    return $pl;

}
##########################################################################
#sub get_latency- Returns the per hop latency 
##########################################################################
sub get_latency($$$$$)
{
    my ($lt_doc, $topo_doc, $hp1, $hp2, $debug) = @_;
    my $latency = -1;
    my @temp;

    @temp = split(" ", $hp1);
    $hp1 = $temp[0];

    @temp = split(" ", $hp2);
    $hp2 = $temp[0];
    
    my $traceroute_path = $hp1 . "," . $hp2 ;

    if($debug)
    {
        print "traceroute_path is $traceroute_path";
    }
    my $inner_ep_path = &get_interface_endpoints($traceroute_path, $topo_doc, $debug);

    my $data_nodes = "";
    my $meta_nodes = "";
    my $data_node = "";
    my $meta_node = "";
    my $src_req = "";
    my $dst_req = "";
    my $src = "";
    my $dst = "";
    my $i = 0;
    my $length = 0;

    my @end_points = split(/,/, $inner_ep_path);
    my $ep_length = @end_points;

    if ($debug)
    {
        print "\n traceroute_path is $traceroute_path";
        print "\n inner_ep_path is $inner_ep_path";
        print "\n The number of endpoints are $ep_length";
    }

    if ($ep_length == 2)
    {
        $src_req = $end_points[0];
        @temp = split(" ", $src_req);
        $src_req = $temp[0];
    
        $dst_req = $end_points[1];
        @temp = split(" ", $dst_req);
        $dst_req = $temp[0];

        if ($debug)
        {
            print "\n -----------src_req is $src_req and dst_req is $dst_req-----------------";
        }

        $meta_nodes = $lt_doc->getElementsByTagName("nmwg:metadata");
        $data_nodes = $lt_doc->getElementsByTagName("nmwg:data");

        if ($meta_nodes->getLength != $data_nodes->getLength)
        {
            print "\n The latency file does not have the correct format"; 
            return -1;
        }

        $length = $meta_nodes->getLength; ## Since data_nodes and meta_nodes have the same length
       
        for ($i = 0; $i < $length; $i++)
        {
             $meta_node = $meta_nodes->item($i);

             $src = $meta_node->getElementsByTagName("nmwgt:src")->item(0)->getAttributeNode("value")->getValue;
             $src = &remove_tag($src, $debug);
 
             if ($debug)
             {            
                 print "\n src is $src";
             }
                     
             $dst = $meta_node->getElementsByTagName("nmwgt:dst")->item(0)->getAttributeNode("value")->getValue;
             $dst = &remove_tag($dst, $debug);
            
             if ($debug)
             { 
                 print "\n dst is $dst";
             }
             if ( ($src eq $src_req) && ($dst eq $dst_req) )
             {
                 $data_node = $data_nodes->item($i);
                 $latency = $data_node->getElementsByTagName("ping:datum")->item(0)->getAttributeNode("value")->getValue;
                 return $latency;
             }
        }

    }

    return $latency;
}
##########################################################################
#sub get_length_of_current_path - Returns the length of the current path
##########################################################################
sub get_length_of_current_path($$)
{
    my($p, $debug) = @_;
    my @paths = split(/,/,$p);
    my $length = @paths - 1; ## Since the current path list starts with , 
                             ## for any path greater than 1, length 
                             ## is incremented by 1 
    return $length;
}
##########################################################################
#sub by_length  - Compares paths by length to be used by the
#                 sort subroutine 
##########################################################################
sub by_length 
{
    ## This is a direct textbook implementation of sort
    ## Not sure how to pass $debug to this subroutine, so initialized its
    ## value to 0

    my $debug = 0;
    my $a_length = &get_length_of_current_path($a, $debug);
    my $b_length = &get_length_of_current_path($b, $debug);

    if ($a_length < $b_length)
    {
        return -1;
    }
    elsif ($a_length == $b_length)
    {
        return 0;
    }
    elsif ($a_length > $b_length)
    {
        return 1;
    }
}
##########################################################################
#sub is_router - Checks if the ip address passed is the loopback address   
#                of a router. The return value is 1 if the ip address is
#                that of a router and 0 otherwise
##########################################################################
sub is_router($$$) 
{
    my($topo_doc, $address, $debug) = @_;
    $address = &remove_tag($address, $debug);

    if ($debug)
    {
        print "\n the address passed is $address";
    }

    my $nodes = $topo_doc->getElementsByTagName("ospf-database");
    my $n = $nodes->getLength;

    for (my $i = 0; $i < $n; $i++)
    {
        my $node = $nodes->item($i);
        my $lsa_type = $node->getElementsByTagName("lsa-type")->item(0)->toString;
        my $lsa_id = $node->getElementsByTagName("lsa-id")->item(0)->toString;
        $lsa_id = &remove_tag($lsa_id, $debug);

        if ($lsa_type =~ /Router/) 
        {
            if ($lsa_id eq $address) 
            {
                return 1;
            }
        }
    }
    return 0;
}
###################################################################################
#sub get_other_end_of_link - Get the other end of the point to point ATM link. The
#                            return value is the ip address of the other end of the
#                            ATM link 
###################################################################################
sub get_other_end_of_link($$$$)
{
    my($topo_doc, $r, $s, ,$counter, $debug) = @_;

    my $nodes = $topo_doc->getElementsByTagName("ospf-database");
    my $n = $nodes->getLength;
    my $l_count = 0;
    $r = &remove_tag($r, $debug);
    $s = &remove_tag($s, $debug);

    for (my $i = 0; $i < $n; $i++)
    {
        my $node = $nodes->item($i);
        my $lsa_type = $node->getElementsByTagName("lsa-type")->item(0)->toString;
        my $lsa_id = $node->getElementsByTagName("lsa-id")->item(0)->toString;
        $lsa_id = &remove_tag($lsa_id, $debug);

        if ($lsa_type =~ /Router/)
        {
            if ($lsa_id eq $r) ## the router on which the link is present
            {
                 my $ospf_links = $node->getElementsByTagName("ospf-link");
                 my $length = $ospf_links->getLength;

                 for (my $k = 0; $k < $length; $k++)
                 { ## start of the for loop for all ospf links on the router - Go through all links

                    my $ospf_link = $ospf_links->item($k);
                    my $link_id = $ospf_link->getElementsByTagName("link-id")->item(0)->toString;
                    my $link_data = $ospf_link->getElementsByTagName("link-data")->item(0)->toString;

                    ## Start remove tags

                    $link_id = &remove_tag($link_id, $debug);
                    $link_data = &remove_tag($link_data, $debug);

                    if ($link_id eq $s) 
                    {
                        if ($l_count == $counter)
                        {
                            return $link_data;
                        }
                        else
                        { 
                            $l_count = $l_count + 1;
                        }
                    }
                    if ($link_data eq $s)
                    {
                        if ($l_count == $counter)
                        {
                            return $link_id;
                        }
                        else
                        {
                            $l_count = $l_count + 1;
                        }
                    }

               }
           }
       }
    }
    return -1;
}
####################################################################################
#sub get_other_transit_link - Get the other end of transit link. The
#                             return value is the ip address of the other end of the
#                             transit link 
####################################################################################
sub get_other_transit_link($$$$)
{
    my($topo_doc, $router, $network, $debug) = @_;

    my $nodes = $topo_doc->getElementsByTagName("ospf-database");
    my $n = $nodes->getLength;

    $router = &remove_tag($router, $debug);
    $network = &remove_tag($network, $debug);
    
    if ($debug)
    {
        print "\n The value of router is $router and network is $network";
    }

    for (my $i = 0; $i < $n; $i++)
    {
        my $node = $nodes->item($i);
        my $lsa_type = $node->getElementsByTagName("lsa-type")->item(0)->toString;
        my $lsa_id = $node->getElementsByTagName("lsa-id")->item(0)->toString;

        $lsa_type = &remove_tag($lsa_type, $debug);
        $lsa_id = &remove_tag($lsa_id, $debug);

        if ($lsa_type =~ /Router/)
        {
            if ($lsa_id eq $router) ## the router on which the link is present
            {
                 my $ospf_links = $node->getElementsByTagName("ospf-link");
                 my $length = $ospf_links->getLength;

                 for (my $k = 0; $k < $length; $k++)
                 { ## start of the for loop for all ospf links on the router - Go through all links
                    my $ospf_link = $ospf_links->item($k);
                    my $link_id = $ospf_link->getElementsByTagName("link-id")->item(0)->toString;
                    my $link_data = $ospf_link->getElementsByTagName("link-data")->item(0)->toString;

                    ## Start remove tags

                    $link_id = &remove_tag($link_id, $debug);
                    $link_data = &remove_tag($link_data, $debug);

                    if ($link_id eq $network)
                    {
                        return $link_data;
                    }
                    if ($link_data eq $network)
                    {
                        return $link_id;
                    }

               }
           }
       }
    }
    return 0;
}
##########################################################################
#sub is_network - Checks if the source ip address passed is that 
#                 of a network. The return value is 1 if the ip address
#                 passed is that of a network and 0 otherwise
##########################################################################
sub is_network($$) 
{
    my($topo_doc, $address, $debug) = @_;

    my $nodes = $topo_doc->getElementsByTagName("ospf-database");
    my $n = $nodes->getLength;
    $address = &remove_tag($address, $debug);

    for (my $i = 0; $i < $n; $i++)
    {
        my $node = $nodes->item($i);
        my $lsa_type = $node->getElementsByTagName("lsa-type")->item(0)->toString;
        my $lsa_id = $node->getElementsByTagName("lsa-id")->item(0)->toString;
        $lsa_id = &remove_tag($lsa_id, $debug);

        if ($lsa_type =~ /Network/) 
        {
            if ($lsa_id eq $address) 
            {
                return 1;
            }
        }
    }
    return 0;
}
##########################################################################
#sub remove_tag - Removes the xml tags, given the  string. The return
#                 value is a string with the tags removed. 
##########################################################################
sub remove_tag($$)
{   
    my($string, $debug) = @_;

    $string =~ s/<\w+\-*\w*>//;
    $string =~ s/<\/\w+\-*\w*>//;
    return $string
}
##########################################################################
#sub path_hash  - Returns the pathlist in a hash format 
##########################################################################
sub path_hash($$$)
{   
    my($self, $pathlist, $debug) = @_;
    my %p_hash = ();

    my @paths = split(/:/, $pathlist);
    my $length = @paths;
    my $path_id;
    my $lat_id;
    my $comment_id;
    my @path_temp;
    my $i = 0;
    my $count =  0;

    for ($i = 0; $i < $length; $i++)
    {
        if ( ($paths[$i] eq "") || ($paths[$i] eq " ") || ($paths[$i] eq -1) )
        {
            next;
        }

        $path_id = "path_" . $count;
        $lat_id = "lat_" . $count;
        $comment_id = "comment_" . $count;
        @path_temp = split(/;/ ,$paths[$i]);


        $p_hash{$path_id} = $path_temp[0];
        $p_hash{$lat_id} = $path_temp[1];

        if (@path_temp == 2)
        {
            $p_hash{$comment_id}{0} = "none";
            $p_hash{$comment_id}{1} = "done";
        }
        else
        {
            my @undetermined_temp = split("[\n]", $path_temp[2]);
            my $undetermined_length = @undetermined_temp;
            my $j = 0;
            my $h_count = 0;
 
            for ($j = 0; $j < $undetermined_length; $j++)
            {
                if ($j == 0)
                {
                    next;
                }
                $p_hash{$comment_id}{$h_count} = $undetermined_temp[$j];
                $h_count = $h_count + 1;
            }
            $p_hash{$comment_id}{$h_count} = "done";

        }
        $count = $count + 1;

    }

    return %p_hash;

}
#####################################################################################
#sub path - Calls the subroutine path recursively to get all the paths in the topology 
#           from a given source to a destination.    
#Input    - Source IP Address , Destination IP Address, Current Path, Recursion Level,
#           and Traversed Routers. The traversed routers are for loop detection.
#           Note the recursion level is for debugging purposes only.  
#Output   - List of paths satisfying a criteria, each path separated by 
#           a colon, and individual ip addresses separated by a comma.
#####################################################################################
#sub path($$$$$$$$$) 
sub path
{ ## Start of subroutine path

    my($self, $topo_doc, $s, $d, $cp, $l_level, $traversed_routers, $r_length, $debug) = @_;

    $s = &remove_tag($s, $debug);
    $d = &remove_tag($d, $debug);

    my $cp_temp;
    my $tr_temp;
    my $link_id; 
    my $link_data;
    my $link_type_name;
    my $router_check = "";
    my $other_end_of_p2p = "";
    my $pathlist = "";
    my $pathlist_temp = "";
    my $link;
    my $router1;
    my $router2;
    my $c_length = 0;
    my $k = 0;
    my $counter = 0;
    my $key;

    if ($debug)
    {
         print "\n%%%%%%%%%%%%%%%%%%% The current level of recursion is $l_level %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
         print "\n The traversed routers are $traversed_routers";
    }

    my $nodes = $topo_doc->getElementsByTagName("ospf-database");
    my $n = $nodes->getLength;

    for (my $i = 0; $i < $n; $i++)
    { ## start of main for loop - Go through all routers
        my $node = $nodes->item($i);
        my $lsa_type = $node->getElementsByTagName("lsa-type")->item(0)->toString;
        my $lsa_id = $node->getElementsByTagName("lsa-id")->item(0)->toString;

        $lsa_type = &remove_tag($lsa_type, $debug);
        $lsa_id = &remove_tag($lsa_id, $debug);

        if ($lsa_type =~ /Router/) 
        { ## start of checking for router loop

            if ($lsa_id eq $s) 
            { ## start of if loop matching the source ip address
                if ($debug)
                {
                    print "\n The router under consideration is $lsa_id";
                }
                my $ospf_links = $node->getElementsByTagName("ospf-link");
                my $length = $ospf_links->getLength;
                my $exhausted_links = "";
                
                for ($k = 0; $k < $length; $k++) 
                { ## start of the for loop for all ospf links on the router - Go through all links
                    my $ospf_link = $ospf_links->item($k);
                    if ($debug)
                    {
                        print "\n---------------------- The value of k is $k ---------------------------------";
                    }
                    $link_id = $ospf_link->getElementsByTagName("link-id")->item(0)->toString;
                    $link_data = $ospf_link->getElementsByTagName("link-data")->item(0)->toString;
                    $link_type_name = $ospf_link->getElementsByTagName("link-type-name")->item(0)->toString;

                    $link_id = &remove_tag($link_id, $debug); 
                    $link_data = &remove_tag($link_data, $debug); 
                    $counter = 0;

                    if ($debug)
                    {
                        print "\n The value of link_id is $link_id and link_data is $link_data";
                    }

                    if ( ($cp =~ /$link_id,/) || ($cp =~ /$link_data,/) ) 
                    {
                        next;
                    }

                    if ($link_type_name =~ /PointToPoint/)
                    {
                        if ($debug)
                        {
                            print "\n in point to point";
                            print "\n %%%%%%%%%%%%%%%%%%%%%%%%%%%%%The current level of recursion is $l_level %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
                        }

                        if (&is_router($topo_doc, $link_id, $debug))
                        {
                            $router_check = $link_id;
                            $link = $link_data;
                        }
                        else
                        {
                            $router_check = $link_data;
                            $link = $link_id;
                        }

                        $other_end_of_p2p =  &get_other_end_of_link($topo_doc, $router_check, $s, $counter, $debug);

                        ## Added new while debugging
                        while ($exhausted_links =~ /,$other_end_of_p2p,/)
                        {
                            $counter = $counter + 1;
                            $other_end_of_p2p =  &get_other_end_of_link($topo_doc, $router_check, $s, $counter, $debug);

                            if ($debug)
                            {
                                print "\n The value of counter is $counter";
                                print "\n other_end_of_p2p is $other_end_of_p2p";
                            }
                            if ($other_end_of_p2p eq "-1")
                            {
                                last;
                            }
                        }
                        ## Added new while debugging 

                        $c_length = &get_length_of_current_path($cp, $debug);
                        $exhausted_links = $exhausted_links . "," . $other_end_of_p2p . ",";

                        if (($other_end_of_p2p eq "-1") || ($cp =~ /$other_end_of_p2p,/) || ($traversed_routers =~ /$router_check,/) || ($c_length >= $r_length))
                        {
                        }
                        else
                        {
                            if ($debug)
                            {
                                print "\n cp is $cp router_check is $router_check and other end is $other_end_of_p2p";
                            }

                            if ($router_check eq $d)
                            {
                                if ($debug)
                                {
                                   print "\n !!! -- In p2p 1 -- cp is $cp and adding link $other_end_of_p2p to it and this is the pl $l_level!!!";
                                }
                                $cp_temp = $cp . ", " . $other_end_of_p2p;

                                if ($self->{lookup_struct}->{$s})
                                {
                                    $self->{lookup_struct}->{$s} = $self->{lookup_struct}->{$s} . " ". $other_end_of_p2p;
                                }
                                else
                                {
                                    $self->{lookup_struct}->{$s} = $other_end_of_p2p;
                                }
     
                                $tr_temp = $traversed_routers . "," . $router_check;
                                if ($pathlist =~ /$cp_temp/) {}
                                else
                                {
                                    $pathlist = $pathlist . ":" . $cp_temp;
                                }
                                #return $pathlist; ## uncommented while debugging
                            }
                            else
                            {
                                 $cp_temp = $cp . ", " . $other_end_of_p2p;
                                 if ($self->{lookup_struct}->{$s})
                                 {
                                    $self->{lookup_struct}->{$s} = $self->{lookup_struct}->{$s} . " ". $other_end_of_p2p;
                                 }
                                 else
                                 {
                                    $self->{lookup_struct}->{$s} = $other_end_of_p2p;
                                 }
                                 $tr_temp = $traversed_routers . "," . $router_check;
                                 #$pathlist_temp = &path($topo_doc, $router_check, $d, $cp_temp, ($l_level+1), $tr_temp, $r_length, $debug);
                                 $pathlist_temp = $self->path($topo_doc, $router_check, $d, $cp_temp, ($l_level+1), $tr_temp, $r_length, $debug);


                                 ## Is this condition needed ?

                                 if ($pathlist =~ /$pathlist_temp/) {} 
                                 else 
                                 {
                                     $pathlist = $pathlist . ":" . $pathlist_temp;
                                 }

                                 ## Is this condition needed ?

                                 if ($debug)
                                 {
                                     print "\n -- in p2p 2 -- The recursion level is $l_level and the pathlist is $pathlist";
                                 }
                            }
                        }
                     }

                    if ($link_type_name =~ /Transit/) 
                    { ## start of if loop - checking for links of transit type

                       if ($debug)
                       {
                           print "\n In Transit Part";
                           print "\n The link under consideration is $link_id and $link_data";
                           print "\n %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% The current level of recursion is $l_level %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% ";
                       }

                       my $ret = &is_network($topo_doc, $link_id, $debug);
                       if ($ret == 1)
                       {
                           $link = $link_id;
                       }
                       else
                       {
                           $link = $link_data;
                       }

                       my $nodes = $topo_doc->getElementsByTagName("ospf-database");
                       my $n = $nodes->getLength;

                        for (my $i = 0; $i < $n; $i++)
                        {
                            my $node = $nodes->item($i);
                            my $lsa_type = $node->getElementsByTagName("lsa-type")->item(0)->toString;
                            my $lsa_id = $node->getElementsByTagName("lsa-id")->item(0)->toString;

                            $lsa_id = &remove_tag($lsa_id, $debug); 
                            $lsa_type = &remove_tag($lsa_type, $debug); 
                            $link = &remove_tag($link, $debug);

                            if ($debug) 
                            {
                                #print "\n The value of i is $i"; 
                            }

                            if (($lsa_type =~ /Network/) && ($lsa_id eq $link))
                            {
                                my $attached_routers = $node->getElementsByTagName("attached-router");
                                my $length = $attached_routers->getLength;

                                for (my $r = 0; $r < $length; $r=($r+2)) 
                                {
                                    if ($debug)
                                    {
                                        print "\n The network under consideration is $lsa_id";
                                        print "\n r is $r";
                                        print "\n source is $s";
                                    }

                                    $router1 = $attached_routers->item($r)->toString;
                                    if (($r+1) < $length) 
                                    {
                                        $router2 = $attached_routers->item($r+1)->toString;
                                    }

                                    $link = &remove_tag($link, $debug); 
                                    $router1 = &remove_tag($router1, $debug);
                                    $router2 = &remove_tag($router2, $debug);

                                    if ($debug)
                                    {
                                        print "\n the value of router1 is $router1 and router2 is $router2" ;
                                    }

                                    if ($router1 eq $s) 
                                    {
                                        my $other_end = &get_other_transit_link($topo_doc, $router2, $link, $debug);
                                        my $c_length = &get_length_of_current_path($cp, $debug);
 
                                        if (($cp =~ /$other_end,/) || ($traversed_routers =~ /$router2,/) || $c_length >= $r_length)
                                        {
                                        }
                                        else
                                        {
                                            $cp_temp = $cp . ", " . $other_end;
                                            if ($self->{lookup_struct}->{$s})
                                            {
                                                $self->{lookup_struct}->{$s} = $self->{lookup_struct}->{$s} . " ". $other_end;
                                            }
                                            else
                                            {
                                                $self->{lookup_struct}->{$s} = $other_end;
                                            }
                                            $tr_temp = $traversed_routers . "," . $router2;
                                            if ($router2 eq $d)
                                            {
                                                if ($debug)
                                                {
                                                    print "\n !!! -- in transit -- cp is $cp_temp and this is the pl $l_level!!!";
                                                }
                                                if($pathlist =~ /$cp_temp/) {}
                                                else
                                                {
                                                    $pathlist = $pathlist . ":" .  $cp_temp; 
                                                }
                                                #return $pathlist; ## uncommented while debugging
                                             }
                                             else
                                             {
                                                 #$pathlist_temp = &path($topo_doc, $router2, $d, $cp_temp, ($l_level+1), $tr_temp, $r_length, $debug);
                                                 $pathlist_temp = $self->path($topo_doc, $router2, $d, $cp_temp, ($l_level+1), $tr_temp, $r_length, $debug);
                                                 if ($pathlist =~ /$pathlist_temp/) {} 
                                                 else 
                                                 {
                                                     $pathlist = $pathlist . ":" . $pathlist_temp;
                                                 }

                                                 if ($debug)
                                                 {
                                                     print "\n -- in transit -- The recursion level is $l_level and the pathlist is $pathlist";
                                                 }
                                              }
                                          }
                                     }
                                    if ($router2 eq $s) 
                                    {
                                        my $other_end = &get_other_transit_link($topo_doc, $router1, $link, $debug);
                                        my $c_length = &get_length_of_current_path($cp, $debug);

                                        if (($cp =~ /$other_end,/) || ($traversed_routers =~ /$router1,/) || ($c_length >= $r_length))
                                        {
                                        }
                                        else
                                        {
                                            $cp_temp = $cp . ", " . $other_end;
                                            if ($self->{lookup_struct}->{$s})
                                            {
                                                $self->{lookup_struct}->{$s} = $self->{lookup_struct}->{$s} . " ". $other_end;
                                            }
                                            else
                                            {
                                                $self->{lookup_struct}->{$s} = $other_end;
                                            }
                                            $tr_temp = $traversed_routers . "," . $router1;
                                            if ($router1 eq $d)
                                            {
                                                if ($debug)
                                                {
                                                    print "\n !!!-- in transit -- cp is $cp_temp and this is the pl $l_level!!!";
                                                }
                                                if ($pathlist =~ /$cp_temp/) {}
                                                else
                                                {
                                                    $pathlist = $pathlist . ":" .  $cp_temp;
                                                }
                                                #return $pathlist; ## uncommented while debugging
                                            }
                                            else
                                            {
                                                #$pathlist_temp =  &path($topo_doc, $router1, $d, $cp_temp, ($l_level+1), $tr_temp, $r_length, $debug);
                                                $pathlist_temp =  $self->path($topo_doc, $router1, $d, $cp_temp, ($l_level+1), $tr_temp, $r_length, $debug);
                                                if ($pathlist =~ /$pathlist_temp/) {} 
                                                else 
                                                {
                                                    $pathlist = $pathlist . ":" . $pathlist_temp;
                                                }
                                                if ($debug)
                                                {
                                                    print "\n -- in transit -- The recursion level is $l_level and the pathlist is $pathlist";
                                                }
                                             }
                                         }
                                    }
                                }
                            } ## end of if loop checking for the network part
                          } ## end of for loop going over all the routers in the network
                     } ## end of if loop - checking for links of transit type
                } ## end of for loop for all ospf links on the router

                if ($debug)
                {
                    print "\n !!! The recursion level is $l_level and the pathlist is $pathlist !!! ";
                }
                return $pathlist;
            } ## end of if loop matching the source ip address
        } ## end of checking for router loop
    } ## end of main for loop
} ## end of subroutine path

# jrl appened library_interface_addresses.pm
########################################################################################################################################
#Library File      : library_interface_addresses.pm 
#Functionality     : Provides subroutines to get the neighboring endpoints of a link, given the endpoints 
#                    in the traceroute form. 
#Developer         : Neena Kaushik (PhD Candidate, Santa Clara University), Summer Intern, ESnet.
#Supervisor        : Chin Guok, ESnet.
#######################################################################################################################################

################################################## Start of subroutines ##################################################################
#The following sub routines are present in this library file
#
# sub get_interface_endpoints($traceroute_path, $topo_doc, $debug)
# - Accepts a one-hop traceroute_path with a comma delimiter, and returns the output one hop path with a comma delimiter,
#   containing the direct inner endpoints of the link. 
#
# sub get_routers_on_interface($endpoint_ip_address, $topo_doc, $debug)
# - Gives the router which contains this interface, given the interface ip addresses. For a point-to-point link, only 
#   1 router will be returned, for ethernet transit links, more than one routers could be
#   returned, each separated by a comma. 
#
# sub get_interface_on_source_router($source_router, $dest_router, $dest_interface1, $topo_doc, $debug) 
# - Gets the ip interface on the source router which directly connects with the dest_interface on the destination 
#   router and returns the path as source interface ip address, dest interface ip address. 
#
# sub get_other_end_of_transit_link($dest_router, $dest_interface, $topo_doc, $debug)
# - This subroutine returns useful information only for transit link types. For all other link types, it returns a -1.
#   If there are two ends of the two transit links present
#   on the dest router, then the inner endpoints can be directly obtained from here.
#
#########################################################################################################################################

##################################################################################
#sub get_interface_endpoints - Given the endpoints in traceroute form with a comma 
#                              delimiter, this function returns the endpoints of 
#                              the two ends of the link with a comma delimiter 
##################################################################################
sub get_interface_endpoints($$$)
{
    my($input_path, $topo_doc, $debug) = @_;
    my $output_path;
    my $router1_attached;
    my $router2_attached;
    my $link;

    my @end_point = split(/,/, $input_path);  ## Get the two endpoints of the path in traceroute format
    if ($debug)
    {
        print "\n ++++++++++++++++++++ The endpoints are $end_point[0] and $end_point[1] ++++++++++++++++++++++++++++++++";
    }

    $router1_attached = &get_routers_on_interface($end_point[0], $topo_doc, $debug); ## Get routers associated with endpoint 0
    if ($debug)
    {
        print "\n routers attached to $end_point[0] are $router1_attached";
    }

    $router2_attached = &get_routers_on_interface($end_point[1], $topo_doc, $debug);
    if ($debug)
    {
        print "\n routers attached to $end_point[1] are $router2_attached";
    }
   
    ## Note that $link is relevant if $dest_interface_type is of type transit
    $link = get_other_end_of_transit_link($router2_attached, $end_point[1], $topo_doc, $debug); ## Get routers associated with endpoint 1

    if ($debug)
    {
        print "\n ------------- End of get_other_end_of_transit_link --------------";
    }

    if ($link eq -1) 
    {
        $output_path = &get_interface_on_source_router($router1_attached, $router2_attached, $end_point[1], $topo_doc, $debug); 
        if ($debug)
        {
            print "\n %%%%%%%%%%%%%%%%%%%% The output_path is $output_path %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
        }
        return $output_path;
    }
    else ## For links in which dest_interface is of type transit
    {
        if ($debug)
        {
            print "\n %%%%%%%%%%%%%%%%%%%% The output_path is $link %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%";
        }
        return $link;
    }
}
################################################################################################
#sub get_other_end_of_transit_link - Get the other end of the ip interface address for the transit link. 
#
################################################################################################
sub get_other_end_of_transit_link($$$$)
{
    my ($routers_attached, $dest_interface, $topo_doc, $debug) = @_;
    my @router = split(/,/ , $routers_attached);
    my $link = -1; ## Initialized to -1
  
    my $length = @router;

    if ($debug)
    {
        print "\n --------------In get_other_end_of_transit_link router[0] are $router[0] and dest_interface is $dest_interface   ------------------";
    }

    my $nodes = $topo_doc->getElementsByTagName("ospf-database");
    my $n = $nodes->getLength;
    
    for (my $i = 0; $i < $length; $i++) ## Iterate over all the dest routers associated with the dest_interface 
    {
      for(my $j = 0; $j < $n; $j++) ## Iterate over all the links on a particular router to find a match
      {
        my $node = $nodes->item($j);
        my $lsa_type = $node->getElementsByTagName("lsa-type")->item(0)->toString;
        my $lsa_id = $node->getElementsByTagName("lsa-id")->item(0)->toString;
        $lsa_id = &remove_tag($lsa_id, $debug);
        $lsa_type = &remove_tag($lsa_type, $debug);

        if ($debug)
        {
            print "\n lsa_id is $lsa_id and lsa_type is $lsa_type";
        }
        
        if ( ($lsa_type =~ /Router/) && ($lsa_id =~ /$router[$i]/) )
        {
            my $router_links = $node->getElementsByTagName("ospf-link");
            my $l = $router_links->getLength;
            for (my $j = 0; $j < $l; $j++)
            {
                my $router_link = $router_links->item($j);
                my $link_id = $router_link->getElementsByTagName("link-id")->item(0)->toString;
                my $link_data = $router_link->getElementsByTagName("link-data")->item(0)->toString;
                my $link_type = $router_link->getElementsByTagName("link-type-name")->item(0)->toString;
                $link_id = &remove_tag($link_id, $debug);
                $link_data = &remove_tag($link_data, $debug);
                $link_type = &remove_tag($link_type, $debug);

                if ($debug)
                {
                    print "\n link_id is $link_id, link_data is $link_data, and link_type is $link_type";
                }

                if ( ($link_id =~ /$dest_interface/) && ($link_data =~ /$dest_interface/) )
                {
                    if ($debug)
                    {
                        print "\n returning a -1";
                    }
                    return -1;
                }
                if ( ($link_id =~ /$dest_interface/) &&  ($link_type =~ /Transit/) )
                {
                    $link =  $link_data . "," . $dest_interface;
                    if ($debug)
                    {
                        print "\n link_id is $link_id, link is $link";
                    }
                    return $link;
                }
                if ( ($link_data =~ /$dest_interface/) && ($link_type =~ /Transit/) )
                {
                    $link = $link_id . "," . $dest_interface;
                    if ($debug)
                    {
                        print "\n link_id is $link_id, link is $link";
                    }
                    return $link;
                }
 
            }
        }
      }
    }
    if ($debug)
    {
        print "\n link is $link";
    }
    return $link;
}
#####################################################################################
# sub get_routers_on_interface -  Returns the loopback address of the router which
#                                 contains the ip address passed as one of its links. 
#                                 For point-to-point links, only 1 router is returned,
#                                 for transit links, more than router can be returned. 
#####################################################################################
sub get_routers_on_interface($$$)
{
    my($end_point, $topo_doc, $debug) = @_;

    #my @temp = split(" ", $end_point);
    #$end_point = $temp[0];

    my $routers_attached = "";

    my $nodes = $topo_doc->getElementsByTagName("ospf-database");
    my $n = $nodes->getLength;
   
    if ($debug)
    {
        print "\n The endpoint is $end_point";
    }

    for (my $i = 0; $i < $n; $i++)
    {
        my $node = $nodes->item($i);
        my $lsa_type = $node->getElementsByTagName("lsa-type")->item(0)->toString;
        my $lsa_id = $node->getElementsByTagName("lsa-id")->item(0)->toString;
        $lsa_id = &remove_tag($lsa_id, $debug);
        $lsa_type = &remove_tag($lsa_type, $debug);

        if ($lsa_type =~ /Router/)
        {
            if ($debug)
            {
                print "\n The router lsa-id is $lsa_id";
            }
            my $router_links = $node->getElementsByTagName("ospf-link");
            my $l = $router_links->getLength;
            for (my $j = 0; $j < $l; $j++)
            {
                my $router_link = $router_links->item($j);
                my $link_id = $router_link->getElementsByTagName("link-id")->item(0)->toString;
                my $link_data = $router_link->getElementsByTagName("link-data")->item(0)->toString;
                $link_id = &remove_tag($link_id, $debug);
                $link_data = &remove_tag($link_data, $debug);

                if ($debug)
                {
                    print "\n The link_id is $link_id and link_data is $link_data";
                }

                if ($lsa_id eq $end_point)
                {
                    $routers_attached = $lsa_id;
                    return $routers_attached;
                }

                if (($link_id eq $end_point) || ($link_data eq $end_point))
                {
                    if ($routers_attached eq "")
                    {
                        $routers_attached = $lsa_id;
                    }
                    else
                    {
                        $routers_attached = $routers_attached . "," . $lsa_id;
                    }

                }
            }
        }
    }

    if ($debug)
    {
        print "\n**** Value returned is $routers_attached ****";
    }
 
    return $routers_attached;
}
##############################################################################################
# sub get_interface_on_source_router - Given the source router, dest router and dest interface, 
#                                      return the interface on source router which directly
#                                      connects to the destination 
##############################################################################################
sub get_interface_on_source_router($$$$$)
{
    my($router1_attached, $router2_attached, $d_interface, $topo_doc, $debug) = @_;
    my $output_path = "";
    my $s_interface = "";
    my $lsa_type = "";
    my $lsa_id = "";
    my $link_id = "";
    my @d_temp;
    my @s_temp;

    my @routers_s = split(/,/, $router1_attached);
    if (@routers_s == 0)
    {
        $routers_s[0] = -1;
        $routers_s[1] = -1;
    }
    if (@routers_s == 1)
    {
        $routers_s[1] = -1;
    }

    my @routers_d = split(/,/, $router2_attached);
    if (@routers_d == 0)
    {
        $routers_d[0] = -1;
        $routers_d[1] = -1;
    }
    if (@routers_d == 1)
    {
        $routers_d[1] = -1;
    }

    my $nodes = $topo_doc->getElementsByTagName("ospf-database");
    my $n = $nodes->getLength;

    if ($debug)
    {
        print "\n The value of routers_s[0] and routers_s[1] is $routers_s[0] and $routers_s[1]";
        print "\n The value of routers_d[0] and routers_d[1] is $routers_d[0] and $routers_d[1]";
        print "\n The value of d_interface $d_interface";
    }

    for (my $i = 0; $i < $n; $i++)
    {
        my $node = $nodes->item($i);
        $lsa_type = $node->getElementsByTagName("lsa-type")->item(0)->toString;
        $lsa_id = $node->getElementsByTagName("lsa-id")->item(0)->toString;
        $lsa_id = &remove_tag($lsa_id, $debug);
        $lsa_type = &remove_tag($lsa_type, $debug);

        if($lsa_type =~ /Network/)
        {
            next;
        }

        if(( $lsa_id eq $routers_s[0]) || ($lsa_id eq $routers_s[1]) ) 
        {
            my $router_links = $node->getElementsByTagName("ospf-link");
            my $l = $router_links->getLength;
            for (my $j = 0; $j < $l; $j++)
            {
                my $router_link = $router_links->item($j);
                my $link_id = $router_link->getElementsByTagName("link-id")->item(0)->toString;
                my $link_data = $router_link->getElementsByTagName("link-data")->item(0)->toString;
                my $link_type = $router_link->getElementsByTagName("link-type-name")->item(0)->toString;
                $link_id = &remove_tag($link_id, $debug);
                $link_data = &remove_tag($link_data, $debug);
                $link_type = &remove_tag($link_type, $debug);

                if ($debug)
                {
                    print "\n +++++++++++++++++++++++++++++++++++++++++++++++++++++++";
                    print "\n lsa_id is $lsa_id";
                    print "\n link_id is $link_id";
                    print "\n link_data is $link_data";
                    print "\n routers_s[0] is $routers_s[0]";
                    print "\n routers_s[1] is $routers_s[1]";
                    print "\n routers_d[0] is $routers_d[0]";
                    print "\n routers_d[1] is $routers_d[1]";
                    print "\n +++++++++++++++++++++++++++++++++++++++++++++++++++++++";
                }

                if ($link_type =~ /Stub/) {} 
                else
                {
                    if ( ($link_id eq $routers_d[0]) || ($link_id eq $routers_d[1])|| ($link_id eq $d_interface))
                    {
                        $s_interface = $link_data; 
                        @d_temp = split ("[\.]", $d_interface);
                        @s_temp = split ("[\.]", $s_interface);


                        if (($link_type =~ /PointToPoint/) && ($s_temp[0] == $d_temp[0]) && ($s_temp[1] == $d_temp[1]) && ($s_temp[2] == $d_temp[2]) )
                        {
                            $output_path = $s_interface . "," . $d_interface;
                            return $output_path;
                        }
                        else
                        {    if ($link_type =~ /Transit/)
                             {
                                $output_path = $s_interface . "," . $d_interface;
                                return $output_path;
                             }
                        }

                    }
                }
                if ($link_type =~ /Stub/) {}
                else
                {
                   if ( ($link_data eq $routers_d[0]) ||  ($link_data eq $routers_d[1]) || ($link_data eq $d_interface) )
                   {
                        $s_interface = $link_id; 
                        @d_temp = split ("[\.]", $d_interface);
                        @s_temp = split ("[\.]", $s_interface);

                        if ( ($link_type =~ /PointToPoint/) && ($s_temp[0] == $d_temp[0]) && ($s_temp[1] == $d_temp[1]) && ($s_temp[2] == $d_temp[2]) )
                        {
                            $output_path = $s_interface . "," . $d_interface;
                            return $output_path;
                        }
                        else
                        {    if ($link_type =~ /Transit/)
                             {
                                $output_path = $s_interface . "," . $d_interface;
                                return $output_path;
                             }
                        }
                   }
                }
            }
       }
   }
   return $output_path;
}

# jrl append library_modify_file.pm

#########################################################################################################################################
#File              : library_modify_file.pm
#Functionality     : Provides subroutines to create a new file with latency data in both directions. 
#Developer         : Neena Kaushik (PhD Candidate, Santa Clara University), Summer Intern, ESnet.
#Supervisor        : Chin Guok, ESnet.
########################################################################################################################################

################################################### Start of subroutines #################################################################
#The following sub routines are present in this file
#
# sub create_file ($input_file, $output_file, $topo_file)
# - Creates a new output latency file given the input latency file and the topology file 
#   The latencies are present in both directions with each link being denoted by inner endpoints 
#########################################################################################################################################

#use library_interface_addresses; ## For getting the link in the inner endpoint format

##############################################################################
#sub create_file - This subroutine creates an output latency file, given 
#                  the input latency file and the topology file. The output
#                  latency file contains latency in both directions. The 
#                  input latency file contains latency only in a single
#                  direction with the link endpoints appearing in the traceroute 
#                  format.
##############################################################################
sub create_file($$$$)
{
    my ($self, $lt_doc, $output_file, $topo_doc, $debug) = @_;

    my $meta_nodes = $lt_doc->getElementsByTagName("nmwg:metadata");
    my $data_nodes = $lt_doc->getElementsByTagName("nmwg:data");

    if ($meta_nodes->getLength != $data_nodes->getLength)
    {
        print "\n The input latency file does not have the correct format";
        return 0;
    }

    my $length = $meta_nodes->getLength; ## Since data_nodes and meta_nodes have the same length
    my $output = new IO::File(">$output_file");
    my $writer = new XML::Writer( OUTPUT => $output );
    
    $writer->xmlDecl('UTF-8');
    $writer->doctype('xml');
    $writer->startTag('xml');
    $writer->startTag('nmwg:message', 'type' => 'InterfaceLatencies', 'xmlns:nmwg' => 'http://ggf.org/ns/nmwg/2.0/',
                                                                      'xmlns:nmwgt' => 'http://ggf.org/ns/nmwg/topology/2.0/',
                                                                      'xmlns:ping' => 'http://ggf.org/ns/nmwg/tools/ping/2.0/');

    for (my $i = 0; $i < $length; $i++)
    {
        my $meta_node = $meta_nodes->item($i);
        my $src = $meta_node->getElementsByTagName("nmwgt:src")->item(0)->getAttributeNode("value")->getValue;
        my $dst = $meta_node->getElementsByTagName("nmwgt:dst")->item(0)->getAttributeNode("value")->getValue;
        my $data_node = $data_nodes->item($i);
        my $latency = $data_node->getElementsByTagName("ping:datum")->item(0)->getAttributeNode("value")->getValue;
        my $time = $data_node->getElementsByTagName("nmwg:commonTime")->item(0)->getAttributeNode("value")->getValue;


        ## Start - Convert source and destination from traceroute format to the link in inner endpoint format

        my $in_path = $src . "," . $dst;
        my $out_path = &get_interface_endpoints($in_path, $topo_doc, $debug);

        my @end_points = split(/,/, $out_path);

        if (@end_points == 2)
        {
            $src = $end_points[0];
            $dst = $end_points[1];
        }
        else
        {
            $src = "undefined";
            $dst = "undefined";
        }

        ## End - Convert source and destination from traceroute format to the link in inner endpoint format


        my $ep1 = $src; 
        my $ep2 = $dst;
        my $num = 2*$i;
        my $metaid = "meta" . $num;
        my $dataid = "data" . $num;

        ## Start of the metadata part in forward direction

        $writer->startTag('nmwg:metadata', 'id' => $metaid);
        $writer->startTag('ping:subject');
        $writer->startTag('nmwgt:endPointPair');
        $writer->startTag('nmwgt:src', 'type' => 'ipv4', 'value' => $ep1);
        $writer->endTag();
        $writer->startTag('nmwgt:dst', 'type' => 'ipv4', 'value' => $ep2);
        $writer->endTag();
        $writer->endTag();
        $writer->endTag();
        $writer->dataElement("eventType", "ping_derived");
        $writer->endTag();

        ## End of the metadata part in forward direction


        ## Start of the data part in forward direction

        $writer->startTag('nmwg:data', 'id' => $dataid, 'metadataIdRef' => $metaid);
        $writer->startTag('nmwg:commonTime', 'type' => 'unix', 'value' => $time);
        $writer->startTag('ping:datum', 'numBytes' => '64', 'numBytesUnits' => 'bytes', 'valueUnits' => 'ms', 'value' => $latency);
        $writer->endTag;
        $writer->endTag;
        $writer->endTag;

        ## End of the data part in forward direction

        $ep1 = $dst; 
        $ep2 = $src;
        $num = 2*$i + 1;
        $metaid = "meta" . $num;
        $dataid = "data" . $num;

        ## Start of the metadata part in reverse direction

        $writer->startTag('nmwg:metadata', 'id' => $metaid);
        $writer->startTag('ping:subject');
        $writer->startTag('nmwgt:endPointPair');
        $writer->startTag('nmwgt:src', 'type' => 'ipv4', 'value' => $ep1);
        $writer->endTag();
        $writer->startTag('nmwgt:dst', 'type' => 'ipv4', 'value' => $ep2);
        $writer->endTag();
        $writer->endTag();
        $writer->endTag();
        $writer->dataElement( "eventType", "ping_derived" );
        $writer->endTag();

        ## End of the metadata part in reverse direction


        ## Start of the data part in reverse direction

        $writer->startTag('nmwg:data', 'id' => $dataid, 'metadataIdRef' => $metaid);
        $writer->startTag('nmwg:commonTime', 'type' => 'unix', 'value' => $time);
        $writer->startTag('ping:datum', 'numBytes' => '64', 'numBytesUnits' => 'bytes', 'valueUnits' => 'ms', 'value' => $latency);
        $writer->endTag;
        $writer->endTag;
        $writer->endTag;

        ## End of the data part in reverse direction
    }
    $writer->endTag();
    $writer->endTag();
    $writer->end();
}
1;
