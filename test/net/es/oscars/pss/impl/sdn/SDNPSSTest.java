package net.es.oscars.pss.impl.sdn;

import java.util.ArrayList;
import java.util.HashSet;

import org.testng.annotations.Test;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.StateEngine;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.Ipaddr;
import net.es.oscars.bss.topology.L2SwitchingCapabilityData;
import net.es.oscars.bss.topology.Layer2Data;
import net.es.oscars.bss.topology.Layer3Data;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.Node;
import net.es.oscars.bss.topology.NodeAddress;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathDirection;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathElemParam;
import net.es.oscars.bss.topology.PathElemParamSwcap;
import net.es.oscars.bss.topology.PathElemParamType;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.bss.topology.Port;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.common.PSSConfigProvider;
import net.es.oscars.pss.common.PSSHandlerConfigBean;
import net.es.oscars.pss.impl.sdn.SDNPSS;

@Test(groups={ "pss.sdn" })
public class SDNPSSTest {

    
    @Test
    public void testL2Setup() throws BSSException, PSSException {
        // Reservation resv = this.makeL2();
        Reservation resv = null;
        PSSHandlerConfigBean config = new PSSHandlerConfigBean();
        config.setCheckStatusAfterSetup(false);
        config.setCheckStatusAfterTeardown(false);
        config.setLogConfig(false);
        config.setStubMode(true);
        config.setTeardownOnFailure(false);
        config.setTemplateDir("conf/pss");
        PSSConfigProvider pc = PSSConfigProvider.getInstance();
        pc.setHandlerConfig(config);
        SDNPSS pss = SDNPSS.getInstance();
        

        pss.createPath(resv);
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println(".");
        }
    }

}
