package net.es.oscars.scheduler;

import java.util.Properties;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Date;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.bss.topology.*;
import net.es.oscars.ws.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.bss.events.*;
import net.es.oscars.PropHandler;

public class ModifyReservationJob extends ChainingJob implements Job {
    private Logger log;
    private OSCARSCore core;
    private StateEngine se;
    private long CONFIRM_TIMEOUT = 600; //10min
    private long COMPLETE_TIMEOUT = 600; //10min


    //TODO: This should probably be a MySQL table
    private static HashMap<String, Long[]> resvCache = new HashMap<String, Long[]>();

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        String jobName = context.getJobDetail().getFullName();
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("ModifyReservationJob.start name:"+jobName);
        this.core = OSCARSCore.getInstance();
        this.se = this.core.getStateEngine();
        ReservationManager rm = this.core.getReservationManager();
        String idcURL = this.core.getServiceManager().getIdcURL();
        EventProducer eventProducer = new EventProducer();
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String gri =  dataMap.getString("gri");
        String login = dataMap.getString("login");
        String loginConstraint = dataMap.getString("loginConstraint");
        String institution = dataMap.getString("institution");
        HashMap<String, String[]> resvMap =
                    (HashMap<String, String[]>) dataMap.get("resvMap");
        Reservation persistentResv = null;
        this.log.debug("GRI is: "+dataMap.get("gri")+"for job name: "+jobName);
        this.init();
        String origState = StateEngine.RESERVED;

        Session bss = core.getBssSession();
        bss.beginTransaction();

