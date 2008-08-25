package net.es.oscars.scheduler;

import java.util.Properties;
import java.util.Date;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.*;
import net.es.oscars.pss.*;
import net.es.oscars.PropHandler;

public class CancelReservationJob  extends ChainingJob  implements Job {
    private Logger log;
    private OSCARSCore core;
    private StateEngine se;
    private long CONFIRM_TIMEOUT = 600;//10min
    private long COMPLETE_TIMEOUT = 600;//10min
    
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getFullName();
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("ModifyReservationJob.start name:"+jobName);
        this.core = OSCARSCore.getInstance();
        this.se = this.core.getStateEngine();
        TypeConverter tc = this.core.getTypeConverter();
        ReservationManager rm = this.core.getReservationManager();
        String idcURL = this.core.getServiceManager().getIdcURL();
        EventProducer eventProducer = new EventProducer();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String gri =  dataMap.getString("gri");
        String login = dataMap.getString("login");
        String institution = dataMap.getString("institution");
        Reservation resv = null;
        this.log.debug("GRI is: "+dataMap.get("gri")+"for job name: "+jobName);
        this.init();

        Session bss = core.getBssSession();
        bss.beginTransaction();
        
        /* Get reservation */
        try{
            resv = rm.getConstrainedResv(gri,login,institution);
        }catch(BSSException ex){
            bss.getTransaction().rollback();
            this.log.error(ex.getMessage());
            eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, login, 
                                   "JOB", "", ex.getMessage());
            return;
        }
        
        /* Perform start, confirm, complete, fail or statusCheck operation */
       /* try{
            if(dataMap.containsKey("start")){
                this.start(resv, login);
            }else if(dataMap.containsKey("confirm")){
                this.confirm(resv, login);
            }else if(dataMap.containsKey("complete")){
                this.complete(resv, login);
            }else if(dataMap.containsKey("fail")){
                String code = dataMap.getString("errorCode");
                String msg = dataMap.getString("errorMsg");
                String src = dataMap.getString("errorSource");
                this.rollback(resv, origState);
                eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, login,
                                      src, persistentResv, code, msg);
            }else if(dataMap.containsKey("statusCheck")){
                String status = this.se.getStatus(persistentResv);
                int localStatus = this.se.getLocalStatus(persistentResv);
                if(status.equals(dataMap.getString("status")) && 
                    localStatus == dataMap.getInt("localStatus")){
                    String op = ((localStatus & 1) == 1 ? 
                                 OSCARSEvent.RESV_CANCEL_COMPLETED : 
                                 OSCARSEvent.RESV_CANCEL_CONFIRMED);
                    throw new BSSException("Modify reservation timed-out " +
                                           "while waiting for event " +  op);
                }
            }else{
                this.log.error("Unknown modifyReservation job cannot be executed");
            }
            bss.getTransaction().commit();
        }catch(Exception ex){
            ex.printStackTrace();
            //Rollback any changes...
            bss.getTransaction().rollback();
            //...then start new transaction to update status
            try{
                bss = core.getBssSession();
                bss.beginTransaction();
                this.rollback(persistentResv, origState);
                eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FAILED, login, 
                                      idcURL, persistentResv, "", ex.getMessage());
                bss.getTransaction().commit();
            }catch(Exception ex2){
                bss.getTransaction().rollback();
                this.log.error(ex2);
            }
        }finally{
            this.runNextJob(context);
        } */
        this.log.info("ModifyReservationJob.end");
    }
    
    /**
     * Reads in timeout properties
     */
     public void init(){
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("timeout", true);
        String defaultTimeoutStr = props.getProperty("default");
        String confirmTimeoutStr = props.getProperty("cancelResv.confirm");
        String completeTimeoutStr = props.getProperty("cancelResv.complete");
        int defaultTimeout = 0;
        if(defaultTimeoutStr != null){
            try{
                defaultTimeout = Integer.parseInt(defaultTimeoutStr);
            }catch(Exception e){
                this.log.error("Default timeout.default property invalid. " +
                               "Defaulting to another value for timeout.");
            }
        }
        
        if(confirmTimeoutStr != null){
            try{
                CONFIRM_TIMEOUT = Integer.parseInt(confirmTimeoutStr);
            }catch(Exception e){
                this.log.error("timeout.cancelResv.confirm property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }else if(defaultTimeout > 0){
            CONFIRM_TIMEOUT = defaultTimeout;
        }
        
        if(completeTimeoutStr != null){
            try{
                COMPLETE_TIMEOUT = Integer.parseInt(completeTimeoutStr);
            }catch(Exception e){
                this.log.error("timeout.cancelResv.complete property invalid." +
                               "Defaulting to another value for timeout.");
            }
        }else if(defaultTimeout > 0){
            COMPLETE_TIMEOUT = defaultTimeout;
        }
     }
     
     /*
        Forwarder forwarder = this.core.getForwarder();
        String remoteStatus = null;
        resv = this.rm.cancel(gri, login, institution);
        
        InterdomainException interException = null;
        try {
            remoteStatus = forwarder.cancel(resv);
            eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_COMPLETED, login, "API", resv);
        } catch (InterdomainException e) {
            interException = e;
            eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, login, "API", resv, "", e.getMessage());
        } finally {
            forwarder.cleanUp();
            if(interException != null){
                throw interException;
            }
        }
     */

}
