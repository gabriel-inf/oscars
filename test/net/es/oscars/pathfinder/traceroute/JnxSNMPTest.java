package net.es.oscars.pathfinder.traceroute;

import junit.framework.*;
import java.util.*;
import java.io.IOException;

import net.es.oscars.PropHandler;


public class JnxSNMPTest extends TestCase {
    private Properties props;
    private JnxSNMP snmp;

    public JnxSNMPTest(String name) {
        super(name);
    }

    public void setUp() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss.topology.JnxSNMP", true);
        try {
            this.snmp = new JnxSNMP();
            this.snmp.initializeSession(this.props.getProperty("routerName"));
        } catch (IOException e) {
            fail("JnxSNMP.setUp: " + e.getMessage());
        }
    }

    public void tearDown() {
        try {
            this.snmp.closeSession();
        } catch (IOException e) {
            fail("JnxSNMP.tearDown: " + e.getMessage());
        }
    }

    public void testJnxSNMP() { 
        String ASnumber = null;

        try {
            ASnumber =
                this.snmp.queryAsNumber(this.props.getProperty("queryIP"));
            System.out.println("ASnumber for " +
                    this.props.getProperty("queryIP") + " is " + ASnumber);
        } catch (IOException e) {
            fail("JnxSNMP.testJnxSNMP: " + e.getMessage());
        }

        Assert.assertNull(snmp.getError());
        Assert.assertNotNull(ASnumber);
    }

    /// XXX: doesn't work yet
    public void testQueryLSPInfo() {
        ArrayList infoArray = null;

        try {
            this.snmp.queryLSPSnmp(); 
            infoArray = this.snmp.queryLspInfo(null,null);
        } catch (IOException e) {
            fail("JnxSNMP.testQueryLSPInfo: " + e.getMessage());
        }
        System.out.println("infoArray: "  + infoArray);
        System.out.println("err : " + this.snmp.getError());
        Assert.assertNotNull(infoArray);
    }   
}
