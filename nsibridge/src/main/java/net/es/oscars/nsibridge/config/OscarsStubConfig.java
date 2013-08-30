package net.es.oscars.nsibridge.config;

public class OscarsStubConfig {
    protected boolean stub;
    protected long queryDelayMillis;
    protected long resvDelayMillis;
    protected long cancelDelayMillis;
    protected long setupDelayMillis;
    protected long teardownDelayMillis;
    protected long authDelayMillis;
    protected long responseDelayMillis;

    public boolean isStub() {
        return stub;
    }

    public void setStub(boolean stub) {
        this.stub = stub;
    }


    public long getQueryDelayMillis() {
        return queryDelayMillis;
    }

    public void setQueryDelayMillis(long queryDelayMillis) {
        this.queryDelayMillis = queryDelayMillis;
    }

    public long getResvDelayMillis() {
        return resvDelayMillis;
    }

    public void setResvDelayMillis(long resvDelayMillis) {
        this.resvDelayMillis = resvDelayMillis;
    }

    public long getCancelDelayMillis() {
        return cancelDelayMillis;
    }

    public void setCancelDelayMillis(long cancelDelayMillis) {
        this.cancelDelayMillis = cancelDelayMillis;
    }

    public long getSetupDelayMillis() {
        return setupDelayMillis;
    }

    public void setSetupDelayMillis(long setupDelayMillis) {
        this.setupDelayMillis = setupDelayMillis;
    }

    public long getTeardownDelayMillis() {
        return teardownDelayMillis;
    }

    public void setTeardownDelayMillis(long teardownDelayMillis) {
        this.teardownDelayMillis = teardownDelayMillis;
    }

    public long getAuthDelayMillis() {
        return authDelayMillis;
    }

    public void setAuthDelayMillis(long authDelayMillis) {
        this.authDelayMillis = authDelayMillis;
    }


    public long getResponseDelayMillis() {
        return responseDelayMillis;
    }

    public void setResponseDelayMillis(long responseDelayMillis) {
        this.responseDelayMillis = responseDelayMillis;
    }
}
