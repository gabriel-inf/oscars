package net.es.oscars.pathfinder;

import java.util.*;

import org.apache.log4j.*;

/**
 * This class is intended to be subclassed by TERCEPathfinder and
 * TraceroutePathfinder.
 *
 * @author David Robertson (dwrobertson@lbl.gov), Andrew Lake (alake@internet2.edu)
 */
public class Pathfinder {
    private Logger log;
    protected String dbname;

    public Pathfinder() {
        this.log = Logger.getLogger(this.getClass());
    }

    protected void setDatabase(String dbname) {
        this.dbname = dbname;
    }
}
