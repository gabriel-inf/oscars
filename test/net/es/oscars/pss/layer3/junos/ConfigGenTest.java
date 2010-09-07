package net.es.oscars.pss.layer3.junos;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.ReservationMaker;
import net.es.oscars.pss.common.PSSDirection;
import net.es.oscars.pss.impl.sdn.SDNNameGenerator;

import org.testng.annotations.*;

@Test(groups={ "pss.layer3" })
public class ConfigGenTest {

    
    @Test
    public void testL3Setup() throws BSSException, PSSException {
        ReservationMaker rm = new ReservationMaker();
        Reservation resv = rm.makeL3();
        Layer3JunosConfigGen th = Layer3JunosConfigGen.getInstance();
        
        th.setTemplateDir("conf/pss");
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        th.setNameGenerator(ng);

        
        String out;
        out = th.generateL3Setup(resv, PSSDirection.A_TO_Z);
        System.out.println(out);
        out = th.generateL3Setup(resv, PSSDirection.Z_TO_A);
        System.out.println(out);
    }
    @Test
    public void testL2Teardown() throws BSSException, PSSException {
        ReservationMaker rm = new ReservationMaker();
        Reservation resv = rm.makeL3();
        Layer3JunosConfigGen th = Layer3JunosConfigGen.getInstance();
        
        th.setTemplateDir("conf/pss");
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        th.setNameGenerator(ng);
        String out;
        out = th.generateL3Teardown(resv, PSSDirection.A_TO_Z);
        // System.out.println(out);
        out = th.generateL3Teardown(resv, PSSDirection.Z_TO_A);
        // System.out.println(out);
    }
    @Test
    public void testL2Status() throws BSSException, PSSException {
        ReservationMaker rm = new ReservationMaker();
        Reservation resv = rm.makeL3();
        Layer3JunosConfigGen th = Layer3JunosConfigGen.getInstance();
        th.setTemplateDir("conf/pss");
        SDNNameGenerator ng = SDNNameGenerator.getInstance();
        th.setNameGenerator(ng);
        String out;
        out = th.generateL3Status(resv, PSSDirection.A_TO_Z);
        // System.out.println(out);
    }

}
