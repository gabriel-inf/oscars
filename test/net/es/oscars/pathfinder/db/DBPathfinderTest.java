package net.es.oscars.pathfinder.db;

import org.testng.annotations.*;
import org.testng.Assert;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

import net.es.oscars.PropHandler;
import net.es.oscars.GlobalParams;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.CommonReservation;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.pathfinder.PathfinderException;

/**
 * This class tests methods in DBPathfinder.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder.db" }, dependsOnGroups={ "importTopology" } )
public class DBPathfinderTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;
    private DBPathfinder pf;

  @BeforeClass
    protected void setUpClass() {
        // database needed for read-only transactions involving loopbacks
        // at some point using a cache would be better
        this.dbname = GlobalParams.getReservationTestDBName();
        this.sf = HibernateUtil.getSessionFactory(this.dbname);
        this.pf = new DBPathfinder(this.dbname);
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

  @Test
    public void dbPathSetup() throws PathfinderException {
        PathInfo pathInfo = new PathInfo();
        Reservation resv = new Reservation();
        CommonReservation common = new CommonReservation();
        common.setLayer2Parameters(resv, pathInfo, "any",
                                  "DBPathfinder test reservation");
        this.sf.getCurrentSession().beginTransaction();
        try {
            List<Path> intraPath = this.pf.findLocalPath(resv);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert pathInfo.getPath() != null;
    }
}
