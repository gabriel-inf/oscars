package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;

import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.database.HibernateUtil;

/**
 * This class updates the test database from a file previously exported
 * from the production database by TopologyXMLExporterTest.  Many tests
 * depend on this test, so it belongs to a number of groups.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Evangelos Chaniotakis (haniotak /at/ es dot net
 */

@Test(groups={ "bss", "pathfinder.traceroute", "importTopology" },
               dependsOnGroups={ "exportTopology" } )
public class TopologyXMLFileReaderTest {
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void importTopology() {
        String fname = GlobalParams.getExportedTopologyFname();
        // has its own session factory
        TopologyXMLFileReader reader = new TopologyXMLFileReader(this.dbname);
        reader.importFile(fname);
    }

  // not currently working
  @Test(dependsOnMethods={ "importTopology" })
    public void importedPortList() {
        // make sure things got populated
        this.sf.getCurrentSession().beginTransaction();
        PortDAO dao = new PortDAO(this.dbname);
        List<Port> ports = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !ports.isEmpty();
    }
}
