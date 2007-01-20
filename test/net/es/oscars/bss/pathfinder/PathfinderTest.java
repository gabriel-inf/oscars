package net.es.oscars.bss.pathfinder;

import junit.framework.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.bss.BSSException;


/**
 * This class tests methods in Pathfinder.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathfinderTest extends TestCase {

    private static Properties props;
    private static Pathfinder pathFinder;
    private static ArrayList<String> hops;
    private static String nextHop;

    public PathfinderTest(String name) {
        super(name);
    }
        
    protected void setUp() {
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler =
            new PropHandler("/oscars.config/properties/test.properties");
        this.props = propHandler.getPropertyGroup("test.bss.topology.pathfinder", true);
    }

    public void testTraceroute() {
        try {
            this.pathFinder = new Pathfinder();
            this.hops = this.pathFinder.traceroute(
                                   this.props.getProperty("srcHost"),
                                   this.props.getProperty("dstHost"));
        } catch (BSSException ex) {
            fail("testTraceroute failed: " + ex.getMessage());
        } 
        Assert.assertFalse(this.hops.isEmpty());
    }

    public void testForwardPath() {
        List<String> ret = null;
        try {
            ret = this.pathFinder.forwardPath(
                                  this.props.getProperty("srcHost"),
                                  this.props.getProperty("dstHost"));
        } catch (BSSException ex) {
            fail("testForwardPath (TopEx): " + ex.getMessage());
        }
        System.out.println("Forward Path == " + ret);
        Assert.assertNotNull(ret);
    }

    public void testQueryDomain() {
        String ret = null; 
        try {
            ret = this.pathFinder.queryDomain(null,
                                  this.props.getProperty("ingressLoopback"),
                                  this.props.getProperty("egressLoopback"));
        } catch (BSSException ex) {
            fail("testQueryDomain: " + ex.getMessage());
        }
        System.out.println("queryDomain == " + ret);
        Assert.assertNotNull("testQueryDomain empty", ret);
    }

    /*
     * Check this call useing null for the various aruments.
     * should probably put the ips into the properties file
     */
    public void testReversePath1() {
        ArrayList<String> ret = null; 
        try {
            ret = this.pathFinder.reversePath("chi-cr1-oscars.es.net", "nettrash3.es.net"); 
        } catch (BSSException ex) {
            fail("testReversePath1: " + ex.getMessage());
        }
        System.out.println("testReversePath1 == " + ret);
        Assert.assertFalse("testReversePath1 empty", ret.isEmpty());
    }

    public void testReversePath2() {
        ArrayList<String> ret = null; 
        try {
            ret = this.pathFinder.reversePath("134.55.75.26", "134.55.75.25"); 
        } catch (BSSException ex) {
            fail("testReversePath2: " + ex.getMessage());
        }
        System.out.println("testReversePath2 == " + ret);
        Assert.assertFalse("testReversePath2 empty", ret.isEmpty());
    }
}
