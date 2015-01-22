package net.es.oscars.nsibridge.state.life;

import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.types.LifecycleStateEnumType;
import net.es.oscars.nsibridge.ifces.SM_State;

public class NSI_Life_State implements SM_State {
    private LifecycleStateEnumType enumType;
    public String value() {
        return enumType.value();

    }
    public void setValue(String value) {
        enumType = LifecycleStateEnumType.fromValue(value);

    }
    public Object state() {
        return enumType;
    }
    public void setState(Object state) {
        if (state instanceof LifecycleStateEnumType) {
            enumType = (LifecycleStateEnumType) state;
        }
    }

}
