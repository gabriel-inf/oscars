package net.es.oscars.nsibridge.test.req;

import net.es.oscars.nsibridge.common.Invoker;
import net.es.oscars.nsibridge.common.JettyContainer;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.impl.ConnectionProvider;
import net.es.oscars.nsibridge.task.ProcNSIResvRequest;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import org.testng.annotations.BeforeSuite;
import net.es.oscars.nsibridge.beans.ResvRequest;
import org.testng.annotations.Test;

import javax.xml.ws.Holder;
import java.util.Date;

public class TaskTest {
    private static ConnectionProvider cp;

    @BeforeSuite (groups = {"task"})
    public void init() throws Exception {
        Invoker i = Invoker.getInstance();
        i.setContext(ConfigDefaults.CTX_TESTING);
        Thread thr = new Thread(i);
        thr.start();

        cp = (ConnectionProvider) JettyContainer.getInstance().getSoapHandlers().get("ConnectionProvider");
        System.out.print("waiting for jetty.");
        while (cp == null) {
            Thread.sleep(500);
            System.out.print(".");
            cp = (ConnectionProvider) JettyContainer.getInstance().getSoapHandlers().get("ConnectionProvider");
        }
        System.out.println("\n got jetty!");

    }

    @Test (groups = {"task"})
    public void testTasks() throws Exception {

        ResvRequest req = NSIRequestFactory.getRequest();


        Holder<CommonHeaderType> holder = new Holder<CommonHeaderType>();
        holder.value = req.getOutHeader();

        cp.reserve(req.getGlobalReservationId(), req.getDescription(), req.getConnectionId(), req.getCriteria(), req.getInHeader(), holder);

        Workflow wf = Workflow.getInstance();
        System.out.println(wf.printTasks());
        while (wf.hasItems()) {
            Thread.sleep(500);
            System.out.println(wf.printTasks());
        }




    }


}
