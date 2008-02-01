package net.es.oscars.notify;

import java.util.Observable;          


public class NotifierSource extends Observable {

	// TODO: what should the argument be?
    public void eventOccured(String[] message) {
    	setChanged();
        notifyObservers(message);
    }

}
