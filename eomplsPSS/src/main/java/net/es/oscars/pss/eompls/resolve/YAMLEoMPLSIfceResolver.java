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
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

public class YAMLEoMPLSIfceResolver implements EoMPLSIfceAddressResolver{
    private GenericConfig config = null;
    private HashMap<URNParserResult, String> ifceAddresses = null;
    
    public YAMLEoMPLSIfceResolver() {
        
    }
    
    public String getIfceAddress(String ifceId) throws PSSException {
        if (this.config == null) {
            throw new PSSException("null config");
        }
        if (ifceId == null ) {
            throw new PSSException("null ifceId");
        }
        URNParserResult urn = URNParser.parseTopoIdent(ifceId);
        System.out.println("resolving: "+urn.toString());
        
        if (ifceAddresses == null) {
            throw new PSSException("empty config for ifceAddresses");
        } else {
            String address = ifceAddresses.get(urn);
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
        ifceAddresses = new HashMap<URNParserResult, String>();
        
        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        try {
            String configFilePath = cc.getFilePath(configFileName);
            InputStream propFile =  new FileInputStream(new File(configFilePath));
            HashMap<String, String> configAddresses = (HashMap<String, String>) Yaml.load(propFile);
            for (String ifceId : configAddresses.keySet()) {
                String address = configAddresses.get(ifceId);
                URNParserResult urn = URNParser.parseTopoIdent(ifceId);
                System.out.println("putting address: "+address+ " for urn "+urn);
                ifceAddresses.put(urn, address);
            }
            
        } catch (ConfigException e) {
            throw new PSSException(e);
        } catch (FileNotFoundException e) {
            throw new PSSException(e);
        }

        for (URNParserResult urn : ifceAddresses.keySet()) {
            System.out.println(urn + " -> "+ ifceAddresses.get(urn));
        }
        
    }


}
