package net.es.oscars.bss.pathfinder;

import junit.framework.Test;
import junit.framework.TestSuite; 


public class AllTests { 
    public static Test suite() { 
        TestSuite suite = new TestSuite("BSS networking test suite");

        //suite.addTest(new TestSuite(JnxTracerouteTest.class));
        suite.addTest(new JnxTracerouteTest("testTraceroute"));
        suite.addTest(new JnxTracerouteTest("testRawHopData"));
        suite.addTest(new JnxTracerouteTest("testHopData"));

        //suite.addTest(new TestSuite(JnxSNMPTest.class));
        suite.addTest(new JnxSNMPTest("testJnxSNMP"));
        //suite.addTest(new JnxSNMPTest("testQueryLSPInfo"));
        // testQuesryLSPInfo doesn't work yet
        //suite.addTest(new JnxSNMPTest("testQueryLSPInfo"));

        // network tests need to run in right order
        suite.addTest(new PathfinderTest("testTraceroute"));
        suite.addTest(new PathfinderTest("testForwardPath"));
        suite.addTest(new PathfinderTest("testQueryDomain"));
        suite.addTest(new PathfinderTest("testReversePath1"));
        suite.addTest(new PathfinderTest("testReversePath2"));
        return suite;
    }
}
