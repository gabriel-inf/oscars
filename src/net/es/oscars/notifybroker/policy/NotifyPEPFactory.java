package net.es.oscars.notifybroker.policy;

import java.util.*;
import net.es.oscars.PropHandler;

public class NotifyPEPFactory{
    public static ArrayList<NotifyPEP> createPEPs(String dbname){
        ArrayList<NotifyPEP> peps = new ArrayList<NotifyPEP>();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("notifybroker", true); 
        
        int i = 1;
        while(props.getProperty("pep." + i) != null){
            String pep = props.getProperty("pep." + i);
            if("net.es.oscars.notify.ws.policy.IDCEventPEP".equals(pep)){
                IDCEventPEP idcEventPEP = new IDCEventPEP();
                idcEventPEP.init();
                peps.add(idcEventPEP);
            }
            i++;
        }
        
        return peps;
    }
}