package net.es.oscars.pss.eompls.test;


import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.config.ConfigHolder;
import net.es.oscars.pss.eompls.alu.ALUNameGenerator;
import net.es.oscars.pss.eompls.alu.SR_VPLS_ConfigGen;

import net.es.oscars.pss.eompls.config.EoMPLSConfigHolder;
import net.es.oscars.pss.eompls.junos.MX_VPLS_ConfigGen;
import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
import net.es.oscars.pss.eompls.util.EoMPLSUtils;
import net.es.oscars.pss.util.ClassFactory;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;
import org.apache.log4j.Logger;
import org.jaxen.function.StringFunction;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

@Test
public class VPLSTest {
    private Logger log = Logger.getLogger(VPLSTest.class);

    @BeforeClass(groups = { "alu-vpls", "mx-vpls", "vpls" })
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

        log.debug("db: "+EoMPLSConfigHolder.getInstance().getEomplsBaseConfig().getDatabase().getDbname());

    }

    @Test(groups = { "alu-vpls-template"} )
    private void testALUVPLSTemplate() throws PSSException{
        SR_VPLS_ConfigGen cg = new SR_VPLS_ConfigGen();
        ALUNameGenerator ng = ALUNameGenerator.getInstance();

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

        String gri = "es.net-3912";
        String portA = "1/5/2";
        // String portB = "1/5/2";
        String vlanA = "3005";
        String vlanB = "3006";
        Integer bandwidth = 5000;
        String vplsId = ng.getVplsId("foo.es.net", 3005, gri);
        String ingQosId = ng.getQosId("foo.es.net", Integer.getInteger(vplsId), gri);
        String pathNameA = ng.getPathName(gri)+"-1";
        String pathNameB = ng.getPathName(gri)+"-2";
        String lspNameA = ng.getLSPName(gri)+"-1";
        String lspNameB = ng.getLSPName(gri)+"-2";
        String sdpIdA = ng.getSdpId("foo.es.net", Integer.getInteger(vplsId), gri);
        String sdpIdB = ng.getSdpId("foo.es.net", Integer.getInteger(vplsId), gri);
        String sdpDescA = gri+"-1";
        String sdpDescB = gri+"-2";


        cg.getVpls().put("id", vplsId);
        cg.getVpls().put("description", gri);


        cg.getIngqos().put("id", ingQosId);
        cg.getIngqos().put("description", gri);
        cg.getIngqos().put("bandwidth", bandwidth);


        HashMap ifceA = new HashMap();
        ifceA.put("name", portA);
        ifceA.put("vlan", vlanA);
        cg.getIfces().add(ifceA);
        /*
        HashMap ifceB = new HashMap();
        ifceB.put("name", portB);
        ifceB.put("vlan", vlanB);
        cg.getIfces().add(ifceB);
        */

        HashMap pathA = new HashMap();
        pathA.put("name", pathNameA);
        ArrayList hopsA = new ArrayList();
        pathA.put("hops", hopsA);
        HashMap hopA1 = new HashMap();
        hopsA.add(hopA1);
        hopA1.put("address", "10.32.0.69");
        hopA1.put("order", "5");
        cg.getPaths().add(pathA);

        HashMap pathB = new HashMap();
        pathB.put("name", pathNameB);
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
        lspA.put("name", lspNameA);
        lspA.put("from", "10.96.0.8");
        lspA.put("to", "10.96.0.2");
        lspA.put("path", pathNameA);
        cg.getLsps().add(lspA);

        HashMap lspB = new HashMap();
        lspB.put("name", lspNameB);
        lspB.put("from", "10.96.0.8");
        lspB.put("to", "10.96.0.4");
        lspB.put("path", pathNameB);
        cg.getLsps().add(lspB);


        HashMap sdpA = new HashMap();
        sdpA.put("id", sdpIdA);
        sdpA.put("description", sdpDescA);
        sdpA.put("far_end", "10.96.0.2");
        sdpA.put("lsp_name", lspNameB);
        cg.getSdps().add(sdpA);


        HashMap sdpB = new HashMap();
        sdpB.put("id", sdpIdB);
        sdpB.put("description", sdpDescB);
        sdpB.put("far_end", "10.96.0.4");
        sdpB.put("lsp_name", lspNameB);
        cg.getSdps().add(sdpB);



        String setup = cg.gen_VPLS_setup(null, null);
        System.out.println(setup);
        String teardown= cg.gen_VPLS_teardown(null, null);
        System.out.println(teardown);

    }


    @Test(groups = { "mx-vpls", "vpls" })
    private void testJunosVPLSTemplate() throws PSSException{
        MX_VPLS_ConfigGen cg = new MX_VPLS_ConfigGen();

        /*

        setup:
        1. policy (string)
        2. community: name, id
        3. filters: stats, policing
        4. policer: name, bandwidth_limit, burst_size_limit
        5. vpls: name, id

        6. ifces: list_of <name, vlan, description>
        7. paths: list_of <name, hops>
                                 hops: list of string >
        8. lsps: list_of <name, from, to, path, neighbor, bandwidth>
        */


        /*
        teardown:
        1. policy (string)
        2. community: name
        3. filters: stats, policing
        4. policer: name
        5. vpls: name

        6. ifces: list_of <name, vlan>
        7. paths: list_of <name>
        8. lsps: list_of <name>
        */

        cg.setPolicy("test_policy");

        cg.getCommunity().put("name", "test_community");
        cg.getCommunity().put("members", "3306");

        cg.getFilters().put("stats", "test_stats");
        cg.getFilters().put("policing", "test_policing");

        cg.getPolicer().put("name", "test_policer");
        cg.getPolicer().put("bandwidth_limit", 500);
        cg.getPolicer().put("burst_size_limit", 50);

        cg.getVpls().put("name", "test_vpls");
        cg.getVpls().put("id", "3006");



        HashMap ifceA = new HashMap();
        ifceA.put("name", "xe-11/2/0");
        ifceA.put("vlan", "3003");
        ifceA.put("description", "ifceA");
        cg.getIfces().add(ifceA);

        HashMap ifceB = new HashMap();
        ifceB.put("name", "xe-11/2/0");
        ifceB.put("vlan", "3005");
        ifceB.put("description", "ifceB");
        cg.getIfces().add(ifceB);



        HashMap pathA = new HashMap();
        pathA.put("name", "pathA");
        ArrayList hopsA = new ArrayList();
        pathA.put("hops", hopsA);
        hopsA.add("10.32.0.61");
        hopsA.add("10.32.0.18");
        cg.getPaths().add(pathA);

        HashMap pathB = new HashMap();
        pathB.put("name", "pathB");
        ArrayList hopsB = new ArrayList();
        pathB.put("hops", hopsB);
        hopsB.add("10.32.0.70");
        cg.getPaths().add(pathB);


        HashMap lspA = new HashMap();
        lspA.put("name", "lspA");
        lspA.put("from", "10.96.0.2");
        lspA.put("to", "10.32.0.18");
        lspA.put("neighbor", "10.96.0.4");
        lspA.put("path", "pathA");
        lspA.put("bandwidth", 500);
        cg.getLsps().add(lspA);

        HashMap lspB = new HashMap();
        lspB.put("name", "lspB");
        lspB.put("from", "10.96.0.2");
        lspB.put("to", "10.32.0.70");
        lspB.put("neighbor", "10.96.0.8");
        lspB.put("path", "pathB");
        lspB.put("bandwidth", 500);
        cg.getLsps().add(lspB);

        String setup = cg.gen_VPLS_setup(null, null);
        System.out.println(setup);
        String teardown= cg.gen_VPLS_teardown(null, null);
        System.out.println(teardown);

    }

    @Test(groups = { "alu-vpls", "vpls" })

    public void testWithResDetails() throws PSSException {
        ResDetails rd = RequestFactory.getALU_ALU();

        SR_VPLS_ConfigGen cg = new SR_VPLS_ConfigGen();

        String srcDeviceId = EoMPLSUtils.getDeviceId(rd, false);
        String dstDeviceId = EoMPLSUtils.getDeviceId(rd, true);
        String srcSetup = cg.gen_VPLS_setup(rd, srcDeviceId);
        System.out.println(srcSetup);
        String srcTeardown = cg.gen_VPLS_teardown(rd, srcDeviceId);
        System.out.println(srcTeardown);


        rd = RequestFactory.getALU_MX();
        srcDeviceId = EoMPLSUtils.getDeviceId(rd, false);
        dstDeviceId = EoMPLSUtils.getDeviceId(rd, true);

        srcSetup = cg.gen_VPLS_setup(rd, srcDeviceId);
        System.out.println(srcSetup);
        srcTeardown = cg.gen_VPLS_teardown(rd, srcDeviceId);
        System.out.println(srcTeardown);

        MX_VPLS_ConfigGen mcg = new MX_VPLS_ConfigGen();
        String dstSetup = mcg.gen_VPLS_setup(rd, dstDeviceId);
        System.out.println(dstSetup);
        String dstTeardown = mcg.gen_VPLS_teardown(rd, dstDeviceId);
        System.out.println(dstTeardown);
    }


}
