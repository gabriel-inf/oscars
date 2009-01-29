package net.es.oscars.pathfinder.staticroute;

import java.util.List;

import net.es.oscars.GlobalParams;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.CommonReservation;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathDirection;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pathfinder.PathfinderException;
import net.es.oscars.wsdlTypes.PathInfo;

import org.hibernate.SessionFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups={ "pathfinder.staticroute" })
public class StaticDBInterPathfinderTest {
    private String dbname;
    private SessionFactory sf;
    private StaticDBInterPathfinder pf;
    
    @BeforeClass
    protected void setUpClass() {
        // database needed for read-only transactions involving loop backs
        // at some point using a cache would be better
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.pf = new StaticDBInterPathfinder(this.dbname);
    }
    
    @Test
    public void testFindInterPath() throws PathfinderException, BSSException{
        List<Path> pathCalcResults = null;
        Reservation resv = new Reservation();
        CommonReservation common = new CommonReservation();
        PathInfo pathInfo = new PathInfo();
        pathInfo.setPathSetupMode("timer-automatic");
        common.setLayer2Parameters(resv, pathInfo, null, "test");

        //TODO: Delete this stuff
        Path path = new Path();
        //required values
        path.setPathType(PathType.REQUESTED);
        path.setDirection(PathDirection.BIDIRECTIONAL);
        PathElem ingr = new PathElem();
        
        PathElem mid = new PathElem();
        
        PathElem egr = new PathElem();
        ingr.setUrn(pathInfo.getLayer2Info().getSrcEndpoint());
        mid.setUrn("urn:ogf:network:domain=dcn.internet2.edu:node=CHIC");
        egr.setUrn(pathInfo.getLayer2Info().getDestEndpoint());
        path.addPathElem(ingr);
        //path.addPathElem(mid);
        path.addPathElem(egr);
        resv.setPath(path);
        
        this.sf.getCurrentSession().beginTransaction();
        try {
            pathCalcResults = this.pf.findInterdomainPath(resv);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            ex.printStackTrace();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        
        assert (pathCalcResults != null && (!pathCalcResults.isEmpty()));
    }
}
