package net.es.oscars.pss.eompls;

public abstract class EoMPLSBasicHandler {
    /**
     * log the generated configuration or not
     */
    protected boolean logConfig           = true;

    /**
     * stub mode: if true, no configuration will be sent to the routers
     * and all requests will be successful
     */
    protected boolean stubMode            = true;

    /**
     * failure behavior: if set, the teardown config will be sent to the routers
     * if not set (i.e. for debugging purposes), the config will remain
     */
    protected boolean teardownOnFailure   = true;

    /**
     * setup behavior: if true, generate a status check job afterwards
     */
    protected boolean checkStatusAfterSetup       = true;

    /**
     * teardown behavior: if true, generate a status check job afterwards
     */
    protected boolean checkStatusAfterTeardown    = true;


    public boolean isLogConfig() {
        return logConfig;
    }

    public void setLogConfig(boolean logConfig) {
        this.logConfig = logConfig;
    }

    public boolean isStubMode() {
        return stubMode;
    }

    public void setStubMode(boolean stubMode) {
        this.stubMode = stubMode;
    }

    public boolean isTeardownOnFailure() {
        return teardownOnFailure;
    }

    public void setTeardownOnFailure(boolean teardownOnFailure) {
        this.teardownOnFailure = teardownOnFailure;
    }

    public boolean isCheckStatusAfterSetup() {
        return checkStatusAfterSetup;
    }

    public void setCheckStatusAfterSetup(boolean checkStatusAfterSetup) {
        this.checkStatusAfterSetup = checkStatusAfterSetup;
    }

    public boolean isCheckStatusAfterTeardown() {
        return checkStatusAfterTeardown;
    }

    public void setCheckStatusAfterTeardown(boolean checkStatusAfterTeardown) {
        this.checkStatusAfterTeardown = checkStatusAfterTeardown;
    }

}
