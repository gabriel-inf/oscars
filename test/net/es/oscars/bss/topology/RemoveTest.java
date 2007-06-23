package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests removal of BSS topology database entries, including the use of 
 * associations between Node, Port, and Ipaddr.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "remove" }, dependsOnGroups={ "ipaddr", "port", "node", "domain" })
public class RemoveTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = "testbss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void domainRemove() {
        this.sf.getCurrentSession().beginTransaction();
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        // remove one of the institutions
        Domain domain2 =
                (Domain) domainDAO.queryByParam("name", "test");
        domainDAO.remove(domain2);
        domain2 = (Domain) domainDAO.queryByParam("name", "test");
        this.sf.getCurrentSession().getTransaction().commit();
        assert domain2 == null;
    }

  @Test(dependsOnMethods={ "domainRemove" })
    public void nodeRemove() {
        this.sf.getCurrentSession().beginTransaction();
        String nodeName = "test";
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = (Node) nodeDAO.queryByParam("name", nodeName);
        nodeDAO.remove(node);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "nodeRemove" })
    public void cascadingDeletedPort() {
        this.sf.getCurrentSession().beginTransaction();
        String description = "test suite";
        PortDAO portDAO = new PortDAO(this.dbname);
        Port port = (Port)
            portDAO.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with node testRemove, this will
        // be null
        assert port == null;
    }

  // done here now that no longer needed
  // note that deletion is not cascaded anymore for ipaddrs
  @Test(dependsOnMethods={ "nodeRemove" })
    public void ipaddrRemove() {
        this.sf.getCurrentSession().beginTransaction();
        String description = "test suite";
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr =
            (Ipaddr) ipaddrDAO.queryByParam("description", description);
        ipaddrDAO.remove(ipaddr);
        ipaddr = (Ipaddr) ipaddrDAO.queryByParam("description", description);
        assert ipaddr == null;
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
