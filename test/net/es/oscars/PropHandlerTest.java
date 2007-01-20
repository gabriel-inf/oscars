package net.es.oscars;

import java.util.Properties;

import junit.framework.*;

public class PropHandlerTest extends TestCase {

    public PropHandlerTest(String name) {
        super(name);
    }
        
    public void testLoad() {
        PropHandler propHandler =
            new PropHandler("/oscars.config/properties/oscars.properties");
        Properties props =
            propHandler.getPropertyGroup("reservation", true);
        Assert.assertNotNull(props);
    }

    public void testGetProperty() {
        PropHandler propHandler =
            new PropHandler("/oscars.config/properties/test.properties");
        Properties props = propHandler.getPropertyGroup("test.bss",
                                                        true);
        Assert.assertNotNull(props.getProperty("domainName"));
    }
}
