package net.es.oscars.pss.api;

import net.es.oscars.pss.beans.PSSAction;

public interface PostCommitConfigGen {
    public String getPostCommitConfig(PSSAction action, String deviceId);
}
