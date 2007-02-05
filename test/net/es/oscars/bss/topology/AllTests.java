package net.es.oscars.bss.topology;

import junit.framework.Test;
import junit.framework.TestSuite; 


public class AllTests { 
    public static Test suite() { 
        TestSuite suite = new TestSuite("BSS topology db test suite");

        // db tests need to run in right order
        suite.addTest(new RouterTest("testCreate"));
        suite.addTest(new RouterTest("testQuery"));
        suite.addTest(new RouterTest("testList"));
        suite.addTest(new RouterTest("testFromIp"));
        suite.addTest(new InterfaceTest("testCreate"));
        suite.addTest(new InterfaceTest("testQuery"));
        suite.addTest(new InterfaceTest("testList"));
        suite.addTest(new IpaddrTest("testCreate"));
        suite.addTest(new IpaddrTest("testQuery"));
        suite.addTest(new IpaddrTest("testList"));
        suite.addTest(new IpaddrTest("testGetIpType"));
        suite.addTest(new PathTest("testCreate"));
        suite.addTest(new PathTest("testQuery"));
        suite.addTest(new PathTest("testList"));
        suite.addTest(new PathTest("testRemove"));
        suite.addTest(new RouterTest("testRemove"));
        suite.addTest(new IpaddrTest("testCascadingDelete"));
        suite.addTest(new InterfaceTest("testCascadingDelete"));
        return suite;
    }
}
