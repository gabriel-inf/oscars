import junit.framework.Test;
import junit.framework.TestSuite; 


public class AllTests { 
    public static Test suite() { 
        TestSuite suite = new TestSuite("Automated client test suite");
        suite.addTest(new ClientTest("testCreate"));
        suite.addTest(new ClientTest("testQuery"));
        suite.addTest(new ClientTest("testList"));
        suite.addTest(new ClientTest("testCancel"));
        suite.addTest(new ClientTest("testForward"));
        return suite;
    }
}
