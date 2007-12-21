package net.es.oscars.pss.jnx;

import org.testng.annotations.*;

import java.io.IOException;
import java.util.*;
import java.util.Properties;

import org.jdom.*;
import org.jdom.output.*;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;


/**
 * This class tests loading/saving to/from the domains table.
 *
 */
@Test(groups={ "pss.jnx", "xml" })
public class TemplateHandlerTest {
    private Properties testProps;
    private TemplateHandler th;
    private Map<String,String> l2HashMap;
    private Map<String,String> l3HashMap;
    private String setupL2FileName;
    private String teardownL2FileName;
    private String setupL3FileName;
    private String teardownL3FileName;
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
        this.l3HashMap.put("name", oscarsProps.getProperty("login") + "1");
        this.l3HashMap.put("lsp_class-of-service",
                          oscarsProps.getProperty("lsp_class-of-service"));
        this.l3HashMap.put("lsp_setup-priority",
                oscarsProps.getProperty("lsp_setup-priority"));
        this.l3HashMap.put("lsp_reservation-priority",
                oscarsProps.getProperty("lsp_reservation-priority"));
        this.l3HashMap.put("internal_interface_filter",
                oscarsProps.getProperty("internal_interface_filter"));
        this.l3HashMap.put("external_interface_filter",
                oscarsProps.getProperty("external_interface_filter"));
        this.l3HashMap.put("dscp", oscarsProps.getProperty("dscp"));
        this.l3HashMap.put("policer_burst-size-limit",
                oscarsProps.getProperty("policer_burst-size-limit"));
        this.l3HashMap.put("firewall_filter_marker",
                oscarsProps.getProperty("firewall_filter_marker"));
        // no router configuration attempted in tests
        this.l3HashMap.put("source-address", CommonParams.getSrc());
        this.l3HashMap.put("destination-address", CommonParams.getDest());
        this.l3HashMap.put("from", CommonParams.getSrc());
        this.l3HashMap.put("to", CommonParams.getDest());
        this.l3HashMap.put("bandwidth", CommonParams.getBandwidth());
        this.l3HashMap.put("protocol", "tcp");

        this.setupL3FileName = System.getenv("CATALINA_HOME") +
                                 "/shared/classes/server/" +
                                 oscarsProps.getProperty("setupL3Template");
        this.teardownL3FileName =  System.getenv("CATALINA_HOME") +
                                 "/shared/classes/server/" +
                                 oscarsProps.getProperty("teardownL3Template");
        // layer 2 parameters
        this.l2HashMap = new HashMap<String,String>();
        this.l2HashMap.put("resv-id", oscarsProps.getProperty("login") + "1");
        this.l2HashMap.put("bandwidth", CommonParams.getBandwidth());
        this.l2HashMap.put("vlan_id", CommonParams.getVlanId());
        this.l2HashMap.put("community", CommonParams.getCommunity());
        this.l2HashMap.put("lsp_from", CommonParams.getSrc());
        this.l2HashMap.put("lsp_to", CommonParams.getDest());
        this.l2HashMap.put("egress-rtr-loopback", CommonParams.getDest());
        this.l2HashMap.put("interface", CommonParams.getInterface());
        this.l2HashMap.put("port", CommonParams.getPort());
        this.l2HashMap.put("lsp_class-of-service",
                          oscarsProps.getProperty("lsp_class-of-service"));
        this.l2HashMap.put("policer_burst-size-limit",
                oscarsProps.getProperty("policer_burst-size-limit"));
        this.l2HashMap.put("lsp_setup-priority",
                oscarsProps.getProperty("lsp_setup-priority"));
        this.l2HashMap.put("lsp_reservation-priority",
                oscarsProps.getProperty("lsp_reservation-priority"));
        this.l2HashMap.put("firewall_filter_marker",
                oscarsProps.getProperty("firewall_filter_marker"));

