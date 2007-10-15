package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests removal of BSS topology database entries, including the use of 
 * associations between Node, Port, Link, and Ipaddr.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "remove" }, dependsOnGroups={ "ipaddr", "link", "port", "node", "domain" })
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
        // remove one of the domains
        Domain domain2 =
                (Domain) domainDAO.queryByParam("topologyIdent", "test suite");
        domainDAO.remove(domain2);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "domainRemove" })
    public void nodeRemove() {
        this.sf.getCurrentSession().beginTransaction();
        String topologyIdent = "test suite";
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = (Node) nodeDAO.queryByParam("topologyIdent", topologyIdent);
        nodeDAO.remove(node);
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "nodeRemove" })
    public void cascadingDeletedNodeAddress() {
        this.sf.getCurrentSession().beginTransaction();
        String address = "test suite";
        NodeAddressDAO nodeAddressDAO = new NodeAddressDAO(this.dbname);
        NodeAddress nodeAddress = (NodeAddress)
            nodeAddressDAO.queryByParam("address", address);
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with node testRemove, this will
        // be null
        assert nodeAddress == null;
    }

  @Test(dependsOnMethods={ "nodeRemove" })
    public void cascadingDeletedPort() {
        this.sf.getCurrentSession().beginTransaction();
        String topologyIdent = "test suite";
        PortDAO portDAO = new PortDAO(this.dbname);
        Port port = (Port)
            portDAO.queryByParam("topologyIdent", topologyIdent);
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with node testRemove, this will
        // be null
        assert port == null;
    }

  @Test(dependsOnMethods={ "nodeRemove" })
    public void cascadingDeletedLink() {
        this.sf.getCurrentSession().beginTransaction();
        String topologyIdent = "test suite";
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = (Link)
            linkDAO.queryByParam("topologyIdent", topologyIdent);
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with node testRemove, this will
        // be null
        assert link == null;
    }

  @Test(dependsOnMethods={ "nodeRemove" })
    public void cascadingDeletedIpaddr() {
        this.sf.getCurrentSession().beginTransaction();
        String description = "test suite";
        IpaddrDAO ipaddrDAO = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = (Ipaddr)
            ipaddrDAO.queryByParam("description", description);
        this.sf.getCurrentSession().getTransaction().commit();
        // if cascading delete works with node testRemove, this will
        // be null
        assert ipaddr == null;
    }
}