        /* Get reservation */
        try {
            persistentResv =
                rm.getConstrainedResv(gri, loginConstraint, institution, null);
        } catch(BSSException ex) {
            bss.getTransaction().rollback();
            this.log.error(ex.getMessage());
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FAILED, login,
                                   "JOB", "", ex.getMessage());
            return;
        }

        /* Perform start, confirm, complete, fail or statusCheck operation */
        try {
            if (StateEngine.getStatus(persistentResv).equals(StateEngine.ACTIVE)) {
                origState = StateEngine.ACTIVE;
            }
            if (dataMap.containsKey("start")) {
                Reservation resv = this.hashMapToReservation(resvMap);
                resv.setLogin(persistentResv.getLogin());
                Long[] times = new Long[2];
                times[0] = persistentResv.getStartTime();
                times[1] = persistentResv.getEndTime();
                resvCache.put(gri, times);
                this.start(resv, persistentResv, login);
            } else if (dataMap.containsKey("confirm")) {
                this.confirm(persistentResv, login);
            } else if (dataMap.containsKey("complete")) {
                this.complete(persistentResv, login);
            } else if (dataMap.containsKey("fail")) {
                String code = dataMap.getString("errorCode");
                String msg = dataMap.getString("errorMsg");
                String src = dataMap.getString("errorSource");
                this.rollback(persistentResv, origState);
                eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FAILED, login,
                                      src, persistentResv, code, msg);
            } else if (dataMap.containsKey("statusCheck")) {
                String status = StateEngine.getStatus(persistentResv);
                int localStatus = StateEngine.getLocalStatus(persistentResv);
                if(status.equals(dataMap.getString("status")) &&
                    localStatus == dataMap.getInt("localStatus")){
                    String op = ((localStatus & 1) == 1 ?
                                 OSCARSEvent.RESV_MODIFY_COMPLETED :
                                 OSCARSEvent.RESV_MODIFY_CONFIRMED);
                    throw new BSSException("Modify reservation timed-out " +
                                           "while waiting for event " +  op);
                }
            } else {
                this.log.error("Unknown modifyReservation job cannot be executed");
            }
            bss.getTransaction().commit();
        } catch(Exception ex) {
            ex.printStackTrace();
            //Rollback any changes...
            bss.getTransaction().rollback();
            //...then start new transaction to update status
            try {
                bss = core.getBssSession();
                bss.beginTransaction();
                this.rollback(persistentResv, origState);
                eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FAILED, login,
                                      idcURL, persistentResv, "", ex.getMessage());
                bss.getTransaction().commit();
            } catch(Exception ex2) {
                bss.getTransaction().rollback();
                this.log.error(ex2);
            }
        } finally {
            this.runNextJob(context);
        }
        this.log.info("ModifyReservationJob.end");
    }

    /**
     * Reads in timeout properties
     */
     public void init() {
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("timeout", true);
        String defaultTimeoutStr = props.getProperty("default");
        String confirmTimeoutStr = props.getProperty("modifyResv.confirm");
        String completeTimeoutStr = props.getProperty("modifyResv.complete");
        int defaultTimeout = 0;
        if (defaultTimeoutStr != null) {
            try {
                defaultTimeout = Integer.parseInt(defaultTimeoutStr);
            } catch(Exception e) {
                this.log.error("Default timeout.default property invalid. " +
                               "Defaulting to another value for timeout.");
            }
        }
        if (confirmTimeoutStr != null) {
            try {
                CONFIRM_TIMEOUT = Integer.parseInt(confirmTimeoutStr);
            } catch(Exception e) {
                this.log.error("timeout.modifyResv.confirm property invalid." +
                               "Defaulting to another value for timeout.");
            }
        } else if(defaultTimeout > 0) {
            CONFIRM_TIMEOUT = defaultTimeout;
        }
        if (completeTimeoutStr != null) {
            try {
                COMPLETE_TIMEOUT = Integer.parseInt(completeTimeoutStr);
            } catch(Exception e) {
                this.log.error("timeout.modifyResv.complete property invalid." +
                               "Defaulting to another value for timeout.");
            }
        } else if(defaultTimeout > 0) {
            COMPLETE_TIMEOUT = defaultTimeout;
        }
     }

    /**
     * Processes an initial request
     *
     * @param resv reservation modified in place
     * @param persistentResv modified reservation saved in database
     * @param login string with user name of person modifying reservation
     */
    public void start(Reservation resv, Reservation persistentResv,
                      String login)
            throws Exception {
        ReservationManager rm = this.core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        Forwarder forwarder = this.core.getForwarder();
        ModifyResReply forwardReply = null;
        Exception error = null;
        eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_STARTED, login,
                               "JOB", persistentResv);
        rm.modify(resv, persistentResv);

        try {
            forwardReply = forwarder.modify(resv, persistentResv);
        } catch(Exception e) {
            error = e;
        } finally {
            forwarder.cleanUp();
            if (error != null) {
                throw error;
            }
        }
        //hold resources
        //TODO: Create mechanism for rolling back to original
        persistentResv = rm.finalizeModifyResv(resv);

        if(forwardReply == null){
            this.confirm(persistentResv, login);
        }else{
            this.scheduleStatusCheck(CONFIRM_TIMEOUT, persistentResv);
        }
    }

    /**
     * Confirms a modification
     *
     * @param resv modified reservation
     * @param login string with user name of person modifying reservation
     */
    public void confirm(Reservation resv, String login)
            throws BSSException, UnknownHostException {
        this.log.debug("confirm.start");
        EventProducer eventProducer = new EventProducer();
        String bssDbName = this.core.getBssDbName();
        Path path = resv.getPath(PathType.INTERDOMAIN);
        int localStatus = StateEngine.getLocalStatus(resv) + 1;
        this.se.updateLocalStatus(resv, localStatus);
        eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_CONFIRMED, login, "JOB", resv);

        /* Not guaranteed interdomain path so try finding src */
        String src = "";
        if(path.getLayer2Data() != null){
            src = path.getLayer2Data().getSrcEndpoint();
        }else if(path.getLayer3Data() != null){
            src = path.getLayer3Data().getSrcHost();
            src = InetAddress.getByName(src).getHostAddress();
            IpaddrDAO ipaddrDAO = new IpaddrDAO(bssDbName);
            Ipaddr ip = ipaddrDAO.queryByParam("IP", src);
            src = ip.getLink().getFQTI();
        }else{
            throw new BSSException("Cannot find src endpoint for reservation");
        }
        DomainDAO domainDAO = new DomainDAO(bssDbName);
        Hashtable<String, String> parseResults = URNParser.parseTopoIdent(src);
        String srcDomainId = parseResults.get("domainId");
        if (domainDAO.isLocal(srcDomainId)) {
            this.complete(resv, login);
        } else {
            this.scheduleStatusCheck(COMPLETE_TIMEOUT, resv);
        }
        this.log.debug("confirm.end");
    }

    /**
     * Completes a modification
     *
     * @param resv modified reservation
     * @param login string with user name of person modifying reservation
     */
    public void complete(Reservation resv, String login) throws BSSException {
        this.log.debug("complete.start");
        EventProducer eventProducer = new EventProducer();
        int localStatus =  StateEngine.getLocalStatus(resv);
        if (localStatus >= 2) {
            this.se.updateStatus(resv, StateEngine.ACTIVE);
        } else {
            this.se.updateStatus(resv, StateEngine.RESERVED);
        }
        this.se.updateLocalStatus(resv, 0);

        eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_COMPLETED, login,
                               "JOB", resv);
        this.log.debug("complete.end");
    }

    /**
     * Schedules a job to check if a request timed out
     *
     * @param timeout time in seconds after which a request times out
     * @param resv the reservation to be checked
     */
    public void scheduleStatusCheck(long timeout, Reservation resv) {
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        String triggerName = "modifyResvTimeoutTrig-" + resv.hashCode();
        String jobName = "modifyResvTimeoutJob-" + resv.hashCode();
        long time = System.currentTimeMillis() + timeout*1000;
        Date date = new Date(time);
        SimpleTrigger trigger = new SimpleTrigger(triggerName, null,
                                                  date, null, 0, 0L);
        JobDetail jobDetail = new JobDetail(jobName, "REQ_TIMEOUT",
                                            ModifyReservationJob.class);
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("statusCheck", true);
        dataMap.put("gri", resv.getGlobalReservationId());
        dataMap.put("status", StateEngine.getStatus(resv));
        dataMap.put("localStatus", StateEngine.getLocalStatus(resv));
        jobDetail.setJobDataMap(dataMap);
        try {
            this.log.debug("Adding job " + jobName);
            sched.scheduleJob(jobDetail, trigger);
            this.log.debug("Job added.");
        } catch(SchedulerException ex) {
            this.log.error("Scheduler exception: " + ex);
        }
     }

    /**
     * Rolls-back a reservation to its state prior to modify
     *
     * @param the reservation to rollback
     * @param origState the initial state prior to going INMODIFY
     */
    private void rollback(Reservation resv, String origState) throws BSSException{
        String gri = resv.getGlobalReservationId();
        if(resvCache.containsKey(gri)){
            Long[] times = resvCache.get(gri);
            resv.setStartTime(times[0]);
            resv.setEndTime(times[1]);
            resvCache.remove(gri);
        } else {
            this.log.info("Original times not found so keeping " +
                          "modifed times. This might cause errors.");
        }
        this.se.updateStatus(resv, origState);
        this.se.updateLocalStatus(resv, StateEngine.LOCAL_INIT);
    }




    /**
     * Converts HashMap to a Reservation Hibernate bean
     *
     * @param map a HashMap with parameters to initialize reservation
     * @return resv the converted Reservation
     */
    private static Reservation hashMapToReservation(HashMap<String, String[]> map){
        Reservation resv = new Reservation();
        if (map == null) {
            return resv;
        }
        resv.setStartTime(Long.parseLong(map.get("startSeconds")[0]));
        resv.setEndTime(Long.parseLong(map.get("endSeconds")[0]));
        resv.setCreatedTime(Long.parseLong(map.get("createSeconds")[0]));
        resv.setBandwidth(Long.parseLong(map.get("bandwidth")[0]));
        resv.setDescription(map.get("description")[0]);
        resv.setGlobalReservationId(map.get("gri")[0]);
        resv.setLogin(map.get("userLogin")[0]);

        //TODO: Any path parameters are basically ignored
        return resv;
    }

}
