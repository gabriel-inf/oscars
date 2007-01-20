package net.es.oscars.bss;

import junit.framework.Test;
import junit.framework.TestSuite; 


public class AllTests { 
    public static Test suite() { 
        TestSuite suite = new TestSuite("BSS tests suite");
        suite.addTest(new DomainTest("testQuery"));
        suite.addTest(new DomainTest("testList"));
        suite.addTest(new ReservationManagerTest("testCreate"));
        suite.addTest(new ReservationManagerTest("testQuery"));
        suite.addTest(new ReservationManagerTest("testAuthList"));
        suite.addTest(new ReservationManagerTest("testUserList"));
        suite.addTest(new ReservationManagerTest("testPathToString"));
        suite.addTest(new ReservationManagerTest("testCancel"));
        suite.addTest(new TestSuite(SchedulerTest.class));
        return suite;
    }
}
