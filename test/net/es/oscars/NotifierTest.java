package net.es.oscars;

import org.testng.annotations.*;

import java.util.Properties;
import javax.mail.*;

public class NotifierTest {

  @Test(groups={ "core" })
    public void notifierConstructor() {
        Notifier notifier = new Notifier();
        assert notifier != null;
    }

  @Test(groups={ "core" })
    public void getWebmaster() {
        Notifier notifier = new Notifier();
        assert notifier.getWebmaster() != null;
    }

  @Test(groups={ "broken" })
    public void sendMessage() throws MessagingException {
        Notifier notifier = new Notifier();
        String subject = "This is a test of email notifications.\n";
        String notification = "This is a test.\n";
        notifier.sendMessage(subject, notification);
    }
}
