package net.es.oscars.tss;

import net.es.oscars.tss.oscars.OSCARSDatabase;
import net.es.oscars.tss.terce.TERCEDatabase;

import org.apache.log4j.*;



/**
 * Factory class that creates an instance of a module that implements
 * TEDB. Created instance will be used to access a topology database.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class TEDBFactory {
    private Logger log;

    /**
    * Constructor that initializes logs.
    */
    public TEDBFactory() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Creates a new TEDB instance of the given type. Currently
     * <i>terce</i> and <i>oscars</i> are the only supported values.
     *
     * @param tedbType a string indicating the type of TEDB to create
     * @return a new instance of a TEDB. null if tedbType is not recognized.
     */
    public TEDB createTEDB(String tedbType) {
        this.log.info("Locating TEDB for:["+tedbType+"]");
        if (tedbType.equals("terce")) {
            this.log.info("TEDB is TERCE");
            return new TERCEDatabase();
        } else if (tedbType.equals("oscars")) {
            this.log.info("TEDB is OSCARS");
            return new OSCARSDatabase();
        }
        this.log.error("TEDB not found!");

        return null;
    }
}
