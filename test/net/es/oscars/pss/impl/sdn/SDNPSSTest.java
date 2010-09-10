package net.es.oscars.pss.impl.sdn;


import org.testng.annotations.Test;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.ReservationMaker;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSConnectorConfigBean;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.impl.sdn.SDNPSS;

@Test(groups={ "pss.sdn" })
public class SDNPSSTest {

    
    @Test
    public void testL2Setup() throws BSSException {
        
        
        ReservationMaker rm = new ReservationMaker();
        Reservation resv = rm.makeL2();

        PSSHandlerConfigBean hc = new PSSHandlerConfigBean();
        hc.setTemplateDir("conf/pss");
        hc.setCheckStatusAfterSetup(false);
        hc.setCheckStatusAfterTeardown(false);
        hc.setLogConfig(true);
        hc.setStubMode(true);
        hc.setTeardownOnFailure(true);     
        
        
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        pc.setHandlerConfig(hc);
        PSSConnectorConfigBean cc = new PSSConnectorConfigBean();
        pc.setConnectorConfig(cc);
        
        
        SDNPSS pss = null;;
        try {
            pss = SDNPSS.getInstance();
            pss.createPath(resv);
            int i = 0;
            while (i < 10){
                try {
                    Thread.sleep(1000);
                    i++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println(".");
            }
        } catch (PSSException ex) {
            ex.printStackTrace();
        }
    }

}
