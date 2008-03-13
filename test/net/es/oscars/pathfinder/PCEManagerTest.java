package net.es.oscars.pathfinder;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.wsdlTypes.*;
import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.Reservation;

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
        PathInfo pathInfo = new PathInfo();
        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(this.props.getProperty("srcHost"));
        layer3Info.setDestHost(this.props.getProperty("destHost"));
        pathInfo.setLayer3Info(layer3Info);
        Reservation reservation = new Reservation();
        this.sf.getCurrentSession().beginTransaction();
        try {
            PathInfo intraPath = this.pceMgr.findPath(pathInfo, reservation);
        } catch (PathfinderException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw new PathfinderException(ex.getMessage());
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
