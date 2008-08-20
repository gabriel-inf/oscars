package net.es.oscars.bss.policy;

import java.util.List;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.wsdlTypes.PathInfo;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;

public interface PolicyFilter {
	void applyFilter(PathInfo pathInfo,
	                    CtrlPlaneHopContent[] hops,
						List<Link> localLinks,
						Reservation newReservation, 
						List<Reservation> activeReservations) throws BSSException; 
}
