package net.es.oscars;

import java.util.Properties;
import javax.mail.*;

import junit.framework.*;

public class NotifierTest extends TestCase {

    public NotifierTest(String name) {
        super(name);
    }
        
    public void testLoad() {
        Notifier notifier = new Notifier();
        Assert.assertNotNull(notifier);
    }

    public void testGetWebmaster() {
        Notifier notifier = new Notifier();
        Assert.assertNotNull(notifier.getWebmaster());
    }

    public void testSendMessage() {
        Notifier notifier = new Notifier();
        String subject = "This is a test of email notifications.\n";
        String notification = "This is a test.\n";
        try {
            notifier.sendMessage(subject, notification);
        } catch (javax.mail.MessagingException e) {
            fail(e.getMessage());
        }
        Assert.assertNotNull(notifier.getWebmaster());
    }
}
