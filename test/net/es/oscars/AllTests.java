package net.es.oscars;

import junit.framework.Test;
import junit.framework.TestSuite; 


public class AllTests { 
    public static Test suite() { 
        TestSuite suite = new TestSuite("OSCARS core tests suite");
        //suite.addTest(new TestSuite(NotifierTest.class));
        suite.addTest(new TestSuite(LogWrapperTest.class));
        suite.addTest(new TestSuite(PropHandlerTest.class));
        return suite;
    }
}
