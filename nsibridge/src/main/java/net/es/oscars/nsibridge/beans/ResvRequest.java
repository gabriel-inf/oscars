package net.es.oscars.nsibridge.beans;

import net.es.oscars.nsibridge.oscars.OscarsOps;
import net.es.oscars.nsi.soap.gen.nsi_2_0_r117.connection.types.ReserveType;

public class ResvRequest extends GenericRequest {

    protected ReserveType reserveType;
    protected OscarsOps oscarsOp;

    public ReserveType getReserveType() {
        return reserveType;
    }

    public void setReserveType(ReserveType reserveType) {
        this.reserveType = reserveType;
    }

    public OscarsOps getOscarsOp() {
        return oscarsOp;
    }

    public void setOscarsOp(OscarsOps oscarsOp) {
        this.oscarsOp = oscarsOp;
    }


}
