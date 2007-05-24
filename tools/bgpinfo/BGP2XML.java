import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import net.es.oscars.oscars.*;
import net.es.oscars.client.Client;
import net.es.oscars.bss.topology.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.Initializer;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.*;
import org.hibernate.*;

public class BGP2XML {

    public static PrintStream output = System.out;
    public static String peer = null;

    public static void init ()  
    {
        output = System.out;
    }

    private static String fName = null;
    private static boolean dryrun = false;
    private static boolean verbosity;

    public static void main(String[] argv) {
        int optind;
        String usage = "tp2xml [-v] [-d] -f /path/to/file.tp";

        init();

        for (optind = 0; optind < argv.length; optind++) {
            if (argv[optind].equals("-d")) {
                dryrun = true;
            } else
                if (argv[optind].equals("-f")) {
                    fName = argv[++optind];
                } else
                    if (argv[optind].equals("-v")) {
                        verbosity = true;
                        } else
                            if (argv[optind].startsWith("-")) {
                                System.out.println("" + usage);
                                System.out.println("-- Invalid option " + argv[optind]);
                                System.exit(1);
                            } else {
                                break;
                            }
        }

        if (fName == null) {
            System.out.println(usage);
            System.exit(1);
        }
        processFile( fName);
    }

    public static void processFile( String fName) {
        try {
            BufferedReader reader = new BufferedReader (new FileReader (fName));
            StringBuffer buf = new StringBuffer();
            String text;

            try {
                while ((text=reader.readLine()) != null) {
                    buf.append(text + "\n");
                }
                reader.close();
            } catch (java.io.IOException e) {
                System.err.println("Unable to read from file "+ fName);
                System.exit(2);
            }
       
            String buffer = new String(buf);
            String records[] =  buffer.split( "\n" );

            outputHeader();

            for (int rec=0; rec <records.length; rec++ ) {
                processLine(records[rec]);
            }   
            outputFooter();

        } catch (java.io.FileNotFoundException Ex) {
            System.err.println("Unable to open file "+ fName);
            System.exit(2);
        }
    }

    // ipv4net-+-211.79.48.172-+-chi-sl-mr1-+-ip_addr-+-211.79.48.173-+-bgp_peers-+-211.79.48.174-+-established-+-2410806
    // ipv4net-+-211.79.48.172-+-chi-sl-mr1-+-ip_addr-+-211.79.48.173-+-bgp_peers-+-211.79.48.174-+-peer_if-+-116
    // ipv4net-+-211.79.48.172-+-chi-sl-mr1-+-ip_addr-+-211.79.48.173-+-bgp_peers-+-211.79.48.174-+-remote_as-+-7539

    public static void processLine( String bgpLine ) 
    {
        String parts[] = bgpLine.split("-\\+-"); 

        if ( parts.length != 9 ) 
            return;

        if ( parts[5].equals("bgp_peers")) 
        {
            if (parts[6].equals(peer) == false)
            {
                    if ((peer != null) ) {
                        output.println("\t</bgp-peer>");
                    }
                    output.println("\t<bgp-peer junos:style=\"terse\">");
                    output.println("\t\t<local-address>" + parts[4] + "</local-address>");
                    output.println("\t\t<peer-address>" + parts[6] + "</peer-address>");
                    output.println("\t\t<peer-broadcast>" + parts[2] + "</peer-broadcast>");
                    peer = parts[6];
            }

            if ((peer != null) && (parts[6].equals(peer) == true) ) {
                if ( parts[7].equals("established"))  { 
                    output.println("\t\t<elapsed-time>" + parts[8] + "</elapsed-time>");
                    output.println("\t\t<peer-state>established</peer-state>");
                } else if ( parts[7].equals("peer_if"))  {
                        output.println("\t\t<local-if>" + parts[8] + "</local-if>");
                } else if ( parts[7].equals("remote_as"))  {
                    output.println("\t\t<peer-as>" + parts[8] + "</peer-as>");
                }
            }

        }

    }

    public static void outputHeader() {
        output.println("<bgp-information xmlns=\"http://xml.juniper.net/junos/8.1I0/junos-routing\">");
    }

    public static void outputFooter() {
        output.println("\t</bgp-peer>");
        output.println("</bgp-information>");
    }
}
