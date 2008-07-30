package net.es.oscars.scheduler;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.OSCARSCore;

public class CreatePathJob extends ChainingJob implements Job {
    private Logger log;
    private OSCARSCore core;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("CreatePathJob.start name:"+context.getJobDetail().getFullName());
        this.core = OSCARSCore.getInstance();
        StateEngine se = new StateEngine();
        EventProducer eventProducer = new EventProducer();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String gri = (String) dataMap.get("gri");
        String bssDbName = core.getBssDbName();
        Session bss = core.getBssSession();
        bss.beginTransaction();
        ReservationDAO resvDAO = new ReservationDAO(bssDbName);
        Reservation resv = null;
        try {
            resv = resvDAO.query(gri);
            Thread.sleep(10000);//simulate setup time
            se.updateStatus(resv, StateEngine.ACTIVE);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_COMPLETED, "", "SCHEDULER", resv);
        }catch (BSSException ex) {
            this.log.error("Could not create reservation "+ gri);
            this.log.error(ex);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", 
                                   "SCHEDULER", resv, "", ex.getMessage());
        }catch (InterruptedException ex) {
            this.log.error("Interrupted", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", 
                                   "SCHEDULER", resv, "", ex.getMessage());
        }catch (Exception ex) {
            this.log.error("Exception", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", 
                                   "SCHEDULER", resv, "", ex.getMessage());
        }finally{
            this.runNextJob(context);
            bss.getTransaction().commit();
        }

        this.log.info("CreatePathJob.end");
    }
}
