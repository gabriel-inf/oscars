package net.es.oscars.notify;

import java.util.*;          


public class NotifierSource extends Observable {

    public void eventOccured(Object messageInfo) {
        setChanged();
        notifyObservers(messageInfo);
    }
}
