package net.es.oscars.pss.common;

import java.util.ArrayList;
import java.util.List;

import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;
import net.es.oscars.pss.impl.sdn.SDNQueuer;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.testng.annotations.Test;

@Test(groups={ "pss.common" })
public class PSSActionWatcherTest {
    @Test
    public void testQueuer() throws BSSException, PSSException, SchedulerException, InterruptedException {
        SchedulerFactory schedFact = new StdSchedulerFactory();
        Scheduler scheduler = schedFact.getScheduler();
        
        PSSQueuer q = SDNQueuer.getInstance();
        q.setScheduler(scheduler);
        scheduler.start();
        
        PSSActionWatcher aw = PSSActionWatcher.getInstance();
        aw.setQueuer(q);
        

        List<PSSDirection> directions_a = new ArrayList<PSSDirection>();
        directions_a.add(PSSDirection.A_TO_Z);
        directions_a.add(PSSDirection.Z_TO_A);

        
        List<PSSDirection> directions_b = new ArrayList<PSSDirection>();
        directions_b.add(PSSDirection.BIDIRECTIONAL);


        
        PSSAction action = PSSAction.SETUP;
        
        Reservation resv_a = new Reservation();
        resv_a.setId(1);
        String gri_a = "foo.net-999";
        resv_a.setGlobalReservationId(gri_a);

        Reservation resv_b = new Reservation();
        String gri_b = "bar.net-123";
        resv_b.setGlobalReservationId(gri_b);
        resv_b.setId(2);
        
        
        
        q.startAction(gri_a, directions_a, action);
        aw.watch(resv_a, action, directions_a);
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        q.startAction(gri_b, directions_b, action);
        aw.watch(resv_b, action, directions_b);
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        q.completeAction(gri_a, PSSDirection.A_TO_Z, action, true, "");
        Thread.sleep(1000); System.out.println(".");
        q.completeAction(gri_b, PSSDirection.BIDIRECTIONAL, action, true, "");
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        q.completeAction(gri_a, PSSDirection.Z_TO_A, action, true, "");
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        Thread.sleep(1000); System.out.println(".");
        System.out.println("done");
        scheduler.shutdown();
    }
    
    
    
}
