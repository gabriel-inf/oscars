package net.es.oscars.scheduler;
import net.es.oscars.pss.vendor.VendorStatusInput;
import net.es.oscars.ws.OSCARSCore;

import org.apache.log4j.Logger;
import org.quartz.*;

import java.util.*;

public class VendorMaintainStatusJob implements Job {
    private Logger log;
    private OSCARSCore core;

    public static HashMap<String, HashMap<String, String>> checklist = new HashMap<String, HashMap<String, String>>();
    public static synchronized void addToCheckList(String gri, HashMap<String, String> params) {
        HashMap<String, String> tmpParams = checklist.get(gri);
        if (tmpParams != null) {
            params.putAll(tmpParams);
        }
        checklist.put(gri, params);
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();


        HashMap<String, HashMap<String,VendorStatusInput>> vlansPerNode =
            new HashMap<String, HashMap<String,VendorStatusInput>>();
        HashMap<String, String> nodeVendor = new HashMap<String, String>();

        ArrayList<String> checkedGris = new ArrayList<String>();

        Iterator<String> griIt = checklist.keySet().iterator();
        while (griIt.hasNext()) {
            String gri = griIt.next();
            this.log.debug("examining gri :"+gri);

            HashMap<String, String> params = checklist.get(gri);
            String ingressVlan 		= params.get("ingressVlan");
            String egressVlan 		= params.get("egressVlan");
            String ingressNodeId	= params.get("ingressNodeId");
            String egressNodeId 	= params.get("egressNodeId");
            String ingressVendor 	= params.get("ingressVendor");
            String egressVendor 	= params.get("egressVendor");
            String description          = params.get("description");
            String operation = params.get("operation");
            String desiredStatus = params.get("desiredStatus");

            if (ingressNodeId != null && egressNodeId != null) {
                nodeVendor.put(ingressNodeId, ingressVendor);
                nodeVendor.put(egressNodeId, egressVendor);

                checkedGris.add(gri);
                HashMap<String,VendorStatusInput> statusInputs =
                    vlansPerNode.get(ingressNodeId);
                if (statusInputs == null) {
                    statusInputs = new HashMap<String,VendorStatusInput>();
                }
                VendorStatusInput statusInput = new VendorStatusInput();
                statusInput.setGri(gri);
                statusInput.setDescription(description);
                statusInput.setOperation(operation);
                statusInput.setDesiredStatus(desiredStatus);
                statusInput.setDirection("FORWARD");
                statusInputs.put(ingressVlan, statusInput);
                vlansPerNode.put(ingressNodeId, statusInputs);

                statusInputs = vlansPerNode.get(egressNodeId);
                if (statusInputs == null) {
                    statusInputs = new HashMap<String,VendorStatusInput>();
                }
                statusInput = new VendorStatusInput();
                statusInput.setGri(gri);
                statusInput.setDescription(description);
                statusInput.setOperation(operation);
                statusInput.setDesiredStatus(desiredStatus);
                statusInput.setDirection("REVERSE");
                statusInputs.put(egressVlan, statusInput);
                vlansPerNode.put(egressNodeId, statusInputs);
            }
        }

        Iterator<String> nodeIt = vlansPerNode.keySet().iterator();
        Scheduler sched = this.core.getScheduleManager().getScheduler();
        while (nodeIt.hasNext()) {
            String nodeId = nodeIt.next();
            Map<String,VendorStatusInput> statusInputs = vlansPerNode.get(nodeId);

            String jobName = "checkStatus-"+nodeId+statusInputs.hashCode();
            JobDetail jobDetail = new JobDetail(jobName, "STATUS", VendorCheckStatusJob.class);
            JobDataMap jobDataMap = new JobDataMap();
            this.log.debug("Adding job "+jobDetail.getFullName());
            // give the full checklist out so that it knows which GRI matches what vlan and where
            jobDataMap.put("nodeId", nodeId);
            jobDataMap.put("vendor", nodeVendor.get(nodeId));
            jobDataMap.put("statusInputs", statusInputs);
            jobDetail.setJobDataMap(jobDataMap);

            String triggerId = "checkStatus-"+nodeId+statusInputs.hashCode();
            Trigger trigger = new SimpleTrigger(triggerId, "STATUS", new Date());
            try {
                sched.scheduleJob(jobDetail, trigger);
            } catch (SchedulerException ex) {
                this.log.error(ex);
            }
        }
        for (String gri : checkedGris) {
            checklist.remove(gri);
        }
    }
}
