package net.es.oscars.nsibridge.test.cuke;
import cucumber.api.junit.Cucumber;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@Cucumber.Options(format = { "pretty", "html:target/cucumber-html-report/" })

/**
 * main class; this is needed to run the rest of the cucumber tests
 */
public class CukeBaseTest {
    @Test
    public void foo() {
        // System.out.println("cuke");
    }
}
