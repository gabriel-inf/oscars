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
        dbnames.add("aaa");
        dbnames.add("bss");
        initializer.initDatabase(dbnames);
    }

  @AfterSuite
    public void teardownDB() {
        System.err.println("tearDownDB");
        HibernateUtil.closeSessionFactory("aaa");
        HibernateUtil.closeSessionFactory("bss");
    }
}
