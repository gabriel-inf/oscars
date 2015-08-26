package net.es.oscars.nsibridge.beans.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity

public class OscarsInfoRecord {
    protected Long id;
    protected String srcVlan;
    protected String dstVlan;

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public String getSrcVlan() {
        return srcVlan;
    }

    public void setSrcVlan(String srcVlan) {
        this.srcVlan = srcVlan;
    }

    public String getDstVlan() {
        return dstVlan;
    }

    public void setDstVlan(String dstVlan) {
        this.dstVlan = dstVlan;
    }

}
