package net.es.oscars.bss.events;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


import net.es.oscars.PropHandler;

/**
 * 
 * @author haniotak
 * Example notify observer class
 *
 */
public class FileWriterObserver implements Observer {

    public FileWriterObserver() {
    }

    public void update (Observable obj, Object arg) {
        BufferedWriter out = null;
        try {
            // obviously do different stuff here
            out = new BufferedWriter( new FileWriter( "/tmp/fileToWrite.txt"));
            out.write(arg.toString());
            out.flush();

        } catch (IOException e) {
            // do something
        }
    }
}
