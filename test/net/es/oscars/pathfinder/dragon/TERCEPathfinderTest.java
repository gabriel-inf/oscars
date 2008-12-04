package net.es.oscars.pathfinder.dragon;

import net.es.oscars.GlobalParams;
import net.es.oscars.bss.CommonReservation;
import net.es.oscars.bss.Reservation;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pathfinder.PathfinderException;
import net.es.oscars.wsdlTypes.PathInfo;

import org.hibernate.SessionFactory;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.testng.annotations.*;

/**
 * This class tests methods in TERCEPathfinder.java.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
@Test(groups={ "pathfinder.dragon" })
public class TERCEPathfinderTest {
    private String dbname;
    private SessionFactory sf;
    private TERCEPathfinder pf;
    
    @BeforeClass
    protected void setUpClass() {
        // database needed for read-only transactions involving loopbacks
        // at some point using a cache would be better
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.pf = new TERCEPathfinder(this.dbname);
    }
    
    @Test
    public void testFindPath() throws PathfinderException{
        Reservation resv = new Reservation();
        CommonReservation common = new CommonReservation();
        PathInfo pathInfo = new PathInfo();
        pathInfo.setPathSetupMode("timer-automatic");
        common.setLayer2Parameters(resv, pathInfo, null, "test");
       
        //TODO: Delete this stuff
        CtrlPlanePathContent path = new CtrlPlanePathContent();
        CtrlPlaneHopContent ingr = new CtrlPlaneHopContent();
        CtrlPlaneHopContent egr = new CtrlPlaneHopContent();
        ingr.setLinkIdRef(pathInfo.getLayer2Info().getSrcEndpoint());
        egr.setLinkIdRef(pathInfo.getLayer2Info().getDestEndpoint());
        path.addHop(ingr);
        path.addHop(egr);
        pathInfo.setPath(path);
        
        this.sf.getCurrentSession().beginTransaction();
        try {
            this.pf.findPath(pathInfo, resv);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            ex.printStackTrace();
            throw new PathfinderException(ex.getMessage());
        }
	this.sf.getCurrentSession().getTransaction().commit();
        assert pathInfo.getPath() != null;
    }
}
