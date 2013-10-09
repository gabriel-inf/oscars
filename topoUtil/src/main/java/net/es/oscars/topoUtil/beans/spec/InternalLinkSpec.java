package net.es.oscars.topoUtil.beans.spec;

public class InternalLinkSpec extends GenericLinkSpec {
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
