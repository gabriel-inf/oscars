package net.es.oscars.pathfinder.db.util.vendor.jnx;

import org.testng.annotations.*;
import org.testng.Assert;

import java.util.List;
import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.AuthHandler;
import net.es.oscars.pss.PSSException;

/**
 * This class tests methods in the JnxShowRoute class.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "pathfinder.db", "jnxShowRoute" })
public class JnxShowRouteTest {

    private Properties props;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

  @Test
    public void allowedTest() {
        AuthHandler authHandler = new AuthHandler();
        boolean authorized = authHandler.checkAuthorization();
        Assert.assertTrue(authorized,
            "You are not authorized to do a show route from this machine. ");
    }

  @Test(dependsOnMethods={ "allowedTest" })
    public void jnxShowRoute() throws PSSException {
        JnxShowRoute jnxShowRoute = new JnxShowRoute();

        String router = this.props.getProperty("jnxSource");
        String portIdent =
            jnxShowRoute.showRoute(router, "inet.0",
                                   this.props.getProperty("destHost"));
        System.out.println("portIdent: " + portIdent);
        assert portIdent != null;
    }
}
