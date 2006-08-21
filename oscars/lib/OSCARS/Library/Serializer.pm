# =============================================================================
package OSCARS::Library::Serializer;

##############################################################################
# Copyright (c) 2006, The Regents of the University of California, through
# Lawrence Berkeley National Laboratory (subject to receipt of any required
# approvals from the U.S. Dept. of Energy). All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
#
# (1) Redistributions of source code must retain the above copyright notice,
#     this list of conditions and the following disclaimer.
#
# (2) Redistributions in binary form must reproduce the above copyright
#     notice, this list of conditions and the following disclaimer in the
#     documentation and/or other materials provided with the distribution.
#
# (3) Neither the name of the University of California, Lawrence Berkeley
#     National Laboratory, U.S. Dept. of Energy nor the names of its
#     contributors may be used to endorse or promote products derived from
#     this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
# SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
# INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
# CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
# ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
# POSSIBILITY OF SUCH DAMAGE.

# You are under no obligation whatsoever to provide any bug fixes, patches,
# or upgrades to the features, functionality or performance of the source
# code ("Enhancements") to anyone; however, if you choose to make your
# Enhancements available either publicly, or directly to Lawrence Berkeley
# National Laboratory, without imposing a separate written license agreement
# for such Enhancements, then you hereby grant the following license: a
# non-exclusive, royalty-free perpetual license to install, use, modify,
# prepare derivative works, incorporate into other computer software,
# distribute, and sublicense such enhancements or derivative works thereof,
# in binary and source code form.
##############################################################################

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
    # not currently implemented, and may not be
    elsif ($payloadContentType eq "listReservations") {
        return undef;		       
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


######
1;
