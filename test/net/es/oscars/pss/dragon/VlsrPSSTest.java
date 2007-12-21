package net.es.oscars.pss.dragon;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in VlsrPSS.java.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
@Test(groups={ "pss.dragon" })
public class VlsrPSSTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

}
