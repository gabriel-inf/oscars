package net.es.oscars.pathfinder;

import junit.framework.Test;
import junit.framework.TestSuite; 


public class AllTests { 
    public static Test suite() { 
        TestSuite suite = new TestSuite("Pathfinder test suite");
        suite.addTest(new DomainTest("testQuery"));
        suite.addTest(new DomainTest("testList"));
        suite.addTest(new PathTest("testCreate"));
        suite.addTest(new PathTest("testQuery"));
        suite.addTest(new PathTest("testList"));
        suite.addTest(new PathTest("testRemove"));
        return suite;
    }
}
