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
 * This test runs after all the Hibernate bean tests.
 * The next test run, TopologyXMLFileReaderTest, reads that file into
 * the test database.  All tests in bss (up a directory) use that database to
 * avoid * possible corruption of the production database.
 * @author David Robertson (dwrobertson@lbl.gov), Evangelos Chaniotakis (haniotak /at/ es dot net
 */

@Test(groups={ "bss.topology", "exportTopology" }, dependsOnGroups={ "remove" })
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
        //String urn = "urn:ogf:network:domain=" + localDomain.getTopologyIdent();
        String urn = localDomain.getTopologyIdent();
        this.sf.getCurrentSession().getTransaction().commit();
        // TopologyXMLExporter has its own session factory
        TopologyXMLExporter exporter = new TopologyXMLExporter(this.dbname);
        
        Document doc = exporter.getTopology(urn);

        String fname = GlobalParams.getExportedTopologyFname();
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
