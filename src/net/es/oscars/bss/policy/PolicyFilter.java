package net.es.oscars.bss.policy;

import java.util.List;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.wsdlTypes.PathInfo;

public interface PolicyFilter {
	void applyFilter(PathInfo pathInfo, 
						List<Link> localLinks,
						Reservation newReservation, 
						List<Reservation> activeReservations) throws BSSException; 
}
