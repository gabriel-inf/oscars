package net.es.oscars.pss;

import junit.framework.Test;
import junit.framework.TestSuite; 


public class AllTests { 
    public static Test suite() { 
        TestSuite suite = new TestSuite("PSS tests suite");
        suite.addTest(new JnxLSPTest("testSetupLSP"));
        suite.addTest(new JnxLSPTest("testTearDownLSP"));
        return suite;
    }
}
