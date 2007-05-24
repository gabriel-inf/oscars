package net.es.oscars.pathfinder.traceroute;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pathfinder.CommonPathElem;
import net.es.oscars.pathfinder.PathfinderException;

/**
 * This class tests methods in TraceroutePathfinder.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder", "traceroute" })
public class TraceroutePathfinderTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;
    private TraceroutePathfinder pf;

  @BeforeClass
    protected void setUpClass() {
        // database needed for read-only transactions involving loopbacks
        // at some point using a cache would be better
        this.dbname = "bss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.pf = new TraceroutePathfinder(this.dbname);
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.pathfinder", true);
    }

  @Test
    public void testFindPath1() throws PathfinderException {
        String srcHost = this.props.getProperty("srcHost");
        String destHost = this.props.getProperty("destHost");
        String ingressRouterIP = null;
        String egressRouterIP = null;
        List<CommonPathElem> pathElems = null;
        this.sf.getCurrentSession().beginTransaction();
        try {
            pathElems = this.pf.findPath(srcHost, destHost, ingressRouterIP,
                                    egressRouterIP);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathElems != null;
    }

  @Test
    public void testFindPath2() throws PathfinderException {
        String srcHost = this.props.getProperty("srcHost");
        String destHost = this.props.getProperty("destHost");
        String ingressRouterIP = this.props.getProperty("ingressRouter");
        String egressRouterIP = null;
        List<CommonPathElem> pathElems = null;
        this.sf.getCurrentSession().beginTransaction();
        try {
            pathElems = this.pf.findPath(srcHost, destHost, ingressRouterIP,
                                    egressRouterIP);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathElems != null;
    }

  @Test
    public void testFindPath3() throws PathfinderException {
        String srcHost = this.props.getProperty("srcHost");
        String destHost = this.props.getProperty("destHost");
        String ingressRouterIP = null;
        String egressRouterIP = this.props.getProperty("egressRouter");
        List<CommonPathElem> pathElems = null;
        this.sf.getCurrentSession().beginTransaction();
        try {
            pathElems = this.pf.findPath(srcHost, destHost, ingressRouterIP,
                                    egressRouterIP);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathElems != null;
    }

  @Test
    public void testFindPath4() throws PathfinderException {
        String srcHost = this.props.getProperty("srcHost");
        String destHost = this.props.getProperty("destHost");
        String ingressRouterIP = this.props.getProperty("ingressRouter");
        String egressRouterIP = this.props.getProperty("egressRouter");
        List<CommonPathElem> pathElems = null;
        this.sf.getCurrentSession().beginTransaction();
        try {
            pathElems = this.pf.findPath(srcHost, destHost, ingressRouterIP,
                                    egressRouterIP);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathElems != null;
    }
}
