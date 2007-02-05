package net.es.oscars.bss.topology;

import junit.framework.*;

import java.util.*;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in RouterDAO.java, which requires a working
 *     Router.java and Router.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class RouterTest extends TestCase {
    private Properties props;
    private Session session;
    private RouterDAO dao;

    public RouterTest(String name) {
        super(name);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss", true);
    }
        
    public void setUp() {
        this.session =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        this.dao = new RouterDAO();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }

    public void testCreate() {
        Router router = new Router();
        router.setValid(false);
        router.setName(this.props.getProperty("routerName"));
        this.dao.create(router);
        this.session.getTransaction().commit();
        Assert.assertNotNull(router);
    }

    public void testQuery() {
        String routerName = this.props.getProperty("routerName");
        Router router = (Router) this.dao.queryByParam("name", routerName);
        this.session.getTransaction().commit();
        Assert.assertNotNull(router);
    }

    public void testList() {
        List<Router> routers = this.dao.list();
        Assert.assertFalse(routers.isEmpty());
    }

    public void testFromIp() {
        Router router = this.dao.fromIp(this.props.getProperty("ipaddrIP"));
        this.session.getTransaction().commit();
        Assert.assertNotNull(router);
    }

    public void testRemove() {
        String routerName = this.props.getProperty("routerName");
        Router router = (Router) this.dao.queryByParam("name", routerName);
        this.dao.remove(router);
        this.session.getTransaction().commit();
        Assert.assertTrue(true);
    }
}
