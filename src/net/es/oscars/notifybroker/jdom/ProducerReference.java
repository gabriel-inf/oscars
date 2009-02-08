package net.es.oscars.notifybroker.jdom;

import net.es.oscars.notifybroker.ws.WSNotifyConstants;

import org.jdom.Element;

public class ProducerReference {
    private String address;
    private String publisherRegId;
    
    /**
     * @return the address
     */
    public String getAddress() {
        return this.address;
    }
    /**
     * @param url the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }
    /**
     * @return the publisherRegId
     */
    public String getPublisherRegId() {
        return this.publisherRegId;
    }
    /**
     * @param publisherRegId the publisherRegId to set
     */
    public void setPublisherRegId(String publisherRegId) {
        this.publisherRegId = publisherRegId;
    }
    
    /**
     * @return the jdom
     */
    public Element getJdom() {
        Element jdom = new Element("ProducerReference", WSNotifyConstants.WSN_NS);
        
        //Add address
        if(this.address != null){
            Element addrElem = new Element("Address", WSNotifyConstants.WSA_NS);
            addrElem.setText(this.address);
            jdom.addContent(addrElem);
        }
        
        //Add publisher registration ID
        if(this.publisherRegId != null){
            Element refParams = new Element("ReferenceParameters", WSNotifyConstants.WSA_NS);
            Element pubRegElem = new Element("publisherRegistrationId", WSNotifyConstants.IDC_NS);
            pubRegElem.setText(this.publisherRegId);
            refParams.addContent(pubRegElem);
            jdom.addContent(refParams);
        }
        
        return jdom;
    }
    
}
