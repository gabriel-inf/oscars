package net.es.oscars.nsibridge.config;

public class TimingConfig {
    protected double taskInterval;
    protected double resvTimeout;
    protected double queryAfterResvWait;
    protected double queryAfterCancelWait;
    protected double queryAfterSetupWait;
    protected double queryAfterTeardownWait;
    protected double queryInterval;
    protected double queryResultDelay;
    protected double oscarsResvTimeout;
    protected double modifyWaitTimeout;

    public double getResvTimeout() {
        return resvTimeout;
    }

    public void setResvTimeout(double resvTimeout) {
        this.resvTimeout = resvTimeout;
    }

    public double getQueryAfterResvWait() {
        return queryAfterResvWait;
    }

    public void setQueryAfterResvWait(double queryAfterResvWait) {
        this.queryAfterResvWait = queryAfterResvWait;
    }

    public double getQueryAfterSetupWait() {
        return queryAfterSetupWait;
    }

    public void setQueryAfterSetupWait(double queryAfterSetupWait) {
        this.queryAfterSetupWait = queryAfterSetupWait;
    }

    public double getQueryAfterTeardownWait() {
        return queryAfterTeardownWait;
    }

    public void setQueryAfterTeardownWait(double queryAfterTeardownWait) {
        this.queryAfterTeardownWait = queryAfterTeardownWait;
    }

    public double getQueryInterval() {
        return queryInterval;
    }

    public void setQueryInterval(double queryInterval) {
        this.queryInterval = queryInterval;
    }

    public double getQueryResultDelay() {
        return queryResultDelay;
    }

    public void setQueryResultDelay(double queryResultDelay) {
        this.queryResultDelay = queryResultDelay;
    }

    public double getTaskInterval() {
        return taskInterval;
    }

    public void setTaskInterval(double taskInterval) {
        this.taskInterval = taskInterval;
    }

    public double getModifyWaitTimeout() {
        return modifyWaitTimeout;
    }

    public void setModifyWaitTimeout(double modifyWaitTimeout) {
        this.modifyWaitTimeout = modifyWaitTimeout;
    }

    public double getOscarsResvTimeout() {
        return oscarsResvTimeout;
    }

    public void setOscarsResvTimeout(double oscarsResvTimeout) {
        this.oscarsResvTimeout = oscarsResvTimeout;
    }

    public double getQueryAfterCancelWait() {
        return queryAfterCancelWait;
    }

    public void setQueryAfterCancelWait(double queryAfterCancelWait) {
        this.queryAfterCancelWait = queryAfterCancelWait;
    }
}
