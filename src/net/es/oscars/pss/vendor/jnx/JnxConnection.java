package net.es.oscars.pss.vendor.jnx;

import java.io.*;
import java.util.*;

import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

import java.net.Socket;
import java.net.URL;
import java.net.MalformedURLException;
import javax.net.SocketFactory;
import javax.net.ssl.*;

import java.security.*;
import java.security.cert.X509Certificate;

import org.apache.log4j.*;

// for ssh
import com.jcraft.jsch.*;

import net.es.oscars.ConfigFinder;
import net.es.oscars.PropHandler;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pss.PSSException;

/**
 * Class to encapsulate ssh connections to routers.
 */
public class JnxConnection {
    public InputStream in;
    public OutputStream out;
    private Session session;
    private Channel channel;
    private SSLSocketFactory sslsf;
    private org.apache.log4j.Logger log;
    private Properties props;
    private boolean isActive;

    public JnxConnection() {
        this.session = null;
        this.channel = null;
        this.in = null;
        this.out = null;
        this.sslsf = null;
        this.isActive = false;
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("pss.jnx", true);
        this.log = org.apache.log4j.Logger.getLogger(this.getClass());
    }

    /**
     * Tries to shut down nicely.
     */
    public void shutDown() throws IOException {
        this.channel.disconnect();
        this.session.disconnect();
        this.in.close();
        this.out.close();
        this.isActive = false;
    }

    /**
     * Creates an ssh connection, given a hash map with name/value pairs.
     *
     *  @param hm
     *  @throws IOException
     *  @throws PSSException
     */
    public void createSSHConnection(Map<String,String> hm)
            throws IOException, PSSException {

        this.log.info("createSSHconnection.start");
        if (!hm.containsKey("login")) {
            throw new PSSException(
                "no user name given for setting up SSH connection to router");
        }
        if (!hm.containsKey("router")) {
            throw new PSSException(
                "no host name given for setting up SSH connection to router");
        }
        if (!hm.containsKey("keyfile")) {
            throw new PSSException(
                "no keyfile given for setting up SSH connection to router");
        }
        if (!hm.containsKey("passphrase")) {
            throw new PSSException(
                "no passphrase given for setting up SSH connection to router");
        }
        JSch jsch = new JSch();
        try {
            jsch.addIdentity(hm.get("keyfile"), hm.get("passphrase"));
            this.session = jsch.getSession(hm.get("login"), hm.get("router"), 22);
            // this.session.setPassword("password");
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            this.session.setConfig(config);
            this.session.connect();

            this.channel = this.session.openChannel("exec");
            this.in = this.channel.getInputStream();
            this.out = this.channel.getOutputStream();
            ((ChannelExec)this.channel).setCommand("junoscript");
            this.channel.connect();
            this.log.info("createSSHconnection.finish");
        } catch (JSchException ex) {
            throw new PSSException(ex.getMessage());
        }
        this.isActive = true;
    }

    /**
     * Indicates whether SSH connection has been set up.
     *
     * @return boolean indicating whether connection has been set up
     */
     public boolean isActive() {
         return this.isActive;
    }

    /**
     * Sets up login to a router given a link.
     *
     */
     public void setupLogin(Link link, HashMap<String, String> hm) 
             throws IOException {
        hm.put("login", this.props.getProperty("login"));
        hm.put("router", link.getPort().getNode().getNodeAddress().getAddress());
        String keyfile = null;
        keyfile = ConfigFinder.getInstance().find(ConfigFinder.PSS_DIR, "oscars.key");
        hm.put("keyfile", keyfile);
        hm.put("passphrase", this.props.getProperty("passphrase"));
    }

    /**
     * Sets up login to a router given a router name.
     *
     */
     public void setupLogin(String router, HashMap<String, String> hm)
            throws IOException {
        hm.put("login", this.props.getProperty("login"));
        hm.put("router", router);
        this.log.info("router: " + router);
        String keyfile = ConfigFinder.getInstance().find(ConfigFinder.PSS_DIR, "oscars.key");
        hm.put("keyfile", keyfile);
        hm.put("passphrase", this.props.getProperty("passphrase"));
    }

    /**
     * Sends the XML command to the server.
     * @param doc XML document with Junoscript commands
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    public void sendCommand(Document doc)
            throws IOException, JDOMException, PSSException {

        XMLOutputter outputter = new XMLOutputter();
        Format format = outputter.getFormat();
        format.setLineSeparator("\n");
        format.setEncoding("US-ASCII");
        outputter.setFormat(format);
        // log, and then send to router
        String logOutput = outputter.outputString(doc);
        this.log.info("\n" + "SENDING\n" + logOutput);
        if (this.isActive) {
            outputter.output(doc, this.out);
        }
    }

    /**
     * Reads the XML response from the socket created earlier into a DOM
     * (makes for easier parsing).
     *
     * @return XML document with response from server
     * @throws IOException
     * @throws JDOMException
     * @throws PSSException
     */
    public Document readResponse()
            throws IOException, JDOMException, PSSException {

        ByteArrayOutputStream buff  = new ByteArrayOutputStream();
        Document doc = null;
        SAXBuilder b = new SAXBuilder();
        doc = b.build(this.in);
        if (doc == null) {
            throw new PSSException("Juniper router did not return a response");
        }
        // for logging purposes only
        XMLOutputter outputter = new XMLOutputter();
        outputter.output(doc, buff);
        this.log.info(buff.toString());
        return doc;
    }

    /**
     * Not currently used.
     * This would be used with SSL sockets that need to connect to a server
     * whose CA cert we don't have in our keyring. The effect of this
     * function would be that we trust ALL SSL connections.
     *
     * @throws IOException
     * @throws PSSException
     */
    public void disableTrustManager() throws IOException, PSSException {

        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[]
                    getAcceptedIssuers() {
                        return null;
                    }

                    public void
                        checkClientTrusted(
                                X509Certificate[] certs,
                                String authType) {
                            System.out.print("in checkClientTrusted");
                    }

                    public void checkServerTrusted(
                                X509Certificate[] certs,
                                String authType) {
                        System.out.print("in checkServerTrusted");
                }
            }
        };

        try {
            // Install the all-trusting trust manager (no pun intended)
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            this.sslsf = sc.getSocketFactory();
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException ex) {
            throw new PSSException(ex.getMessage());
        } catch (KeyManagementException ex) {
            throw new PSSException(ex.getMessage());
        }
    }

    /**
     * Creates an SSL connection; currently it is
     * NOT WORKING so we are using ssh (above) instead.
     * ssh is OK for now, so fixing this is on the back burner.
     *
     * @throws IOException
     */
    public void createSSLConnection() throws IOException {
        int port = 3220;
        // dev-m20-rt1.es.net has address 198.128.8.11
        // String hostname = "dev-m20-rt1.es.net";
        String hostname = "foobar.lbl.gov";
        Socket socket = sslsf.createSocket(hostname, port);

        // Create streams to securely send and receive data to the server
        this.in = socket.getInputStream();
        this.out = socket.getOutputStream();
    }
}
