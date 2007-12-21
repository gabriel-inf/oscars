package net.es.oscars.pathfinder.traceroute;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.PropHandler;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pathfinder.PathfinderException;

/**
 * This class tests methods in TraceroutePathfinder.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder.traceroute" })
public class TraceroutePathfinderTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;
    private TraceroutePathfinder pf;

  @BeforeClass
    protected void setUpClass() {
        // database needed for read-only transactions involving loopbacks
        // at some point using a cache would be better
        this.dbname = "testbss";
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.pf = new TraceroutePathfinder(this.dbname);
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

  @Test
    public void testFindPath1() throws PathfinderException {
        PathInfo pathInfo = new PathInfo();
        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(this.props.getProperty("srcHost"));
        layer3Info.setDestHost(this.props.getProperty("destHost"));
        pathInfo.setLayer3Info(layer3Info);
        this.sf.getCurrentSession().beginTransaction();
        try {
            boolean isExplict = this.pf.findPath(pathInfo);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathInfo.getPath() != null;
    }

  @Test
    public void testFindPath2() throws PathfinderException {
        PathInfo pathInfo = new PathInfo();
        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(this.props.getProperty("srcHost"));
        layer3Info.setDestHost(this.props.getProperty("destHost"));
        //layer3Info.setIngressNodeIP(this.props.getProperty("ingressNode"));
        pathInfo.setLayer3Info(layer3Info);
        this.sf.getCurrentSession().beginTransaction();
        try {
            boolean isExplicit = this.pf.findPath(pathInfo);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathInfo.getPath() != null;
    }

  @Test
    public void testFindPath3() throws PathfinderException {
        PathInfo pathInfo = new PathInfo();
        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(this.props.getProperty("srcHost"));
        layer3Info.setDestHost(this.props.getProperty("destHost"));
        //layer3Info.setEgressNodeIP(this.props.getProperty("egressNode"));
        pathInfo.setLayer3Info(layer3Info);
        this.sf.getCurrentSession().beginTransaction();
        try {
            boolean isExplicit = this.pf.findPath(pathInfo);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathInfo.getPath() != null;
    }

  @Test
    public void testFindPath4() throws PathfinderException {
        PathInfo pathInfo = new PathInfo();
        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(this.props.getProperty("srcHost"));
        layer3Info.setDestHost(this.props.getProperty("destHost"));
        //layer3Info.setIngressNodeIP(this.props.getProperty("ingressNode"));
        //layer3Info.setEgressNodeIP(this.props.getProperty("egressNode"));
        pathInfo.setLayer3Info(layer3Info);
        this.sf.getCurrentSession().beginTransaction();
        try {
            boolean isExplicit = this.pf.findPath(pathInfo);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathInfo.getPath() != null;
    }
}
