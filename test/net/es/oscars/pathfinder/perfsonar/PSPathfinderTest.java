package net.es.oscars.pathfinder.perfsonar;

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

/**
 * This class tests methods in PSPathfinder.java.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
@Test(groups={ "pathfinder.perfsonar" })
public class PSPathfinderTest {
    private String dbname;
    private SessionFactory sf;
    //private PSPathfinder pf;
    
    @BeforeClass
    protected void setUpClass(){
        // database needed for read-only transactions involving loop backs
        // at some point using a cache would be better
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
    }
    

    public void testFindLocalPath() throws PathfinderException, BSSException{
        this.sf.getCurrentSession().beginTransaction();
        PSPathfinder pf = new PSPathfinder(this.dbname);
        List<Path> pathCalcResults = null;
        Reservation resv = new Reservation();
        CommonReservation common = new CommonReservation();
        PathInfo pathInfo = new PathInfo();
        pathInfo.setPathSetupMode("timer-automatic");
        common.setLayer2Parameters(resv, pathInfo, null, "test");
       
        //TODO: Delete this stuff
        Path path = new Path();
        //required values
        path.setExplicit(false);
        path.setPathType(PathType.INTERDOMAIN);
        path.setDirection(PathDirection.BIDIRECTIONAL);
        PathElem ingr = new PathElem();
        ingr.setSeqNumber(1);
        PathElem egr = new PathElem();
        ingr.setSeqNumber(2);
        ingr.setUrn(pathInfo.getLayer2Info().getSrcEndpoint());
        egr.setUrn(pathInfo.getLayer2Info().getDestEndpoint());
        path.addPathElem(ingr);
        path.addPathElem(egr);
        resv.addPath(path);
        
        pathCalcResults = pf.findLocalPath(resv);
        this.sf.getCurrentSession().getTransaction().commit();
        assert (pathCalcResults != null && (!pathCalcResults.isEmpty()));
        for(Path p : pathCalcResults){
            int seqNum = 0;
            for(PathElem e : p.getPathElems()){
                assert e.getUrn() != null;
                assert e.getSeqNumber() == ++seqNum;
            }
        }
    }
    
    @Test
    public void testFindInterdomainPath() throws PathfinderException, BSSException{
        this.sf.getCurrentSession().beginTransaction();
        PSPathfinder pf = new PSPathfinder(this.dbname);
        List<Path> pathCalcResults = null;
        Reservation resv = new Reservation();
        CommonReservation common = new CommonReservation();
        PathInfo pathInfo = new PathInfo();
        pathInfo.setPathSetupMode("timer-automatic");
        common.setLayer2Parameters(resv, pathInfo, null, "test");
       
        //TODO: Delete this stuff
        Path path = new Path();
        //required values
        path.setExplicit(false);
        path.setPathType(PathType.REQUESTED);
        path.setDirection(PathDirection.BIDIRECTIONAL);
        PathElem ingr = new PathElem();
        ingr.setSeqNumber(1);
        PathElem egr = new PathElem();
        ingr.setSeqNumber(2);
        ingr.setUrn(pathInfo.getLayer2Info().getSrcEndpoint());
        egr.setUrn(pathInfo.getLayer2Info().getDestEndpoint());
        path.addPathElem(ingr);
        path.addPathElem(egr);
        resv.addPath(path);
        
        pathCalcResults = pf.findInterdomainPath(resv);
        this.sf.getCurrentSession().getTransaction().commit();
        assert (pathCalcResults != null && (!pathCalcResults.isEmpty()));
        for(Path p : pathCalcResults){
            int seqNum = 0;
            for(PathElem e : p.getPathElems()){
                assert e.getUrn() != null;
                assert e.getSeqNumber() == ++seqNum;
            }
        }
    }
}
