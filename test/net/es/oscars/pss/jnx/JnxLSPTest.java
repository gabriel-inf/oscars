package net.es.oscars.pss.jnx;

import org.testng.annotations.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;


/**
 * This class tests loading/saving to/from the domains table.
 *
 */
@Test(groups={ "pss.jnx" })
public class JnxLSPTest {
    private final String BANDWIDTH = "10000000"; // 10 Mbps
    private Properties testProps;
    private JnxLSP jnxLSP;
    private Map<String, String> commonHm;
    private Logger log;

  @BeforeClass
    protected void setUpClass() {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("test.properties");
        this.testProps = propHandler.getPropertyGroup("test.common", true);
        propHandler = new PropHandler("oscars.properties");
        // fill in name/value pairs common to all tests
        Properties oscarsProps = propHandler.getPropertyGroup("pss.jnx", true);
        this.commonHm = new HashMap<String,String>();
        String keyfile = System.getenv("CATALINA_HOME") +
                "/shared/oscars.conf/server/oscars.key";
        this.commonHm.put("keyfile", keyfile);
        this.commonHm.put("router", this.testProps.getProperty("ingressRouter"));
        this.commonHm.put("source-address",
                this.testProps.getProperty("srcHost"));
        this.commonHm.put("destination-address",
                this.testProps.getProperty("destHost"));
        this.commonHm.put("from", this.testProps.getProperty("ingressRouter"));
        this.commonHm.put("to", this.testProps.getProperty("egressRouter"));
        this.commonHm.put("bandwidth", BANDWIDTH);

        this.commonHm.put("login", oscarsProps.getProperty("login"));
        // to distinguish tests
        this.commonHm.put("name", oscarsProps.getProperty("login") + "1000000");
        this.commonHm.put("passphrase", oscarsProps.getProperty("passphrase"));
        this.commonHm.put("firewall_filter_marker",
                oscarsProps.getProperty("firewall_filter_marker"));
        this.commonHm.put("internal_interface_filter",
                oscarsProps.getProperty("internal_interface_filter"));
        this.commonHm.put("external_interface_filter",
                oscarsProps.getProperty("external_interface_filter"));
        this.commonHm.put("lsp_class-of-service",
                oscarsProps.getProperty("lsp_class-of-service"));
        this.commonHm.put("dscp", oscarsProps.getProperty("dscp"));
        this.commonHm.put("policer_burst-size-limit",
                oscarsProps.getProperty("policer_burst-size-limit"));
        this.commonHm.put("lsp_setup-priority",
                oscarsProps.getProperty("lsp_setup-priority"));
        this.commonHm.put("lsp_reservation-priority",
                oscarsProps.getProperty("lsp_reservation-priority"));
        this.jnxLSP = new JnxLSP("bss");
    }

  @Test
    public void setupLSP() throws PSSException {

        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);

        hm.put("lsp_description", "testSetupLSP");
        assert this.jnxLSP.setupLSP(hm, null);
    }

  @Test
    public void tearDownLSP() throws PSSException {

        // if a hash value is unused in connecting to the router, or
        // in the template, it is ignored
        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);
        assert this.jnxLSP.teardownLSP(hm);
    }
}
