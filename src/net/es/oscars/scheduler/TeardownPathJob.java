package net.es.oscars.scheduler;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.pss.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.OSCARSCore;

public class TeardownPathJob extends ChainingJob implements Job {
    private Logger log;
    private OSCARSCore core;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("TeardownPathJob.start name:"+context.getJobDetail().getFullName());
        this.core = OSCARSCore.getInstance();
        EventProducer eventProducer = new EventProducer();
        PathSetupManager pm = this.core.getPathSetupManager();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String gri = dataMap.getString("gri");
        String newStatus = dataMap.getString("newStatus");
        String bssDbName = core.getBssDbName();
        Session bss = core.getBssSession();
        bss.beginTransaction();
        ReservationDAO resvDAO = new ReservationDAO(bssDbName);
        Reservation resv = null;
        try {
            resv = resvDAO.query(gri);
            Thread.sleep(10000);//simulate setup time
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_COMPLETED, "", "SCHEDULER", resv);
            pm.updateTeardownStatus(1, resv);
        }catch (BSSException ex) {
            this.log.error("Could not teardown reservation "+ gri);
            this.log.error(ex);
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", 
                                   "SCHEDULER", resv, "", ex.getMessage());
        }catch (InterruptedException ex) {
            this.log.error("Interrupted", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", 
                                   "SCHEDULER", resv, "", ex.getMessage());
        }catch (Exception ex) {
            this.log.error("Exception", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", 
                                   "SCHEDULER", resv, "", ex.getMessage());
        }finally{
            //this delays the queue a domain-specific amount
            try{ 
                Thread.sleep(3000);
            }catch(InterruptedException e){}
            this.runNextJob(context);
            bss.getTransaction().commit();
        }
    }

}
