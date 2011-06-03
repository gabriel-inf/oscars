package net.es.oscars.utils.validator.wrappers.net.es.oscars.api.soap.gen.v06;

import net.es.oscars.utils.validator.DataValidator;
import net.es.oscars.utils.validator.EndpointValidator;

import net.es.oscars.api.soap.gen.v06.Layer2Info;

public class Layer2InfoValidator {
    /**
     * Validate the content of a Layer2Info object. Note that object will never be null.
     * @param obj to validate
     * @throws RuntimeException
     */
    public static void validator (Layer2Info obj) throws RuntimeException {

        if ((obj.getDestEndpoint() == null) || (obj.getDestEndpoint() == "")) {
            throw new RuntimeException ("destination endpoint is null");
        }
        if ((obj.getSrcEndpoint() == null) || (obj.getSrcEndpoint() == "")) {
            throw new RuntimeException ("source endpoint is null");
        }
        if(!obj.getSrcEndpoint().startsWith("urn:ogf:network")){
            obj.setSrcEndpoint(EndpointValidator.lookupHostUrn(obj.getSrcEndpoint()));
        }
        if(!obj.getDestEndpoint().startsWith("urn:ogf:network")){
            obj.setDestEndpoint(EndpointValidator.lookupHostUrn(obj.getDestEndpoint()));
        }

        DataValidator.validate (obj.getDestVtag(), false);
        DataValidator.validate (obj.getSrcVtag(), false);

    }
}
