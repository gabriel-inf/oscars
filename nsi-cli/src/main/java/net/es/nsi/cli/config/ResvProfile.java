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
    protected String srcStp;
    protected String dstStp;
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
        out += "\n  src: stp:    "+srcStp;
        out += "\n  dst: stp:    "+dstStp;
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
