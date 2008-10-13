package net.es.oscars.pathfinder.perfsonar.util;

import net.es.oscars.bss.topology.Domain;

public interface DomainFinder {
    Domain lookupDomain(String id);
}
