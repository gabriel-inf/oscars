package net.es.oscars.pss.vendor.cisco;

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
//@Test(groups={ "pss.cisco, xml" })
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
        // this just tests template filling; doesn't matter what values are
        this.commonHm.put("vlan-id", "0");
        this.commonHm.put("port", "GigabitEthernet1/0/0");
        this.commonHm.put("egress-rtr-loopback",
                          this.testProps.getProperty("layer2Dest"));
        this.commonHm.put("lsp_to", this.testProps.getProperty("layer2Dest"));

        this.setupFileName = System.getenv("CATALINA_HOME") +
                                  "/shared/classes/server/" +
                                 oscarsProps.getProperty("setupL2Template");
        this.teardownFileName =  System.getenv("CATALINA_HOME") +
                                 "/shared/classes/server/" +
                                 oscarsProps.getProperty("teardownL2Template");
        this.th = new TemplateHandler();
    }

  @Test
    public void ciscoTemplateSetup() throws IOException, PSSException {

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
        String[] pathHops =
            this.testProps.getProperty("layer2Path").split(", ");
        for (int i=0; i < pathHops.length; i++) {
            pathHops[i] = pathHops[i].trim();
            hops.add(pathHops[i]);
        }

        String buf = this.th.buildString(hm, hops, this.setupFileName);
        this.log.info("\n" + buf);
        this.log.info("explicitPathSetup.finish");
        assert buf != null;
    }

  @Test
    public void ciscoTemplateTeardown() throws IOException, PSSException {

        this.log.info("testBasicTeardown.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);
        String buf = this.th.buildString(hm, null, this.teardownFileName);
        this.log.info("\n" + buf);
        this.log.info("testBasicTeardown.finish");
        assert buf != null;
    }
}
