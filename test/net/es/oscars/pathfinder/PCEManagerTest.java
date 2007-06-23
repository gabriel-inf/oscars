package net.es.oscars.pathfinder;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.pathfinder.CommonPath;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in PCEManager.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder" })
public class PCEManagerTest {
    private SessionFactory sf;
    private String dbname;
    private PCEManager pceMgr;
    private Properties props;

  @BeforeClass
    protected void setUpClass() {
        this.dbname = "testbss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.pceMgr = new PCEManager(this.dbname);
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

  @Test
    public void testGetPathMethod() throws PathfinderException {
        assert this.pceMgr.getPathMethod() != null;
    }

  @Test
    public void testFindPath() throws PathfinderException {
        String srcHost = this.props.getProperty("srcHost");
        String destHost = this.props.getProperty("destHost");
        String ingressRouterIP = null;
        String egressRouterIP = null;
        CommonPath reqPath = null;
        CommonPath retPath = null;
        this.sf.getCurrentSession().beginTransaction();
        try {
            this.pceMgr.findPath(srcHost, destHost, ingressRouterIP,
                                 egressRouterIP, reqPath);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
