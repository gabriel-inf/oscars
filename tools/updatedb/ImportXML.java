import net.es.oscars.bss.topology.*;
import net.es.oscars.database.Initializer;

import java.util.*;

/**
 * This class updates the topology database.  It reads a single XML
 * file into the database. The file should be in the topology
 * control plane exchange schema. The XML importer will do all
 * necessary changes to the database. (currently only adding is\
 * supported)
 * @author Evangelos Chaniotakis (haniotak /at/ es dot net
 */

public class ImportXML {
    public static void main(String[] argv) {
        String usage = "Usage:\nimportXMLFile.sh /path/to/file";
        String filename = "";
        
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);

        if (argv.length != 1) {
            System.out.println(""+usage);
            System.exit(1);
        }
        filename = argv[0];
        
        TopologyXMLFileReader reader = new TopologyXMLFileReader("bss");

        reader.importFile(filename);
    }
}
