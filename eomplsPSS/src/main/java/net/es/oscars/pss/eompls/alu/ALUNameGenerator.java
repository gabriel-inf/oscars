package net.es.oscars.pss.eompls.alu;


import net.es.oscars.pss.beans.PSSException;
import org.apache.commons.lang.StringUtils;

public class ALUNameGenerator {
    private static ALUNameGenerator instance;
    private ALUNameGenerator() {
    }

    public static ALUNameGenerator getInstance() {
        if (instance == null) {
            instance = new ALUNameGenerator();
        }
        return instance;
    }
    public String getLSPName(String gri)  throws PSSException {
        return getName(gri, "_lsp", 28);
    }

    public String getPathName(String gri)  throws PSSException {
        return getName(gri, "_path", 28);
    }

    public String getIfceName(String gri)  throws PSSException {
        return getName(gri, "_ifce", 28);
    }

    public String getName(String gri, String postfix, int maxLength) throws PSSException {
        String pathName = gri+postfix;
        if (pathName.length() > maxLength) {
            String[] gri_parts = StringUtils.split(gri, "-");
            String number = gri_parts[1];
            String domain = gri_parts[0];
            String[] domparts = StringUtils.split(domain, ".");
            for (int i = 0; i < domparts.length; i++) {
                char firstChar = domparts[i].toCharArray()[0];
                domparts[i] = firstChar+"";
            }
            pathName = StringUtils.join(domparts, ".")+"-"+number;
        }

        if (pathName.length() > maxLength) {
            throw new PSSException("Could not generate name!");
        }
        return pathName;
    }

    public String numbers(String gri) {

        String circuitStr = gri;
        circuitStr = circuitStr.replaceAll("[^0-9]+", "");
        
        return circuitStr;
    }



    /**
     * @deprecated
     * @param gri
     * @return
     */

    public String getEpipeId(String gri) {
        return numbers(gri);
    }

    /**
     * @deprecated
     * @param gri
     * @return
     */

    public String getSdpId(String gri) {
        return numbers(gri);
    }

    /**
     * @deprecated
     * @param gri
     * @return
     */

    public String getQosId(String gri) {
        return numbers(gri);
    }

}
