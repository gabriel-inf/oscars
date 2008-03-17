package net.es.oscars.pathfinder.traceroute;

import org.testng.annotations.*;
import org.testng.Assert;

import java.util.List;
import java.util.Properties;
import java.io.IOException;

import net.es.oscars.PropHandler;
import net.es.oscars.AuthHandler;
import net.es.oscars.pathfinder.PathfinderException;

/**
 * This class tests methods in the JnxTraceroute class.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder.traceroute", "jnxTraceroute" })
public class JnxTracerouteTest {

    private Properties props;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

  @Test
    public void allowedTest() {
        AuthHandler authHandler = new AuthHandler();
        boolean authorized = authHandler.checkAuthorization();
        Assert.assertTrue(authorized,
            "You are not authorized to do a traceroute from this machine. ");
    }

  @Test(dependsOnMethods={ "allowedTest" })
    public void jnxTraceroute() throws PathfinderException, IOException {
        JnxTraceroute jnxTraceroute = new JnxTraceroute();

        String ingressNode = this.props.getProperty("ingressNode");
        String pathSrc =
            jnxTraceroute.traceroute(ingressNode,
                                     this.props.getProperty("destHost"));
        // just tests that traceroute completed
        assert ingressNode.equals(pathSrc);
    
    }

  @Test(dependsOnMethods={ "allowedTest" })
    public void jnxRawHopData() throws PathfinderException, IOException {
        List<String> hops = null;
        JnxTraceroute jnxTraceroute = new JnxTraceroute();

        String ingressNode = this.props.getProperty("ingressNode");
        String pathSrc =
            jnxTraceroute.traceroute(ingressNode,
                                     this.props.getProperty("destHost"));
        // should be at least one hop
        hops = jnxTraceroute.getRawHopData();
        assert !hops.isEmpty();
    }

  @Test(dependsOnMethods={ "allowedTest" })
    public void jnxHopData() throws PathfinderException, IOException {
        List<String> hops = null;
        JnxTraceroute jnxTraceroute = new JnxTraceroute();

        String ingressNode = this.props.getProperty("ingressNode");
        String pathSrc =
            jnxTraceroute.traceroute(ingressNode,
                                     this.props.getProperty("destHost"));
        // should be at least one hop
        hops = jnxTraceroute.getHops();
        assert !hops.isEmpty();
    }
}
