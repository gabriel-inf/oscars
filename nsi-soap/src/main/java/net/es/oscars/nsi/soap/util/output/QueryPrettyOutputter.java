package net.es.oscars.nsi.soap.util.output;

import java.util.List;

import javax.xml.bind.JAXBElement;

import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.ConnectionStatesType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.DataPlaneStatusType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.QueryRecursiveResultCriteriaType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.QueryRecursiveResultType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.QuerySummaryResultCriteriaType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.QuerySummaryResultType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.ScheduleType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.types.ServiceExceptionType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.framework.types.TypeValuePairType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.services.point2point.P2PServiceBaseType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.services.types.OrderedStpType;

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
        P2PServiceBaseType p2p = null;
        for (Object o : any) {
            if (o instanceof P2PServiceBaseType ) {
                p2p = (P2PServiceBaseType) o;
            } else {
                try {
                    JAXBElement<P2PServiceBaseType> payload = (JAXBElement<P2PServiceBaseType>) o;
                    p2p = payload.getValue();
                } catch (ClassCastException ex) {
                    p2p = null;
                }

            }
        }

        if(p2p != null){
            System.out.println("\tCapacity: " + p2p.getCapacity());
            if(p2p.getSourceSTP() != null){
                System.out.println("\tSource STP: " + p2p.getSourceSTP());
            }
            if(p2p.getSourceSTP() != null){
                System.out.println("\tDestination STP: " + p2p.getDestSTP());
            }
            if(p2p.getDirectionality() != null){
                System.out.println("\tDirectionality: " + p2p.getDirectionality().value());
            }
            if(p2p.getEro() != null && p2p.getEro().getOrderedSTP() != null &&
                    !p2p.getEro().getOrderedSTP().isEmpty()){
                System.out.println("\tERO:");
                for(OrderedStpType hop: p2p.getEro().getOrderedSTP()){
                    System.out.println("\t\tOrder:" + hop.getOrder());
                    System.out.println("STP: "+hop.getStp());
                    System.out.println();
                }
            }
            System.out.println("\tSymmetric Path: " + p2p.isSymmetricPath());
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


}
