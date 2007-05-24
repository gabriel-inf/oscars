package net.es.oscars.pss;

import org.testng.annotations.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;


/**
 * This class tests loading/saving to/from the domains table.
 *
 */
@Test(groups={ "pss" })
public class JnxLSPTest {
    private Properties testProps;
    private JnxLSP jnxLSP;
    private Map<String, String> commonHm;
    private Logger log;

  @BeforeClass
    protected void setUpClass() {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("test.properties");
        this.testProps = propHandler.getPropertyGroup("test.pss", true);
        propHandler = new PropHandler("oscars.properties");
        // fill in name/value pairs common to all tests
        Properties props = propHandler.getPropertyGroup("pss", true);
        this.commonHm = new HashMap<String,String>();
        String keyfile = System.getenv("CATALINA_HOME") +
                "/shared/oscars.conf/server/oscars.key";
        this.commonHm.put("keyfile", keyfile);
        this.commonHm.put("router", this.testProps.getProperty("router"));
        this.commonHm.put("source-address",
                this.testProps.getProperty("source-address"));
        this.commonHm.put("destination-address",
                this.testProps.getProperty("destination-address"));
        this.commonHm.put("from", this.testProps.getProperty("from"));
        this.commonHm.put("to", this.testProps.getProperty("to"));
        this.commonHm.put("bandwidth",
                this.testProps.getProperty("bandwidth"));

        this.commonHm.put("login", props.getProperty("login"));
        // to distinguish tests
        this.commonHm.put("name", props.getProperty("login") + "1000000");
        this.commonHm.put("passphrase", props.getProperty("passphrase"));
        this.commonHm.put("firewall_filter_marker",
                props.getProperty("firewall_filter_marker"));
        this.commonHm.put("internal_interface_filter",
                props.getProperty("internal_interface_filter"));
        this.commonHm.put("external_interface_filter",
                props.getProperty("external_interface_filter"));
        this.commonHm.put("lsp_class-of-service",
                props.getProperty("lsp_class-of-service"));
        this.commonHm.put("dscp", props.getProperty("dscp"));
        this.commonHm.put("policer_burst-size-limit",
                props.getProperty("policer_burst-size-limit"));
        this.commonHm.put("lsp_setup-priority",
                props.getProperty("lsp_setup-priority"));
        this.commonHm.put("lsp_reservation-priority",
                props.getProperty("lsp_reservation-priority"));
        this.jnxLSP = new JnxLSP();
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
        assert this.jnxLSP.teardownLSP(hm, null);
    }
}
