package net.es.oscars.pathfinder.dragon;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in TERCEPathfinder.java.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
@Test(groups={ "pathfinder.dragon" })
public class TERCEPathfinderTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

}
