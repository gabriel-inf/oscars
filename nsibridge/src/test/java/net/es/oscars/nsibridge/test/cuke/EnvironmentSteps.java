package net.es.oscars.nsibridge.test.cuke;


import cucumber.api.java.en.Given;
import net.es.oscars.nsibridge.config.SpringContext;
import org.springframework.context.ApplicationContext;

public class EnvironmentSteps {
    @Given("^I have set up the environment$")
    public void I_have_set_up_the_environment() throws Throwable {
        System.out.print("Initializing Spring... ");
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.initContext("config/beans.xml");
    }

}
