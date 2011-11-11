package net.es.oscars.nsibridge.util;

import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;

public class RequestPrinter {
    public static String printResvReq(ReserveRequestType rrt) {
        String out = "";
        out += "\ncorrId: " + rrt.getCorrelationId();
        out += "\nreplyTo: " + rrt.getReplyTo();
        
        out += "\n  provNSA: "+rrt.getReserve().getProviderNSA();
        out += "\n  reqNSA: "+rrt.getReserve().getRequesterNSA();
        out += "\n    connId: "+rrt.getReserve().getReservation().getConnectionId();
        out += "\n    desc: "+rrt.getReserve().getReservation().getDescription();
        out += "\n    gri: "+rrt.getReserve().getReservation().getGlobalReservationId();
        out += "\n      dir: "+rrt.getReserve().getReservation().getPath().getDirectionality();
        out += "\n      src: "+rrt.getReserve().getReservation().getPath().getSourceSTP().getStpId();
        out += "\n      dst: "+rrt.getReserve().getReservation().getPath().getDestSTP().getStpId();
        out += "\n      bw: "+rrt.getReserve().getReservation().getServiceParameters().getBandwidth().getDesired();
        out += "\n      start: "+rrt.getReserve().getReservation().getServiceParameters().getSchedule().getStartTime();
        out += "\n      end: "+rrt.getReserve().getReservation().getServiceParameters().getSchedule().getEndTime();
        
        return out;
    }
}
