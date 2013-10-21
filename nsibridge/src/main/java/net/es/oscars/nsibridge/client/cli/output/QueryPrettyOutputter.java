package net.es.oscars.nsibridge.client.cli.output;

import java.util.List;

import javax.xml.bind.JAXBElement;

import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.ConnectionStatesType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.DataPlaneStatusType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QueryRecursiveResultCriteriaType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QueryRecursiveResultType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryResultCriteriaType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryResultType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.ScheduleType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.framework.types.TypeValuePairType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.point2point.EthernetVlanType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.types.OrderedStpType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.services.types.StpType;

/**
 * Outputs results using a human-readable text format
 *
 */
public class QueryPrettyOutputter implements QueryOutputter{

    public void outputSummary(List<QuerySummaryResultType> results) {
        for(QuerySummaryResultType querySummRes : results){
            System.out.println();
            System.out.println("Connection ID: " + querySummRes.getConnectionId());
            System.out.println("Global Reservation ID: " + querySummRes.getGlobalReservationId());
            System.out.println("Requester NSA: " + querySummRes.getRequesterNSA());
            System.out.println("Description: " + querySummRes.getDescription());
            if(querySummRes.getConnectionStates() != null){
                this.outputConnectionStates(querySummRes.getConnectionStates());
            }
            if(querySummRes.getCriteria() != null && !querySummRes.getCriteria().isEmpty()){
                System.out.println("Criteria: ");
                int i = 0;
                for(QuerySummaryResultCriteriaType crit : querySummRes.getCriteria()){
                    if(i > 0){
                        System.out.println("------");
                    }
                    System.out.println("\tVersion: " + crit.getVersion());
                    System.out.println("\tService Type: " + crit.getServiceType());
                    this.outputSchedule(crit.getSchedule());
                    this.outputCritAny(crit.getAny());
                    i++;
                }
            }
            System.out.println();
        }
        System.out.println(results.size() + " results returned");
    }
    

    @Override
    public void outputRecursive(List<QueryRecursiveResultType> results) {
        for(QueryRecursiveResultType queryRecRes : results){
            System.out.println();
            System.out.println("Connection: " + queryRecRes.getConnectionId());
            System.out.println("Global Reservation Id: " + queryRecRes.getGlobalReservationId());
            System.out.println("Requester NSA: " + queryRecRes.getRequesterNSA());
            System.out.println("Description: " + queryRecRes.getDescription());
            if(queryRecRes.getConnectionStates() != null){
                this.outputConnectionStates(queryRecRes.getConnectionStates());
            }
            if(queryRecRes.getCriteria() != null && !queryRecRes.getCriteria().isEmpty()){
                System.out.println("Criteria: ");
                int i = 0;
                for(QueryRecursiveResultCriteriaType crit : queryRecRes.getCriteria()){
                    if(i > 0){
                        System.out.println("------");
                    }
                    System.out.println("\tVersion: " + crit.getVersion());
                    System.out.println("\tService Type: " + crit.getServiceType());
                    this.outputSchedule(crit.getSchedule());
                    this.outputCritAny(crit.getAny());
                    i++;
                }
            }
            System.out.println();
        }
        System.out.println(results.size() + " results returned");
    }

    @Override
    public void outputFailed(ServiceExceptionType serviceException) {
        System.out.println("Query Error:");
        System.out.println("\tConnection ID: " + serviceException.getConnectionId());
        System.out.println("\tNSA ID: " + serviceException.getNsaId());
        System.out.println("\tService Type: " + serviceException.getServiceType());
        System.out.println("\tError ID: " + serviceException.getErrorId());
        System.out.println("\tError Message: " + serviceException.getText());
        if(serviceException.getVariables() != null && 
                serviceException.getVariables().getVariable() != null &&
                !serviceException.getVariables().getVariable().isEmpty()){
            System.out.println("\tVariables:");
            for(TypeValuePairType variable : serviceException.getVariables().getVariable()){
                System.out.println("\t\tType:" + variable.getType());
                if(variable.getValue() != null){
                    for(String val : variable.getValue()){
                        System.out.println("\t\tValue:" + val);
                    }
                }
            }
        }
    }

