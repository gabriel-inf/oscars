package net.es.oscars.pss;

import junit.framework.*;

import java.util.HashMap;
import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;


/**
 * This class tests loading/saving to/from the domains table.
 *
 */
public class JnxLSPTest extends TestCase {
    private Properties props;
    private JnxLSP jlsp;
    private HashMap<String, String> testHM;

    public JnxLSPTest(String name) {
        super(name);
    }

    public void setUp() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.pss", true);
        this.jlsp = new JnxLSP();
        this.testHM = build_hash();
    }

    public void testSetupLSP() {
        boolean ret = false;
        try {
            ret = this.jlsp.setupLSP(this.testHM);
        } catch (BSSException ex) {
            fail("JnxLSP.SetupLSP: " + ex.getMessage());
        }
        Assert.assertTrue(ret);
    }

    public void testTearDownLSP() {
    
        boolean ret = false;
        try {
            ret = this.jlsp.teardownLSP(this.testHM);
        } catch (BSSException ex) {
            fail("JnxLSP.TearDownLSP: " + ex.getMessage());
        }
        Assert.assertTrue(ret);
    }

    public HashMap<String,String> build_hash() {
        HashMap<String,String> hm = new HashMap<String,String>();

        hm.put("user", this.props.getProperty("user"));
        hm.put("passphrase",this.props.getProperty("passphrase"));
        String keyfile = System.getenv("CATALINA_HOME") + "/shared/oscars.conf/server/pss_key";
        hm.put("keyfile", keyfile);
        hm.put("host", this.props.getProperty("host"));
        hm.put("user", this.props.getProperty("user"));
        hm.put("user_var_name_user_var","zippy");
        hm.put("user_var_lsp_from_user_var","172.16.1.1");
        hm.put("user_var_lsp_to_user_var","172.16.2.2");
        hm.put("user_var_bandwidth_user_var","100000");
        hm.put("user_var_lsp_class-of-service_user_var","4");
        hm.put("user_var_lsp_setup-priority_user_var","4");
        hm.put("user_var_lsp_reservation-priority_user_var","4");
        hm.put("user_var_lsp_description_user_var","test");
        hm.put("user_var_policer_burst-size-limit_user_var","10000000");
        hm.put("user_var_external_interface_filter_user_var","external-interface-inbound-inet.0-filter");
        hm.put("user_var_internal_interface_filter_user_var","internal-interface-inbound-inet.0-filter");
        hm.put("user_var_source-address_user_var","172.16.1.1");
        hm.put("user_var_destination-address_user_var","172.16.2.2");
        hm.put("user_var_dscp_user_var","4");
        hm.put("user_var_protocol_user_var","tcp");
//        hm.put("user_var_source-port_user_var","5555");
//        hm.put("user_var_destination-port_user_var","6666");
        hm.put("user_var_firewall_filter_marker_user_var","oscars-filters-start");
        return hm;
    }
}
