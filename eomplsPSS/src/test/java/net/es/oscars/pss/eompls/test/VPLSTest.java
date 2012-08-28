package net.es.oscars.pss.eompls.test;


import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.eompls.alu.SR_VPLS_ConfigGen;

import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import org.apache.log4j.Logger;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@Test
public class VPLSTest {
    private Logger log = Logger.getLogger(VPLSTest.class);

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

    }

    @Test(groups = { "vpls" })
    private void testALUVPLS() throws ConfigException, PSSException{
        this.configure();
        SR_VPLS_ConfigGen cg = new SR_VPLS_ConfigGen();

        /*
        1. vpls: id, description
        2. ingqos: id, description, bandwidth
        3. ifces: list_of <name, vlan>
        4. paths: list_of <name, hops>
                                 hops: list_of: <address, order>
        5. lsps: list_of <from, to, name, path>
        6. sdps: list_of <id, description, far_end, lsp_name>

        notes
           each sdp.lsp_name should correspond to an lsp.name,
              and the sdp.far_end for that should correspond to the lsp.to
        */

        cg.getVpls().put("id", "3006");
        cg.getVpls().put("description", "vpls_desc");


        cg.getIngqos().put("id", "3006");
        cg.getIngqos().put("description", "ingqos_desc");
        cg.getIngqos().put("bandwidth", 5000);


        HashMap ifceA = new HashMap();
        ifceA.put("name", "1/5/2");
        ifceA.put("vlan", "3304");
        cg.getIfces().add(ifceA);

        HashMap ifceB = new HashMap();
        ifceB.put("name", "1/5/2");
        ifceB.put("vlan", "3312");
        cg.getIfces().add(ifceB);



        HashMap pathA = new HashMap();
        pathA.put("name", "pathA");
        ArrayList hopsA = new ArrayList();
        pathA.put("hops", hopsA);
        HashMap hopA1 = new HashMap();
        hopsA.add(hopA1);
        hopA1.put("address", "10.32.0.69");
        hopA1.put("order", "5");
        cg.getPaths().add(pathA);

        HashMap pathB = new HashMap();
        pathB.put("name", "pathB");
        ArrayList hopsB = new ArrayList();
        pathB.put("hops", hopsB);
        HashMap hopB1 = new HashMap();
        hopsB.add(hopB1);
        HashMap hopB2 = new HashMap();
        hopsB.add(hopB2);
        hopB1.put("address", "10.32.0.69");
        hopB1.put("order", "5");
        hopB2.put("address", "10.32.0.13");
        hopB2.put("order", "10");
        cg.getPaths().add(pathB);


        HashMap lspA = new HashMap();
        lspA.put("name", "lspA");
        lspA.put("from", "10.96.0.8");
        lspA.put("to", "10.96.0.2");
        lspA.put("path", "pathA");
        cg.getLsps().add(lspA);

        HashMap lspB = new HashMap();
        lspB.put("name", "lspB");
        lspB.put("from", "10.96.0.8");
        lspB.put("to", "10.96.0.4");
        lspB.put("path", "pathB");
        cg.getLsps().add(lspB);


        HashMap sdpA = new HashMap();
        sdpA.put("id", "3005");
        sdpA.put("description", "sdpA");
        sdpA.put("far_end", "10.96.0.2");
        sdpA.put("lsp_name", "lspA");
        cg.getSdps().add(sdpA);


        HashMap sdpB = new HashMap();
        sdpB.put("id", "3006");
        sdpB.put("description", "sdpB");
        sdpB.put("far_end", "10.96.0.4");
        sdpB.put("lsp_name", "lspB");
        cg.getSdps().add(sdpB);



        String setup = cg.gen_VPLS_setup(null, null);
        System.out.println(setup);
        String teardown= cg.gen_VPLS_teardown(null, null);
        System.out.println(teardown);

    }



}
