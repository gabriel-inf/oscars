package net.es.oscars.pathfinder;

import net.es.oscars.pathfinder.db.DBPathfinder;
import net.es.oscars.pathfinder.perfsonar.PSPathfinder;
import net.es.oscars.pathfinder.staticroute.*;
import net.es.oscars.pathfinder.terce.TERCEPathfinder;


/**
 * This class contains a factory method to create a Pathfinder instance
 * implementing the PCE interface.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class PathfinderFactory {
    /**
     * Factory method.
     *
     * @return interdomain pathfinder An instance of a class implementing the InterdomainPCE interface.
     */
    public InterdomainPCE getInterdomainPCE(String method, String dbname) {
        if (method.equals(PCEMethod.DATABASE)) {
            return new DBPathfinder(dbname);
        } else if (method.equals(PCEMethod.PERFSONAR)) {
            return new PSPathfinder(dbname);
        } else if (method.equals(PCEMethod.STATIC)) {
            return new StaticDBInterPathfinder(dbname);
        }
        return null;
    }

    /**
     * Factory method.
     *
     * @return local pathfinder An instance of a class implementing the LocalPCE interface.
     */
    public LocalPCE getLocalPCE(String method, String dbname) {
        if (method.equals(PCEMethod.DATABASE)) {
            return new DBPathfinder(dbname);
        } else if (method.equals(PCEMethod.PERFSONAR)) {
            return new PSPathfinder(dbname);
        } else if (method.equals(PCEMethod.STATIC)) {
            return new XMLFileLocalPathfinder(dbname);
        }else if (method.equals(PCEMethod.TERCE)) {
            return new TERCEPathfinder(dbname);
        }
        return null;
    }

}
