package net.es.oscars.pss;

import org.testng.annotations.*;

import java.io.IOException;
import java.util.*;
import java.util.Properties;

import org.jdom.*;
import org.jdom.output.*;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;


/**
 * This class tests loading/saving to/from the domains table.
 *
 */
@Test(groups={ "pss", "xml" })
public class TemplateHandlerTest {
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
        this.testProps = propHandler.getPropertyGroup("test.pss", true);
        propHandler = new PropHandler("oscars.properties");
        // fill in name/value pairs common to all tests
        Properties props = propHandler.getPropertyGroup("pss", true);
        this.commonHm = new HashMap<String,String>();
        this.commonHm.put("name", props.getProperty("login") + "1");
        this.commonHm.put("lsp_class-of-service", props.getProperty("lsp_class-of-service"));
        this.commonHm.put("lsp_setup-priority",
                props.getProperty("lsp_setup-priority"));
        this.commonHm.put("lsp_reservation-priority",
                props.getProperty("lsp_reservation-priority"));
        this.commonHm.put("internal_interface_filter",
                props.getProperty("internal_interface_filter"));
        this.commonHm.put("external_interface_filter",
                props.getProperty("external_interface_filter"));
        this.commonHm.put("dscp", props.getProperty("dscp"));
        this.commonHm.put("policer_burst-size-limit",
                props.getProperty("policer_burst-size-limit"));
        this.commonHm.put("firewall_filter_marker",
                props.getProperty("firewall_filter_marker"));
        this.commonHm.put("source-address",
                this.testProps.getProperty("source-address"));
        this.commonHm.put("destination-address",
                this.testProps.getProperty("destination-address"));
        this.commonHm.put("from", this.testProps.getProperty("from"));
        this.commonHm.put("to", this.testProps.getProperty("to"));
        this.commonHm.put("bandwidth",
                this.testProps.getProperty("bandwidth"));

        this.setupFileName = System.getenv("CATALINA_HOME") +
                                 "/shared/oscars.conf/server/" +
                                 props.getProperty("setupFile");
        this.teardownFileName =  System.getenv("CATALINA_HOME") +
                                 "/shared/oscars.conf/server/" +
                                 props.getProperty("teardownFile");
        this.th = new TemplateHandler();
    }

  @Test
    public void basicSetup()
            throws IOException, JDOMException, PSSException {

        StringBuilder sb = new StringBuilder();

        this.log.info("testBasicSetup.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);
        hm.put("lsp_description", "testBasicSetup");

        this.log.info("configureLSP.start");
        for (String key: hm.keySet()) {
            sb.append(key + ": " + hm.get(key) + "\n");
        }
        this.log.info(sb.toString());

        Document doc = this.th.fillTemplate(hm, null, this.setupFileName);
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        this.log.info("testBasicSetup.finish");
        assert doc != null;
    }

  @Test(dependsOnMethods={ "basicSetup" })
    public void basicTeardown()
            throws IOException, JDOMException, PSSException {

        this.log.info("testBasicTeardown.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);
        Document doc = this.th.fillTemplate(hm, null, this.teardownFileName);
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        this.log.info("testBasicTeardown.finish");
        assert doc != null;
    }

  @Test
    public void explicitPathSetup()
            throws IOException, JDOMException, PSSException {

        StringBuilder sb = new StringBuilder();

        this.log.info("explicitPathSetup.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.commonHm);
        hm.put("lsp_description", "explicitPathSetup");

        this.log.info("configureLSP.start");
        for (String key: hm.keySet()) {
            sb.append(key + ": " + hm.get(key) + "\n");
        }
        this.log.info(sb.toString());

        List<String> hops = new ArrayList<String>();
        hops.add("134.55.75.94");  // ingress
        hops.add("134.55.209.21");
        hops.add("134.55.218.30");
        hops.add("134.55.217.2");
        hops.add("134.55.207.37");
        hops.add("134.55.209.54");
        hops.add("134.55.219.26");

        Document doc = this.th.fillTemplate(hm, hops, this.setupFileName);
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        this.log.info("explicitPathSetup.finish");
        assert doc != null;
    }

}
