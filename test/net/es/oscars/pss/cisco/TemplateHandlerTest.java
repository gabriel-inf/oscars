package net.es.oscars.pss.cisco;

import org.testng.annotations.*;

import java.io.IOException;
import java.util.*;
import java.util.Properties;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;


/**
 * This class tests loading/saving to/from the domains table.
 *
 */
@Test(groups={ "pss.cisco" })
public class TemplateHandlerTest {
    private final String BANDWIDTH = "10000000"; // 10 Mbps
    private Properties testProps;
    private TemplateHandler th;
    private Map<String,String> commonHm;
    private String setupFileName;
    private String teardownFileName;
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
                          this.testProps.getProperty("asNum") + "-1");
        this.commonHm.put("resv-num",
                          this.testProps.getProperty("asNum") + "1");
        this.commonHm.put("lsp_setup-priority",
                oscarsProps.getProperty("lsp_setup-priority"));
        this.commonHm.put("lsp_reservation-priority",
                oscarsProps.getProperty("lsp_reservation-priority"));
        this.commonHm.put("bandwidth", BANDWIDTH);
        // this just tests template filling
        this.commonHm.put("vlan-id", "0");
        this.commonHm.put("port", "GigabitEthernet1/0/0");
        this.commonHm.put("egress-rtr-loopback",
                          this.testProps.getProperty("egressNode"));
        this.commonHm.put("lsp_to", this.testProps.getProperty("egressNode"));

        this.setupFileName = System.getenv("CATALINA_HOME") +
                                 "/shared/oscars.conf/server/" +
                                 oscarsProps.getProperty("setupFile");
        this.teardownFileName =  System.getenv("CATALINA_HOME") +
                                 "/shared/oscars.conf/server/" +
                                 oscarsProps.getProperty("teardownFile");
        this.th = new TemplateHandler();
    }

  @Test
    public void ciscoSetup() throws IOException, PSSException {

        StringBuilder sb = new StringBuilder();

        this.log.info("explicitPathSetup.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);

        this.log.info("configureLSP.start");
        for (String key: hm.keySet()) {
            sb.append(key + ": " + hm.get(key) + "\n");
        }
        this.log.info(sb.toString());

        List<String> hops = new ArrayList<String>();
        // these are just used to fill in the template as a test
        hops.add("134.55.75.94");  // ingress
        hops.add("134.55.209.21");
        hops.add("134.55.219.10");
        hops.add("134.55.217.2");
        hops.add("134.55.207.37");
        hops.add("134.55.220.49");
        hops.add("134.55.209.46");
        hops.add("134.55.207.34");
        hops.add("134.55.219.26");

        String buf = this.th.buildString(hm, hops, this.setupFileName);
        this.log.info("\n" + buf);
        this.log.info("explicitPathSetup.finish");
        assert buf != null;
    }

  @Test
    public void ciscoTeardown() throws IOException, PSSException {

        this.log.info("testBasicTeardown.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);
        String buf = this.th.buildString(hm, null, this.teardownFileName);
        this.log.info("\n" + buf);
        this.log.info("testBasicTeardown.finish");
        assert buf != null;
    }
}
