package net.es.oscars.pss.common;

import java.util.List;

import org.quartz.Scheduler;

import net.es.oscars.bss.Reservation;
import net.es.oscars.pss.PSSException;

public interface PSSQueuer {
    public PSSActionStatus getActionStatus(String gri, PSSDirection direction, PSSAction action) throws PSSException;
    public void startAction(String gri, List<PSSDirection> directions, PSSAction action) throws PSSException;
    public void completeAction(String gri, PSSDirection direction, PSSAction action, boolean success, String error) throws PSSException;
    public void scheduleAction(Reservation resv, PSSDirection direction, PSSAction action, PSSHandler handler) throws PSSException;
    
    public void setScheduler(Scheduler scheduler);
    public Scheduler getScheduler();
    
}
