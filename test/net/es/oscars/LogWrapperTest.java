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
        this.log.debug("testDebug", "test LogWrapper debug level");
        Assert.assertTrue(true);
    }

    public void testInfo() {
        this.log.info("testInfo", "test LogWrapper info level");
        Assert.assertTrue(true);
    }

    public void testWarn() {
        this.log.warn("testWarn", "test LogWrapper warn level");
        Assert.assertTrue(true);
    }

    public void testError() {
        this.log.error("testError", "test LogWrapper error level");
        Assert.assertTrue(true);
    }

    public void testFatal() {
        this.log.fatal("testFatal", "test LogWrapper fatal level");
        Assert.assertTrue(true);
    }
}
