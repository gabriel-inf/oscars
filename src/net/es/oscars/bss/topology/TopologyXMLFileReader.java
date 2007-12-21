package net.es.oscars.bss.topology;

import net.es.oscars.*;

import org.apache.log4j.*;

import org.jdom.*;
import org.jdom.input.SAXBuilder;

import java.io.*;
import java.util.*;


/**
 * This class contains methods that will read an XML file into
 * a JDOM Document object, validate it against the control plane
 * schema, and finally import it into the local topology DB.
 * @author Evangelos Chaniotakis (haniotak@es.net)
 */
public class TopologyXMLFileReader {
	
    private Logger log;
    private Properties props;
    private String nsUri;
    private String xsdFilename = "";
    private String dbname;


    /**
     * Constructor initializes logging, local parameters
     */
    public TopologyXMLFileReader(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;

        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("topo", true);
        
        this.setNsUri(this.props.getProperty("nsuri").trim());
        this.setXsdFilename(this.props.getProperty("xsdFilename").trim());
    }

    
    /**
     * This method will read an XML file into a JDOM Document object,
     * validating it against the schema .csd file, and call the
     * appropriate class to import the Document into the local DB.
     * @param xmlFilename the filename to read
     */
    public void importFile(String xmlFilename) {
        Document doc = null;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        try {
            doc = this.loadFile(xmlFilename);
        } catch (JDOMException e) {
            e.printStackTrace(pw);
            this.log.error(sw.toString());
            return;
        } catch (IOException e) {
            e.printStackTrace(pw);
            this.log.error(sw.toString());
            return;
        }

        TopologyXMLImporter importer = new TopologyXMLImporter(this.dbname);

        importer.importXML(doc, "merge");
    }

    /**
     * This method will read an XML file into a JDOM Document object,
     * validating it against the schema .xsd file.
     * @param fName the filename to read
     * @return the JDOM Document object
     */
    protected Document loadFile(String fName) throws JDOMException, IOException {
        Document doc = null;
        
        File xsdFile = new File( this.getXsdFilename());
        if (!xsdFile.exists()) {
        	throw new IOException("XSD file not found!");
        }
        
        SAXBuilder sb = new SAXBuilder("org.apache.xerces.parsers.SAXParser",
                true);

        sb.setFeature("http://apache.org/xml/features/validation/schema", true);
        sb.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
            this.getNsUri() + " " + this.getXsdFilename());

        try {
            doc = sb.build(new File(fName));
        } catch (JDOMException e) {
            throw e;
        } catch (IOException e) {
            throw e;
        }

        return doc;
    }
    
    
    // Getter / setter functions
    
    /**
     * nsUri getter
     * @return the value of nsUri
     */
    public String getNsUri() {
        return this.nsUri;
    }

    /**
     * nsUri setter
     * @param uri The value to be set
     */
    public void setNsUri(String uri) {
        this.nsUri = uri;
    }

    
    /**
     * xsdFilename getter
     * @return the value of xsdFilename
     */
    public String getXsdFilename() {
    	return this.xsdFilename;
    }

    /**
     * xsdFilename setter
     * @param filename The value to set; the path to the XSD file
     */
    public void setXsdFilename(String filename) {
    	this.xsdFilename = filename;
    }
    
}
