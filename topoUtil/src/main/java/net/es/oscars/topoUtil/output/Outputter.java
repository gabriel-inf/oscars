package net.es.oscars.topoUtil.output;

import freemarker.template.TemplateException;
import net.es.oscars.topoUtil.beans.Network;

import java.io.IOException;

public interface Outputter {
    public void output(Network network) throws IOException, TemplateException;


}
