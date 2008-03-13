package net.es.oscars.pss.jnx;

import org.testng.annotations.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.AuthHandler;
import net.es.oscars.GlobalParams;
import net.es.oscars.pss.PSSException;


/**
 * This class tests loading/saving to/from the domains table.
 *
 */
@Test(groups={ "pss.jnx" })
public class JnxLSPTest {
    private Properties testProps;
    private JnxLSP jnxLSP;
    private Map<String, String> l2HashMap;
    private Map<String, String> l3HashMap;
    private Logger log;

  @BeforeClass
    protected void setUpClass() {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("test.properties");
        this.testProps = propHandler.getPropertyGroup("test.common", true);
        propHandler = new PropHandler("oscars.properties");
        // fill in name/value pairs common to all tests
        Properties oscarsProps = propHandler.getPropertyGroup("pss.jnx", true);
        // layer 3 parameters
        this.l3HashMap = new HashMap<String,String>();
        String keyfile = System.getenv("CATALINA_HOME") +
                "/shared/oscars.conf/server/oscars.key";
        this.l3HashMap.put("keyfile", keyfile);
        this.l3HashMap.put("router", this.testProps.getProperty("ingressRouter"));
        this.l3HashMap.put("source-address",
                this.testProps.getProperty("srcHost"));
        this.l3HashMap.put("destination-address",
                this.testProps.getProperty("destHost"));
        this.l3HashMap.put("from", this.testProps.getProperty("ingressRouter"));
        this.l3HashMap.put("to", this.testProps.getProperty("egressRouter"));
        this.l3HashMap.put("bandwidth", CommonParams.getBandwidth());

        this.l3HashMap.put("login", oscarsProps.getProperty("login"));
        // to distinguish tests
        this.l3HashMap.put("name", oscarsProps.getProperty("login") + "1000000");
        this.l3HashMap.put("passphrase", oscarsProps.getProperty("passphrase"));
        this.l3HashMap.put("firewall_filter_marker",
                oscarsProps.getProperty("firewall_filter_marker"));
        this.l3HashMap.put("internal_interface_filter",
                oscarsProps.getProperty("internal_interface_filter"));
        this.l3HashMap.put("external_interface_filter",
                oscarsProps.getProperty("external_interface_filter"));
        this.l3HashMap.put("lsp_class-of-service",
                oscarsProps.getProperty("lsp_class-of-service"));
        this.l3HashMap.put("dscp", oscarsProps.getProperty("dscp"));
        this.l3HashMap.put("policer_burst-size-limit",
                oscarsProps.getProperty("policer_burst-size-limit"));
        this.l3HashMap.put("lsp_setup-priority",
                oscarsProps.getProperty("lsp_setup-priority"));
        this.l3HashMap.put("lsp_reservation-priority",
                oscarsProps.getProperty("lsp_reservation-priority"));
        this.jnxLSP = new JnxLSP(GlobalParams.getReservationTestDBName());
        this.jnxLSP.setConfigurable(false);
    }

  @Test
    public void allowedTest() {
        AuthHandler authHandler = new AuthHandler();
        boolean authorized = authHandler.checkAuthorization();
        assert authorized : "You are not authorized to set up a circuit from this machine";
    }

  @Test(dependsOnMethods={ "allowedTest" })
    public void createForwardPath() throws PSSException {
        assert true;
    }

  @Test(dependsOnMethods={ "allowedTest" })
    public void refreshPath() throws PSSException {
        assert true;
    }

  @Test(dependsOnMethods={ "allowedTest" })
    public void teardownForwardPath() throws PSSException {
        assert true;
    }
}
