package net.es.oscars.pss.common;

public class PSSHandlerConfigBean {
    /**
     * log the generated configuration or not
     */
    private boolean logConfig           = true;

    /**
     * stub mode: if true, no configuration will be sent to the routers
     * and all requests will be successful
     */
    private boolean stubMode            = true;

    /**
     * failure behavior: if set, the teardown config will be sent to the routers
     * if not set (i.e. for debugging purposes), the config will remain
     */
    private boolean teardownOnFailure   = true;

    /**
     * setup behavior: if true, generate a status check job afterwards
     */
    private boolean checkStatusAfterSetup       = true;

    /**
     * teardown behavior: if true, generate a status check job afterwards
     */
    private boolean checkStatusAfterTeardown    = true;


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
