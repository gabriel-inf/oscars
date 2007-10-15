package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in NodeDAO.java, which requires a working
 *     Node.java and Node.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "node" }, dependsOnGroups={ "create" })
public class NodeTest {
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
    public void nodeQuery() {
        this.sf.getCurrentSession().beginTransaction();
        NodeDAO dao = new NodeDAO(this.dbname);
        String topologyIdent = "test suite";
        Node node = (Node) dao.queryByParam("topologyIdent", topologyIdent);
        this.sf.getCurrentSession().getTransaction().commit();
        assert node != null;
    }

  @Test
    public void nodeList() {
        this.sf.getCurrentSession().beginTransaction();
        NodeDAO dao = new NodeDAO(this.dbname);
        List<Node> nodes = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !nodes.isEmpty();
    }

  @Test
    public void nodeFromIp() {
        this.sf.getCurrentSession().beginTransaction();
        NodeDAO dao = new NodeDAO(this.dbname);
        Node node = dao.fromIp(this.props.getProperty("ingressNode"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert node != null;
    }
}
