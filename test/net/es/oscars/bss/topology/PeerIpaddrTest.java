package net.es.oscars.bss.topology;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in PeerIpaddrDAO.java, which requires a working
 *     PeerIpaddr.java and PeerIpaddr.hbm.xml.
 *
 * @author Andrew Lake, David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss/topology", "peerIpaddr" })
public class PeerIpaddrTest {
    private Properties props;
    private SessionFactory sf;
    private String dbname;

}
