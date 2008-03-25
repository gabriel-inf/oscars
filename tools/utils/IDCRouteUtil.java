import java.util.*;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.database.*;

import org.apache.log4j.*;
import org.hibernate.*;

/**
 * Allows users to view, add and delete routes to the static routing
 * database. Specifically, the table it affects are the interdomainRoutes
 * and routeElems tables.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class IDCRouteUtil {
    private HashMap<String, String> params;
    private String dbname;
    private Logger log;

    /**
     * Constructor that accepts array of command-line parameters
     *
     * @param args String array containing command line parameters
     */
    public IDCRouteUtil(String[] args){
        this.log = Logger.getLogger(this.getClass());
        this.params = new HashMap<String, String>();
        this.dbname = "bss";
        String function = this.parseParams(args);

        if(function.equals("add")){
            this.addRoute();
        }else if(function.equals("del")){
            this.deleteRoute();
        }else{
            this.showRoutes();
        }
    }

    /**
     * Parses parameters from the command line and stores them in a globally
     * undertood hash. Also returns the function (show, add, or del) the user
     * wishes to perform.
     *
     * @param args String array containing command line parameters
     * @return a String indicating the function the user wishes to perform
     */
    private String parseParams(String[] args){
        String state = "";
        String function = null;
        int start = 0;
        function = "show";

        if(args == null || args.length < 1){
            return function;
        }

        if(args[0].equals("add") || args[0].equals("del")){
            start = 1;
            function = args[0];
        }else if(!(args[0].equals("-detail") || args[0].equals("-id"))){
            this.printHelp();
            System.exit(0);
        }

        for(int i = start; i < args.length; i++){
            String arg = args[i];

            if(state.equals("source") || state.equals("dest") ||
                state.equals("egress") || state.equals("id")){
                params.put(state, arg);
                state = "";
            }else if(state.equals("") && (arg.equals("-source") ||
                     arg.equals("-dest") || arg.equals("-egress") ||
                     arg.equals("-id"))){
                state = arg.replaceAll("^-", "");
            }else if(state.equals("") && arg.equals("-multi") ||
                     arg.equals("-loose") || arg.equals("-default") ||
                     arg.equals("-detail")){
                String key = arg.replaceAll("^-", "");
                params.put(key, "1");
            }else if(arg.equals("-help")){
                this.printHelp();
                System.exit(0);
            }else{
                System.err.println("Invalid parameter \"" + arg + "\"");
                System.exit(0);
            }
        }

        if(!state.equals("")){
            System.err.println("You did not specify a value for the \"" +
                               args[args.length -1] + "\" option");
            System.exit(0);
        }

        return function;
    }

    /**
     * Adds new routes to the database
     */
    private void addRoute(){
        InterdomainRoute route = new InterdomainRoute();
        String egressURN = params.get("egress");
        String srcURN = params.get("source");
        String destURN = params.get("dest");
        String file = params.get("file");
        boolean hasMulti = (params.containsKey("multi") &&
                         params.get("multi").equals("1"));
        boolean isDefault = (params.containsKey("default") &&
                         params.get("default").equals("1"));
        boolean isLoose = (params.containsKey("loose") &&
                         params.get("loose").equals("1"));
        Node srcNode = null;
        Port srcPort = null;
        Link srcLink = null;
        Domain destDomain = null;
        Node destNode = null;
        Port destPort = null;
        Link destLink = null;
        Link egressLink = null;
        ArrayList<String> hops = new ArrayList<String>();

        /* Validate parameters */
        if(egressURN == null && file == null && (!hasMulti)){
            System.err.println("You must specify the -egress or -multi option");
            System.exit(0);
        }

        if(srcURN == null && destURN == null && (!isDefault)){
            System.err.println("You must specify the -source, -dest, and/or " +
                               "-default option");
            System.exit(0);
        }

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();

        DomainDAO domainDAO = new DomainDAO("bss");

        /* Get source */
        if(srcURN != null){
            int urnType = TopologyUtil.getURNType(srcURN);
            String strURNType = "";
            if(!domainDAO.isLocal(TopologyUtil.getURNDomainId(srcURN))){
                System.err.println("The source must be in the local domain. " +
                                   "(NOTE: The source corresponds to the " +
                                   "ingress in the local domain NOT the " +
                                   "source of a createReservation request)");
                System.exit(0);
            }

            try{
                if(urnType == TopologyUtil.NODE_URN){
                    strURNType = "node ";
                    srcNode = TopologyUtil.getNode(srcURN, this.dbname);
                }else if(urnType == TopologyUtil.PORT_URN){
                    strURNType = "port ";
                    srcPort = TopologyUtil.getPort(srcURN, this.dbname);
                }else if(urnType == TopologyUtil.LINK_URN){
                    strURNType = "link ";
                    srcLink = TopologyUtil.getLink(srcURN, this.dbname);
                }else{
                    System.err.println("Source URN must be a valid node, " +
                                       "port, or link URN");
                    System.exit(0);
                }
            }catch(BSSException e){
                System.err.println("Could not find the specified source" +
                                    strURNType + "URN " + srcURN + " in the " +
                                    "database. You should verify that you " +
                                    "entered the URN correctly and that " +
                                    "OSCARS has the most recent version of " +
                                    "your domain's intradomain topology.");
                System.exit(0);
            }
        }

        /* Get destination */
        if(destURN != null){
            URNElements urnElems = this.prepareEntry(destURN, destDomain,
                                      destNode, destPort, destLink, bss);
            destDomain = urnElems.domain;
            destNode = urnElems.node;
            destPort = urnElems.port;
            destLink = urnElems.link;
            if(destDomain == null && destNode == null &&
               destPort == null && destLink == null){
                System.err.println("Invalid destination URN given.");
                System.exit(0);
            }
        }

        /* Get egress link */
        if(egressURN != null){
            hops.add(egressURN);
        }

        /* Get any hops past egress */
        if(hasMulti){
            hops = this.processMulti(hops, isLoose);
        }

        int hopCount = 0;
        RouteElem elem = null;
        RouteElem prevElem = null;
        for(int i = (hops.size() - 1); i >= 0; i--){
            String hop = hops.get(i);
            elem = new RouteElem();
            Domain domain = null;
            Node node = null;
            Port port = null;
            Link link = null;
            String domainId = TopologyUtil.getURNDomainId(hop);

            if(domainId == null){
                System.err.println("Invalid URN provided.");
                System.exit(0);
            }

            /* Handle egress link */
            if(hopCount == (hops.size() - 1)){
                if(!domainDAO.isLocal(domainId)){
                    System.err.println("The egress must be in the local domain");
                    System.exit(0);
                }
                try{
                    link = TopologyUtil.getLink(hop, this.dbname);
                    elem.setLink(link);
                }catch(BSSException e){
                    System.err.println("Could not find the specified egress " +
                                    "URN " + hop + " in the" +
                                    " database. You should verify that you " +
                                    "entered the URN correctly and that " +
                                    "OSCARS has the most recent version of " +
                                    "your domain's intradomain topology.");
                    System.exit(0);
                }
            }else{
                /* add entry */
                URNElements urnElems = this.prepareEntry(hop, domain, node, port,
                                                         link, bss);
                domain = urnElems.domain;
                node = urnElems.node;
                port = urnElems.port;
                link = urnElems.link;
                if(domain == null && node == null && port == null && link == null){
                    System.err.println("Invalid URN given for hop: " + hop);
                    System.exit(0);
                }
            }

            elem.setDomain(domain);
            elem.setNode(node);
            elem.setPort(port);
            elem.setLink(link);
            elem.setStrict(!isLoose);
            elem.setNextHop(prevElem);
            bss.save(elem);

            prevElem = elem;
            hopCount++;
        }

        /* save entry */
        route.setSrcNode(srcNode);
        route.setSrcPort(srcPort);
        route.setSrcLink(srcLink);
        route.setDestDomain(destDomain);
        route.setDestNode(destNode);
        route.setDestPort(destPort);
        route.setDestLink(destLink);
        route.setRouteElem(elem);
        route.setPreference(100);
        route.setDefaultRoute(isDefault);
        bss.save(route);

        bss.getTransaction().commit();
        System.out.println("Route added.");
    }

    /**
     * Deletes routes from the database
     */
    public void deleteRoute(){
        String idStr = params.get("id");
        int id = -1;
        Scanner in = new Scanner(System.in);

        if(idStr == null){
            this.showRoutes();
            System.out.print("Please enter the id of the route to delete: ");
            id = in.nextInt();
        }else{
            try{
                id=Integer.parseInt(idStr);
            }catch(Exception e){
                System.err.println("The provided ID is not an integer.");
                System.exit(0);
            }
        }

        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        InterdomainRouteDAO interDAO = new InterdomainRouteDAO(this.dbname);
        List<InterdomainRoute> routes = interDAO.list();
        if(id > routes.size() || id < 1){
            System.err.println("There is no route with that id.");
            System.exit(0);
        }

        /* Verify delete */
        System.out.print("Are you sure you want to delete the route with id " +
                          id + "? (y/n) ");
        String confirm = in.next();
        if(confirm != null && !confirm.toLowerCase().startsWith("y")){
            System.out.println("Route NOT deleted.");
            System.exit(0);
        }

        InterdomainRoute route = routes.get(id-1);
        RouteElem routeElem = route.getRouteElem();
        bss.delete(route);
        while(routeElem != null){
            RouteElem nextRouteElem = routeElem.getNextHop();
            bss.delete(routeElem);
            routeElem = nextRouteElem;
        }

        bss.getTransaction().commit();
        System.out.println("Route deleted.");

    }

    /**
     * Prints routes currently in the database. The level of detail printed
     * depends of the paramters stored in the global hash.
     */
    public void showRoutes(){
        /* Init database */
        Initializer initializer = new Initializer();
        ArrayList<String> dbnames = new ArrayList<String>();
        dbnames.add(this.dbname);
        initializer.initDatabase(dbnames);
        Session bss =
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        boolean showDetail = (params.containsKey("detail") &&
                         params.get("detail").equals("1"));
        InterdomainRouteDAO interDAO = new InterdomainRouteDAO(this.dbname);
        List<InterdomainRoute> routes = interDAO.list();
        int idWidth = 2;
        int srcWidth = 6;
        int destWidth = 11;
        int egrWidth = 6;
        int strictWidth = 6;
        int multiWidth = 5;
        int id = 1;
        ArrayList<HashMap<String,String>> fields =
            new ArrayList<HashMap<String,String>>();
        String idParam = params.get("id");
        int start = 0;
        int end = routes.size();

        /* Get ID if given */
        if(idParam != null){
            try{
                id = Integer.parseInt(idParam);
            }catch(Exception e){
                System.err.println("The provided ID is not an integer.");
                System.exit(0);
            }

            if(id > routes.size() || id < 1){
                System.err.println("No route with id " + id + " exists.");
                System.exit(0);
            }

            start = id - 1;
            end = id;
        }

        /* Build header */
        if(!showDetail){
            HashMap<String, String> header = new HashMap<String, String>();
            header.put("id", "ID");
            header.put("src", "Source");
            header.put("dest", "Destination");
            header.put("egr", "Egress");
            header.put("strict", "Strict");
            header.put("multi", "Multi");
            fields.add(header);
        }

        /* Add routes */
        for(int i = start; i < end; i++){
            InterdomainRoute route = routes.get(i);
            String srcURN = "*";
            String destURN = "*";
            String egrURN = "*";
            RouteElem egress = route.getRouteElem();
            HashMap<String, String> map = new HashMap<String, String>();

            /* find Id */
            String idStr = id + "";
            if(idStr.length() > idWidth){
                idWidth = idStr.length();
            }
            id++;

            /* find source */
            Link srcLink = route.getSrcLink();
            Port srcPort = route.getSrcPort();
            Node srcNode = route.getSrcNode();
            if(route.isDefaultRoute()){
                srcURN =  "default";
            }else if(srcLink != null){
                srcURN = srcLink.getFQTI();
            }else if(srcPort != null){
                srcURN = srcPort.getFQTI();
            }else if(srcNode != null){
                srcURN = srcNode.getFQTI();
            }
            if(srcURN.length() > srcWidth){
                srcWidth = srcURN.length();
            }

            /* find destination */
            Link destLink = route.getDestLink();
            Port destPort = route.getDestPort();
            Node destNode = route.getDestNode();
            Domain destDomain = route.getDestDomain();
            if(route.isDefaultRoute()){
                destURN =  "default";
            }else if(destLink != null){
                destURN = destLink.getFQTI();
            }else if(destPort != null){
                destURN = destPort.getFQTI();
            }else if(destNode != null){
                destURN = destNode.getFQTI();
            }else if(destDomain != null){
                destURN = destDomain.getFQTI();
            }
            if(destURN.length() > destWidth){
                destWidth = destURN.length();
            }

            /* find egress */
            Link egrLink = egress.getLink();
            Port egrPort = egress.getPort();
            Node egrNode = egress.getNode();
            Domain egrDomain = egress.getDomain();
            if(egrLink != null){
                egrURN = egrLink.getFQTI();
            }else if(egrPort != null){
                egrURN = egrPort.getFQTI();
            }else if(egrNode != null){
                egrURN = egrNode.getFQTI();
            }else if(egrDomain != null){
                egrURN = egrDomain.getFQTI();
            }
            if(egrURN.length() > egrWidth){
                egrWidth = egrURN.length();
            }

            map.put("id", idStr);
            map.put("src", srcURN);
            map.put("dest", destURN);
            map.put("egr", egrURN);
            map.put("strict", (egress.isStrict()? "Y" : "N"));
            map.put("multi", (egress.getNextHop() != null ? "Y" : "N"));
            fields.add(map);
        }

        if(showDetail){
            /* Print detail view */
            this.printDetail(fields, routes);
        }else{
            /* Print table */
            System.out.println("IDC static routing table");
            for(HashMap<String, String> entry: fields){

                String entryId = entry.get("id");
                System.out.print(entryId);
                for(int i = 0; i < ((idWidth - entryId.length()) + 4); i++){
                    System.out.print(" ");
                }

                String srcURN = entry.get("src");
                System.out.print(srcURN);
                for(int i = 0; i < ((srcWidth - srcURN.length()) + 4); i++){
                    System.out.print(" ");
                }

                String destURN = entry.get("dest");
                System.out.print(destURN);
                for(int i = 0; i < ((destWidth - destURN.length()) + 4); i++){
                    System.out.print(" ");
                }

                String strict = entry.get("strict");
                System.out.print(strict);
                for(int i = 0; i < ((strictWidth - strict.length()) + 4); i++){
                    System.out.print(" ");
                }
                String multi = entry.get("multi");
                System.out.print(multi);
                for(int i = 0; i < ((multiWidth - multi.length()) + 4); i++){
                    System.out.print(" ");
                }

                String egrURN = entry.get("egr");
                System.out.print(egrURN);
                for(int i = 0; i < ((egrWidth - egrURN.length()) + 4); i++){
                    System.out.print(" ");
                }
                System.out.println();
            }
        }

        bss.getTransaction().commit();
    }

    /**
     * Prints detailed view of a list of routes
     *
     * @param fields an ArrayList of HashMaps containing the fields to be printed
     * @param routes the routes to print
     */
    private void printDetail(ArrayList<HashMap<String,String>> fields,
                        List<InterdomainRoute> routes){
        for(HashMap<String,String> map:fields){
            int index = Integer.parseInt(map.get("id")) - 1;
            InterdomainRoute route = routes.get(index);
            RouteElem routeElem = route.getRouteElem();

            System.out.println("ID: " + map.get("id"));
            System.out.println("Source: " + map.get("src"));
            System.out.println("Destination: " + map.get("dest"));
            if(map.get("strict").equals("Y")){
                System.out.println("Strict Interdomain Path (SIDP)");
            }else{
                System.out.println("Loose Interdomain Path (LIDP)");
            }
            System.out.println("Route: ");
            while(routeElem != null){
                Domain domain = routeElem.getDomain();
                Node node = routeElem.getNode();
                Port port = routeElem.getPort();
                Link link = routeElem.getLink();

                if(link != null){
                    System.out.println("    " + link.getFQTI());
                }else if(port != null){
                    System.out.println("    " + port.getFQTI());
                }else if(node != null){
                    System.out.println("    " + node.getFQTI());
                }else if(domain != null){
                    System.out.println("    " + domain.getFQTI());
                }
                routeElem = routeElem.getNextHop();
            }
            System.out.println();
        }
    }

    /**
     * Prints help message indicating acceptable parameters to this command
     */
    public void printHelp(){
        System.out.println("Usage: idc-route (function) (parameters)");
        System.out.println();
        System.out.println("Functions:");
        System.out.println("\tadd\tadds a route with the given parameters " +
                           "to the database");
        System.out.println("\tdel\tdeletes a route with the given parameters" +
                           " from the database");
         System.out.println("\t*If no function specified, then will display " +
                            "current routing tables.");
        System.out.println();
        System.out.println("'add' Parameters:");
        System.out.println("\t-egress\tthe URN of the egress link to use for" +
                           " all requests matching this route.");
        System.out.println("\t-src\ta node, port, or link URN indicating " +
                           "that all requests that have an ingress matching" +
                           " this element should use this route. If not " +
                           "specified any ingress matches. The domain " +
                           "portion of URNs passed to this parameter MUST " +
                           "always be the local domain.");
        System.out.println("\t-dest\ta domain, node, port, or link URN " +
                           "indicating that all requests that have an " +
                           "destination matching this element should use " +
                           "this route. If not specified any destination "+
                           "matches. The domain portion of URNs passed to " +
                           "this parameter may be in any domain");
        System.out.println("\t-default\tindicates this is the default " +
                           "route. If no other routes match a request " +
                           "this route will always be used.");
        System.out.println("\t-multi\tindicates a multi hop path. This " +
                           "option will cause a prompt to appear requesting " +
                           "the hops past the egress.");
        System.out.println("\t-loose\tindicates that this should be a loose " +
                           "path rather than a strict. default is strict");
        System.out.println();
        System.out.println("'del' Parameters:");
        System.out.println("\t-id\tthe ID of the route to delete. If no ID " +
                            "specified then a list of routes will be shown " +
                            "and you will be prompted for the ID.");
        System.out.println();
        System.out.println("View Parameters:");
        System.out.println("\t-id\tthe ID of the route to show. If no ID " +
                            "specified then all routes shown");
        System.out.println("\t-detail\tprints a detailed view of the selected" +
                           " routes");


    }

    /**
     * Reads in hops when user wants to specify multiple hops
     *
     * @param hops the current hops in the path (probably just the egress)
     * @param isLoose boolean indicateing if this is a loose hop
     * @return a new list of hops as input by the user
     */
    private ArrayList<String> processMulti(ArrayList<String> hops,
                                           boolean isLoose){
        Scanner in = new Scanner(System.in);

        while(true){
            if(hops.size() != 0){
                System.out.print("Enter hop URN or type 'end' to complete: ");
            }else{
                System.out.print("Enter local egress: ");
            }

            String input = in.next();
            if(input.toLowerCase().equals("end")){
                break;
            }

            int urnType = TopologyUtil.getURNType(input);
            if((!isLoose) &&  urnType != TopologyUtil.LINK_URN){
                System.err.println("This is a strict path so only " +
                    "fully-qualified link-ids are accepted. Use the -loose " +
                    "option if you would like to specifiy domain, node, or " +
                    "port ids.");
                System.exit(0);
            }

            hops.add(input.trim());
        }

        if(hops.size() == 0){
            System.err.println("No egress link specified for the path");
            System.exit(0);
         }

        return hops;
    }

    /**
     * Prepares an entry for the database. If a given domain, node, port, or
     * link does not exists in the database it will create the element with
     * a default set of values.
     *
     * @param hop URN or hop to be examined
     * @param domain Domain to save
     * @param node Node to save
     * @param port Port to save
     * @param link Link to save
     * @param bss hibernate session to use
     * @return URNElements containing final domain, node, port, and link
     */
    private URNElements prepareEntry(String hop, Domain domain, Node node,
                                     Port port, Link link, Session bss){
        int urnType = TopologyUtil.getURNType(hop);
        String[] urnList = hop.split(":");
        String domainURN = "";
        String nodeURN = "";
        String portURN = "";
        String linkURN = "";
        URNElements result = new URNElements();

        if(urnType >= TopologyUtil.DOMAIN_URN){
            domainURN = "urn:ogf:network:" + urnList[3];
            try{
                domain = TopologyUtil.getDomain(domainURN, this.dbname);
            }catch(BSSException e){}
            if(domain == null){
                domain = new Domain(true);
                domain.setTopologyIdent(urnList[3].replaceAll("domain=",""));
            }
            bss.save(domain);
        }


        if(urnType >= TopologyUtil.NODE_URN){
            nodeURN += domainURN + ":" + urnList[4];
            try{
                node = TopologyUtil.getNode(nodeURN, this.dbname);
            }catch(BSSException e){}
            if(node == null){
                node = new Node(domain, true);
                node.setTopologyIdent(urnList[4].replaceAll("node=", ""));
                domain = null;
            }
            bss.save(node);
        }

        if(urnType >= TopologyUtil.PORT_URN){
            portURN += nodeURN + ":" + urnList[5];
            try{
                port = TopologyUtil.getPort(portURN, this.dbname);
            }catch(BSSException e){}
            if(port == null){
                port = new Port(node, true);
                port.setTopologyIdent(urnList[5].replaceAll("port=", ""));
                node = null;
            }
            bss.save(port);
        }

        if(urnType >= TopologyUtil.LINK_URN){
            linkURN += portURN + ":" + urnList[6];
            try{
                link = TopologyUtil.getLink(linkURN, this.dbname);
            }catch(BSSException e){}
            if(link == null){
                link = new Link(port, true);
                link.setTopologyIdent(urnList[6].replaceAll("link=", ""));

                port = null;
            }
            bss.save(link);
        }

        /* Set and return result */
        result.domain = domain;
        result.node = node;
        result.port = port;
        result.link = link;

        return result;
    }

    /**
     * Class for storing a domain, node, port, and/or link object
     */
    private class URNElements{
        public Domain domain;
        public Node node;
        public Port port;
        public Link link;
    }


    public static void main(String[] args){
        IDCRouteUtil util = new IDCRouteUtil(args);
    }
}