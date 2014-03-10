package net.es.oscars.nsibridge.prov;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class STPUtil {
    private static final Logger log = Logger.getLogger(STPUtil.class);

    public static String getVlanRange(String stp) throws TranslationException {
        checkStp(stp);
        String[] stpParts = StringUtils.split(stp, "?");
        if (stpParts.length <= 2) {
            throw new TranslationException("no labels (VLAN or otherwise) for stp "+stp);
        }
        boolean foundVlan = false;
        String vlanRange = "";
        for (int i = 1; i <= stpParts.length; i++) {
            String labelAndValue = stpParts[i];
            String[] lvParts = StringUtils.split(labelAndValue, "=");
            if (lvParts == null || lvParts.length == 0) {
                log.info("empty label-and-value part");
            } else if (lvParts.length == 1) {
                log.info("just a label: "+lvParts[0]);
            } else if (lvParts.length > 2) {
                log.info("label-and-value part has more than one '=' chars: ["+labelAndValue+"]");
            } else {
                // lvParts.length == 2
                String label = lvParts[0];
                String value = lvParts[1];
                if (label.equals("vlan")) {
                    vlanRange = value;
                    foundVlan = true;
                } else {
                    log.info("label-and-value: "+lvParts[0]+" = "+lvParts[1]);
                }
            }
        }
        if (!foundVlan) {
            throw new TranslationException("could not locate VLAN range for STP "+stp);
        }
        return vlanRange;


    }

    public static String getNetworkId(String stp) throws TranslationException {
        String urn = getUrn(stp);
        String[] urnParts = StringUtils.split(urn, ":");
        if (urnParts.length <= 4) {
            throw new TranslationException("unable to determine networkId for STP: "+stp);
        }
        // urn:ogf:network:NETWORK_ID:LOCAL_ID?label=value
        return urnParts[4];
    }

    public static String getLocalId(String stp) throws TranslationException {
        String urn = getUrn(stp);

        String[] urnParts = StringUtils.split(urn, ":");
        if (urnParts.length <= 5) {
            throw new TranslationException("unable to determine local id for STP: "+stp);
        }
        // urn:ogf:network:NETWORK_ID:LOCAL_ID?label=value
        return urnParts[5];
    }

    public static String getUrn(String stp) throws TranslationException {
        checkStp(stp);
        String[] parts = StringUtils.split(stp, "?");
        return parts[0];
    }


    public static void checkStp(String stp) throws TranslationException {
        if (stp == null) {
            log.error("null STP");
            throw new TranslationException("null STP");
        } else if (stp.length() == 0) {
            log.error("empty STP");
            throw new TranslationException("empty STP string");

        }
        if (!stp.startsWith("urn:ogf:network:")) {
            throw new TranslationException("STP does not start with 'urn:ogf:network:' :"+stp);
        }

    }

}
