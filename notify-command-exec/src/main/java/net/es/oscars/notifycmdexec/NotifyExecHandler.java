package net.es.oscars.notifycmdexec;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mortbay.jetty.handler.AbstractHandler;

public class NotifyExecHandler extends AbstractHandler{
    private String setupCommand = null;
    private String teardownCommand = null;
    
    final private String SETUP_EVENT = "PATH_SETUP_COMPLETED";
    final private String TEARDOWN_EVENT = "PATH_TEARDOWN_COMPLETED";
    
    public void handleNotify(Element notification) {
        XMLOutputter outputter = new XMLOutputter();
        try {
            outputter.output(notification, System.out);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public void handleError(String type, Exception e) {
        System.err.println("An error occurred!");
        System.err.println("    Error Type: " + type);
        System.err.println("    Error Msg: " + e.getMessage());
    }

    public void handle(String target, HttpServletRequest request,
            HttpServletResponse response, int dispatch) throws IOException,
            ServletException {
        
        System.out.println("target=" + target);
        String msgString = "";
        String line = null;
        while((line = request.getReader().readLine()) != null){
            //System.out.println(line);
            msgString += line;
        }
        System.out.println();
        
        SAXBuilder builder = new SAXBuilder(false);
        Element eventTypeElem = null;
        try {
            Document doc = builder.build(new ByteArrayInputStream(msgString.getBytes()));
            XPath xpath = XPath.newInstance("//*[local-name() = 'event']/*[local-name() = 'type']");
            eventTypeElem = (Element) xpath.selectSingleNode(doc);
        } catch (JDOMException e) {
            System.err.println("Parsing error: " + e.getMessage());
        }
        
        if(eventTypeElem != null && eventTypeElem.getText() != null){
            System.out.println("EVENT TYPE: " + eventTypeElem.getText());
            this.runCommand(eventTypeElem.getText());
        }

        response.setStatus(202);
        response.setContentType("Content-Type: text/xml;charset=UTF-8");
        response.setContentLength(0);
        
    }

    private void runCommand(String eventType) {
        String command = null;
        if(SETUP_EVENT.equals(eventType) && setupCommand != null){
            System.out.println("Running setup command");
            command = setupCommand;
        }else if(TEARDOWN_EVENT.equals(eventType) && teardownCommand != null){
            System.out.println("Running teardown command");
            command = teardownCommand;
        }else{
            System.out.println("No command to run");
            return;
        }
        
        //run command
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            System.err.println("Error executing command: " + e.getMessage());
        }
    }
    
    /**
     * @return the setupCommand
     */
    public String getSetupCommand() {
        return this.setupCommand;
    }

    /**
     * @param setupCommand the setupCommand to set
     */
    public void setSetupCommand(String setupCommand) {
        this.setupCommand = setupCommand;
    }

    /**
     * @return the teardownCommand
     */
    public String getTeardownCommand() {
        return this.teardownCommand;
    }

    /**
     * @param teardownCommand the teardownCommand to set
     */
    public void setTeardownCommand(String teardownCommand) {
        this.teardownCommand = teardownCommand;
    }
}
