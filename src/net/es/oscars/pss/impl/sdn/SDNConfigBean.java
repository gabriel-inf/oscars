package net.es.oscars.pss.impl.sdn;

import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;

public class SDNConfigBean {
    private String mplsMethod   = null;



    public static SDNConfigBean loadConfig(String propertyFile, String propertyGroup) throws PSSException {
        SDNConfigBean config = new SDNConfigBean();
        
        PropHandler propHandler = new PropHandler(propertyFile);
        Properties props = propHandler.getPropertyGroup(propertyGroup, true);
        if (props == null) {
            throw new PSSException("No PSS config");
        }
        String mplsMethodProp        = (String) props.get("mplsMethod");
        if (mplsMethodProp == null)  mplsMethodProp = "L2VPN";
        
        mplsMethodProp  = mplsMethodProp.trim().toUpperCase();

        if (mplsMethodProp.equals("L2VPN")) {
            config.setMplsMethod("L2VPN");
        } else {
            config.setMplsMethod("EOMPLS");
        }
        

        return config;
    }



    public void setMplsMethod(String mplsMethod) {
        this.mplsMethod = mplsMethod;
    }



    public String getMplsMethod() {
        return mplsMethod;
    }


    
}
