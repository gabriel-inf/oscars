package net.es.oscars.bss.events;

import java.util.*;          


public class ObserverSource extends Observable {

    public void eventOccured(Object messageInfo) {
        setChanged();
        notifyObservers(messageInfo);
    }
}
