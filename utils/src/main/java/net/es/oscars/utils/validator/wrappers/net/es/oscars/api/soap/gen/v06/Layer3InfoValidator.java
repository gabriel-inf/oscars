package net.es.oscars.utils.validator.wrappers.net.es.oscars.api.soap.gen.v06;


import net.es.oscars.api.soap.gen.v06.Layer3Info;

public class Layer3InfoValidator {
    /**
     * Validate the content of a Layer3Info object. Note that object will never be null.
     * @param obj to validate
     * @throws RuntimeException
     */
    public static void validator (Layer3Info obj) throws RuntimeException {
        
        if ((obj.getDestHost() == null) || (obj.getDestHost() == "")) {
            throw new RuntimeException ("destination host is null");
        }
        if (obj.getDestIpPort() == 0) {
            throw new RuntimeException ("destination IP is null");
        }
        if ((obj.getDscp() == null) || (obj.getDscp() == "")) {
            throw new RuntimeException ("getDscp returns null");
        }
        if ((obj.getProtocol() == null) || (obj.getProtocol() == "")) {
            throw new RuntimeException ("protocol is null");
        }
        if ((obj.getSrcHost() == null) || (obj.getSrcHost() == "")) {
            throw new RuntimeException ("source host is null");
        }
        if (obj.getSrcIpPort() == 0) {
            throw new RuntimeException ("source IP is null");
        }
    }
}
