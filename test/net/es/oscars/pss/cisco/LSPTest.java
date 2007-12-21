package net.es.oscars.pss.cisco;

import org.testng.annotations.*;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;


/**
 * This class tests Cisco router configuration.
 *
 */
@Test(groups={ "TODO" })
public class LSPTest {
    private final String BANDWIDTH = "10000000"; // 10 Mbps
    // resv-num's will wrap at 65534
    private final String GRI = "65535";
    private Properties testProps;
    private LSP lsp;
    private Map<String, String> commonHm;
    private Logger log;

  @BeforeClass
    protected void setUpClass() {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("test.properties");
        this.testProps = propHandler.getPropertyGroup("test.common", true);
        propHandler = new PropHandler("oscars.properties");
        // fill in name/value pairs common to all tests
        Properties oscarsProps =
                propHandler.getPropertyGroup("pss.cisco", true);
        this.commonHm = new HashMap<String,String>();
        this.commonHm.put("resv-id", "oscars_" + 
                           this.testProps.getProperty("asNum") + "-" + GRI);
        this.commonHm.put("resv-num", GRI);
        // TODO:  temporary placeholders
        this.commonHm.put("lsp_to", this.testProps.getProperty("egressNode"));
        this.commonHm.put("egress-rtr-loopback", this.testProps.getProperty("egressNode"));
        this.commonHm.put("bandwidth", BANDWIDTH);
        this.commonHm.put("vlan-id", "0");
        this.commonHm.put("port", "non-existent");
        this.commonHm.put("lsp_setup-priority",
                oscarsProps.getProperty("lsp_setup-priority"));
        this.commonHm.put("lsp_reservation-priority",
                oscarsProps.getProperty("lsp_reservation-priority"));
        this.lsp = new LSP("bss");
    }

  /*
  @Test
    public void setupLSP() throws PSSException {

        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);
        List<String> hops = new ArrayList<String>();

        // TODO:  Cisco ingress, properties
        hops.add("134.55.75.94");  // ingress
        hops.add("134.55.209.21");
        hops.add("134.55.219.10");
        hops.add("134.55.217.2");
        hops.add("134.55.207.37");
        hops.add("134.55.220.49");
        hops.add("134.55.209.46");
        hops.add("134.55.207.34");
        hops.add("134.55.219.26");
        this.lsp.setupLSP(hm, hops);
        // if got to here without exception, successful for now
        assert true;
    }

  @Test
    public void tearDownLSP() throws PSSException {

        // if a hash value is unused in connecting to the router, or
        // in the template, it is ignored
        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);
        this.lsp.teardownLSP(hm);
        // if got to here without exception, successful for now
        assert true;
    }
    */
}
