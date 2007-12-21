package net.es.oscars;

import org.testng.annotations.*;

import java.util.Properties;
import javax.mail.*;

@Test(groups={ "core" })
public class NotifierTest {
    private Notifier notifier;

  @BeforeClass
    public void setUpClass() {
        this.notifier = new Notifier();
    }

  @Test
    public void getWebmaster() {
        assert this.notifier.getWebmaster() != null;
    }

  // This test is ordinarily disabled to avoid a sea of notification
  // messages.
  @Test(enabled=false)
    public void sendMessage()
            throws MessagingException, UnsupportedOperationException {
        String subject = "This is a test of email notifications.\n";
        String notification = "This is a test.\n";
        this.notifier.sendMessage(subject, notification);
    }
}
