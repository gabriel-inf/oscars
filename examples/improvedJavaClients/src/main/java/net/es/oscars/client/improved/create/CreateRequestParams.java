package net.es.oscars.client.improved.create;

import java.util.List;

public class CreateRequestParams {
    private String gri;
    private String src;
    private String dst;
    private String pathSetupMode;
    private String srcVlan;
    private String dstVlan;
    private String layer;
    private Integer bandwidth;
    private String description;
	private List<String> path;
    private Long startTime;
    private Long endTime;
    public String getGri() {
        return gri;
    }
    public void setGri(String gri) {
        this.gri = gri;
    }
    public String getSrc() {
        return src;
    }
    public void setSrc(String src) {
        this.src = src;
    }
    public String getDst() {
        return dst;
    }
    public void setDst(String dst) {
        this.dst = dst;
    }
    public String getPathSetupMode() {
        return pathSetupMode;
    }
    public void setPathSetupMode(String pathSetupMode) {
        this.pathSetupMode = pathSetupMode;
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
    public String getLayer() {
        return layer;
    }
    public void setLayer(String layer) {
        this.layer = layer;
    }
    public Integer getBandwidth() {
        return bandwidth;
    }
    public void setBandwidth(Integer bandwidth) {
        this.bandwidth = bandwidth;
    }
    public String getDescription() {
    	return description;
    }
	public void setDescription(String description) {
    	this.description = description;
    }

    public List<String> getPath() {
        return path;
    }
    public void setPath(List<String> path) {
        this.path = path;
    }
    public Long getStartTime() {
        return startTime;
    }
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    public Long getEndTime() {
        return endTime;
    }
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

}
