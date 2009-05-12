package net.es.oscars.client.improved;

import org.apache.ws.security.WSPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

import java.io.FileInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import javax.xml.namespace.QName;
import java.util.Iterator;

import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;


public class PWCallback implements CallbackHandler {
    public static String  rampConfigFname;
    /*
     *
     * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
     */
    @SuppressWarnings("unchecked")
    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {

        String keyPass = null;
        try {
            FileInputStream fis = new FileInputStream(rampConfigFname);
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
        } catch (XMLStreamException e){
            throw new IOException (e.getMessage());
        }
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
