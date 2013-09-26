package net.es.nsi.cli.config;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class ResvProfile {
    protected Long id;
    protected String name;
    protected String gri;
    protected String description;
    protected String srcNet;
    protected String dstNet;
    protected String srcStp;
    protected String dstStp;
    protected Integer srcVlan;
    protected Integer dstVlan;
    protected Integer bandwidth;
    protected Integer version = 0;
    protected Date startTime = new Date();
    protected Date endTime = new Date();

    public String toString() {

        String out = "";
        out += "\n  name:        "+name;
        out += "\n  gri:         "+gri;
        out += "\n  description: "+description;
        out += "\n  bw:          "+bandwidth;
        out += "\n  v:           "+version;
        out += "\n  startTime:   "+startTime;
        out += "\n  endTime:     "+endTime;
        out += "\n  src: net:    "+srcNet;
        out += "\n       stp:    "+srcStp;
        out += "\n       vlan:   "+srcVlan;
        out += "\n  dst: net:    "+dstNet;
        out += "\n       stp:    "+dstStp;
        out += "\n       vlan:   "+dstVlan;
        out += "\n";
        return out;
    }
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSrcNet() {
        return srcNet;
    }

    public void setSrcNet(String srcNet) {
        this.srcNet = srcNet;
    }

    public String getDstNet() {
        return dstNet;
    }

    public void setDstNet(String dstNet) {
        this.dstNet = dstNet;
    }

    public String getSrcStp() {
        return srcStp;
    }

    public void setSrcStp(String srcStp) {
        this.srcStp = srcStp;
    }

    public String getDstStp() {
        return dstStp;
    }

    public void setDstStp(String dstStp) {
        this.dstStp = dstStp;
    }

    public Integer getSrcVlan() {
        return srcVlan;
    }

    public void setSrcVlan(Integer srcVlan) {
        this.srcVlan = srcVlan;
    }

    public Integer getDstVlan() {
        return dstVlan;
    }

    public void setDstVlan(Integer dstVlan) {
        this.dstVlan = dstVlan;
    }

    public Integer getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(Integer bandwidth) {
        this.bandwidth = bandwidth;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getGri() {
        return gri;
    }

    public void setGri(String gri) {
        this.gri = gri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