    private void outputCritAny(List<Object> any) {
        EthernetVlanType evts = null;
        for (Object o : any) {
            if (o instanceof EthernetVlanType ) {
                evts = (EthernetVlanType) o;
            } else {
                try {
                    JAXBElement<EthernetVlanType> payload = (JAXBElement<EthernetVlanType>) o;
                    evts = payload.getValue();
                } catch (ClassCastException ex) {
                    evts = null;
                }

            }
        }
        if(evts != null){
            System.out.println("\tCapacity: " + evts.getCapacity());
            if(evts.getSourceSTP() != null){
                System.out.println("\tSource STP:");
                outputSTP(evts.getSourceSTP());
            }
            if(evts.getSourceSTP() != null){
                System.out.println("\tDestination STP:");
                outputSTP(evts.getDestSTP());
            }
            System.out.println("\tSource VLAN: " + evts.getSourceVLAN());
            System.out.println("\tDestination VLAN: " + evts.getDestVLAN());
            if(evts.getDirectionality() != null){
                System.out.println("\tDirectionality: " + evts.getDirectionality().value());
            }
            if(evts.getEro() != null && evts.getEro().getOrderedSTP() != null &&
                    !evts.getEro().getOrderedSTP().isEmpty()){
                System.out.println("\tERO:");
                for(OrderedStpType hop: evts.getEro().getOrderedSTP()){
                    System.out.println("\t\tOrder:" + hop.getOrder());
                    outputSTP(hop.getStp());
                    System.out.println();
                }
            }
            System.out.println("\tBurst Size: " + evts.getBurstsize());
            System.out.println("\tMTU: " + evts.getMtu());
            System.out.println("\tSymmetric Path: " + evts.isSymmetricPath());
        }
        
    }

    private void outputSchedule(ScheduleType schedule) {
        if(schedule != null){
            System.out.println("\tSchedule: ");
            System.out.println("\t\tStart Time: " + schedule.getStartTime());
            System.out.println("\t\tEnd Time: " + schedule.getEndTime());
        }
        
    }

    private void outputConnectionStates(ConnectionStatesType connectionStates) {
        if(connectionStates.getLifecycleState() != null){
            System.out.println("Life Cycle State: " + connectionStates.getLifecycleState().value());
        }
        if(connectionStates.getReservationState() != null){
            System.out.println("Reservations State: " + connectionStates.getReservationState().value());
        }
        if(connectionStates.getProvisionState() != null){
            System.out.println("Provision State: " + connectionStates.getProvisionState().value());
        }
        if(connectionStates.getDataPlaneStatus() != null){
            System.out.println("Dataplane Status: ");
            DataPlaneStatusType dpStatus = connectionStates.getDataPlaneStatus();
            System.out.println("\tVersion: " + dpStatus.getVersion());
            System.out.println("\tActive: " + dpStatus.isVersionConsistent());
            System.out.println("\tVersion Consistent: " + dpStatus.isActive());
        }
        
    }
    
    private void outputSTP(StpType sourceSTP) {
        System.out.println("\t\tNetwork ID:" + sourceSTP.getNetworkId());
        System.out.println("\t\tLocal ID:" + sourceSTP.getLocalId());
        if(sourceSTP.getLabels() != null && 
                sourceSTP.getLabels().getAttribute() != null && 
                !sourceSTP.getLabels().getAttribute().isEmpty()){
            System.out.println("\t\tLabels:");
            for(TypeValuePairType attr : sourceSTP.getLabels().getAttribute()){
                System.out.println("\t\t\tType: " + attr.getType());
                if(attr.getValue() != null && !attr.getValue().isEmpty()){
                    for(String val : attr.getValue()){
                        System.out.println("\t\t\tValue: " + val);
                    }
                }
            }
        }

    }
}