        this.setupL2FileName = System.getenv("CATALINA_HOME") +
                                 "/shared/classes/server/" +
                                 oscarsProps.getProperty("setupL2Template");
        this.teardownL2FileName =  System.getenv("CATALINA_HOME") +
                                 "/shared/classes/server/" +
                                 oscarsProps.getProperty("teardownL2Template");
        this.th = new TemplateHandler();
    }

  @Test
    public void basicL3Setup()
            throws IOException, JDOMException, PSSException {

        StringBuilder sb = new StringBuilder();

        this.log.info("testBasicL3Setup.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.l3HashMap);
        hm.put("lsp_description", "testBasicSetup");

        this.log.info("configureLSP.start");
        for (String key: hm.keySet()) {
            sb.append(key + ": " + hm.get(key) + "\n");
        }
        this.log.info(sb.toString());

        Document doc = this.th.fillTemplate(hm, null, this.setupL3FileName);
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        this.log.info("testBasicL3Setup.finish");
        assert doc != null;
    }

  @Test
    public void basicL2Setup()
            throws IOException, JDOMException, PSSException {

        StringBuilder sb = new StringBuilder();

        this.log.info("testBasicL2Setup.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.l2HashMap);
        hm.put("lsp_description", "testBasicL2Setup");

        this.log.info("configureLSP.start");
        for (String key: hm.keySet()) {
            sb.append(key + ": " + hm.get(key) + "\n");
        }
        this.log.info(sb.toString());

        List<String> hops = new ArrayList<String>();
        // these are just used to fill in the template as a test
        hops.add("urn:ogf:network:domain=es.net:node=chi-sl-sdn1:port=ge-1/0/0:link=*");
        hops.add("urn:ogf:network:domain=es.net:node=chi-sl-sdn1:port=ge-2/1/0:link=ge-2/1/0.1803");
        hops.add("urn:ogf:network:domain=es.net:node=chi-sl-mr1:port=TenGigabitEthernet2/1:link=TenGigabitEthernet2/1.1803");
        hops.add("urn:ogf:network:domain=es.net:node=chi-sl-mr1:port=TenGigabitEthernet9/1:link=TenGigabitEthernet9/1.1804");
        hops.add("urn:ogf:network:domain=es.net:node=chic-sdn1:port=TenGigabitEthernet1/1:link=TenGigabitEthernet1/1.1804");
        hops.add("urn:ogf:network:domain=es.net:node=chic-sdn1:port=TenGigabitEthernet7/3:link=*");

        Document doc = this.th.fillTemplate(hm, hops, this.setupL2FileName);
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        this.log.info("testBasicL2Setup.finish");
        assert doc != null;
    }

  @Test(dependsOnMethods={ "basicL3Setup" })
    public void basicL3Teardown()
            throws IOException, JDOMException, PSSException {

        this.log.info("testBasicL3Teardown.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.l3HashMap);
        Document doc = this.th.fillTemplate(hm, null, this.teardownL3FileName);
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        this.log.info("testBasicL3Teardown.finish");
        assert doc != null;
    }

  @Test(dependsOnMethods={ "basicL2Setup" })
    public void basicL2Teardown()
            throws IOException, JDOMException, PSSException {

        this.log.info("testBasicL2Teardown.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.l2HashMap);
        Document doc = this.th.fillTemplate(hm, null, this.teardownL2FileName);
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        this.log.info("testBasicL2Teardown.finish");
        assert doc != null;
    }

  @Test
    public void explicitPathL3Setup()
            throws IOException, JDOMException, PSSException {

        StringBuilder sb = new StringBuilder();

        this.log.info("explicitPathL3Setup.start");
        Map<String,String> hm =
            new HashMap<String,String>(this.l3HashMap);
        hm.put("lsp_description", "explicitPathSetup");

        this.log.info("configureLSP.start");
        for (String key: hm.keySet()) {
            sb.append(key + ": " + hm.get(key) + "\n");
        }
        this.log.info(sb.toString());

        List<String> hops = new ArrayList<String>();
        // these are just used to fill in the template as a test
        hops.add("134.55.75.94");  // ingress
        hops.add("134.55.209.21");
        hops.add("134.55.218.30");
        hops.add("134.55.217.2");
        hops.add("134.55.207.37");
        hops.add("134.55.209.54");
        hops.add("134.55.219.26");

        Document doc = this.th.fillTemplate(hm, hops, this.setupL3FileName);
        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        outputter.setFormat(format);
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + logOutput);
        this.log.info("explicitPathL3Setup.finish");
        assert doc != null;
    }

}
