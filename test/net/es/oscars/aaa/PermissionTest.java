package net.es.oscars.aaa;

import junit.framework.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests access to the permissions table, which requires a working
 *     Permission.java and Permission.hbm.xml.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PermissionTest extends TestCase {
    private Properties props;
    private Session session;
    private PermissionDAO dao;

    public PermissionTest(String name) {
        super(name);
        Initializer initializer = new Initializer();
        initializer.initDatabase();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.aaa", true);
    }
        
    public void setUp() {
        this.dao = new PermissionDAO();
        this.session =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        this.dao.setSession(this.session);
        this.session.beginTransaction();
    }

    public void testQuery() {
        Permission permission = (Permission) this.dao.queryByParam("name",
                                    this.props.getProperty("permissionName"));
        this.session.getTransaction().commit();
        Assert.assertNotNull(permission);
    }

    public void testList() {
        List<Permission> perms = this.dao.findAll();
        this.session.getTransaction().commit();
        Assert.assertFalse(perms.isEmpty());
    }
}
