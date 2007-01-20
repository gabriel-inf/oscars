package net.es.oscars.bss.pathfinder;

import junit.framework.*;

import java.util.List;
import java.util.Properties;
import java.io.IOException;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;

/**
 * This class tests methods in the JnxTraceroute class.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class JnxTracerouteTest extends TestCase {

    private Properties props;

    public JnxTracerouteTest(String name) {
        super(name);
    }

    public void setUp() {
        PropHandler propHandler =
            new PropHandler("/oscars.config/properties/test.properties");
        this.props = propHandler.getPropertyGroup("test.bss.JnxTraceroute", true);
    }

    // this does the traceroute
    public void testTraceroute() {
        String route = null;
        JnxTraceroute jnxTraceroute = new JnxTraceroute();

        try {
            route = jnxTraceroute.traceroute( 
                this.props.getProperty("srcHost"),
                this.props.getProperty("dstHost"));
        } catch (IOException e) {
            fail("JnxTraceroute.traceroute: " + e.getMessage());
        } catch (BSSException e) {
            fail("JnxTraceroute.traceroute: " + e.getMessage());
        }

        // return value should be same as dstHost
        Assert.assertEquals(this.props.getProperty("srcHost"), route);
    
    }

    public void testRawHopData() {
        String route = null;
        List<String> hops = null;
        JnxTraceroute jnxTraceroute = new JnxTraceroute();

        try {
            route = jnxTraceroute.traceroute( 
                this.props.getProperty("srcHost"),
                this.props.getProperty("dstHost"));
        } catch (IOException e) {
            fail("JnxTraceroute.traceroute: " + e.getMessage());
        } catch (BSSException e) {
            fail("JnxTraceroute.traceroute: " + e.getMessage());
        }

        // should be at least one hop
        hops = jnxTraceroute.getRawHopData();
        System.out.println("RawHopData: " + hops);
        Assert.assertFalse(hops.isEmpty());
    }

    public void testHopData() {
        String route = null;
        List<String> hops = null;
        JnxTraceroute jnxTraceroute = new JnxTraceroute();

        try {
            route = jnxTraceroute.traceroute( 
                this.props.getProperty("srcHost"),
                this.props.getProperty("dstHost"));
        } catch (IOException e) {
            fail("JnxTraceroute.traceroute: " + e.getMessage());
        } catch (BSSException e) {
            fail("JnxTraceroute.traceroute: " + e.getMessage());
        }

        // should be at least one hop
        hops = jnxTraceroute.getHops();
        System.out.println("HopData: " + hops);
        Assert.assertFalse(hops.isEmpty());
    }
}
