package net.es.oscars.pss.eompls.test;

import net.es.oscars.database.hibernate.HibernateUtil;
import net.es.oscars.database.hibernate.Initializer;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.topology.PathTools;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Test(groups={ "db", "init", "vpls", "alu-vpls" })
public class InitTest {
    private static ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
    private static String context = ConfigDefaults.CTX_TESTING;
 
    private static String username = null;
    private static String password = null;
    private static String dbname = null;
    private static String monitor = null;
    private static Logger log = null;
    public static SessionFactory sf;

    @BeforeSuite
    public void setUpTests() {
      System.out.println("starting pss tests");
      cc.setContext(context);
      cc.setServiceName(ServiceNames.SVC_PSS);
      try {
          cc.loadManifest(new File("config/"+ConfigDefaults.MANIFEST));
          cc.setLog4j();
          // need to do this after the log4j.properties file has been set
          log = Logger.getLogger(InitTest.class);
          log.debug("starting tests");

          String configFile =cc.getFilePath("config-eompls.yaml");
          log.debug("eompls config file for tests: "+configFile);
          Map config = ConfigHelper.getConfiguration(configFile);
          assert config != null : "No configuration";
          Map database = (Map) config.get("database");
          assert database != null : "No database stanza in configuration";
          username = (String) database.get("username");
          assert username != null : "No user name in configuration";
          password = (String) database.get("password");
          assert password != null : "No password in configuration";
          dbname = (String) database.get("dbname");
          assert dbname != null: "No dbname in configuration";
          monitor = (String) database.get("monitor");
          log.debug("dbname: ["+dbname+"]");


          configFile = cc.getFilePath(ConfigDefaults.CONFIG);
          log.debug("general config file for tests: "+configFile);
          ConfigHolder.loadConfig(configFile);
          PathTools.setLocalDomainId("foo.net");


      } catch (ConfigException ex) {
          System.out.println("caught ConfigurationException " + ex.getMessage());
          System.exit(-1);
      }
      Initializer initializer = new Initializer();
      List<String> dbnames = new ArrayList<String>();
      dbnames.add(dbname);
      initializer.initDatabase(dbnames, username, password, monitor, ServiceNames.SVC_PSS);
      sf = HibernateUtil.getSessionFactory(dbname);

    }

    @AfterSuite
    public void teardownTests() {
        /* If these run, there are error messages in the hibernate log
         * each time the tests are run.  If these are commented out, the
         * error messages don't appear, but the number of aborted clients
         * increases in MySQL.. */
        HibernateUtil.closeSessionFactory(dbname);
    }
}
