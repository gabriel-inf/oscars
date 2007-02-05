package net.es.oscars.pss;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.String;
//import java.text.Format;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.net.Socket;
import java.net.URL;
import java.net.MalformedURLException;

import javax.xml.*;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

/*
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 */

// Lets use jdom (even tho it doesn't have XPath support)
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;

// for the ssh stuff
import com.jcraft.jsch.*;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.BSSException;


/**
 * JnxLSP performs setup/teardown of paths on the router.
 */
public class JnxLSP {

    private Properties props;

    public JnxLSP() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("pss", true);
    }

    /**
     * Sets up an LSP circuit.
     *
     * @param hm a hash map with configuration values
     * @return boolean indicating success
     * @throws BSSException
     */
    public boolean setupLSP(Map<String,String> hm) throws BSSException {

        String user = null;
        String host = null;

        if (hm.containsKey("user")) user = hm.get("user").toString();
        else throw new BSSException("no user");

        if (hm.containsKey("host")) host = hm.get("host");
        else throw new BSSException("no host specified");

        LSP_Connection conn = new LSP_Connection();
        conn.createSSHConnection(user, host, hm);

        createLSP(hm, conn.out);

        readResponse(conn.in, System.out);
        conn.shutDown();

        return true;
    }

    /**
     * Tears down an LSP circuit.
     *
     * @param hm A hash map with configuration values
     * @return boolean indicating success
     * @throws BSSException
     */
    public boolean teardownLSP(Map<String,String> hm) 
            throws BSSException {

        String user = null;
        String host = null;

        if (hm.containsKey("user")) user = hm.get("user");
        else throw new BSSException("No use specified");

        if (hm.containsKey("host")) host = hm.get("host");
        else throw new BSSException("No host specified");

        LSP_Connection conn = new LSP_Connection();
        conn.createSSHConnection(user, host, hm);

        destroyLSP(hm, conn.out);

        readResponse(conn.in, System.out);
        conn.shutDown();

        return true;
    }

    /** 
     * Gets the LSP status on a Juniper router.
     *
     * @param hm a hash map with configuration values
     * @return an int, with value -1 if NA (e.g not found), 0 if down, 1 if up
     * @throws BSSException
     */
    public int statusLSP(Map<String,String> hm) throws BSSException {

        String user = null;
        String host = null;

        int status = -1;

        if (hm.containsKey("user")) user = hm.get("user");
        else throw new BSSException("No use specified");

        if (hm.containsKey("host")) host = hm.get("host");
        else throw new BSSException("No host specified");

        LSP_Connection conn = new LSP_Connection();
        conn.createSSHConnection(user, host, hm);

        status = getStatus(hm, conn);

        conn.shutDown();

        return status;
    }

    /**
     * Shows example setting up of a DOM to request information from a router.
     * The ouputted document looks like:
     *
     *    <?xml version="1.0" encoding="us-ascii"?>
     *    <junoscript version="1.0" release="7.3">
     *       <rpc>
     *           <get-chassis-inventory>
     *               <detail />
     *           </get-chassis-inventory>
     *       </rpc>
     *     </junoscript>
     *
     * @param out
     * @param operation
     */
    public void doConfig(OutputStream out, String operation) {

        Element rpcElement = new Element("rpc");
        Document myDocument = new Document(rpcElement);
        Element commit = new Element(operation);
        rpcElement.addContent(commit);
        try {
            XMLOutputter fmt = new XMLOutputter();
            Format f = fmt.getFormat();
            f.setOmitDeclaration(true);
            fmt.setFormat(f);
            fmt.output(myDocument, out);
        } catch (Exception e) {
            System.out.println("exception on output: " + e.getMessage());
        }
    }

    /**
     * @param out
     */
    public void commitConfig(OutputStream out) {
        doConfig(out, "commit-configuration");
    }

    /**
     * @param out
     */
    public void lockConfig(OutputStream out) {
        doConfig(out, "lock-configuration");
    }

    /**
     * @param out
     */
    public void unlockConfig(OutputStream out) {
        doConfig(out, "unlock-configuration");
    }

    /**
     * @param out
     * @throws IOException
     */
    public void writeHeader(OutputStream out) throws IOException {
        out.write("<?xml version=\"1.0\" encoding=\"us-ascii\"?>".getBytes());
        out.write("<junoscript version=\"1.0\" release=\"7.3\">".getBytes());
    }

    /**
     * @param out
     * @throws IOException
     */
    public void startLoad(OutputStream out) throws IOException {
        out.write("<rpc>".getBytes());
        out.write("<load-configuration>".getBytes());
    }

    /**
     * @param out
     * @throws IOException
     */
    public void endLoad(OutputStream out) throws IOException {
        out.write("</load-configuration>".getBytes());
        out.write("</rpc>".getBytes());
    }

    /**
     * @param out
     * @throws IOException
     */
    public void writeFooter(OutputStream out) throws IOException {
        out.write("</junoscript>".getBytes());
    }

    /**
     * @param out
     */
    public void makeDoc(OutputStream out) {

        Element junosElement = new Element("junoscript");
        Document myDocument = new Document(junosElement);
        junosElement.setAttribute(new Attribute("version", "1.0"));
        junosElement.setAttribute(new Attribute("release", "7.3"));

        // add something here...

        Element rpc = new Element("rpc");
        junosElement.addContent(rpc);
    }

    /**
     * @param hm
     * @return boolean
     */
    public static boolean getChassis(Map<String,String> hm) {

        String user = null;
        String host = null;

        if (hm.containsKey("user")) user = hm.get("user");
        else return false;
        if (hm.containsKey("host"))
            host = hm.get("host");
        else return false;

        LSP_Connection conn = new LSP_Connection();
        conn.createSSHConnection(user, host, hm);

        // setup root
        Element junosElement = new Element("junoscript");
        Document myDocument = new Document(junosElement);
        junosElement.setAttribute(new Attribute("version", "1.0"));
        junosElement.setAttribute(new Attribute("release", "7.3"));
        // setup the elements
        Element rpc = new Element("rpc");
        Element inv = new Element("get-chassis-inventory");
        Element detail = new Element("detail");
        // make the tree
        inv.addContent(detail);
        rpc.addContent(inv);
        junosElement.addContent(rpc);
        // how to tweak the doc type
        // DocType baz = new DocType("foo");
        // myDocument.setDocType(baz);

        try {
            XMLOutputter fmt = new XMLOutputter();
            fmt.output(myDocument, conn.out);
        } catch (Exception e) {
            System.out.println("exception on output: " + e.getMessage());
        }

        readResponse(conn.in, System.out);
        conn.shutDown();
        //return myDocument;
        return true;
    }

    /**
     * Performs a dummy test of configuration parameters used upon
     * setup and teardown.
     *
     * @return hash map
     */
    public static Map<String,String> populateHash() {

        Map<String,String> hm = new HashMap<String,String>();
        hm.put("user", "jason");
        hm.put("passphrase","passphrase");
        hm.put("keyfile","id_dsa");
        hm.put("host", "dev-m20-rt1.es.net");
        hm.put("user", "jason");
        hm.put("user_var_name_user_var","zippy");
        hm.put("user_var_lsp_from_user_var","lsp-from");
        hm.put("user_var_lsp_to_user_var","lsp-to");
        hm.put("user_var_bandwidth_user_var","10");
        hm.put("user_var_lsp_class-of-service_user_var","4");
        hm.put("user_var_lsp_setup-priority_user_var","4");
        hm.put("user_var_lsp_reservation-priority_user_var","4");
        hm.put("user_var_lsp_description_user_var","user_var_lsp_description_user_var");
        hm.put("user_var_policer_burst-size-limit_user_var","1000000");
        hm.put("user_var_external_interface_filter_user_var","external-interface-inbound-inet.0-filter");
        hm.put("user_var_source-address_user_var","user_var_source-address_user_var");
        hm.put("user_var_destination-address_user_var","somedstaddr");
        hm.put("user_var_dscp_user_var","user_var_dscp_user_var");
        hm.put("user_var_protocol_user_var","user_var_protocol_user_var");
        hm.put("user_var_source-port_user_var","5555");
        hm.put("user_var_destination-port_user_var","6666");
        hm.put("user_var_firewall_filter_marker_user_var","oscars-filters-start");
        return hm;
    }

    /**
     * Class to encapsulate the connection stuff.
     */
    public static class LSP_Connection {
        private Session session;
        private Channel channel;
        private InputStream in;
        private OutputStream out;
        private static SSLSocketFactory sslsf;

        public LSP_Connection() {
            session = null;
            channel = null;
            in = null;
            out = null;
            sslsf = null;

            disableTrustManager();
        }

        /** 
         * This is used with SSL sockets that need to connect to a server
         * whose CA cert we don't have in our keyring. The effect of this
         * function is that we trust ALL SSL connections.
         */
        public static void disableTrustManager() {

            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] { 
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[]
                        getAcceptedIssuers() {

                        return null;
                    }

                    public void
                        checkClientTrusted(
                                    java.security.cert.X509Certificate[] certs,
                                    String authType) {
                            System.out.print("in checkClientTrusted");
                    }

                    public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] certs,
                                    String authType) {
                        System.out.print("in checkSeverTrusted");
                }
            } 
        };

        // Install the all-trusting trust manager (no pun intended)
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            sslsf = sc.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(
                                                         sc.getSocketFactory());
        } catch (Exception e) {
             System.out.println("Exception in install trustManager");
        }
    }

    /**
     * Tries to shut down nicely.
     */
    public void shutDown() {
        try {
            channel.disconnect();
            session.disconnect();
            in.close();
            out.close();
        } catch (Exception ex ) {
            System.out.println("shutDown: " + ex.getMessage());
        }
    }

    /**
     * Creates an ssh connection, assuming that the key is not passphrase
     * protected, and that it's named "id_dsa" in the current directory.
     * 
     *  XXX: Should be put into the config hashtable 
     *
     *  @param host
     *  @param user
     *  @param hm
     */
    public void createSSHConnection(String host, String user,
                                    Map<String,String> hm) {
        // some defaults
        host = "localhost";
        user = "jason";
        String keyfile = "id_dsa";
        String passphrase = "passphrase";
        boolean prompt = false;

        JSch jsch = new JSch();
        try {
            if (hm.containsKey("host")) { host = hm.get("host"); }
            if (hm.containsKey("user")) { user = hm.get("user"); }

            if (hm.containsKey("keyfile")) { keyfile = hm.get("keyfile"); }
            if ( hm.containsKey("passphrase")) {
                passphrase = hm.get("passphrase");
            }

            jsch.addIdentity(keyfile, passphrase);
            Session session = jsch.getSession(user, host, 22);
            // session.setPassword("password");
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = session.openChannel("exec");
            in = channel.getInputStream();
            out = channel.getOutputStream();
            ((ChannelExec)channel).setCommand("junoscript");
            channel.connect();

        } catch (Exception e) {
            System.out.println("\ncreateSSHConnection" + e + "\n");
        }
        System.out.println("createSSHconnection done.");
    }

    /**
     * Creates an SSL connection; currently it is
     * NOT WORKING so we are using ssh (above) instead.
     * FIXME XXX 
     */
    public void createSSLConnection() {
        try {
            int port = 3220;
            // dev-m20-rt1.es.net has address 198.128.8.11
            // String hostname = "dev-m20-rt1.es.net";
            String hostname = "foobar.lbl.gov";
            Socket socket = sslsf.createSocket(hostname, port);

            // Create streams to securely send and receive data to the server
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.out.print("Exception in createConnection" + e);
        }
    }
};  // end of internal class

    /**
     * Replaces the current text with whatever it is keyed to in the
     * hashtable.
     *
     * @param lst
     * @param hm
     */
    public void replaceXML(List lst, Map hm) {
        Iterator i = lst.iterator();
        Pattern usrPattern = Pattern.compile(".*user_var_.*_user_var.*");
        while (i.hasNext()) {
            Element tmp = (Element) i.next();
            String txt = tmp.getText();
            Matcher m = usrPattern.matcher(txt);
            if (m.matches()) {
                if (hm.containsKey(txt)) {
                    String subs = (String) hm.get(txt);
                    tmp.setText(subs);
                    System.out.println("text: " + txt + " subs: " + subs);
                }
                else 
                {
                    tmp.setText("");
                    System.out.println("zapped: " + txt);
                }
            }
        }
    }

    /**
     * Finds and substitutes the correct values into the document.
     * If only XPath worked with JDOM life would be sooooo much easier....
     *
     * @param doc
     * @param hm
     */
    public void parseSetup(Document doc, Map hm) {

        String zappee;

        Element root = doc.getRootElement();

        List prot = root.getChildren("protocols");
        List mpls = ((Element) (prot.get(0))).getChildren("mpls");
        List lsp = ((Element) (mpls.get(0))).getChildren("label-switched-path");
        List vars = ((Element) (lsp.get(0))).getChildren();
        replaceXML(vars, hm);

        List firewall = root.getChildren("firewall");
        List policer = ((Element)firewall.get(0)).getChildren("policer");
        vars = ((Element)policer.get(0)).getChildren();
        //replaceXML(policer, hm);
        replaceXML(vars, hm);

        List ifexceed = ((Element)policer.get(0)).getChildren("if-exceeding");
        vars = ((Element)ifexceed.get(0)).getChildren();
        replaceXML(vars, hm);

        List family = ((Element)firewall.get(0)).getChildren("family");
        List inet = ((Element)family.get(0)).getChildren("inet");
        List filter = ((Element)inet.get(0)).getChildren("filter");
        vars = ((Element)filter.get(0)).getChildren();
        replaceXML(vars,hm);

        List term = ((Element)filter.get(0)).getChildren("term");
        vars = ((Element)term.get(0)).getChildren();
        replaceXML(term,hm);
        replaceXML(vars,hm);

        List from = ((Element)term.get(0)).getChildren("from");
        vars = ((Element)from.get(0)).getChildren();
        replaceXML(vars,hm);

        List then =
            ((Element)((Element)term.get(0)).getChildren("then").get(0)).getChildren();
        replaceXML(then,hm);

        // grrr special case attr
        String special = ((Element)term.get(1)).getAttributeValue("name");
        if (hm.containsKey(special)) {
            ((Element)term.get(1)).setAttribute("name", (String)hm.get(special));
        }
        vars = ((Element)term.get(1)).getChildren();
        replaceXML(vars,hm);

        List ri = root.getChildren("routing-instances");
        List inst = ((Element)ri.get(0)).getChildren("instance");
        vars = ((Element)inst.get(0)).getChildren();
        replaceXML(vars, hm);
        System.out.println("baz");
        List ro = ((Element)inst.get(0)).getChildren("routing-options");
        List stat = ((Element)ro.get(0)).getChildren("static");
        List route = ((Element)stat.get(0)).getChildren("route");
        vars = ((Element)route.get(0)).getChildren();
        replaceXML(vars,hm);

        List nhop = ((Element)route.get(0)).getChildren("lsp-next-hop");
        vars = ((Element)nhop.get(0)).getChildren();
        replaceXML(vars,hm);

        System.out.println("sp =" +
                           ((Element)term.get(1)).getAttributeValue("name"));
        System.out.println("finished parseOutElements");
    }

    /**
     * Finds and substitutes the correct values into the xml doc.
     *
     * @param doc
     * @param hm
     */
    public void parseTeardown(Document doc, Map hm) {

        Element root = doc.getRootElement();
        List prot = root.getChildren("protocols");
        List mpls = ((Element) (prot.get(0))).getChildren("mpls");
        List lsp = ((Element) (mpls.get(0))).getChildren("label-switched-path");

        List vars = ((Element)lsp.get(0)).getChildren();
        replaceXML(vars,hm);

        List firewall = root.getChildren("firewall");
        List policer = ((Element)firewall.get(0)).getChildren("policer");
        vars = ((Element)policer.get(0)).getChildren();
        replaceXML(vars, hm);

        List family = ((Element)firewall.get(0)).getChildren("family");
        List inet = ((Element)family.get(0)).getChildren("inet");
        List filter = ((Element)inet.get(0)).getChildren("filter");
        vars = ((Element)filter.get(0)).getChildren();
        replaceXML(vars, hm);

        List term = ((Element)filter.get(0)).getChildren("term");
        vars = ((Element)term.get(0)).getChildren();
        replaceXML(vars, hm);

        List ri = root.getChildren("routing-instances");
        List inst = ((Element)ri.get(0)).getChildren("instance");
        vars = ((Element)inst.get(0)).getChildren();
        replaceXML(vars, hm);
    }

    /**
     * Creates an LSP. Sends the LSP-XML command to the server
     * and sees what happens.
     *
     * @param hm
     * @param out
     * @return boolean
     * @throws BSSException
     */
    private boolean createLSP(Map hm, OutputStream out) 
            throws BSSException {
        String lsp_setup =  System.getenv("OSCARS_HOME") +
                this.props.getProperty("setupFile");

        Document doc = null;

        try {
            writeHeader(out);
            lockConfig(out);
            startLoad(out);

            // Request document building without validation
            SAXBuilder builder = new SAXBuilder(false);
            doc = builder.build(new File(lsp_setup));
            parseSetup(doc, hm);

            XMLOutputter fmt = new XMLOutputter();
            Format f = fmt.getFormat();
            f.setOmitDeclaration(true);
            fmt.setFormat(f);
            fmt.output(doc, out);

            endLoad(out);
            commitConfig(out);
            unlockConfig(out);
            writeFooter(out);
        } catch (IOException e) {
            throw new BSSException("Error createLSP (IO): " + e.getMessage());
        } catch ( org.jdom.JDOMException e)  {
            throw new BSSException("Error in createLSP (jdom): "+ e.getMessage());
        }
        return true;
    }

    /**
     * Destroys an LSP. Sends the LSP-XML command to the server
     * and sees what happens.
     * Shouldn't catch exceptions at all, just pass them up 
     * much less as four blocks.....geez
     *
     * @param hm
     * @param out
     * @return boolean
     * @throws BSSException
     */
    public boolean destroyLSP(Map hm, OutputStream out) 
            throws BSSException {

        String lsp_teardown = System.getenv("OSCARS_HOME") +
                this.props.getProperty("teardownFile");

        Document doc = null;

        try {
            // Request document building without validation
            SAXBuilder builder = new SAXBuilder(false);
            doc = builder.build(new File(lsp_teardown));
            parseTeardown(doc, hm);

            writeHeader(out);
            lockConfig(out);
            startLoad(out);

            XMLOutputter fmt = new XMLOutputter();
            Format f = fmt.getFormat();
            f.setOmitDeclaration(true);
            fmt.setFormat(f);
            fmt.output(doc, out);

            endLoad(out);
            commitConfig(out);
            unlockConfig(out);
            writeFooter(out);
        } catch (IOException e) {
            throw new BSSException("Error in destroyLSP (IO):" + e.getMessage());
        } catch (org.jdom.JDOMException e ) {
            throw new BSSException("Error in destroyLSP (JDOM):" + e.getMessage());
        }
        return true;
    }

    /**
     * @param hm
     * @param conn
     * @return int
     * @throws BSSException
     */
    public int getStatus(Map hm, LSP_Connection conn) 
            throws BSSException {

        ByteArrayOutputStream buff  = new ByteArrayOutputStream();

        try {
            writeHeader(conn.out);
            doConfig(conn.out, "get-mpls-lsp-information");
            writeFooter(conn.out);

            readResponse(conn.in, buff);

            // XXX: parse this
            String reply = buff.toString();

        } catch (IOException e) {
            throw new BSSException("Error in getStatus (IO):" + e.getMessage());
        }
        return -1;
    }

    /**
     * Reads the response from the socket created earlier into a DOM
     * (makes for easier parsing).
     *
     * @param in
     * @param out
     */
    public static void readResponse(InputStream in, OutputStream out) {

        Document d = null;
        try {
            SAXBuilder b = new SAXBuilder();
            d = b.build(in);
        } catch(Exception ex) {
            System.out.println("error readingResponse: " + ex.getMessage());
        }
        try {
            XMLOutputter fmt = new XMLOutputter();
            fmt.output(d, out);
            /* XXX: parse output here! */
        } catch (Exception ex) {
            System.out.println("error outputting: " + ex.getMessage());
        }
    }
}
