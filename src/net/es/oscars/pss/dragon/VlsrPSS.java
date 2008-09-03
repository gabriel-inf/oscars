package net.es.oscars.pss.dragon;

import java.util.Properties;
import net.es.oscars.PropHandler;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.scheduler.VlsrPSSJob;
import net.es.oscars.oscars.OSCARSCore;
import net.es.oscars.pss.*;

public class VlsrPSS implements PSS {
    private Logger log;
    private OSCARSCore core;
    private Properties props;
    /** Constructor */
    public VlsrPSS(){
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("pss.dragon", true);
    }

    /**
     * Schedules LSP creation.
     *
     * @param resv the reservation whose path will be created
     * @throws PSSException
     */
    public String createPath(Reservation resv) throws PSSException{
        this.log.info("vlsrpss.create.start");
        Path path = resv.getPath();
        Link ingressLink = path.getPathElem().getLink();
        String queueName = this.generateQueueName(ingressLink);
        
        try {
            String gri = resv.getGlobalReservationId();
            Scheduler sched = this.core.getScheduleManager().getScheduler();
            String jobName = "pathsetup-"+gri;
            JobDetail jobDetail = new JobDetail(jobName, queueName, VlsrPSSJob.class);
            this.log.debug("Adding job "+jobName);
            jobDetail.setDurability(true);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("gri", gri);
            jobDataMap.put("task", "create");
            jobDetail.setJobDataMap(jobDataMap);
            sched.addJob(jobDetail, false);
        }catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }

        this.log.info("vlsrpss.create.end");

        return resv.getStatus();
    }

    /**
     * Schedules path refresh
     *
     * @param resv the reservation whose path will be refreshed
     * @throws PSSException
     */
    public String refreshPath(Reservation resv) throws PSSException{
        this.log.info("vlsrpss.refresh.start");
        this.log.info("vlsrpss.teardown.start");
        Path path = resv.getPath();
        Link ingressLink = path.getPathElem().getLink();
        String queueName = this.generateQueueName(ingressLink);
        try {
            String gri = resv.getGlobalReservationId();
            Scheduler sched = this.core.getScheduleManager().getScheduler();
            String jobName = "refresh-"+gri;
            JobDetail jobDetail = new JobDetail(jobName, queueName, VlsrPSSJob.class);
            this.log.debug("Adding job "+jobName);
            jobDetail.setDurability(true);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("gri", gri);
            jobDataMap.put("task", "refresh");
            jobDetail.setJobDataMap(jobDataMap);
            sched.addJob(jobDetail, false);
        }catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }
        this.log.info("vlsrpss.refresh.end");

        return resv.getStatus();
    }

    /**
     * Schedules LSP teardown.
     *
     * @param resv the reservation whose path will be removed
     * @throws PSSException
     */
    public String teardownPath(Reservation resv, String newStatus) throws PSSException{
        this.log.info("vlsrpss.teardown.start");
        Path path = resv.getPath();
        Link ingressLink = path.getPathElem().getLink();
        String queueName = this.generateQueueName(ingressLink);
        try {
            String gri = resv.getGlobalReservationId();
            Scheduler sched = this.core.getScheduleManager().getScheduler();
            String jobName = "teardown-"+gri;
            JobDetail jobDetail = new JobDetail(jobName, queueName, VlsrPSSJob.class);
            this.log.debug("Adding job "+jobName);
            jobDetail.setDurability(true);
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("gri", gri);
            jobDataMap.put("task", "teardown");
            jobDetail.setJobDataMap(jobDataMap);
            sched.addJob(jobDetail, false);
        }catch (SchedulerException ex) {
            this.log.error("Scheduler exception", ex);
        }
        this.log.info("vlsrpss.teardown.end");

        return resv.getStatus();
    }
    
    /**
     * Generates the queue name given the ingressLink
     *
     * @param ingressLink the ingress link
     * @return the queue name
     * @throws PSSException
     */
    private String generateQueueName(Link ingressLink) throws PSSException{
        VlsrPSSJob job = new VlsrPSSJob();
        String sshPortForwardStr = this.props.getProperty("ssh.portForward");
        boolean sshPortForward = (sshPortForwardStr != null && sshPortForwardStr.equals("1"));
        String queueName = "SERIALIZE_";
        
        if(sshPortForward){
            queueName += job.findSshAddress(ingressLink);
        }else{
            queueName += job.findTelnetAddress(ingressLink);
        }
        
        return queueName;
    }
}
