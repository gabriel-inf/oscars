package net.es.oscars;

import org.testng.annotations.*;

@Test(groups={ "core" })
public class LogWrapperTest {
    private LogWrapper logWrapper;

  @BeforeClass
    protected void setUpClass() {
        this.logWrapper = new LogWrapper(getClass());
    }

    public void logDebug() {
        this.logWrapper.debug("logDebug", "test LogWrapper debug level");
    }

    public void logInfo() {
        this.logWrapper.info("logInfo", "test LogWrapper info level");
    }

    public void logWarn() {
        this.logWrapper.warn("logWarn", "test LogWrapper warn level");
    }

    public void logError() {
        this.logWrapper.error("logError", "test LogWrapper error level");
    }

    public void logFatal() {
        this.logWrapper.fatal("logFatal", "test LogWrapper fatal level");
    }
}
