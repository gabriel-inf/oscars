package net.es.oscars.pathfinder.overlay;

import org.testng.annotations.*;
import org.testng.Assert;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;

import net.es.oscars.PropHandler;
import net.es.oscars.GlobalParams;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pathfinder.PathfinderException;

/**
 * This class tests methods in OverlayPathfinder.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder.overlay" }, dependsOnGroups={ "importTopology" } )
public class OverlayPathfinderTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;
    private OverlayPathfinder pf;

  @BeforeClass
    protected void setUpClass() {
        // database needed for read-only transactions involving loopbacks
        // at some point using a cache would be better
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.pf = new OverlayPathfinder(this.dbname);
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

  @Test
    public void layer2ERO() throws PathfinderException {
        PathInfo pathInfo = new PathInfo();
        Layer2Info layer2Info = new Layer2Info();
        layer2Info.setSrcEndpoint(this.props.getProperty("layer2SSrc"));
        layer2Info.setDestEndpoint(this.props.getProperty("layer2Dest"));
        pathInfo.setLayer2Info(layer2Info);
        String[] pathHops = this.props.getProperty("layer2Path").split(", ");
        CtrlPlanePathContent path = new CtrlPlanePathContent();
        for (int i=0; i < pathHops.length; i++) {
            pathHops[i] = pathHops[i].trim();
            CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
            hop.setId(String.valueOf(i));
            hop.setLinkIdRef(pathHops[i]);
            path.addHop(hop);
        } 
        pathInfo.setPath(path);
        // only used to satisfy the interface
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
