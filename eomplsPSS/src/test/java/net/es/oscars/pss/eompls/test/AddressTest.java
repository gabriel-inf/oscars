package net.es.oscars.pss.eompls.test;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.eompls.common.EoMPLSPSSCore;
import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.eompls.util.Ipv4AddressRange;
import net.es.oscars.pss.eompls.util.VPLS_DeviceLoopback;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import org.apache.log4j.Logger;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;


@Test(groups={ "util" })
public class AddressTest {
    private Logger log = Logger.getLogger(AddressTest.class);


    @BeforeClass(groups = { "util"})
    private void configure() throws ConfigException, PSSException {
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        cc.loadManifest(new File("src/test/resources/"+ ConfigDefaults.MANIFEST));
        cc.setContext(ConfigDefaults.CTX_TESTING);
        cc.setServiceName(ServiceNames.SVC_PSS);

        try {
            String configFn = cc.getFilePath("config.yaml");
            ConfigHolder.loadConfig(configFn);
            ClassFactory.getInstance().configure();



            String eoMPLSConfigFilePath = cc.getFilePath("config-eompls.yaml");
            EoMPLSConfigHolder.loadConfig(eoMPLSConfigFilePath);
            EoMPLSClassFactory.getInstance().configure();
        } catch (ConfigException ex ) {
            ex.printStackTrace();
            log.debug ("skipping Tests, eompls is  not configured");
            throw new SkipException("skipping Tests, eompls is  not configured");
        }

        log.debug("db: "+ EoMPLSPSSCore.getInstance().getDbname());

    }


    @Test(groups = { "util"} )
    public void ipTest() {

        // basic math
        Ipv4AddressRange iprange = new Ipv4AddressRange("0.0.0.0/30");
        System.out.println(iprange);
        assert iprange.getBase() == 0;
        assert iprange.getRange() == 4;

        // ensure a /24 is 256 IPs long
        iprange = new Ipv4AddressRange("128.0.0.0/24");
        System.out.println(iprange);
        assert iprange.getRange() == 256;

        // ensure we get the right addr if we ask for it
        String addr = iprange.getAddressInRange(34);
        System.out.println(addr);
        assert addr.equals("128.0.0.34");

        // more complicated stuff
        iprange = new Ipv4AddressRange("232.6.0.0/20");
        addr = iprange.getAddressInRange(258);
        System.out.println(addr);
        assert addr.equals("232.6.1.2");
    }
    @Test(groups = { "util"} )
    public void reserveIpTest() throws PSSException {
        String gri = "some.gri-1235";


        VPLS_DeviceLoopback loopback = VPLS_DeviceLoopback.reserve(gri, "device1");
        System.out.println("reserved loopback: "+ loopback.getVplsLoopback());
        loopback = VPLS_DeviceLoopback.reserve(gri, "device2");
        System.out.println("reserved loopback: "+ loopback.getVplsLoopback());

        loopback = VPLS_DeviceLoopback.release(gri, "device2");
        System.out.println("released loopback: "+ loopback.getVplsLoopback());


        loopback = VPLS_DeviceLoopback.release(gri, "device1");
        System.out.println("released loopback: "+ loopback.getVplsLoopback());


    }

}
