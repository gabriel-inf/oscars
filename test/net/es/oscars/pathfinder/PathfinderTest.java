package net.es.oscars.pathfinder;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in Pathfinder.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder" })
public class PathfinderTest {
    private Properties props;
    private SessionFactory sf;
    private Pathfinder pf;

  @BeforeClass
    protected void setUpClass() {
        this.sf = HibernateUtil.getSessionFactory("bss");
        this.pf = new Pathfinder();
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }
}
