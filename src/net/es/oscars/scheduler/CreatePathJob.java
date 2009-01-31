package net.es.oscars.scheduler;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.*;
import net.es.oscars.pss.PathSetupManager;

public class CreatePathJob extends ChainingJob implements Job {
    private Logger log;
    private OSCARSCore core;
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("CreatePathJob.start name:"+context.getJobDetail().getFullName());
        this.core = OSCARSCore.getInstance();
        StateEngine se = this.core.getStateEngine();
        PathSetupManager pm = this.core.getPathSetupManager();
        EventProducer eventProducer = new EventProducer();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String gri = (String) dataMap.get("gri");
        String bssDbName = core.getBssDbName();
        Session bss = core.getBssSession();
        bss.beginTransaction();
        ReservationDAO resvDAO = new ReservationDAO(bssDbName);
        Reservation resv = null;
        String login = null;
        try {
            resv = resvDAO.query(gri);
            login = resv.getLogin();
            Thread.sleep(10000);//simulate setup time
            pm.updateCreateStatus(StateEngine.CONFIRMED, resv);
        }catch (BSSException ex) {
            this.log.error("Could not create reservation "+ gri);
            this.log.error(ex);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, login, 
                                   "JOB", resv, "", ex.getMessage());
        }catch (InterruptedException ex) {
            this.log.error("Interrupted", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, login, 
                                   "JOB", resv, "", ex.getMessage());
        }catch (Exception ex) {
            this.log.error("Exception", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, login, 
                                   "JOB", resv, "", ex.getMessage());
        }finally{
            //this delays the queue a domain-specific amount
            try{ 
                Thread.sleep(3000);
            }catch(InterruptedException e){}
            this.runNextJob(context);
            bss.getTransaction().commit();
        }

        this.log.info("CreatePathJob.end");
    }
}
