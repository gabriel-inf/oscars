package net.es.oscars.bss.topology;

import org.testng.annotations.*;
import org.testng.Assert;

import java.util.*;
import java.io.*;

import org.hibernate.*;

import net.es.oscars.GlobalParams;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.database.HibernateUtil;

/**
 * This class updates the test database from a file previously exported
 * from the production database by TopologyXMLExporterTest.  Many tests
 * depend on this test, so it belongs to a number of groups.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Evangelos Chaniotakis (haniotak /at/ es dot net
 */

@Test(groups={ "bss", "pathfinder.overlay", "pathfinder.traceroute",
               "pathfinder.db", "importTopology" },
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
        System.err.println("Starting import of bss topology.  This may take " +
                           "a minute or two.");
        this.sf.getCurrentSession().beginTransaction();
        // use localDomain that was saved during export test
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain d = new Domain();
        d.setTopologyIdent(CommonParams.localDomainId);
        d.setName("test local domain");
        d.setLocal(true);
        d.setAbbrev("test");
        d.setUrl("test URL");
        domainDAO.create(d);
        domainDAO.flush();
        this.sf.getCurrentSession().getTransaction().commit();


        String line = null;
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        domainDAO = new DomainDAO(this.dbname);

        String fname = GlobalParams.getExportedTopologyFname();
        // has its own session factory
        TopologyXMLFileReader reader = new TopologyXMLFileReader(this.dbname);
        reader.importFile(fname);
        // fill in ipaddrs table (not part of the topology)
        fname = GlobalParams.getExportedIpaddrFname();
        BufferedReader ipaddrReader = null;
        try {
            ipaddrReader =
                new BufferedReader(new FileReader(fname));
        } catch (FileNotFoundException e) {
            Assert.fail(e.getMessage());
        }
        this.sf.getCurrentSession().beginTransaction();

        try {
            while ((line=ipaddrReader.readLine()) != null) {
                Ipaddr ipaddr = new Ipaddr();
                String[] fields = line.split(" ");
                if (fields.length != 3) {
                    Assert.fail(fname + " is not in a valid format");
                }
                boolean valid = new Boolean(fields[0].trim()).booleanValue();
                ipaddr.setValid(valid);
                ipaddr.setIP(fields[1].trim());
                Link link = domainDAO.getFullyQualifiedLink(fields[2].trim());
                ipaddr.setLink(link);
                if (link == null) {
                    Assert.fail("cannot get link for ipaddr");
                }
                ipaddrDAO.create(ipaddr);
            }
        } catch (IOException e) {
            this.sf.getCurrentSession().getTransaction().rollback();
            Assert.fail(e.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "importTopology" })
    public void importedPortList() {
        // make sure things got populated
        this.sf.getCurrentSession().beginTransaction();
        PortDAO dao = new PortDAO(this.dbname);
        List<Port> ports = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !ports.isEmpty();
    }

  @Test(dependsOnMethods={ "importTopology" })
    public void importedIpaddrList() {
        // make sure things got populated
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        List<Ipaddr> ipaddrs = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !ipaddrs.isEmpty();
    }
}
