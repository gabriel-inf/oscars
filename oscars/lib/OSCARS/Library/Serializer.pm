# =============================================================================
package OSCARS::Library::Serializer;

=head1 NAME

OSCARS::Library::Serializer - Serializes the return values to match XML schema

=head1 SYNOPSIS

  use OSCARS::Library::Serializer;

=head1 DESCRIPTION

Serializes the return values supplied in hashes to match the XML schema.
Called from oscars just before the return to the user.

=head1 AUTHORS

Mary Thompson (mrthompson@lbl.gov

=head1 LAST MODIFIED

July 20, 2006

=cut

use strict;
use Data::Dumper;
use WSRF::Lite;


###############################################################################
#
sub serializeForwardReply {
    print STDERR "in serializeForwardReply\n";
    my $payloadContentType = shift;
    my $values = shift;
    print STDERR Dumper($values);
    print STDERR "payLoadContentType is $payloadContentType\n";
    my $emMsg;

    # return value is just a status
    if ($payloadContentType eq "cancelReservation") {
        my $status = $values->{status};
	$status =~ tr/a-z/A-Z/;
        my $response = SOAP::Data->name(testForwardResponse=>
	    SOAP::Data->value(
 		SOAP::Data->name('contentType')
			  ->value($payloadContentType)
		          ->attr({'xmlns:tns' =>
				'http://oscars.es.net/OSCARS/Dispatcher'})
			  ->type('tns:payloadContentType'),
		SOAP::Data->name($payloadContentType)
	                 ->value($status)
    	  	 	  ->attr({'xmlns:tns' =>
				     'http://oscars.es.net/OSCARS/Dispatcher'})
			  ->type('tns:resStatus')));
        return $response;
    }
    # an array of resInfo structures may is returned
    elsif ($payloadContentType eq "listReservations") {
        my @emMsg = serializeListResReply($values);
        my $response= SOAP::Data->name(testForwardResponse=>
	      SOAP::Data->value( 
		SOAP::Data->name('contentType')
			  ->value($payloadContentType)
		          ->attr({'xmlns:tns' =>
				'http://oscars.es.net/OSCARS/Dispatcher'})
			  ->type('tns:payloadContentType'),
 		SOAP::Data->name($payloadContentType => 
		      \SOAP::Data->value(@emMsg))));
        return $response;		       
    }
    # a resDetails structure is returned
    elsif ( $payloadContentType eq "queryReservation") { 
        $emMsg = serializeQueryResReply($values);
    }  
    # tag and status are returned
    elsif ($payloadContentType eq "createReservation") { 
        $emMsg = serializeCreateResReply($values);
    }
    my $response = SOAP::Data->name(testForwardResponse =>
	    SOAP::Data->value(
               SOAP::Data->name('contentType')
			  ->value($payloadContentType)
		          ->attr({'xmlns:tns' =>
				'http://oscars.es.net/OSCARS/Dispatcher'})
			  ->type('tns:payloadContentType'),
	       SOAP::Data->name($payloadContentType =>$emMsg) ));
    return $response;
} #____________________________________________________________________________


###############################################################################
#
sub serializeCreateResReply {
    my $results = shift;
    my $status = $results->{status};
    $status =~ tr/a-z/A-Z/;
    print STDERR "in serializeCreateResReply results are \n",Dumper ($results), "\n";
    my $emMsg = SOAP::Data->name(createReservation =>
    	  	  \SOAP::Data->value(
    	  	 	SOAP::Data->name('tag')
    	  	 	   -> value($results->{tag})
    	  	 	   ->attr({'xmlns:xsd' =>
				     'http://www.w3.org/2001/XMLSchema'})
    	  	 	      -> type('xsd:string'),   #otherwise it defaults to "int"
    	  	 	SOAP::Data->name('status')
    	  	 	     ->value($status)
    	  	 	     ->attr({'xmlns:tns' =>
				     'http://oscars.es.net/OSCARS/Dispatcher'})
			    ->type('tns:resStatus')));
     
    return $emMsg;
} #____________________________________________________________________________

    
##############################################################################
#  Need to deal with the optional arguments 
#
sub serializeQueryResReply{
    my  $results = shift;
        
    if (!defined($results)) {
        my $emMsg = SOAP::Data->name(queryReservation => $results);
	return $emMsg;
    }	
    my $protocol = $results->{protocol};
    $protocol =~ tr/a-z/A-Z/;
    my $status = $results->{status};	
    $status =~ tr/a-z/A-Z/;
    my $emMsg = SOAP::Data->name(queryReservation =>
		\SOAP::Data->value(		   
                SOAP::Data->name(tag => $results->{tag}),
    		SOAP::Data->name('status')
			->value($status)
	  		->attr({'xmlns:tns' =>
		  		'http://oscars.es.net/OSCARS/Dispatcher'})
	    		->type('tns:resStatus'),
    		SOAP::Data->name(srcHost => $results->{srcHost}),
    		SOAP::Data->name(destHost => $results->{destHost}),
    		SOAP::Data->name(startTime => $results->{startTime}),
    		SOAP::Data->name(endTime =>  $results->{endTime}),
    		SOAP::Data->name(origTimeZone => $results->{origTimeZone}),
    		SOAP::Data->name(createTime => $results->{createTime}),
     		SOAP::Data->name(bandwidth => $results->{bandwidth}),
     		SOAP::Data->name(burstLimit => $results->{burstLimit}),
     		SOAP::Data->name(resClass => $results->{resClass}),
     		SOAP::Data->name(path => $results->{path}),
     		SOAP::Data->name(description => $results->{description}),
     		SOAP::Data->name('protocol')
	 		-> value( $protocol)
	 		->attr({'xmlns:tns' =>
		   		'http://oscars.es.net/OSCARS/Dispatcher'})
	 		->type('tns:resProtocolType')));
    return $emMsg;
} #____________________________________________________________________________


##############################################################################
#
sub serializeResInfo{
    my $results = shift;
    my $status = $results->{status};
    $status =~ tr/a-z/A-Z/;

    my $resInfo =
       SOAP::Data->name(resInfo =>
	   \SOAP::Data->value(
   	       SOAP::Data->name(tag => $results->{tag}),
 	       SOAP::Data->name('status')
			      ->value($status)
			      ->attr({'xmlns:tns' =>
				        'http://oscars.es.net/OSCARS/Dispatcher'})
			      ->type('tns:resStatus'),  
		SOAP::Data->name(srcHost => $results->{srcHost}),
		SOAP::Data->name(destHost => $results->{destHost}), 
		SOAP::Data->name(startTime => $results-> {startTime}),
		SOAP::Data->name(endTime =>  $results->{endTime})));
    return $resInfo;
} #____________________________________________________________________________


##############################################################################
#
sub serializeListResReply{
    # @_ is an array of resInfo hashes
    my $values = shift;
    print STDERR "values is a reference to an ref($values) \n";
    my @resInfo;
    my $i;
    my $len = @{$values} + 0;
    print STDERR "in serializeListResReply len is $len\n";
    for ( $i = 0; $i < $len ; $i++ ) {
        print STDERR "calling serializeResInfo\n";
        my $resInfoItem = $values->[$i];
        print STDERR "item ", Dumper($resInfoItem), "\n";
        push @resInfo, serializeResInfo ($resInfoItem);
    }
    return @resInfo;
} #____________________________________________________________________________


######
1;
