package net.es.oscars.notifybroker.jdom;

import net.es.oscars.notifybroker.ws.WSNotifyConstants;

import org.jdom.Element;

public class SubscriptionReference {
    private String address;
    private String subscriptionId;
    
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
    public String getSubscriptionId() {
        return this.subscriptionId;
    }
    
    /**
     * 
     * @param publisherRegId the publisherRegId to set
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }
    
    /**
     * @return the jdom
     */
    public Element getJdom() {
        Element jdom = new Element("SubscriptionReference", WSNotifyConstants.WSN_NS);
        
        //Add address
        if(this.address != null){
            Element addrElem = new Element("Address", WSNotifyConstants.WSA_NS);
            addrElem.setText(this.address);
            jdom.addContent(addrElem);
        }
        
        //Add subscription ID
        if(this.subscriptionId != null){
            Element refParams = new Element("ReferenceParameters", WSNotifyConstants.WSA_NS);
            Element subscrIdElem = new Element("subscriptionId", WSNotifyConstants.IDC_NS);
            subscrIdElem.setText(this.subscriptionId);
            refParams.addContent(subscrIdElem);
            jdom.addContent(refParams);
        }
        
        return jdom;
    }
}
