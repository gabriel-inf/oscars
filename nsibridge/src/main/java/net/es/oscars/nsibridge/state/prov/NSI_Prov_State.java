package net.es.oscars.nsibridge.state.prov;

import net.es.oscars.nsibridge.ifces.SM_State;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.ProvisionStateEnumType;


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
