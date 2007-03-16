package net.es.oscars.pathfinder.traceroute;

import junit.framework.Test;
import junit.framework.TestSuite; 


public class AllTests { 
    public static Test suite() { 
        TestSuite suite = new TestSuite("BSS networking test suite");

        //suite.addTest(new TestSuite(JnxTracerouteTest.class));
        suite.addTest(new JnxTracerouteTest("testTraceroute"));
        suite.addTest(new JnxTracerouteTest("testRawHopData"));
        suite.addTest(new JnxTracerouteTest("testHopData"));

        return suite;
    }
}
