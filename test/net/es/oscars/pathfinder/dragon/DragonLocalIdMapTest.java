package net.es.oscars.pathfinder.dragon;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in DragonLocalIdMapDAO.java, which requires a
 * working DragonLocalIdMap.java and DragonLocalIdMap.hbm.xml.
 *
 * @author Andrew Lake, David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder", "narb" })
public class DragonLocalIdMapTest {
    private Properties props;
    private SessionFactory sf;

}
