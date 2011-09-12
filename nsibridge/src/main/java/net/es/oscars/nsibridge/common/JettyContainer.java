package net.es.oscars.nsibridge.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.cxf.configuration.jsse.TLSServerParameters;
import org.apache.cxf.configuration.security.ClientAuthentication;
import org.apache.cxf.configuration.security.FiltersType;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.apache.cxf.service.invoker.BeanInvoker;
import org.apache.cxf.transport.http_jetty.JettyHTTPServerEngineFactory;
import org.apache.log4j.Logger;

public class JettyContainer {
    private String hostname;
    private Integer port;
    private HashMap<String, Object> servicesToExpose = new HashMap<String, Object>();
    private boolean useSSL = false;
    private boolean useBasicAuth = false;
    private HashMap<String, String> userPasswords = new HashMap<String, String>();
    private String sslKeystorePath = "";
    private String sslKeystorePass = "";
    private String sslKeyPass = "";
    private String sslTruststorePath = "";
    private String sslTruststorePass = "";
    private Logger log = Logger.getLogger(JettyContainer.class);


    public void startServer() {


        Iterator<String> servicePathIt = servicesToExpose.keySet().iterator();

        String basePath = "http://"+hostname+":"+port+"/";
        if (useSSL) {
            basePath = "https://"+hostname+":"+port+"/";
        }
        log.info("Starting jetty at: "+basePath);

        while (servicePathIt.hasNext()) {

            String servicePath = servicePathIt.next();
            Object implementor = servicesToExpose.get(servicePath);


            JaxWsServerFactoryBean sf = new JaxWsServerFactoryBean();
            sf.setAddress(basePath+servicePath);
            sf.setServiceClass(implementor.getClass());
            sf.getServiceFactory().setInvoker(new BeanInvoker(implementor));

            if (useSSL) {
                log.info("Using SSL");
                sf = configureSSLOnTheServer(sf, port);
            }

            org.apache.cxf.endpoint.Server server = sf.create();
            if (useBasicAuth) {
                log.info("Using HTTP Basic Auth");
                BasicAuthAuthorizationInterceptor icpt = new BasicAuthAuthorizationInterceptor();
                icpt.setUsers(userPasswords);
                server.getEndpoint().getInInterceptors().add(icpt);
           }
            String endpoint = server.getEndpoint().getEndpointInfo().getAddress();
            log.info("Server started at " + endpoint);
        }
    }



    private JaxWsServerFactoryBean configureSSLOnTheServer(JaxWsServerFactoryBean sf, int port) {
        try {

            TLSServerParameters tlsParams = new TLSServerParameters();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            File keystoreFile = new File(sslKeystorePath);
            keyStore.load(new FileInputStream(keystoreFile), sslKeystorePass.toCharArray());
            KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyFactory.init(keyStore, sslKeyPass.toCharArray());
            KeyManager[] km = keyFactory.getKeyManagers();
             tlsParams.setKeyManagers(km);

            File truststoreFile = new File(sslTruststorePath);
            keyStore.load(new FileInputStream(truststoreFile), sslTruststorePass.toCharArray());
            TrustManagerFactory trustFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustFactory.init(keyStore);
            TrustManager[] tm = trustFactory.getTrustManagers();
            tlsParams.setTrustManagers(tm);
            FiltersType filter = new FiltersType();
            filter.getInclude().add(".*_EXPORT_.*");
            filter.getInclude().add(".*_EXPORT1024_.*");
            filter.getInclude().add(".*_WITH_DES_.*");
            filter.getInclude().add(".*_WITH_NULL_.*");
            filter.getExclude().add(".*_DH_anon_.*");
            tlsParams.setCipherSuitesFilter(filter);
            ClientAuthentication ca = new ClientAuthentication();
            ca.setRequired(false);
            ca.setWant(false);
            tlsParams.setClientAuthentication(ca);
            JettyHTTPServerEngineFactory factory = new JettyHTTPServerEngineFactory();
            factory.setTLSServerParametersForPort(port, tlsParams);
        } catch (KeyStoreException kse) {
            kse.printStackTrace();
            log.error(kse);
            log.error("Security configuration failed with the following: " + kse.getCause());
        } catch (NoSuchAlgorithmException nsa) {
            nsa.printStackTrace();
            log.error(nsa);
            log.error("Security configuration failed with the following: " + nsa.getCause());
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            log.error(fnfe);
            log.error("Security configuration failed with the following: " + fnfe.getCause());
        } catch (UnrecoverableKeyException uke) {
            uke.printStackTrace();
            log.error(uke);
            log.error("Security configuration failed with the following: " + uke.getCause());
        } catch (CertificateException ce) {
            ce.printStackTrace();
            log.error(ce);
            log.error("Security configuration failed with the following: " + ce.getCause());
        } catch (GeneralSecurityException gse) {
            gse.printStackTrace();
            log.error(gse);
            log.error("Security configuration failed with the following: " + gse.getCause());
        } catch (IOException ioe) {
            ioe.printStackTrace();
            log.error(ioe);
            log.error("Security configuration failed with the following: " + ioe.getCause());
        }

        return sf;
    }


    public String getHostname() {
        return hostname;
    }
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }

    public HashMap<String, Object> getServicesToExpose() {
        return servicesToExpose;
    }
    public void setServicesToExpose(HashMap<String, Object> servicesToExpose) {
        this.servicesToExpose = servicesToExpose;
    }
    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }
    public boolean isUseSSL() {
        return useSSL;
    }

    public void setUseBasicAuth(boolean useBasicAuth) {
        this.useBasicAuth = useBasicAuth;
    }

    public boolean isUseBasicAuth() {
        return useBasicAuth;
    }
    public String getSslKeystorePath() {
        return sslKeystorePath;
    }
    public void setSslKeystorePath(String sslKeystorePath) {
        this.sslKeystorePath = sslKeystorePath;
    }
    public String getSslKeystorePass() {
        return sslKeystorePass;
    }
    public void setSslKeystorePass(String sslKeystorePass) {
        this.sslKeystorePass = sslKeystorePass;
    }
    public String getSslTruststorePath() {
        return sslTruststorePath;
    }
    public void setSslTruststorePath(String sslTruststorePath) {
        this.sslTruststorePath = sslTruststorePath;
    }
    public String getSslTruststorePass() {
        return sslTruststorePass;
    }
    public void setSslTruststorePass(String sslTruststorePass) {
        this.sslTruststorePass = sslTruststorePass;
    }



    public void setUserPasswords(HashMap<String, String> userPasswords) {
        this.userPasswords = userPasswords;
    }



    public HashMap<String, String> getUserPasswords() {
        return userPasswords;
    }



    public void setSslKeyPass(String sslKeypass) {
        this.sslKeyPass = sslKeypass;
    }



    public String getSslKeyPass() {
        return sslKeyPass;
    }

}
