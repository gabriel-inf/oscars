package net.es.oscars.utils.validator.wrappers.net.es.oscars.api.soap.gen.v06;


import net.es.oscars.api.soap.gen.v06.Layer3Info;

public class Layer3InfoValidator {
    /**
     * Validate the content of a Layer3Info object. Note that object will never be null.
     * @param obj to validate
     * @throws RuntimeException
     */
    public static void validator (Layer3Info obj) throws RuntimeException {
        
        if ((obj.getDestHost() == null) || ("".equals(obj.getDestHost().trim()))) {
            throw new RuntimeException ("destination host is null");
        }

        if ((obj.getSrcHost() == null) ||("".equals(obj.getSrcHost().trim()))) {
            throw new RuntimeException ("source host is null");
        }
    }
}
