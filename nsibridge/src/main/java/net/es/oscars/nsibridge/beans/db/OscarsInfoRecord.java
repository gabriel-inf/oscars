package net.es.oscars.nsibridge.beans.db;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity

public class OscarsInfoRecord {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    protected Long id;
    protected String srcVlan;
    protected String dstVlan;
    protected boolean present;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}
