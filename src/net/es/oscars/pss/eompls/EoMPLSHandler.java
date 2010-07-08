package net.es.oscars.pss.eompls;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;

public interface EoMPLSHandler {
    public void setup(Reservation resv, Path localPath, EoMPLSDirection direction);
    public void teardown(Reservation resv, Path localPath, EoMPLSDirection direction);
    public void status(Reservation resv, Path localPath, EoMPLSDirection direction);

    public boolean isLogConfig();
    public void setLogConfig(boolean logConfig);

    public boolean isStubMode();
    public void setStubMode(boolean stubMode);

    public boolean isTeardownOnFailure();

    public void setTeardownOnFailure(boolean teardownOnFailure);

    public boolean isCheckStatusAfterSetup();

    public void setCheckStatusAfterSetup(boolean checkStatusAfterSetup);

    public boolean isCheckStatusAfterTeardown();

    public void setCheckStatusAfterTeardown(boolean checkStatusAfterTeardown);
}
