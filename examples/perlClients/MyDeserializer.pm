package MyDeserializer;

use WSRF::Lite;
@MyDeserializer::ISA = 'WSRF::Deserializer';

sub typecast {

    my ($self, $val, $name, $attrs, $kids, $type ) = @_;
    #if ($type) { print "in typecast type is ", $type; }
    if ($type)  {
       return $val if  $type =~ /enumType/;
    	return $val if $type =~ /resProtocolType/;
    	return $val if $type =~ /createRouteDirectionType/;
   	return $val if $type =~ /resStatus/;  	
	return $val if $type =~ /payloadContentType/;
    }
    return undef;
};

# the following style does not work with qualified names since the
# types are {https://oscars.es.net/OSCARS/Dispatcher}resProtocolType 

sub as_resProtocolType {
	 my($self, $value, $name, $type, $attr) = @_;
 	 return $value;
};




