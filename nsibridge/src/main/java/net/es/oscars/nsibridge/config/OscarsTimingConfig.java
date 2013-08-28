package net.es.oscars.nsibridge.config;

public class OscarsTimingConfig {
    protected double submitTimeout;
    protected double pollTimeout;
    protected double pollInterval;

    public double getSubmitTimeout() {
        return submitTimeout;
    }

    public void setSubmitTimeout(double submitTimeout) {
        this.submitTimeout = submitTimeout;
    }

    public double getPollTimeout() {
        return pollTimeout;
    }

    public void setPollTimeout(double pollTimeout) {
        this.pollTimeout = pollTimeout;
    }

    public double getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(double pollInterval) {
        this.pollInterval = pollInterval;
    }
}
