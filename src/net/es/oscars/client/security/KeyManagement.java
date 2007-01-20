package net.es.oscars.client.security;

import javax.net.ssl.*;
import java.security.*;

public class KeyManagement {

    /* setup to use our keystore ... */
    public static void setKeyStore() {
        try {  
            System.setProperty("javax.net.ssl.keyStoreType", "JKS");
            System.setProperty("javax.net.ssl.trustStore", 
                               System.getProperty("user.home") + "/.keystore");
            // should set this somewhere else
            System.setProperty("javax.net.ssl.trustStorePassword", "oscars");
            System.setProperty("java.protocol.handler.pkgs", 
                               "com.sun.net.ssl.internal.www.protocol" );
            Security.addProvider( new com.sun.net.ssl.internal.ssl.Provider() );
        } catch (Exception e1) {
            e1.printStackTrace();
        }      
    } // end setKeyStore

    // Does not work, but it should
    public static void installTrustMgr() {
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void
                checkClientTrusted(java.security.cert.X509Certificate[] certs,
                                   String authType) {
            }

            public void
                checkServerTrusted(java.security.cert.X509Certificate[] certs,
                                   String authType) {
            }
          }
        };

        // Install the all-trusting trust manager ;-)
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            System.out.println("failed to install trust mgr");
        }
    System.out.println("trust mgr installed");
    }
}


