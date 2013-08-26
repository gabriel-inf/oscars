package net.es.oscars.nsibridge.beans;


import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;

import java.util.HashSet;
import java.util.UUID;

public class GenericRequest {
    protected CommonHeaderType inHeader;
    protected CommonHeaderType outHeader;
    protected HashSet<UUID> taskIds = new HashSet<UUID>();



    public GenericRequest() {

    }

    public CommonHeaderType getInHeader() {
        return inHeader;
    }

    public void setInHeader(CommonHeaderType inHeader) {
        this.inHeader = inHeader;
    }

    public CommonHeaderType getOutHeader() {
        return outHeader;
    }

    public void setOutHeader(CommonHeaderType outHeader) {
        this.outHeader = outHeader;
    }
    public HashSet<UUID> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(HashSet<UUID> taskIds) {
        this.taskIds = taskIds;
    }
}
