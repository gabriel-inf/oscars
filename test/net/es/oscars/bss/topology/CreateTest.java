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
        domain1.setAsNum(Integer.parseInt(this.props.getProperty("asNum").trim()));
        domain1.setUrl(this.props.getProperty("url"));
        domain1.setLocal(true);

        domain2.setName("test");
        domain2.setAbbrev("test");
        domain2.setAsNum(0);
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
        node.setName("test");
        dao.create(node);
        this.sf.getCurrentSession().getTransaction().commit();
        assert node != null;
    }

  @Test(dependsOnMethods={ "nodeCreate" })
    public void portCreate() {
        this.sf.getCurrentSession().beginTransaction();
        PortDAO dao = new PortDAO(this.dbname);
        String nodeName = "test";
        NodeDAO nodeDAO = new NodeDAO(this.dbname);
        Node node = nodeDAO.queryByParam("name", nodeName);

        Port port = new Port();
        port.setValid(true);
        port.setSnmpIndex(0);
        port.setMaximumCapacity(10000000L);
        port.setDescription("test suite");
        port.setNode(node);
        dao.create(port);
        this.sf.getCurrentSession().getTransaction().commit();
        assert port != null;
    }

  @Test(dependsOnMethods={ "portCreate" })
    public void ipaddrCreate() {
        this.sf.getCurrentSession().beginTransaction();
        IpaddrDAO dao = new IpaddrDAO(this.dbname);
        Ipaddr ipaddr = new Ipaddr();
        ipaddr.setValid(true);
        ipaddr.setIP(this.props.getProperty("ingressNode"));
        ipaddr.setDescription("test suite");
        PortDAO portDAO = new PortDAO(this.dbname);
        Port port = (Port)
            portDAO.queryByParam("description", "test suite");
        ipaddr.setPort(port);
        dao.create(ipaddr);
        this.sf.getCurrentSession().getTransaction().commit();
        assert ipaddr != null;
    }
}
