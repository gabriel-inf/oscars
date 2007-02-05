package net.es.oscars.bss.topology;

import junit.framework.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;


/**
 * This class tests access to the interfaces table, which requires a working
 *     Interface.java and Interface.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class InterfaceTest extends TestCase {
    private Properties props;
    private Session session;
    private InterfaceDAO dao;

    public InterfaceTest(String name) {
        super(name);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.bss", true);
    }
        
    public void setUp() {
        this.session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        this.dao = new InterfaceDAO();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }

    public void testCreate() {
        String routerName = this.props.getProperty("routerName");
        RouterDAO routerDAO = new RouterDAO();
        routerDAO.setSession(this.session);
        Router router = routerDAO.queryByParam("name", routerName);

        Interface xface = new Interface();
        xface.setValid(false);
        xface.setSnmpIndex(0);
        xface.setSpeed(10000000L);
        xface.setDescription(this.props.getProperty("xfaceDescription"));
        xface.setRouter(router);
        this.dao.create(xface);
        this.session.getTransaction().commit();
        Assert.assertNotNull(xface);
    }

    public void testQuery() {
        String description = this.props.getProperty("xfaceDescription");
        Interface xface = (Interface)
            this.dao.queryByParam("description", description);
        this.session.getTransaction().commit();
        Assert.assertNotNull(xface);
    }

    public void testList() {
        List<Interface> interfaces = this.dao.list();
        this.session.getTransaction().commit();
        Assert.assertFalse(interfaces.isEmpty());
    }

    public void testCascadingDelete() {
        String description = this.props.getProperty("xfaceDescription");
        Interface xface = (Interface)
            this.dao.queryByParam("description", description);
        this.session.getTransaction().commit();
        // if cascading delete works with router testRemove, this will
        // be null
        Assert.assertNull(xface);
    }
}
