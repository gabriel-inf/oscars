package net.es.oscars.nsibridge.util;

import net.es.oscars.nsibridge.soap.gen.ifce.ReservationRequestType;

public class RequestPrinter {
    public static String printResvReq(ReservationRequestType rrt) {
        String out = "";
        out += "\ncorrId: " + rrt.getCorrelationId();
        out += "\nreplyTo: " + rrt.getReplyTo();
        
        out += "\n  provNSA: "+rrt.getReservation().getProviderNSA();
        out += "\n  reqNSA: "+rrt.getReservation().getRequesterNSA();
        out += "\n    connId: "+rrt.getReservation().getReservation().getConnectionId();
        out += "\n    desc: "+rrt.getReservation().getReservation().getDescription();
        out += "\n    gri: "+rrt.getReservation().getReservation().getGlobalReservationId();
        out += "\n      dir: "+rrt.getReservation().getReservation().getPath().getDirectionality();
        out += "\n      src: "+rrt.getReservation().getReservation().getPath().getSourceSTP().getStpId();
        out += "\n      dst: "+rrt.getReservation().getReservation().getPath().getDestSTP().getStpId();
        out += "\n      bw: "+rrt.getReservation().getReservation().getServiceParameters().getBandwidth().getDesired();
        out += "\n      start: "+rrt.getReservation().getReservation().getServiceParameters().getSchedule().getStartTime();
        out += "\n      end: "+rrt.getReservation().getReservation().getServiceParameters().getSchedule().getEndTime();
        
        return out;
    }
}
