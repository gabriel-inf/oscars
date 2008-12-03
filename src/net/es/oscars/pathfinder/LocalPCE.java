package net.es.oscars.pathfinder;

import java.util.List;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.Reservation;


public interface LocalPCE {
	List<Path> findLocalPath(Reservation resv);
}
