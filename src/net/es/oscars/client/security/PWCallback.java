/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
//package sample.security;

package net.es.oscars.client.security;

import org.apache.ws.security.WSPasswordCallback;

//import javax.security.auth.callback.Callback;
//import javax.security.auth.callback.CallbackHandler;
//import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.*;
import java.io.IOException;

import java.io.*;
import javax.xml.stream.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import javax.xml.namespace.QName;
import java.util.Iterator;

import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;

/**
 * Class PWCallback
 */

public class PWCallback implements CallbackHandler {
    /*
     * 
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */

    /**
     * Method handle
     * @author Mary Thompson -ESnet
     * 
     * This handler is called by the rampartSender digital signature builder to get password
     * for the user who is sending the message. The password is used to access the user's private key 
     * in the keystore which is used to sign the message.
     * This code assumes that all user passwords are the same as the keystore password. If you
     * wish to have different user passwords you will need to change this code to return a different 
     * passwords for different users. See the commented out code at the end of the file for a clue.
     * 
     * @param callbacks in/out param, the password field WSPasswordCallback is set here.
     * @throws java.io.IOException                  
     * @throws javax.security.auth.callback.UnsupportedCallbackException 
     */

    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {

        String keyPass = null;
        //System.out.println("PWCallback called");
        //System.out.println(RampartConfig.NS);
        try {

            /* Get keystore password  from repo/rampConfig.xml on the CLASSPATH
             * For the servers and the core, it will be in ${CATALINA_HOME/shared/classes 
             * For a standalone client, it will be in the working directory.
             */
            InputStream fis = getClass().getResourceAsStream("/repo/rampConfig.xml");
            XMLInputFactory xif= XMLInputFactory.newInstance();
            XMLStreamReader reader= xif.createXMLStreamReader(fis);
            StAXOMBuilder builder= new StAXOMBuilder(reader);
            
            OMElement rampConfig= builder.getDocumentElement();
            OMElement sigCrypto = null;
            for (Iterator<OMElement> elementIter = rampConfig.getChildElements(); elementIter.hasNext();) {
                sigCrypto = elementIter.next();
                QName prop = new QName(RampartConfig.NS, RampartConfig.SIG_CRYPTO_LN);
                if (prop.equals(sigCrypto.getQName()) ) {
                    break;
                }
            }

            OMElement crypto = sigCrypto.getFirstElement();
            for (Iterator<OMElement> elementIter = crypto.getChildElements(); elementIter.hasNext();) {
                OMElement element = elementIter.next();
                OMAttribute nameAttr = element.getAttribute(new QName("",CryptoConfig.PROPERTY_NAME_ATTR));
                if (nameAttr.getAttributeValue().equals("org.apache.ws.security.crypto.merlin.keystore.password")) {
                    keyPass = element.getText().trim();
                }
            }
            //System.out.println("password= " + keyPass);
        } catch (XMLStreamException e){
            throw new IOException (e.getMessage());
        }   
        /*
         * It would be  nice to get the password from the rampartConfiguration that
         * axis has.
 
         import org.apache.axis2.engine.AxisConfiguration;
         import org.apache.axis2.description.ModuleConfiguration;
         import org.apache.axis2.description.Parameter;
         import java.util.HashMap;
         import java.util.Set;

        AxisConfiguration axisConf= configContext.getAxisConfiguration();
        
        HashMap <String,Object> mods = axisConf.getModules();
        Set keys = mods.keySet();
        for (Iterator iter = keys.iterator(); iter.hasNext();) {
            this.log.debug(iter.next());
        }
        Shows that the module rampart-SNAPSHOT is defined
        /* not sure why the following doesn't work
        ModuleConfiguration rampartConfig = axisConf.getModuleConfig("rampart-SNAPSHOT");
        if (rampartConfig != null) {
            ArrayList<String> params = rampartConfig.getParameters();
            this.log.debug("Contents of rampartConfig");
            for (Iterator <String> iter = params.iterator(); iter.hasNext();) {
                this.log.debug(iter.next());
            } 
        } */
         

        /* assume we are getting a WSPasswordCall instance from Rampart */
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof WSPasswordCallback) {
                WSPasswordCallback pc = (WSPasswordCallback) callbacks[i];
                if (keyPass != null) {
                    pc.setPassword(keyPass);
                } else {
                    pc.setPassword("password");
                } 
            } else {
                throw new UnsupportedCallbackException(callbacks[i],
                "Unrecognized Callback");
            }
        }
    }
}
/*
 *   Old style reading password from properties file
 
 import java.util.Properties;
import net.es.oscars.PropHandler;
   Properties props = new Properties();

   FileInputStream in = new FileInputStream("repo/rampConfig.xml");
    props.load(in);
    propKeyPass = props.getProperty(
                "net.es.oscars.client.security.PWCallback.keypass");
   "org.apache.ws.security.crypto.merlin.keystore.password");
    in.close();

         * This usage type is used only in case we received a
         * username token with a password of type PasswordText or
         * an unknown password type.
         * 
         * This case the WSPasswordCallback object contains the
         * identifier (aka username), the password we received, and
         * the password type string to identify the type.
         * 
         * Here we perform only a very simple check.
 
    if (pc.getUsage() == WSPasswordCallback.USERNAME_TOKEN_UNKNOWN) {
            if(pc.getIdentifer().equals("Ron") && pc.getPassword().equals("noR")) {
                 return;
            }
            if (pc.getPassword().equals("sirhC")) {
                return;
            }               	
            throw new UnsupportedCallbackException(callbacks[i],
                "check failed");
      } 

        
         * here call a function/method to lookup the password for
         * the given identifier (e.g. a user name or keystore alias)
         * e.g.: pc.setPassword(passStore.getPassword(pc.getIdentfifier))
         * for Testing we supply a fixed name here.

    if (pc.getUsage() == WSPasswordCallback.KEY_NAME) {
            pc.setKey(key);
        }else if(propKeyPass != null){
            pc.setPassword(propKeyPass);
        } else if(pc.getIdentifer().equals("Ron")) {
            pc.setPassword("noR");
        } else {
            pc.setPassword("password");
        } 
        if (keyPass != null) {
            pc.setPassword(keyPass);
        } else {
            pc.setPassword("password");
        }
 */
 
