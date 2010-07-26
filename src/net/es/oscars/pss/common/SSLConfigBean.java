package net.es.oscars.pss.common;

import java.util.Properties;

import net.es.oscars.PropHandler;
import net.es.oscars.pss.PSSException;

public class SSLConfigBean {
    
    private String privateKeyFile;
    private String passphrase;
    private String username;



    public static SSLConfigBean loadConfig() throws PSSException {
        SSLConfigBean config = new SSLConfigBean();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("pss", true);
        if (props == null) {
            throw new PSSException("No PSS config");
        }
        String privateKeyFile   = (String) props.get("privateKeyFile");
        String username         = (String) props.get("username");
        String passphrase       = (String) props.get("passphrase");
        if (username == null) {
            throw new PSSException("Required property pss.username not set in oscars.properties");
        }
        config.setUsername(username.trim());
        if (passphrase == null) {
            throw new PSSException("Required property pss.passphrase not set in oscars.properties");
        }
        config.setPassphrase(passphrase.trim());
        if (privateKeyFile == null) {
            throw new PSSException("Required property pss.privateKeyFile not set in oscars.properties!");
        }
        config.setPrivateKeyFile(privateKeyFile.trim());
        return config;
    }



    public void setPrivateKeyFile(String privateKeyFile) {
        this.privateKeyFile = privateKeyFile;
    }



    public String getPrivateKeyFile() {
        return privateKeyFile;
    }



    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }



    public String getPassphrase() {
        return passphrase;
    }



    public void setUsername(String username) {
        this.username = username;
    }



    public String getUsername() {
        return username;
    }


}
