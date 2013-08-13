package net.es.oscars.nsibridge.test.cuke;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class SampleSteps {
    private char theLetter;
    @Given("^the letter '([A-Za-z])'$")
    public void gimmeALetter(final char theLetter) {
        this.theLetter = theLetter;
    }
    @When("^I check the letter(?:s)?$")
    public void checkThem() {
        // just a stub
    }
    @Then("^the letter should be '([A-Za-z])'$")
    public void checkTheLetter(final char aLetter) {
        assertThat(theLetter, is(aLetter));
    }
}
