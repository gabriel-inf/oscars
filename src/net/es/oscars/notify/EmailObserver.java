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
        //String catalinaHome = System.getProperty("catalina.home");
        String catalinaHome = System.getenv("CATALINA_HOME");
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
        HashMap<String,String[]> resv = event.getReservationParams();
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
            String startTime = this.formatTime(resv.get("startSeconds"));
            String endTime = this.formatTime(resv.get("endSeconds"));
            String createdTime = this.formatTime(resv.get("createSeconds"));
            msg = this.replaceResvField("##reservation##", resv, msg);
            msg = this.replaceTemplateField("##gri##", resv.get("gri"), msg);
            msg = this.replaceTemplateField("##startTime##", startTime, msg);
            msg = this.replaceTemplateField("##endTime##", endTime, msg);
            msg = this.replaceTemplateField("##createdTime##", createdTime, msg);
            msg = this.replaceTemplateField("##bandwidth##", 
                                            resv.get("bandwidth"), msg);
            msg = this.replaceTemplateField("##resvUserLogin##", 
                                            resv.get("userLogin"), msg);
            msg = this.replaceTemplateField("##status##", 
                                            resv.get("status"), msg);
            msg = this.replaceTemplateField("##description##", 
                                            resv.get("description"), msg);
            msg = this.applyUserDefinedTags(resv.get("description"), msg);
            msg = this.replaceTemplateField("##layer##", resv.get("layer"), msg);
            msg = this.replaceTemplateField("##pathSetupMode##", 
                                            resv.get("pathSetupMode"), msg);
            msg = this.replaceTemplateField("##pathType##", 
                                            resv.get("pathType"), msg);
            msg = this.replaceTemplateField("##isExplicitPath##", 
                                            resv.get("isExplicitPath"), msg);
            msg = this.replaceTemplateField("##nextDomain##",
                                            resv.get("nextDomain"), msg);
            msg = this.replaceTemplateField("##source##", 
                                            resv.get("source"), msg);
            msg = this.replaceTemplateField("##destination##", 
                                            resv.get("destination"), msg);
            msg = this.replaceTemplateField("##vlanTag##", 
                                            resv.get("vlanTag"), msg);
            msg = this.replaceTemplateField("##tagSrcPort##", 
                                            resv.get("tagSrcPort"), msg);
            msg = this.replaceTemplateField("##tagDestPort##", 
                                            resv.get("tagDestPort"), msg);
            msg = this.replaceTemplateField("##srcPort##", 
                                            resv.get("srcPort"), msg);
            msg = this.replaceTemplateField("##destPort##", 
                                            resv.get("destPort"), msg);
            msg = this.replaceTemplateField("##protocol##", 
                                            resv.get("protocol"), msg);
            msg = this.replaceTemplateField("##dscp##", resv.get("dscp"), msg);
            msg = this.replaceTemplateField("##burstLimit##", 
                                            resv.get("burstLimit"), msg);
            msg = this.replaceTemplateField("##lspClass##", 
                                            resv.get("lspClass"), msg);
            msg = this.replaceTemplateField("##interdomainPath##",
                                            resv.get("interdomainPath"), msg);
            msg = this.replaceTemplateField("##intradomainPath##",
                                            resv.get("intradomainPath"), msg);
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
            msg = this.replaceTemplateField("##pathSetupMode##", "", msg);
            msg = this.replaceTemplateField("##isExplicitPath##", "", msg);
            msg = this.replaceTemplateField("##nextDomain##", "", msg);
            msg = this.replaceTemplateField("##source##", "", msg);
            msg = this.replaceTemplateField("##destination##", "", msg);
            msg = this.replaceTemplateField("##srcPort##", "", msg);
            msg = this.replaceTemplateField("##destPort##", "", msg);
            msg = this.replaceTemplateField("##protocol##", "", msg);
            msg = this.replaceTemplateField("##dscp##", "", msg);
            msg = this.replaceTemplateField("##burstLimit##", "", msg);
            msg = this.replaceTemplateField("##lspClass##", "", msg);
            msg = this.replaceTemplateField("##interdomainPath##", "", msg);
            msg = this.replaceTemplateField("##intradomainPath##", "", msg);
        }
        
        this.log.debug(msg);
        this.log.info("applyTemplate.end");
        
        return msg;
    }
    
    /**
     * Convenience method for replacing a single value in a template
     *
     * @param field the field to replace
     * @param value the String[] value with which to replace the field
     * @param template the template on which the replacement will be made
     * @return the template with replaced fields
     */
    private String replaceTemplateField(String field, String[] value, String template){
        //clear out fields if value is null
        if(value == null){
            return template.replaceAll(field, "");
        }
        
        String delim = (value.length > 1 ? "\n" : "");
        String strValue = "";
        for(int i = 0; i < value.length; i++){
            strValue += (value[i] + delim);
        }
        
        return template.replaceAll(field, strValue);
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
     * Convenience method for replacing a an entire reservation
     *
     * @param field the field to replace
     * @param resv the reservation fields
     * @param template the template on which the replacement will be made
     * @return the template with replaced fields
     */
    private String replaceResvField(String field, 
                                        HashMap<String,String[]> resv, 
                                        String template){       
        String resvTemplate = "";
        
        if(resv.get("gri") != null){
            resvTemplate += "GRI: ##gri##\n";
        }
        if(resv.get("description") != null){
            resvTemplate += "description: ##description##\n";
        }
        if(resv.get("userLogin") != null){
            resvTemplate += "login: ##resvUserLogin##\n";
        }
        if(resv.get("status") != null){
            resvTemplate += "status: ##status##\n";
        }
        if(resv.get("startSeconds") != null){
            resvTemplate += "start time: ##startTime##\n";
        }
        if(resv.get("endSeconds") != null){
            resvTemplate += "end time: ##endTime##\n";
        }
        if(resv.get("bandwidth") != null){
            resvTemplate += "bandwidth: ##bandwidth##\n";
        }
        if(resv.get("pathSetupMode") != null){
            resvTemplate += "path setup mode: ##pathSetupMode##\n";
        }
        if(resv.get("layer") != null){
            resvTemplate += "layer: ##layer##\n";
        }
        if(resv.get("source") != null){
            resvTemplate += "source: ##source##\n";
        }
        if(resv.get("destination") != null){
            resvTemplate += "destination: ##destination##\n";
        }
        if(resv.get("vlanTag") != null){
            resvTemplate += "VLAN tag: ##vlanTag##\n";
        }
        if(resv.get("tagSrcPort") != null){
            resvTemplate += "source tagged: ##tagSrcPort##\n";
        }
        if(resv.get("tagDestPort") != null){
            resvTemplate += "destination tagged: ##tagDestPort##\n";
        }
        if(resv.get("protocol") != null){
            resvTemplate += "protocol: ##protocol##\n";
        }
        if(resv.get("srcPort") != null){
            resvTemplate += "src IP port: ##srcPort##\n";
        }
        if(resv.get("destPort") != null){
            resvTemplate += "dest IP port: ##destPort##\n";
        }
        if(resv.get("dscp") != null){
            resvTemplate += "dscp: ##dscp##\n";
        }
        if(resv.get("burstLimit") != null){
            resvTemplate += "burst limit: ##burstLimit##\n";
        }
        if(resv.get("lspClass") != null){
            resvTemplate += "LSP class: ##lspClass##\n";
        }
        if(resv.get("intradomainPath") != null){
            resvTemplate += "intradomain hops: \n\n ##intradomainPath##\n";
        }
        if(resv.get("interdomainPath") != null){
            resvTemplate += "interdomain hops: \n\n ##interdomainPath##\n";
        }
        
        return template.replaceAll(field, resvTemplate);
    }
    
    /**
     * Applies user-defined tags specified in ##TAG:<i>TAG_NAME</i>## fields to
     * a given template.
     *
     * @param description the description of the reservation that may contain tags
     * @param template the template to which in which tags may be shown
     * @return the template with all user-defined tags displayed
     */
    private String applyUserDefinedTags(String[] description, String template){
        Pattern tagPattern = Pattern.compile("##TAG:(.+?)##");
        Matcher tagMatcher = tagPattern.matcher(template);
        
        if(description == null || description.length < 1){
            return template;
        }
        
        while(tagMatcher.find()){
            String tag = tagMatcher.group(1);
            String printTag = "[" + tag + "]";
            if(description[0].contains(printTag)){
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
    private String formatTime(String[] timestamp){
        if(timestamp == null || timestamp.length < 1){
            return "";
        }
        
        String time = "";
        try{
           long stamp = Long.parseLong(timestamp[0]);
           time = this.formatTime(stamp);
        }catch(Exception e){}
        
        return time;
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
