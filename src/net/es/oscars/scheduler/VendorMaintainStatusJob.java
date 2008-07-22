package net.es.oscars.scheduler;
import net.es.oscars.oscars.OSCARSCore;

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
        this.log.debug("VendorMaintainStatusJob.start name:"+context.getJobDetail().getFullName());


        HashMap<String, ArrayList<String>> vlansPerNode = new HashMap<String, ArrayList<String>>();
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

            /*
            Iterator<String> keyIt = params.keySet().iterator();
            while (keyIt.hasNext()) {
                String key = keyIt.next();
                this.log.debug("key: "+key+" val:"+params.get(key));
            }
            */

            if (ingressNodeId != null && egressNodeId != null) {

                nodeVendor.put(ingressNodeId, ingressVendor);
                nodeVendor.put(egressNodeId, egressVendor);

                checkedGris.add(gri);
                ArrayList<String> vlanList = vlansPerNode.get(ingressNodeId);
                if (vlanList == null) {
                    vlanList = new ArrayList<String>();
                }
                vlanList.add(ingressVlan);
                vlansPerNode.put(ingressNodeId, vlanList);

                vlanList = vlansPerNode.get(egressNodeId);
                if (vlanList == null) {
                    vlanList = new ArrayList<String>();
                }
                vlanList.add(egressVlan);
                vlansPerNode.put(egressNodeId, vlanList);
            }
        }

        Iterator<String> nodeIt = vlansPerNode.keySet().iterator();

        Scheduler sched = this.core.getScheduleManager().getScheduler();

        while (nodeIt.hasNext()) {
            String nodeId = nodeIt.next();
            ArrayList<String> vlanList = vlansPerNode.get(nodeId);

            String jobName = "checkStatus-"+nodeId+vlanList.hashCode();
            JobDetail jobDetail = new JobDetail(jobName, "STATUS", VendorCheckStatusJob.class);
            JobDataMap jobDataMap = new JobDataMap();

            this.log.debug("Adding job "+jobDetail.getFullName());

            // give the full checklist out so that it knows which GRI matches what vlan and where
            jobDataMap.put("nodeId", nodeId);
            jobDataMap.put("vendor", nodeVendor.get(nodeId));
            jobDataMap.put("vlanList", vlanList);
            jobDataMap.put("checklist", checklist);
            jobDetail.setJobDataMap(jobDataMap);

            String triggerId = "checkStatus-"+nodeId+vlanList.hashCode();
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

        this.log.debug("VendorMaintainStatusJob.end name:"+context.getJobDetail().getFullName());

    }

}
