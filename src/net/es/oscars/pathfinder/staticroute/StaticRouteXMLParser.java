package net.es.oscars.pathfinder.staticroute;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.pathfinder.PathfinderException;

import org.apache.log4j.Logger;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class StaticRouteXMLParser {
    private Logger log;
    private String staticRoutesFile;
    
    public StaticRouteXMLParser(String staticRoutesFile){
        this.log = Logger.getLogger(this.getClass());
        this.staticRoutesFile = staticRoutesFile;
        if(this.staticRoutesFile == null){
            this.staticRoutesFile= System.getenv("CATALINA_HOME");
            this.staticRoutesFile += (this.staticRoutesFile.endsWith("/") ? "" : "/");
            this.staticRoutesFile += "shared/classes/terce.conf/static-routes.xml";
        }
    }
    
    public List<PathElem> findPath(String src, String dest) throws PathfinderException{
        ArrayList<PathElem> path = new ArrayList<PathElem>();
        
        try {
            File fin = new File(this.staticRoutesFile);
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = null;
            Document rceXMLDoc = null;

            docBuilder = docFactory.newDocumentBuilder();
            rceXMLDoc = docBuilder.parse(fin);

            NodeList children = rceXMLDoc.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                String nodeName = child.getNodeName();

                if ((nodeName != null) && nodeName.equalsIgnoreCase("staticPathDatabase")){
                    this.parseStaticPathDatabase(child, src, dest, path);
                }
            }
        } catch (ParserConfigurationException e) {
            throw new PathfinderException("Parser Config Exception: " +
                e.getMessage());
        } catch (SAXException e) {
            throw new PathfinderException("SAX Exception: " + e.getMessage());
        } catch (IOException e) {
            throw new PathfinderException("IO Exception: " + e.getMessage());
        }

        /* Verify a path was found */
        if (path.isEmpty()) {
            throw new PathfinderException(
                "Unable to calculate a path between given source and destination");
        }

        return path;
    }

    /**
     * Utility function for parsing the &lt;staticPathDatabase&gt; element information
     * in the static route file and converting it to a response to a findPath request
     *
     * @param elem the staticPathDatabase element to parse
     * @param src the source endpoint of the request
     * @param returnVtags boolean value that is set to true if available vlan tags should be included in response
     * @param dest the destination endpoint of the request
     * @return the findPath  response including the path in the format needed by Axis2
     */
    private void parseStaticPathDatabase(Node elem,
        String src, String dest, List<PathElem> path) {
        NodeList children = elem.getChildNodes();

        /* Parse elements */
        for (int i = 0; (i < children.getLength()) && path.isEmpty(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();

            if (nodeName == null) {
                continue;
            } else if (nodeName.equalsIgnoreCase("staticPathEntry")) {
                this.lookupPath(child, src, dest, path);
            }
        }
    }

    /**
     * Parses the static route file and looks up a path with the same source and destination
     * as the request.
     *
     * @param elem the staticPathEntry element to parse
     * @param requestSrc the source endpoint of the request
     * @param requestDest the destination endpoint of the request
     * @return the findPath  response including the path in the format needed by Axis2
     */
    private void lookupPath(Node elem, String requestSrc,
        String requestDest, List<PathElem> path) {
        NodeList children = elem.getChildNodes();
        String entrySrc = null;
        String entryDest = null;
        List<PathElem> entryRoute = null;
        requestSrc = requestSrc.replaceAll("\\s", "");
        requestDest = requestDest.replaceAll("\\s", "");
        
        /* Parse elements */
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();

            if (nodeName == null) {
                continue;
            } else if (nodeName.equalsIgnoreCase("srcEndpoint")) {
                entrySrc = child.getTextContent();
            } else if (nodeName.equalsIgnoreCase("destEndpoint")) {
                entryDest = child.getTextContent();
            } else if (nodeName.equalsIgnoreCase("path")) {
                entryRoute = this.parsePath(child);
            } else if (nodeName.equalsIgnoreCase("availableVtags")) {
                continue;
            }
        }

        /* check source and destination */
        entrySrc = entrySrc.replaceAll("\\s", "");
        entryDest = entryDest.replaceAll("\\s", "");
        
        if ((requestSrc.equals(entrySrc) && requestDest.equals(entryDest)) || 
                (requestSrc.equals(entryDest) && requestDest.equals(entrySrc))) {
           this.log.debug("Found a path that matches!");
           path.addAll(entryRoute);
        }
    }

    /**
     * Parses a path element in the static route file and returns the path
     * as an object used in the Axis2 response
     *
     * @param elem the path element to parse
     * @return the path information in the format needed by Axis2
     */
    private List<PathElem> parsePath(Node elem) {
        ArrayList<PathElem> route = new ArrayList<PathElem>();
        NodeList children = elem.getChildNodes();

        /* Parse elements */
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();

            if (nodeName == null) {
                continue;
            } else if (nodeName.equalsIgnoreCase("hop")) {
                PathElem hop = this.parseHop(child);
                route.add(hop);
            }
        }

        return route;
    }

    /**
     * Parses a &lt;node&gt; object and maps it to a CtrlPlaneNodeContent object
     * for use in an Axis2 response
     *
     * @param elem the node element to parse
     * @return CtrlPlaneNodeContent object for use in an Axis2 response
     */
    private PathElem parseHop(Node elem) {
        PathElem hop = new PathElem();
        NodeList children = elem.getChildNodes();

        /* Parse elements */
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            String nodeName = child.getNodeName();

            if (nodeName == null) {
                continue;
            } else if (nodeName.equalsIgnoreCase("domainIdRef")) {
                hop.setUrn(child.getTextContent());
            } else if (nodeName.equalsIgnoreCase("nodeIdRef")) {
                hop.setUrn(child.getTextContent());
            } else if (nodeName.equalsIgnoreCase("portIdRef")) {
                hop.setUrn(child.getTextContent());
            } else if (nodeName.equalsIgnoreCase("linkIdRef")) {
                hop.setUrn(child.getTextContent());
            }
        }

        return hop;
    }
}
