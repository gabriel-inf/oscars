package net.es.oscars.nsibridge.common;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.es.oscars.nsibridge.beans.NSAInfo;
import net.es.oscars.nsibridge.bridge.NSAAPI;
import net.es.oscars.nsibridge.bridge.NSAFactory;
import net.es.oscars.nsibridge.bridge.OSCARS_0_5_NSA;
import net.es.oscars.nsibridge.bridge.SimpleNSA;
import net.es.oscars.nsibridge.util.NSAList;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Invoker {

    private static Map<String, Object> config;
    private static HashMap<String, Object> servicesToExpose;
    private static Integer port = 8080;
    private static String hostname = "localhost";
    private static boolean useSSL = false;
    private static boolean useBasicAuth = false;
    private static HashMap<String, String> userPasswords = new HashMap<String, String>();
    private static String sslKeystorePath = "";
    private static String sslKeystorePass = "";
    private static String sslKeyPass = "";
    private static String sslTruststorePath = "";
    private static String sslTruststorePass = "";

    public static void main(String[] args) throws Exception {

        configure();

        // create a parser
        OptionParser parser = new OptionParser() {
            {
                acceptsAll( asList( "h", "?" ), "show help then exit" );
                accepts( "help", "show extended help then exit" );
                accepts( "port" , "HTTP port" ).withRequiredArg().describedAs("int (default: "+port+")").ofType(Integer.class);
                accepts( "host" , "HTTP hostname" ).withRequiredArg().describedAs("string (default: "+hostname+")").ofType(String.class);
                accepts( "test" , "IDC test (run list())" );
            }
        };

        OptionSet options = parser.parse( args );

        // check for help
        if ( options.has( "?" ) || options.has("h") || options.has("help")) {
            parser.printHelpOn( System.out );
            System.exit(0);
        }
        

        if (options.has("port")) {
            port = (Integer) options.valueOf("port");
        }
        if (options.has("host")) {
            hostname = (String) options.valueOf("host");
        }

        JettyContainer jc = new JettyContainer();
        jc.setPort(port);
        jc.setHostname(hostname);

        jc.setServicesToExpose(servicesToExpose);


        if (useBasicAuth) {
            //jc.setUseBasicAuth(true);
            //jc.setUserPasswords(userPasswords);
        }

        if (useSSL) {
            jc.setUseSSL(true);
            jc.setSslKeystorePath(sslKeystorePath);
            jc.setSslKeystorePass(sslKeystorePass);
            jc.setSslKeyPass(sslKeyPass);
            jc.setSslTruststorePath(sslTruststorePath);
            jc.setSslTruststorePass(sslTruststorePass);
        }

        jc.startServer();
        setupNSAs();
        
        while (true) {
            for (NSAAPI nsa : NSAFactory.getInstance().getNSAs()) {
                nsa.tick();
            }
            Thread.sleep(500);

        }
    }
    
    // make this configurable
    private static void setupNSAs() {
        NSAInfo nsaD = new NSAInfo();
        nsaD.setNsaId("urn:ogf:network:nsa:Dominica-OSCARS");
        nsaD.setProviderWsdl("http://jupiter.es.net:8088/ConnectionProvider?wsdl");
        nsaD.setRequesterUrl("http://jupiter.es.net:8088/ConnectionRequester");
        NSAList.getInstance().addNSA("dominica", nsaD);
        NSAFactory.getInstance().addNSA(nsaD.getNsaId(), new OSCARS_0_5_NSA(nsaD.getNsaId()));
        
        NSAInfo nsaG = new NSAInfo();
        nsaG.setNsaId("urn:ogf:network:nsa:Grenada-GLAMBDA-AIST");
        nsaG.setProviderWsdl("http://163.220.30.174:8090/nsi_gl_proxy/services/ConnectionServiceProvider?wsdl");
        NSAList.getInstance().addNSA("grenada", nsaG);

    }
    
    /**
     * loads configuration from config/config.yaml 
     */
    @SuppressWarnings("unchecked")
    private static void configure() {
        ConfigManager cm = ConfigManager.getInstance();
        config = cm.getConfiguration();
        Map<String, Object> httpConfig = (Map<String, Object>) config.get("http");
        port = (Integer) httpConfig.get("port");
        hostname = (String) httpConfig.get("hostname");



        useSSL = (Boolean) httpConfig.get("useSSL");
        if (useSSL) {
            Map<String, Object> sslConfig = (Map<String, Object>) config.get("ssl");
            sslKeystorePath = (String) sslConfig.get("keystorePath");
            sslKeystorePass = (String) sslConfig.get("keystorePass");
            sslKeyPass = (String) sslConfig.get("keyPass");
            sslTruststorePath = (String) sslConfig.get("truststorePath");
            sslTruststorePass = (String) sslConfig.get("truststorePass");
        }

        useBasicAuth = (Boolean) httpConfig.get("useBasicAuth");
        if (useBasicAuth) {
            String passwdFileName = (String) httpConfig.get("passwdFileName");
            Map<String, Object> usernamePasswd = cm.getConfiguration(passwdFileName);
            for (String username : usernamePasswd.keySet()) {
                String passwd = (String) usernamePasswd.get(username);
                userPasswords.put(username, passwd);
            }
        }

        servicesToExpose = new HashMap<String, Object>();

        ArrayList<Map<String, Object>> servicesConfig = (ArrayList<Map<String, Object>>) config.get("services");
        for (Map<String, Object> serviceConfig : servicesConfig) {
            String path = (String) serviceConfig.get("path");
            String impl = (String) serviceConfig.get("implementor");
            try {
                Object implementorObj = Class.forName(impl).getConstructor((Class[]) null).newInstance((Object[]) null);
                servicesToExpose.put(path, implementorObj);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Problem loading class: "+impl);
                System.exit(1);
            }
        }
    }}
