package net.es.oscars.api.common;

import net.es.oscars.utils.config.ContextConfig;
import org.apache.wss4j.common.crypto.PasswordEncryptor;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.crypto.Merlin;

import java.io.IOException;
import java.security.KeyStore;
import java.util.Properties;

public class NewDynaMerlin extends Merlin {

    private Properties deployProps;  // properties from site specific property file

    /**
     * Constructor sets static properties in parent class
     *    and site modified properties in deployProps
     * @param props  static properties from jar classpath
     * @throws java.io.IOException
     */
    public NewDynaMerlin(Properties props) throws WSSecurityException, IOException {
        super();
        this.setProps(props);
    }

    public NewDynaMerlin(Properties props, ClassLoader loader, PasswordEncryptor enc) throws WSSecurityException, IOException {
        super(props, loader, enc);
        this.setProps(props);
    }

    /**
     * Uses the unqualified service and keystore.properties names that have
     * come from the properties file in the jar/classes directory to 
     * load the properties from the property file.
     * deployProps has properties that can be modified by a deployment site.
     * @param file unqualified property file name
     */
    private void readProps (String file) {

        this.deployProps = new Properties();
        try {
            ContextConfig cc = ContextConfig.getInstance();
            String pathname = cc.getFilePath(file);
            java.io.FileInputStream fis = new java.io.FileInputStream(pathname);
            this.deployProps.load(fis);
            fis.close();
            
        } catch (Exception ee) {
            throw new RuntimeException (ee);
        }
    }
    
    /**
     * Adds the properties defined in the site specific properties file to the
     * Merlin.properties.
     * NOTE: has only been tested with JKS keystores
     */
    private void setProps (Properties props) throws IOException {
        this.properties = props;


        // Read deployment property file
        String propFile = this.properties.getProperty ("net.es.oscars.api.common.NewDynaMerlin.propfile");
        this.readProps (propFile);

        // Retrieve the remaining properties
        String password = this.deployProps.getProperty("org.apache.ws.security.crypto.merlin.keystore.password");
        String keystore = this.deployProps.getProperty("org.apache.ws.security.crypto.merlin.file");
        String keystoreType = this.deployProps.getProperty("org.apache.ws.security.crypto.merlin.keystore.type");
        
        this.properties.setProperty ("org.apache.ws.security.crypto.merlin.keystore.type", keystoreType);
        this.properties.setProperty ("org.apache.ws.security.crypto.merlin.keystore.password",password);
        this.properties.setProperty ("org.apache.ws.security.crypto.merlin.file",keystore);

        try {
            KeyStore ks = KeyStore.getInstance(keystoreType);
            java.io.FileInputStream fis = new java.io.FileInputStream(keystore);
            ks.load(fis, password.toCharArray());
            fis.close();
            super.setKeyStore(ks);
        } catch (Exception ee) {
            throw new RuntimeException (ee);
        }
    }
}
