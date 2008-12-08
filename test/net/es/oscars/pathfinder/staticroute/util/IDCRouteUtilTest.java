package net.es.oscars.pathfinder.staticroute.util;

import java.util.HashMap;
import java.util.Properties;

import net.es.oscars.GlobalParams;
import net.es.oscars.PropHandler;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

@Test(groups={ "staticroute.init" })
public class IDCRouteUtilTest {
    private Logger log;
    private String dbname;
    private Properties props;
    
    public void setUpClass(){
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.domain", true);
        this.log = Logger.getLogger(this.getClass());
        this.dbname = GlobalParams.getReservationTestDBName();
    }
    
    @BeforeGroups(groups={ "pathfinder.staticroute" })
    @Test
    public void addTestRoutes(){
        this.setUpClass();
        HashMap<String,String> routeParams = new HashMap<String,String>();
        //Add route
        routeParams.put("egress", "urn:ogf:network:domain=dcn.internet2.edu:node=NEWY:port=S26623:link=10.100.80.189");
        //routeParams.put("source", "");
        routeParams.put("dest", "urn:ogf:network:domain=es.net");
        //routeParams.put("multi", "0");
        //routeParams.put("default", "1");
        routeParams.put("loose", "1");
        
        IDCRouteUtil routeUtil = new IDCRouteUtil(routeParams, this.dbname);
        routeUtil.addRoute();
    }
}
