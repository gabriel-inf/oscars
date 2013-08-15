package net.es.oscars.nsibridge.beans;

import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReserveType;

public class ResvRequest extends GenericRequest {

    protected ReserveType reserveType;

    public ReserveType getReserveType() {
        return reserveType;
    }

    public void setReserveType(ReserveType reserveType) {
        this.reserveType = reserveType;
    }
}
