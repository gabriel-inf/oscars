package net.es.oscars.nsibridge.state.prov;

import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.types.ProvisionStateEnumType;
import net.es.oscars.nsibridge.ifces.SM_State;


public class NSI_Prov_State implements SM_State {

    private ProvisionStateEnumType enumType;
    public String value() {
        return enumType.value();

    }
    public void setValue(String value) {
        enumType = ProvisionStateEnumType.fromValue(value);

    }
    public Object state() {
        return enumType;
    }
    public void setState(Object state) {
        if (state instanceof ProvisionStateEnumType) {
            enumType = (ProvisionStateEnumType) state;
        }
    }

}
