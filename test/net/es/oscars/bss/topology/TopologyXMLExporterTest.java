package  net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import java.io.*;

import org.hibernate.*;
import org.jdom.*;
import org.jdom.output.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.*;

/**
 * This class tests exporting the production topology database into the
 * interdomain XML format.  The resulting file is written into /tmp
 * The next test run, TopologyXMLFileReaderTest, reads that file into
 * the test database.  Many tests depend on this test, so it belongs to a number
 * of groups.
 * @author David Robertson (dwrobertson@lbl.gov), Evangelos Chaniotakis (haniotak /at/ es dot net
 */

@Test(groups={ "bss", "bss.topology", "pathfinder.traceroute", "pathfinder.overlay",
               "pathfinder.db", "exportTopology" })
public class TopologyXMLExporterTest {
    private String dbname;
    private SessionFactory sf;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = "bss";
        List<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void exportTopology() throws java.io.IOException {

        this.sf.getCurrentSession().beginTransaction();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain localDomain = domainDAO.getLocalDomain();
        // Material having to do with the ipaddrs table is for the
        // tests only.  That table is not part of the network
        // topology.
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        List<Ipaddr> ipaddrs = ipaddrDAO.list();
        // Copy the ipaddrs table into the temporary file.  The
        // fully qualified topology identifier will be used to look up
        // the link on import.
        String fname = GlobalParams.getExportedIpaddrFname();
        PrintWriter outputStream = new PrintWriter(new FileWriter(fname));
        for (Ipaddr ipaddr: ipaddrs) {
            if (!ipaddr.isValid()) {
                continue;
            }
            String output = String.valueOf(ipaddr.isValid());
            output += " " + ipaddr.getIP();
            Link link = ipaddr.getLink();
            if (link == null) {
                continue;
            }
            Port port = link.getPort();
            Node node = port.getNode();
            Domain domain = node.getDomain();
            output += " urn:ogf:network:" +
                      domain.getTopologyIdent() + ":" +
                      node.getTopologyIdent() + ":" +
                      port.getTopologyIdent() + ":" +
                      link.getTopologyIdent();
            outputStream.println(output);
        }
        outputStream.close();
        // do topology export
        //String urn = "urn:ogf:network:domain=" + localDomain.getTopologyIdent();
        String urn = localDomain.getTopologyIdent();
        TopologyXMLExporter exporter = new TopologyXMLExporter(this.dbname);
        
        Document doc = exporter.getTopology(urn);
        this.sf.getCurrentSession().getTransaction().commit();

        fname = GlobalParams.getExportedTopologyFname();
        FileWriter writer = new FileWriter(fname);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(doc, writer);
    }

  @AfterClass
    protected void tearDownClass() {
        // this is the only test that uses the production database
        HibernateUtil.closeSessionFactory("bss");
    }
}
