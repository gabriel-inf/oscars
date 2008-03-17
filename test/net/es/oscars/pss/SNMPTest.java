package net.es.oscars.pss;

import org.testng.annotations.*;
import org.testng.Assert;

import java.util.*;
import java.io.IOException;

import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;


import net.es.oscars.PropHandler;
import net.es.oscars.AuthHandler;

/**
 * This class tests making SNMP calls using SNMP.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pss" })
public class SNMPTest {
    private Properties props;

  @BeforeClass
    protected void setUpClass() throws IOException {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.snmp", true);
    }
        
  @Test
    public void allowedTest() {
        AuthHandler authHandler = new AuthHandler();
        boolean authorized = authHandler.checkAuthorization();
        Assert.assertTrue(authorized,
            "You are not authorized to make an SNMP query from this machine. ");
    }

/*
    public void testQueryLSPSnmp() throws IOException{
        SNMP snmp = new SNMP();
        String router = this.props.getProperty("router");
        snmp.initializeSession(router);
        snmp.queryLSPSnmp();
        snmp.closeSession();
        if (snmp.getError() != null) {
            System.err.println(snmp.getError());
        }
        assert snmp.getError() == null;
    }
*/

  @Test(dependsOnMethods={ "allowedTest" })
    public void testQueryRouterType() throws IOException, PSSException {
        SNMP snmp = new SNMP();
        String router = this.props.getProperty("router");
        snmp.initializeSession(router);
        String sysDescr = snmp.queryRouterType();
        System.err.println(sysDescr);
        snmp.closeSession();
    }

/*
    public void testQueryAsNumber() throws IOException {
        String asNumber = null;

        String queryAsNumberIP = this.props.getProperty("bgpPeer");
        asNumber = this.snmp.queryAsNumber(queryAsNumberIP);
        if (this.snmp.getError() != null) {
            System.err.println(this.snmp.getError());
        }
        String localAsNumber = this.props.getProperty("localAsNumber");
        assert asNumber == localAsNumber : "AS number returned: " +
                           asNumber + " does not equal local AS number: " +
                           localAsNumber;
    }
*/
}
