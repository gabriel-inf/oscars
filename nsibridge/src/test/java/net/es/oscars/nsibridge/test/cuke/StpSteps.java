package net.es.oscars.nsibridge.test.cuke;


import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.nsa.NsaConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfigProvider;
import net.es.oscars.nsibridge.config.nsa.StpConfig;
import net.es.oscars.nsibridge.config.nsa.StpTransformConfig;
import net.es.oscars.nsibridge.prov.NSI_OSCARS_Translation;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;


import java.util.ArrayList;
import java.util.List;

public class StpSteps {
    private String sourceStp;
    private String oscarsUrn;

    private static Logger log = Logger.getLogger(StpSteps.class);

    @Given("^I have cleared all stp mapping config$")
    public void I_have_cleared_all_stp_mapping_config() throws Throwable {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();

        NsaConfigProvider np = ax.getBean("nsaConfigProvider", NsaConfigProvider.class);

        NsaConfig nc = np.getConfig("local");

        List<StpConfig> stps = new ArrayList<StpConfig>();
        List<StpTransformConfig> stpTransformConfigs = new ArrayList<StpTransformConfig>();


        nc.setStps(stps);
        nc.setStpTransforms(stpTransformConfigs);
    }

    @Given("^the source stp is \"([^\"]*)\"$")
    public void the_source_stp_is(String arg1) throws Throwable {
        sourceStp = arg1;
    }

    @Given("^i set one stp mapping as \"([^\"]*)\" to \"([^\"]*)\"$")
    public void i_set_one_stp_mapping_as_to(String arg1, String arg2) throws Throwable {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();

        NsaConfigProvider np = ax.getBean("nsaConfigProvider", NsaConfigProvider.class);

        NsaConfig nc = np.getConfig("local");
        List<StpConfig> stps = new ArrayList<StpConfig>();
        StpConfig stpConfig = new StpConfig();
        stpConfig.setStpId(arg1);
        stpConfig.setOscarsId(arg2);
        stps.add(stpConfig);
        nc.setStps(stps);
    }

    @When("^I perform all transforms and mappings$")
    public void I_perform_all_transforms_and_mappings() throws Throwable {
        StpConfig sc = NSI_OSCARS_Translation.findStp(sourceStp);
        oscarsUrn = sc.getOscarsId();
    }

    @Then("^the oscars urn is \"([^\"]*)\"$")
    public void the_oscars_urn_is(String arg1) throws Throwable {
        assertThat(oscarsUrn, notNullValue());
        assertThat(oscarsUrn, is(arg1));
    }

    @Given("^i set the stp transform as match: \"([^\"]*)\" replace: \"([^\"]*)\"$")
    public void i_set_the_stp_transform_as_match_replace(String arg1, String arg2) throws Throwable {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();

        NsaConfigProvider np = ax.getBean("nsaConfigProvider", NsaConfigProvider.class);

        NsaConfig nc = np.getConfig("local");

        List<StpTransformConfig> stpTransformConfigs = new ArrayList<StpTransformConfig>();
        StpTransformConfig stg = new StpTransformConfig();
        stg.setMatch(arg1);
        stg.setReplace(arg2);
        stpTransformConfigs.add(stg);

        nc.setStpTransforms(stpTransformConfigs);
    }
}
