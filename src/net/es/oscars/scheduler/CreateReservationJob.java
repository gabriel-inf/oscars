package net.es.oscars.scheduler;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;
import net.es.oscars.bss.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.*;
import net.es.oscars.pss.*;

public class CreateReservationJob extends ChainingJob implements org.quartz.Job {
    private Logger log;
    private OSCARSCore core;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobName = context.getJobDetail().getFullName();
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("CreateReservationJob.start name:"+jobName);
        this.core = OSCARSCore.getInstance();

        Forwarder forwarder = core.getForwarder();
        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();

        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Reservation resv = (Reservation) dataMap.get("reservation");
        PathInfo pathInfo = (PathInfo) dataMap.get("pathInfo");
        String login = (String) dataMap.get("login");

        String gri;
        if (resv != null) {
            gri = (String) resv.getGlobalReservationId();
        } else {
            this.log.error("No reservation associated with job name:"+jobName);
            this.runNextJob(context);
            return;
        }
        this.log.debug("GRI is: "+gri+"for job name: "+jobName);

        try {
            StateEngine.canUpdateStatus(resv, StateEngine.RESERVED);
        } catch (BSSException ex) {
            this.log.error(ex);
            this.runNextJob(context);
            return;
        }

        Session bss = core.getBssSession();
        bss.beginTransaction();
        boolean wasReserved = true;
        String errMessage = null;

        try {
            rm.create(resv, pathInfo);

            TypeConverter tc = core.getTypeConverter();
            tc.ensureLocalIds(pathInfo);

            // FIXME: why does this sometimes get unset?
            pathInfo.getPath().setId("unimplemented");

            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response

            // **** IMPORTANT ****
            // FIXME: this bit right here makes CreateReservationJobs kinda slow
            CreateReply forwardReply = forwarder.create(resv, pathInfo);
            rm.finalizeResv(forwardReply, resv, pathInfo);
            rm.store(resv);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_COMPLETED, login, "JOB", resv);
        } catch (BSSException ex) {
            this.log.error(ex);
            errMessage = ex.getMessage();
            wasReserved = false;
        } catch (InterdomainException ex) {
            this.log.error(ex);
            errMessage = ex.getMessage();
            wasReserved = false;
        } catch (Exception ex) {
            this.log.error(ex);
            errMessage = ex.getMessage();
            wasReserved = false;
        } finally {
            forwarder.cleanUp();
            if (errMessage != null) {
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login, "JOB", resv, "", errMessage);
                this.log.debug("createReservation failed: " + errMessage);
            }
        }

        String status;
        StateEngine stateEngine = new StateEngine();
        try {
            status = StateEngine.getStatus(resv);
            this.log.debug("Reservation status was: "+status);
            if (wasReserved) {
                status = stateEngine.updateStatus(resv, StateEngine.RESERVED);
            } else {
                status = stateEngine.updateStatus(resv, StateEngine.FAILED);
            }
            this.log.debug("Reservation status now is: "+status);
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
        }
        // just in case this is an immediate reservation, check pending & add setup actions
        
        PSSScheduler sched = new PSSScheduler(core.getBssDbName());
        sched.pendingReservations(0);

        bss.getTransaction().commit();
        this.runNextJob(context);
        this.log.debug("CreateReservationJob.end name:"+jobName);
    }
}
