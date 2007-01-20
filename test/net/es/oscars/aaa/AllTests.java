package net.es.oscars.aaa;

import junit.framework.Test;
import junit.framework.TestSuite; 


public class AllTests { 
    public static Test suite() { 
        TestSuite suite = new TestSuite("AAA tests suite");
        suite.addTest(new PermissionTest("testQuery"));
        suite.addTest(new PermissionTest("testList"));
        suite.addTest(new ResourceTest("testQuery"));
        suite.addTest(new ResourceTest("testList"));
        suite.addTest(new AuthorizationTest("testQuery"));
        suite.addTest(new AuthorizationTest("testList"));
        suite.addTest(new InstitutionTest("testQuery"));
        suite.addTest(new InstitutionTest("testList"));
        suite.addTest(new UserTest("testCreate"));
        suite.addTest(new UserTest("testAssociation"));
        suite.addTest(new UserTest("testVerifyLogin"));
        suite.addTest(new UserTest("testQuery"));
        suite.addTest(new UserTest("testList"));
        suite.addTest(new UserTest("testUpdate"));
        suite.addTest(new UserTest("testRemove"));
        suite.addTest(new UserTest("testLoginFromDN"));
        return suite;
    }
}
