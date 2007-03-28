package net.es.oscars;

import org.testng.annotations.*;

import java.util.Properties;

@Test(groups={ "core" })
public class PropHandlerTest {

    public void getPropertyGroup() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("reservation", true);
        assert props != null;
    }

    public void getProperty() {
        PropHandler propHandler = new PropHandler("test.properties");
        Properties props = propHandler.getPropertyGroup("test.bss", true);
        assert props.getProperty("domainName") != null;
    }
}
