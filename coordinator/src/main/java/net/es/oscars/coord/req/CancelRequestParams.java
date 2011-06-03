package net.es.oscars.coord.req;

import net.es.oscars.api.soap.gen.v06.GlobalReservationId;
import net.es.oscars.authZ.soap.gen.CheckAccessReply;

public class CancelRequestParams {
    private CheckAccessReply authConds= null;
    private GlobalReservationId gri = null;
    
    public CancelRequestParams (CheckAccessReply authConditions,GlobalReservationId gri) {
        this.authConds = authConditions;
        this.gri = gri;
    }
    
    public GlobalReservationId  getGRI() {
        return this.gri;
    }
    
    public CheckAccessReply getAuthConds() {
        return this.authConds;
    }
}