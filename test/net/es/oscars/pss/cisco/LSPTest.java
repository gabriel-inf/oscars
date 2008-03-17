package net.es.oscars.pss.cisco;

import org.testng.annotations.*;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.AuthHandler;
import net.es.oscars.GlobalParams;
import net.es.oscars.pss.PSSException;


/**
 * This class tests Cisco router configuration.
 *
 */
@Test(groups={ "broken" })
public class LSPTest {
    private final String BANDWIDTH = "10000000"; // 10 Mbps
    // resv-num's will wrap at 65534
    private final String GRI = "65535";
    private Properties testProps;
    private LSP lsp;
    private HashMap<String, String> commonHm;
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
                        this.testProps.getProperty("localAsNum") + "-" + GRI);
        this.commonHm.put("resv-num", GRI);
        // not actually setting up circuit, just testing layer on top of
        // TemplateHandler
        this.commonHm.put("lsp_to", this.testProps.getProperty("layer2Dest"));
        this.commonHm.put("egress-rtr-loopback",
                this.testProps.getProperty("layer2Dest"));
        this.commonHm.put("bandwidth", BANDWIDTH);
        this.commonHm.put("vlan-id", "0");
        this.commonHm.put("port", "non-existent");
        this.commonHm.put("lsp_setup-priority",
                oscarsProps.getProperty("lsp_setup-priority"));
        this.commonHm.put("lsp_reservation-priority",
                oscarsProps.getProperty("lsp_reservation-priority"));
        this.lsp = new LSP(GlobalParams.getReservationTestDBName());
        this.lsp.setConfigurable(false);
    }

  @Test
    public void createCiscoCircuit() throws PSSException {

        List<String> hops = new ArrayList<String>();
        String[] pathHops =
            this.testProps.getProperty("layer2Path").split(", ");
        for (int i=0; i < pathHops.length; i++) {
            pathHops[i] = pathHops[i].trim();
            hops.add(pathHops[i]);
        }
        //this.lsp.setParameters(this.commonHm);
        this.lsp.setupLSP(hops);
        // if got to here without exception, successful for now
        assert true;
    }

  @Test
    public void teardownCiscoCircuit() throws PSSException {

        this.lsp.teardownLSP();
        // if got to here without exception, successful for now
        assert true;
    }
}
