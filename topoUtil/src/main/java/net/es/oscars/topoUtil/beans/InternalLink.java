package net.es.oscars.topoUtil.beans;

public class InternalLink extends GenericLink {
    protected Integer metric;
    protected String remote;

    public Integer getMetric() {
        return metric;
    }

    public void setMetric(Integer metric) {
        this.metric = metric;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }
}
