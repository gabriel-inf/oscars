package net.es.oscars;

import org.apache.log4j.*;

/**
 * LogWrapper exists to make logging less verbose.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class LogWrapper {
    private Logger log;
    private String className;

    /**
     * Constructor for LogWrapper.
     *
     * @param classObj class of invoker
     */
    public LogWrapper(Class classObj) {
//        PropertyConfigurator.configure("log4j.properties");
        this.log = Logger.getLogger(classObj);
        this.className = classObj.getName();
    }

    /**
     * Wraps log4j, debug level.
     *
     * @param evtName a string containing event name
     * @param evtMsg a string containing event message
     */
    public void debug(String evtName, String evtMsg) {
        this.log.debug(this.className + "." + evtName + ": " + evtMsg);
    }

    /**
     * Wraps log4j, info level.
     *
     * @param evtName a string containing event name
     * @param evtMsg a string containing event message
     */
    public void info(String evtName, String evtMsg) {
        this.log.info(this.className + "." + evtName + ": " + evtMsg);
    }

    /**
     * Wraps log4j, warn level.
     *
     * @param evtName a string containing event name
     * @param evtMsg a string containing event message
     */
    public void warn(String evtName, String evtMsg) {
        this.log.warn(this.className + "." + evtName + ": " + evtMsg);
    }

    /**
     * Wraps log4j, error level.
     *
     * @param evtName a string containing event name
     * @param evtMsg a string containing event message
     */
    public void error(String evtName, String evtMsg) {
        this.log.error(this.className + "." + evtName + ": " + evtMsg);
    }
    /**
     * Wraps log4j, fatal level.
     *
     * @param evtName a string containing event name
     * @param evtMsg a string containing event message
     */
    public void fatal(String evtName, String evtMsg) {
        this.log.fatal(this.className + "." + evtName + ": " + evtMsg);
    }
}
