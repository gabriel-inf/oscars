package net.es.oscars.database;

import java.util.List;
import java.util.ArrayList;

import org.testng.annotations.*;

/**
 * All database tests require that the initdb group be included in the suite.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "initdb" })
public class DBTests {

  @BeforeSuite
    public void setUpDB() {
        System.err.println("setUpDB");
        Initializer initializer = new Initializer();
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("testaaa");
        dbnames.add("testbss");
        initializer.initDatabase(dbnames);
    }

  @AfterSuite
    public void teardownDB() {
        System.err.println("tearDownDB");
        /* If these run, there are error messages in the hibernate log
         * each time the tests are run.  If these are commented out, the
         * error messages don't appear, but the number of aborted clients
         * increases in MySQL. For now, choosing the former. */
        HibernateUtil.closeSessionFactory("testaaa");
        HibernateUtil.closeSessionFactory("testbss");
    }
}
