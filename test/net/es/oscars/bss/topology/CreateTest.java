package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests creating BSS topology database entries.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "create" })
public class CreateTest {
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
    public void domainCreate() {
        Domain domain1 = new Domain();
        Domain domain2 = new Domain();
        domain1.setName(this.props.getProperty("domainName"));
        domain1.setAbbrev(this.props.getProperty("abbrev"));
        domain1.setTopologyIdent(this.props.getProperty("asNum").trim());
        domain1.setUrl(this.props.getProperty("url"));
        domain1.setLocal(true);

        domain2.setName("test");
        domain2.setAbbrev("test");
        domain2.setTopologyIdent("test suite");
        domain2.setUrl("test");
        domain2.setLocal(false);
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        this.sf.getCurrentSession().beginTransaction();
        domainDAO.create(domain1);
        domainDAO.create(domain2);
        this.sf.getCurrentSession().getTransaction().commit();
        assert domain1.getId() != null;
        assert domain2.getId() != null;
    }

  @Test(dependsOnMethods={ "domainCreate" })
    public void nodeCreate() {
        this.sf.getCurrentSession().beginTransaction();
        NodeDAO dao = new NodeDAO(this.dbname);
        Node node = new Node();
        node.setValid(true);
        node.setTopologyIdent("test suite");
        DomainDAO domainDAO = new DomainDAO(this.dbname);
        Domain domain = domainDAO.queryByParam("topologyIdent", "test suite");
        node.setDomain(domain);
        dao.create(node);
        this.sf.getCurrentSession().getTransaction().commit();
        assert node != null;
    }

  @Test(dependsOnMethods={ "nodeCreate" })
    public void nodeAddressCreate() {
        this.sf.getCurrentSession().beginTransaction();
        NodeAddressDAO dao = new NodeAddressDAO(this.dbname);
        NodeAddress nodeAddress = new NodeAddress();
        nodeAddress.setAddress("test suite");
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = nodeDAO.queryByParam("topologyIdent", "test suite");
        nodeAddress.setNode(node);
        dao.create(nodeAddress);
        this.sf.getCurrentSession().getTransaction().commit();
        assert nodeAddress != null;
    }

  @Test(dependsOnMethods={ "nodeCreate" })
    public void portCreate() {
        this.sf.getCurrentSession().beginTransaction();
        PortDAO dao = new PortDAO(this.dbname);
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = nodeDAO.queryByParam("topologyIdent", "test suite");

        Port port = new Port();
        port.setValid(true);
        port.setSnmpIndex(0);
        port.setTopologyIdent("test suite");
        port.setCapacity(10000000L);
        port.setMaximumReservableCapacity(5000000L);
        port.setMinimumReservableCapacity(1000000L);
        port.setGranularity(1000000L);
        port.setUnreservedCapacity(5000000L);
        port.setNode(node);
        dao.create(port);
        this.sf.getCurrentSession().getTransaction().commit();
        assert port != null;
    }

  @Test(dependsOnMethods={ "portCreate" })
    public void linkCreate() {
        this.sf.getCurrentSession().beginTransaction();
        LinkDAO dao = new LinkDAO(this.dbname);
        PortDAO portDAO = new PortDAO(this.dbname);
        Port port = portDAO.queryByParam("topologyIdent", "test suite");

        Link link = new Link();
        link.setValid(true);
        link.setSnmpIndex(0);
        link.setCapacity(10000000L);
        link.setMaximumReservableCapacity(5000000L);
        link.setTopologyIdent("test suite");
        link.setPort(port);
        dao.create(link);
        this.sf.getCurrentSession().getTransaction().commit();
        assert link != null;
    }

  @Test(dependsOnMethods={ "linkCreate" })
    public void ipaddrCreate() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = new Ipaddr();
        ipaddr.setValid(true);
        ipaddr.setIP(this.props.getProperty("ingressNode"));
        LinkDAO linkDAO = new LinkDAO(this.dbname);
        Link link = (Link)
            linkDAO.queryByParam("topologyIdent", "test suite");
        ipaddr.setLink(link);
        dao.create(ipaddr);
        this.sf.getCurrentSession().getTransaction().commit();
        assert ipaddr != null;
    }
}
