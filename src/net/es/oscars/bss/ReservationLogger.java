package net.es.oscars.bss;

import java.util.*;
import java.io.*;

import org.apache.log4j.*;

import net.es.oscars.PropHandler;


/**
 * ReservationLogger is a utility class that does per-reservation logging.
 * It will configure log4j to append its logs into files according to GRI, 
 * in a logging directory set by the rsvlogdir property in oscars.properties.
 * 
 * To use, construct an instance of this class giving as argument the Logger
 * you want to redirect, then wrap the statements for which you want the
 * redirection to happen in ReservationLogger.configure(gri) / unconfigure()
 * 
 * Example:
 * 
 * Logger this.log = Logger.getLogger(this.getClass());
 * 
 * ReservationLogger rsvLog = new ReservationLogger(this.log);
 *     Now: this.log output as per log4j properties
 * rsvLog.redirect(someGRI);
 *     Now: all this.log output is also appended to someGRI.log 
 * rsvLog.stop(); 
 *     Now: this.log output as per log4j properties.
 *
 * @author Evangelos Chaniotakis, haniotak@es.net
 */
public class ReservationLogger {
    private Logger log;
    private FileAppender appender; 

    /** Constructor. */
    public ReservationLogger(Logger log) {
        this.log = log;
    }
    
    /**
     * Sets up a per-reservation logger 
     * @param gri the reservation identifier
     */
    public void redirect(String gri) {
    	String rsvLogDir = "";
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties logprops = propHandler.getPropertyGroup("logging", true);
        if (logprops != null) {
        	rsvLogDir = logprops.getProperty("rsvlogdir");
        }
        rsvLogDir = rsvLogDir.trim();
        if (rsvLogDir.equals("")) {
        	this.log.error("Per-reservation log dir is empty. Set oscars.properties: logging.rsvlogdir");
        	this.appender = null;
        	return;
        }
        File d = new File(rsvLogDir);
        if (!d.exists()) {
        	this.log.error("Per-reservation log dir "+rsvLogDir+" does not exist (oscars.properties:logging.rsvlogdir)");
        	this.appender = null;
        	return;
        } else if (!d.canWrite()) {
        	this.log.error("Per-reservation log dir "+rsvLogDir+" unwritable (oscars.properties:logging.rsvlogdir)");
        	this.appender = null;
        	return;
        }
        
        if (this.appender == null) {
	        this.appender = new FileAppender();
	        PatternLayout layout = new PatternLayout();
	        layout.setConversionPattern("%d{ISO8601} [%p] %C{2} %m%n");
	        this.appender.setLayout(layout);
	        this.appender.setFile( rsvLogDir+"/"+gri+".log");
	        this.appender.activateOptions();  
	        this.log.addAppender( this.appender );
        }
    }
    
    /**
     * Tears down the per-reservation logger 
     */
    public void stop() {
    	if (this.appender != null) {
    		this.log.removeAppender(this.appender);
    	}
    }
}
