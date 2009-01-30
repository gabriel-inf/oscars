package net.es.oscars.bss.events;

import java.util.*;          


public class NotifierSource extends Observable {

    public void eventOccured(Object messageInfo) {
        setChanged();
        notifyObservers(messageInfo);
    }
}
