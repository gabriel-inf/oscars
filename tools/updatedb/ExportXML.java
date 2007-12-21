import net.es.oscars.bss.topology.*;
import net.es.oscars.database.Initializer;

import org.jdom.*;
import org.jdom.output.*;

import java.util.*;

/**
 * This class will export the entire topology database into the
 * interdomain XML format. This is mostly for debugging purposes.
 * The user can pass a domain URN (ie. urn:ofg:network:ASNUMBER)
 * in string format to the getTopology call to get the stored
 * topology for a particular domain.
 * @author Evangelos Chaniotakis (haniotak /at/ es dot net
 */
public class ExportXML {
    public static void main(String[] argv) {

        String usage = "Usage:\nexportXML.sh [urn-of-domain]";
        String urn = "";

        if (argv.length > 1) {
            System.out.println(""+usage);
            System.exit(1);
        } else if (argv.length == 1) {
            urn = argv[0];
            if (urn.equals("-h") || urn.equals("--help")) {
                System.out.println(""+usage);
                System.exit(1);
            }
        }
        
        List<String> dbnames = new ArrayList<String>();
        dbnames.add("bss");

        Initializer initializer = new Initializer();
        initializer.initDatabase(dbnames);

        TopologyXMLExporter exporter = new TopologyXMLExporter("bss");
        
        Document doc = exporter.getTopology(urn);

        try {
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            outputter.output(doc, System.out);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
