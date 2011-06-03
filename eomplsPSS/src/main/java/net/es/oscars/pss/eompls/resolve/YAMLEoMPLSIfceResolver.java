package net.es.oscars.pss.eompls.resolve;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

import org.ho.yaml.Yaml;

import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.config.GenericConfig;
import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class YAMLEoMPLSIfceResolver implements EoMPLSIfceAddressResolver{
    private GenericConfig config = null;
    private HashMap<String, String> ifceAddresses = null;
    
    public YAMLEoMPLSIfceResolver() {
        
    }
    
    public String getIfceAddress(String ifceId) throws PSSException {
        if (this.config == null) {
            throw new PSSException("null config");
        }
        if (ifceId == null ) {
            throw new PSSException("null ifceId");
        }
        
        if (ifceAddresses == null) {
            throw new PSSException("empty config for ifceAddresses");
        } else {
            String address = ifceAddresses.get(ifceId.trim());
            if (address == null) {
                throw new PSSException("address not found for ifce: "+ifceId);
            } else {
                return address;
            }
        }
        
    }

    @SuppressWarnings({ "unchecked" })
    public void setConfig(GenericConfig config) throws PSSException {
        if (config == null) {
            throw new PSSException("null config");
        } else if (config.getParams() == null) {
            throw new PSSException("no config parameters set");
        }
        
        this.config = config;
        String configFileName = (String)config.getParams().get("configFile");
        if (configFileName == null) {
            throw new PSSException("required configFile parameter not set");
        }
        
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        try {
            String configFilePath = cc.getFilePath(configFileName);
            InputStream propFile =  new FileInputStream(new File(configFilePath));
            ifceAddresses = (HashMap<String, String>) Yaml.load(propFile);
        } catch (ConfigException e) {
            throw new PSSException(e);
        } catch (FileNotFoundException e) {
            throw new PSSException(e);
        }

        
        
    }


}
