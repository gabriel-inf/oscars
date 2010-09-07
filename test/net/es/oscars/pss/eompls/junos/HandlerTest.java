package net.es.oscars.pss.eompls.junos;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.ReservationMaker;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSConnectorConfigBean;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.impl.sdn.SDNNameGenerator;

import org.testng.annotations.*;

@Test(groups={ "pss.eompls" })
public class HandlerTest {

    
    @Test
    public void testSetup() throws BSSException, PSSException {
        ReservationMaker rm = new ReservationMaker();
        Reservation resv = rm.makeL2();
        
        
        EoMPLSJunosConfigGen th = EoMPLSJunosConfigGen.getInstance();
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        th.setNameGenerator(ng);
        
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        
        PSSHandlerConfigBean hc = new PSSHandlerConfigBean();
        PSSConnectorConfigBean cc = new PSSConnectorConfigBean();
        pc.setHandlerConfig(hc);
        pc.setConnectorConfig(cc);
        
        hc.setTemplateDir("conf/pss");
        hc.setCheckStatusAfterSetup(false);
        hc.setCheckStatusAfterTeardown(false);
        hc.setLogConfig(true);
        hc.setStubMode(true);
        hc.setTeardownOnFailure(false);        
        
        EoMPLS_Junos handler = new EoMPLS_Junos();
        handler.setup(resv, PSSDirection.A_TO_Z);
        
    }
 
}
