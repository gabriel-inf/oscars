package net.es.oscars.bss;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;

/**
 * This class tests methods in ParamValidator.java.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss" })
public class ParamValidatorTest {
    private Properties props;
    private SessionFactory sf;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
    }

  @Test
    public void testNullPathValidation() {
        Reservation resv = new Reservation();
        CommonReservation common = new CommonReservation();
        common.setParameters(resv, "test of validation");
        ParamValidator validator = new ParamValidator();
        StringBuilder msg = validator.validate(resv, null);
        assert msg.length() == 0;
    }
}
