package net.es.oscars.pss.bridge;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.ReservationMaker;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.impl.bridge.BridgeHandler;

import org.testng.annotations.*;

@Test(groups={ "pss.bridge" })
public class HandlerTest {

    
    @Test
    public void testSetup() throws BSSException, PSSException {
        ReservationMaker rm = new ReservationMaker();
        Reservation resv = rm.makeL2();
        
        

        
        BridgeHandler handler = BridgeHandler.getInstance();
            
        handler.setup(resv, PSSDirection.BIDIRECTIONAL);
        
    }
 
}
