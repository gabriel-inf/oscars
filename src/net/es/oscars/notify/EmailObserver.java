package net.es.oscars.notify;

import java.util.*;
import java.util.regex.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.text.DateFormat;

import org.apache.log4j.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.*;
import net.es.oscars.PropHandler;

/**
 * EmailObserver handles sending email to sys admins and users. 
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class EmailObserver implements Observer {
    private Session session;
    private Properties props;
    private Logger log;
    private String webmaster;
    private String localhostname;
    private List<String> sysadmins;
    private static String staticOverrideNotification;
    
    /**
     * Constructor. Reads-in mail.webmaster and mail.recipients from 
     * oscars.properties to build mailing list.
     */
    public EmailObserver() {
        this.log = Logger.getLogger(this.getClass());
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("mail", true);
        // not ideal; exception will be thrown in sendMessage if null
        this.webmaster = this.props.getProperty("webmaster");
        // fill props with any information
        this.session = Session.getDefaultInstance(this.props, null);
        this.sysadmins = new ArrayList<String>();
        this.sysadmins.add(this.webmaster);
        String recipientList = this.props.getProperty("recipients");
        if (recipientList != null) {
            recipientList = recipientList.trim();
            String[] recipients = recipientList.split(":");
            for (String recipient: recipients) {
                this.sysadmins.add(recipient);
            }
        }
        try {
            java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
            this.localhostname = localMachine.getHostName();
        } catch (java.net.UnknownHostException uhe) {
            this.localhostname = "host: unknown";
        }
    }

    /**
     * Observer method called whenever a change occurs. It accepts an 
     * Observable object and an net.es.oscars.notify.OSCARSEvent object as
     * arguments. Sends email notifications if template exists for
     * event.
     *
     * @param obj the observable object
     * @param arg the event that ocurred
     */
    public void update (Observable obj, Object arg) {
        // Observer interface requires second argument to be Object
        if ((staticOverrideNotification != null) &&
                !staticOverrideNotification.equals("1")) {
            this.log.info("email notification overriden");
            return;
        }
        if(!(arg instanceof OSCARSEvent)){
            this.log.error("[ALERT] Wrong argument passed to EmailObserver");
            return;
        }
        
        //TODO: Do not hardcode this path
        OSCARSEvent event = (OSCARSEvent) arg;
        String catalinaHome = System.getProperty("catalina.home");
        if(!catalinaHome.endsWith("/")){
            catalinaHome += "/";
        }
        String templateDir = catalinaHome + "shared/classes/server/mail_templates/";
        String fname = templateDir + event.getType() + ".xml";
        File fin = new File(fname);
        String contentType = "text/plain";
        
        /* Only send email if template exists*/
        if(!fin.exists()){
            this.log.debug(fname + " does not exist!");
            return;
        }
        
        String line = null;
        String template = null;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(fin));
            while((line = reader.readLine()) != null){
                template += (line + "\n");
            }
        }catch(IOException e){
            this.log.error("Unable to read template file: " + 
                e.getMessage());
            return;
        }
        
        //replace elements
        String msg = this.applyTemplate(template, event);
        
        this.log.info("parse.start");
        //split body and subject
        Pattern typePat = Pattern.compile("<contentType>(.+)</contentType>");
        Pattern subjPat = Pattern.compile("<subject>(.+)</subject>");
        Pattern bodyPat = Pattern.compile(
            "<messageBody>([\\w\\W]+)</messageBody>");
        Matcher subjMat = subjPat.matcher(msg);
        Matcher bodyMat = bodyPat.matcher(msg);
        Matcher typeMat = typePat.matcher(msg);
        
        String subject = null;
        String body = null;

        if(subjMat.find()){
            subject = subjMat.group(1);
        }else{
            subject = "[ALERT] no subject";
        }

        if(bodyMat.find()){
            body = bodyMat.group(1);
            //remove leading whitespace
            body = body.replaceFirst("^\\s+","");
            body += "\n";
        }else{
            body = "no message body\n";
        }

        if(typeMat.find()){
            contentType = typeMat.group(1);
        }
        this.log.debug("parse.end");

        try {
            this.sendMessage(subject, body, contentType);
        } catch (javax.mail.MessagingException ex) {
            this.log.error("[ALERT] Cannot email the following message: " +
                           "subject: " + subject + "\n" + body);
        }
    }
    
    /**
     * Given a template as a string this replaces all the dynamic fields with
     * values in the given event object.
     *
     * @param template String of the template containing the fields to replace
     * @param event the event containing the values to fill-in
     * @return the template with all dynamic fields replaced
     */
    private String applyTemplate(String template, OSCARSEvent event){
        Reservation resv = event.getReservation();
        Path path = null;
        Layer2Data l2Data = null;
        Layer3Data l3Data = null;
        MPLSData mplsData = null;
        String msg = template;
        String eventTime = this.formatTime(event.getTimestamp());
        String val = null;
        Integer intVal = null;
        
        //NOTE: There are more efficient ways to parse and replace fields
        //but this seems to work for now.
        this.log.info("applyTemplate.start");
        msg = this.replaceTemplateField("##event##", event.toString(), msg);
        msg = this.replaceTemplateField("##eventType##", event.getType(), msg);
        msg = this.replaceTemplateField("##eventTimestamp##", eventTime, msg);
        msg = this.replaceTemplateField("##eventUserLogin##", event.getUserLogin(), msg);
        msg = this.replaceTemplateField("##eventSource##", event.getSource(), msg);
        msg = this.replaceTemplateField("##errorCode##", event.getErrorCode(), msg);
        msg = this.replaceTemplateField("##errorMessage##", event.getErrorMessage(), msg);
        
        if(resv != null){
            path = resv.getPath();
            String startTime = this.formatTime(resv.getStartTime());
            String endTime = this.formatTime(resv.getEndTime());
            String createdTime = this.formatTime(resv.getCreatedTime());
            msg = this.replaceTemplateField("##reservation##", 
                resv.toString("bss"), msg);
            msg = this.replaceTemplateField("##gri##", 
                resv.getGlobalReservationId(), msg);
            msg = this.replaceTemplateField("##startTime##", startTime, msg);
            msg = this.replaceTemplateField("##endTime##", endTime, msg);
            msg = this.replaceTemplateField("##createdTime##", 
                createdTime, msg);
            msg = this.replaceTemplateField("##bandwidth##", 
                resv.getBandwidth() + "", msg);
            msg = this.replaceTemplateField("##resvUserLogin##", 
                resv.getLogin(), msg);
            msg = this.replaceTemplateField("##status##", 
                resv.getStatus(), msg);
            msg = this.replaceTemplateField("##description##", 
                resv.getDescription(), msg);
            msg = this.applyUserDefinedTags(resv.getDescription(), msg);
        }else{
            //need to clear out template objects so aren't in sent messages
            msg = this.replaceTemplateField("##reservation##", "", msg);
            msg = this.replaceTemplateField("##gri##", "", msg);
            msg = this.replaceTemplateField("##startTime##", "", msg);
            msg = this.replaceTemplateField("##endTime##", "", msg);
            msg = this.replaceTemplateField("##createdTime##", "", msg);
            msg = this.replaceTemplateField("##bandwidth##", "", msg);
            msg = this.replaceTemplateField("##resvUserLogin##", "", msg);
            msg = this.replaceTemplateField("##status##", "", msg);
            msg = this.replaceTemplateField("##description##", "", msg);
        }
        
        if(path != null){
            l2Data = path.getLayer2Data();
            l3Data = path.getLayer3Data();
            mplsData = path.getMplsData();
            msg = this.replaceTemplateField("##pathSetupMode##", 
                path.getPathSetupMode(), msg);
            msg = this.replaceTemplateField("##isExplicitPath##", 
                path.isExplicit() + "", msg);
            String nextDomain = null;
            if(path.getNextDomain() != null){
               nextDomain = path.getNextDomain().getTopologyIdent();
            }
            msg = this.replaceTemplateField("##nextDomain##", nextDomain, msg);
            //TODO: Add path fields. May require some toString() changes
        }else{
            msg = this.replaceTemplateField("##pathSetupMode##", "", msg);
            msg = this.replaceTemplateField("##isExplicitPath##", "", msg);
            msg = this.replaceTemplateField("##nextDomain##", "", msg);
        }
        
        if(l2Data != null){
            msg = this.replaceTemplateField("##l2Source##", 
                l2Data.getSrcEndpoint(), msg);
            msg = this.replaceTemplateField("##l2Dest##", 
                l2Data.getDestEndpoint(), msg);
        }else{
            msg = this.replaceTemplateField("##l2Source##", "", msg);
            msg = this.replaceTemplateField("##l2Dest##", "", msg);
        }
        
        if(l3Data != null){
            msg = this.replaceTemplateField("##l3Source##", 
                l3Data.getSrcHost(), msg);
            msg = this.replaceTemplateField("##l3Dest##", 
                l3Data.getDestHost(), msg);
            intVal = l3Data.getSrcIpPort();
            if (intVal != null) {
                val = intVal.toString();
            } else {
                val = "";
            }
            msg = this.replaceTemplateField("##l3SrcPort##", 
                val, msg);
            intVal = l3Data.getDestIpPort();
            if (intVal != null) {
                val = intVal.toString();
            } else {
                val = "";
            }
            msg = this.replaceTemplateField("##l3DestPort##", 
                val, msg);
            val = l3Data.getProtocol();
            if (val == null) {
                val = "";
            }
            msg = this.replaceTemplateField("##protocol##", 
                val, msg);
            val = l3Data.getDscp();
            if (val == null) {
                val = "";
            }
            msg = this.replaceTemplateField("##dscp##", 
                val, msg);
        }else{
            msg = this.replaceTemplateField("##l3Source##", "", msg);
            msg = this.replaceTemplateField("##l3Dest##", "", msg);
            msg = this.replaceTemplateField("##l3SrcPort##", "", msg);
            msg = this.replaceTemplateField("##l3DestPort##", "", msg);
            msg = this.replaceTemplateField("##protocol##", "", msg);
            msg = this.replaceTemplateField("##dscp##", "", msg);
        }
        
        if(mplsData != null){
            msg = this.replaceTemplateField("##burstLimit##", 
                mplsData.getBurstLimit()+"", msg);
            msg = this.replaceTemplateField("##lspClass##", 
                mplsData.getLspClass(), msg);
        }else{
            msg = this.replaceTemplateField("##burstLimit##", "", msg);
            msg = this.replaceTemplateField("##lspClass##", "", msg);
        }
        
        this.log.debug(msg);
        this.log.info("applyTemplate.end");
        
        return msg;
    }
    
    /**
     * Convenience method for replacing a single value in a template
     *
     * @param field the field to replace
     * @param value the value with which to replace the field
     * @param template the template on which the replacement will be made
     * @return the template with replaced fields
     */
    private String replaceTemplateField(String field, String value, String template){
        String msg = template;
        
        //clear out fields if value is null
        if(value == null){
            value = "";
        }
        msg = template.replaceAll(field, value);
        
        return msg;
    }
    
    /**
     * Applies user-defined tags specified in ##TAG:<i>TAG_NAME</i>## fields to
     * a given template.
     *
     * @param description the description of the reservation that may contain tags
     * @param template the template to which in which tags may be shown
     * @return the template with all user-defined tags displayed
     */
    private String applyUserDefinedTags(String description, String template){
        Pattern tagPattern = Pattern.compile("##TAG:(.+?)##");
        Matcher tagMatcher = tagPattern.matcher(template);
        
        while(tagMatcher.find()){
            String tag = tagMatcher.group(1);
            String printTag = "[" + tag + "]";
            if(description != null && description.contains(printTag)){
                template = template.replaceAll("##TAG:" + tag + "##", 
                    printTag + " ");
            }else{
                template = template.replaceAll("##TAG:" + tag + "##", "");
            }
        }
        
        return template;
    }
    
    /**
     * Sends an email message with the given parameters.
     *
     * @param subject the subject of the email to send
     * @param notification the body of the email to send
     * @param contentType the type of message (i.e. text/plain, text/html)
     */
    public void sendMessage(String subject, String notification, 
        String contentType) throws javax.mail.MessagingException {

        subject += " ("+this.localhostname+")";
        // Define message
        MimeMessage message = new MimeMessage(this.session);
        message.setFrom(new InternetAddress(this.webmaster));
        for (String to: this.sysadmins) {
            message.addRecipient(Message.RecipientType.TO,
                                 new InternetAddress(to));
        }
        message.setSubject(subject);
        message.setContent(notification, contentType);
        Transport.send(message);   // Send message
    }
    
    /**
     * Returns string formatted UTC datetime
     *
     * @param timestamp a Long containing the timestamp
     * @return string-formatted datetime
     */
    private String formatTime(Long timestamp){
        if(timestamp != null){
            return this.formatTime(timestamp.longValue());
        }
        return "";
    }
    
    /**
     * Returns string formatted UTC datetime
     *
     * @param timestamp a long containing the timestamp
     * @return string-formatted datetime
     */
    private String formatTime(long timestamp){
        DateFormat df = DateFormat.getInstance();
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String time = df.format(timestamp*1000L) + " UTC";
        return time;
    }

    /**
     * Allows overriding whether a notification is sent.  Primarily for tests.
     *
     * @param overrideNotification "0" or "1" indicating whether email sent 
     */
    public static void setNotification(String overrideNotification) {
        staticOverrideNotification = overrideNotification;
    }

    public String getWebmaster() { return this.webmaster; }
    public void setWebmaster(String webmaster) { this.webmaster = webmaster; }

    public List<String> getSysadmins() { return this.sysadmins; }
    public void addSysadmin(String admin) { this.sysadmins.add(admin); }
    public void removeSysadmin(String admin) { this.sysadmins.remove(admin); }
}
