package net.es.oscars.notify;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import net.es.oscars.PropHandler;

/**
 * EmailObserver handles sending email to sys admins and users. 
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
public class EmailObserver implements Observer {
    private Session session;
    private Properties props;
    private String webmaster;
    private String localhostname;
    private List<String> sysadmins;

    public EmailObserver() {
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

    // TODO: probably change arg datatype
    public void update (Observable obj, Object arg) {
        if (arg instanceof String[]) {
            String[] message = (String[]) arg;
            try {
            	this.sendMessage(message[0], message[1]);
            } catch (javax.mail.MessagingException ex) {
            	// TODO: handle exception properly
            }
        }
    }
    
    
    public void sendMessage(String subject, String notification)
            throws javax.mail.MessagingException {
    	subject += " ("+this.localhostname+")";
        // Define message
        MimeMessage message = new MimeMessage(this.session);
        message.setFrom(new InternetAddress(this.webmaster));
        for (String to: this.sysadmins) {
            message.addRecipient(Message.RecipientType.TO,
                                 new InternetAddress(to));
        }
        message.setSubject(subject);
        message.setText(notification);
        Transport.send(message);   // Send message
    }

    public String getWebmaster() { return this.webmaster; }
    public void setWebmaster(String webmaster) { this.webmaster = webmaster; }

    public List<String> getSysadmins() { return this.sysadmins; }
    public void addSysadmin(String admin) { this.sysadmins.add(admin); }
    public void removeSysadmin(String admin) { this.sysadmins.remove(admin); }
}