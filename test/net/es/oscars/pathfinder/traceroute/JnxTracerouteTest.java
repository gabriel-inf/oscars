package net.es.oscars.pathfinder.traceroute;

import org.testng.annotations.*;

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
@Test(groups={ "pathfinder.traceroute" })
public class JnxTracerouteTest {

    private Properties props;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

  @Test
    public void testTraceroute() throws PathfinderException, IOException {
        JnxTraceroute jnxTraceroute = new JnxTraceroute();

        String ingressRouter = this.props.getProperty("ingressRouter");
        String pathSrc =
            jnxTraceroute.traceroute(ingressRouter,
                                     this.props.getProperty("destHost"));
        // just tests that traceroute completed
        assert ingressRouter.equals(pathSrc);
    
    }

  @Test
    public void testRawHopData() throws PathfinderException, IOException {
        List<String> hops = null;
        JnxTraceroute jnxTraceroute = new JnxTraceroute();

        String ingressRouter = this.props.getProperty("ingressRouter");
        String pathSrc =
            jnxTraceroute.traceroute(ingressRouter,
                                     this.props.getProperty("destHost"));
        // should be at least one hop
        hops = jnxTraceroute.getRawHopData();
        System.out.println("RawHopData: " + hops);
        assert !hops.isEmpty();
    }

  @Test
    public void testHopData() throws PathfinderException, IOException {
        List<String> hops = null;
        JnxTraceroute jnxTraceroute = new JnxTraceroute();

        String ingressRouter = this.props.getProperty("ingressRouter");
        String pathSrc =
            jnxTraceroute.traceroute(ingressRouter,
                                     this.props.getProperty("destHost"));
        // should be at least one hop
        hops = jnxTraceroute.getHops();
        System.out.println("HopData: " + hops);
        assert !hops.isEmpty();
    }
}
