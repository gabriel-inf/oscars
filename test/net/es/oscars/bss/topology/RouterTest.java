package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in RouterDAO.java, which requires a working
 *     Router.java and Router.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "router" })
public class RouterTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss.topology", true);
        this.dbname = "bss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
        
  @Test
    public void routerCreate() {
        this.sf.getCurrentSession().beginTransaction();
        RouterDAO dao = new RouterDAO(this.dbname);
        Router router = new Router();
        router.setValid(true);
        router.setName(this.props.getProperty("routerName"));
        dao.create(router);
        this.sf.getCurrentSession().getTransaction().commit();
        assert router != null;
    }

  @Test(dependsOnMethods={ "routerCreate" })
    public void routerQuery() {
        this.sf.getCurrentSession().beginTransaction();
        RouterDAO dao = new RouterDAO(this.dbname);
        String routerName = this.props.getProperty("routerName");
        Router router = (Router) dao.queryByParam("name", routerName);
        this.sf.getCurrentSession().getTransaction().commit();
        assert router != null;
    }

  @Test(dependsOnMethods={ "routerCreate" })
    public void routerList() {
        this.sf.getCurrentSession().beginTransaction();
        RouterDAO dao = new RouterDAO(this.dbname);
        List<Router> routers = dao.list();
        this.sf.getCurrentSession().getTransaction().commit();
        assert !routers.isEmpty();
    }

  @Test(dependsOnMethods={ "routerCreate" })
    public void routerFromIp() {
        this.sf.getCurrentSession().beginTransaction();
        RouterDAO dao = new RouterDAO(this.dbname);
        Router router = dao.fromIp(this.props.getProperty("routerIP"));
        this.sf.getCurrentSession().getTransaction().commit();
        assert router != null;
    }
}
