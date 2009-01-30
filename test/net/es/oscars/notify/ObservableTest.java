package net.es.oscars.notify;

import net.es.oscars.bss.events.NotifierSource;
import net.es.oscars.bss.events.NotifyException;
import net.es.oscars.bss.events.NotifyInitializer;

import org.testng.annotations.*;

import java.util.*;
import javax.mail.*;

@Test(groups={ "notify" })
public class ObservableTest {
    private NotifyInitializer notifyInitializer;

  @BeforeClass
    public void setUpClass() throws NotifyException {
        this.notifyInitializer = new NotifyInitializer();
        this.notifyInitializer.init();
    }

  // These tests are ordinarily disabled to avoid a sea of notification
  // messages.
  @Test(enabled=true)
    public void sendEvent() {

        Map<String,String> messageInfo = new HashMap<String,String>();
        messageInfo.put("subject", "This is a test of email notifications.");
        messageInfo.put("body", "This is a test.");
        messageInfo.put("alertLine", "Nothing of importance.");
        NotifierSource observable = this.notifyInitializer.getSource();
        Object obj = (Object) messageInfo;
        observable.eventOccured(obj);
    }

  @Test(enabled=true)
    public void sendAlertEvent() {

        Map<String,String> messageInfo = new HashMap<String,String>();
        messageInfo.put("subject", "This is a test of email notifications.");
        messageInfo.put("body", "This is a test.");
        messageInfo.put("alertLine", "Reservation [PRODUCTION CIRCUIT]");
        NotifierSource observable = this.notifyInitializer.getSource();
        Object obj = (Object) messageInfo;
        observable.eventOccured(obj);
    }
}
