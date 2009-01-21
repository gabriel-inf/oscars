package net.es.oscars.bss.topology;

import java.util.Properties;
import net.es.oscars.PropHandler;

// TODO:  perhaps not static
public class L2SwitchingCapType {
    public static String DEFAULT_SWCAP_TYPE;
    public static String DEFAULT_ENC_TYPE;
    final public static int DEFAULT_MTU = 9000;
    
    // never construct this object
    private L2SwitchingCapType() {
    }

    /** Initializes global variables */
    public static void initGlobals() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties topoProps = propHandler.getPropertyGroup("topo", true);
        DEFAULT_SWCAP_TYPE = topoProps.getProperty("defaultSwcapType");
        if(DEFAULT_SWCAP_TYPE == null){
            DEFAULT_SWCAP_TYPE = "tdm";
        }
        DEFAULT_ENC_TYPE = topoProps.getProperty("defaultEncodingType");
        if(DEFAULT_ENC_TYPE == null){
            DEFAULT_ENC_TYPE = "sdh/sonet";
        }
    }

}
