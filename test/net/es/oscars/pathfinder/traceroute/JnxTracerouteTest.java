package net.es.oscars.pathfinder.traceroute;

import org.testng.annotations.*;
import static org.testng.AssertJUnit.*;

import java.util.List;
import java.util.Properties;
import java.io.IOException;

import net.es.oscars.PropHandler;
import net.es.oscars.pathfinder.PathfinderException;

/**
 * This class tests methods in the JnxTraceroute class.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder" })
public class JnxTracerouteTest {

    private Properties props;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
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
        } catch (PathfinderException e) {
            fail("JnxTraceroute.traceroute: " + e.getMessage());
        }

        // return value should be same as dstHost
        assert this.props.getProperty("srcHost").equals(route);
    
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
        } catch (PathfinderException e) {
            fail("JnxTraceroute.traceroute: " + e.getMessage());
        }

        // should be at least one hop
        hops = jnxTraceroute.getRawHopData();
        System.out.println("RawHopData: " + hops);
        assert !hops.isEmpty();
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
        } catch (PathfinderException e) {
            fail("JnxTraceroute.traceroute: " + e.getMessage());
        }

        // should be at least one hop
        hops = jnxTraceroute.getHops();
        System.out.println("HopData: " + hops);
        assert !hops.isEmpty();
    }
}
