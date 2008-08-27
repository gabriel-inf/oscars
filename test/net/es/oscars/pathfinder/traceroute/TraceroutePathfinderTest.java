package net.es.oscars.pathfinder.traceroute;

import org.testng.annotations.*;
import org.testng.Assert;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

import net.es.oscars.PropHandler;
import net.es.oscars.AuthHandler;
import net.es.oscars.GlobalParams;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pathfinder.PathfinderException;

/**
 * This class tests methods in TraceroutePathfinder.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder.traceroute" },
        dependsOnGroups={ "jnxTraceroute", "importTopology" } )
public class TraceroutePathfinderTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;
    private TraceroutePathfinder pf;

  @BeforeClass
    protected void setUpClass() {
        // database needed for read-only transactions involving loopbacks
        // at some point using a cache would be better
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.pf = new TraceroutePathfinder(this.dbname);
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

  @Test
    public void allowedTest() {
        AuthHandler authHandler = new AuthHandler();
        boolean authorized = authHandler.checkAuthorization();
        Assert.assertTrue(authorized,
            "You are not authorized to do a traceroute from this machine. ");
    }

  @Test(dependsOnMethods={ "allowedTest" })
    public void traceroutePath() throws PathfinderException {
        PathInfo pathInfo = new PathInfo();
        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(this.props.getProperty("srcHost"));
        layer3Info.setDestHost(this.props.getProperty("destHost"));
        pathInfo.setLayer3Info(layer3Info);
        Reservation reservation = new Reservation();
        this.sf.getCurrentSession().beginTransaction();
        try {
            PathInfo intraPath = this.pf.findPath(pathInfo, reservation);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathInfo.getPath() != null;
    }
}
