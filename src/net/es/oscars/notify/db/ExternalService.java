package net.es.oscars.notify.db;

import java.io.Serializable;

import net.es.oscars.database.HibernateBean;

/**
 * ExternalService is adapted from a Middlegen class automatically generated
 * from the schema for the notify.externalServices table.
 */
public class ExternalService  extends HibernateBean implements Serializable{
	// TODO:  need to do this via Ant rather than manually
    // The number is the latest Subversion revision number
    private static final long serialVersionUID = 4151;

    /** persistent field */
    private String type;
    
    /** persistent field */
    private String url;
    
    /** null-able persistent field */
    private String serviceKey;
    
    public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}
}
