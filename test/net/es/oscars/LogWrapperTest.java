package net.es.oscars;

import junit.framework.*;

public class LogWrapperTest extends TestCase {
    private LogWrapper log;

    public LogWrapperTest(String name) {
        super(name);
    }
        
    protected void setUp() {
        this.log = new LogWrapper(LogWrapperTest.class);
    }

    public void testDebug() {
        this.log.debug("testDebug", "test debug method");
        Assert.assertTrue(true);
    }

    public void testInfo() {
        this.log.info("testInfo", "test info method");
        Assert.assertTrue(true);
    }

    public void testWarn() {
        this.log.warn("testWarn", "test warn method");
        Assert.assertTrue(true);
    }

    public void testError() {
        this.log.error("testError", "test error method");
        Assert.assertTrue(true);
    }

    public void testFatal() {
        this.log.fatal("testFatal", "test fatal method");
        Assert.assertTrue(true);
    }
}
