package net.es.oscars.nsibridge.config;

public class TimingConfig {
    protected double taskInterval;
    protected double resvTimeout;
    protected OscarsTimingConfig oscarsTimingConfig;

    public OscarsTimingConfig getOscarsTimingConfig() {
        return oscarsTimingConfig;
    }

    public void setOscarsTimingConfig(OscarsTimingConfig oscarsTimingConfig) {
        this.oscarsTimingConfig = oscarsTimingConfig;
    }

    public double getResvTimeout() {
        return resvTimeout;
    }

    public void setResvTimeout(double resvTimeout) {
        this.resvTimeout = resvTimeout;
    }


    public double getTaskInterval() {
        return taskInterval;
    }

    public void setTaskInterval(double taskInterval) {
        this.taskInterval = taskInterval;
    }

}
