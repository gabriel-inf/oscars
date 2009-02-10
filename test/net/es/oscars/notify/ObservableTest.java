package net.es.oscars.notify;

import net.es.oscars.bss.events.ObserverSource;
import net.es.oscars.bss.events.ObserverManager;

import org.testng.annotations.*;

import java.util.*;
import javax.mail.*;

@Test(groups={ "notify" })
public class ObservableTest {
    private ObserverManager notifyInitializer;

  @BeforeClass
    public void setUpClass() {
        this.notifyInitializer = new ObserverManager();
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
        ObserverSource observable = this.notifyInitializer.getSource();
        Object obj = (Object) messageInfo;
        observable.eventOccured(obj);
    }

  @Test(enabled=true)
    public void sendAlertEvent() {

        Map<String,String> messageInfo = new HashMap<String,String>();
        messageInfo.put("subject", "This is a test of email notifications.");
        messageInfo.put("body", "This is a test.");
        messageInfo.put("alertLine", "Reservation [PRODUCTION CIRCUIT]");
        ObserverSource observable = this.notifyInitializer.getSource();
        Object obj = (Object) messageInfo;
        observable.eventOccured(obj);
    }
}
