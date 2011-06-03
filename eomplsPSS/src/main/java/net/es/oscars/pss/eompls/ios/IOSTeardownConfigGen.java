package net.es.oscars.pss.eompls.ios;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.svc.ServiceNames;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class IOSTeardownConfigGen {
    private String vlan;
    private String pseudowireName;
    private String policyName;
    private String pathName;
    private String ifceName;

    @SuppressWarnings("unchecked")
    public String generateConfig() throws Exception {

        ContextConfig cc = ContextConfig.getInstance(ServiceNames.SVC_PSS);
        String templateDir = cc.getFilePath("templateDir");

        String config = "";
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File(templateDir));
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        Template temp = cfg.getTemplate("ios-teardown.txt");

        // create data model objects
        Map root = new HashMap();
        Map pseudowire = new HashMap();
        Map policy = new HashMap();
        Map lsp = new HashMap();
        Map path = new HashMap();
        Map ifce = new HashMap();

        // set up data model structure
        root.put("pseudowire", pseudowire);
        root.put("ifce", ifce);
        root.put("policy", policy);
        root.put("lsp", lsp);
        lsp.put("path", path);

        // fill in scalars
        root.put("vlan", vlan);
        pseudowire.put("name", pseudowireName);
        policy.put("name", policyName);
        ifce.put("name", ifceName);
        path.put("name", pathName);

        /* Merge data-model with template */
        Writer out = new OutputStreamWriter(System.out);
        temp.process(root, out);
        out.flush();
        return config;
    }

    public String getVlan() {
        return vlan;
    }

    public void setVlan(String vlan) {
        this.vlan = vlan;
    }

    public String getPseudowireName() {
        return pseudowireName;
    }

    public void setPseudowireName(String pseudowireName) {
        this.pseudowireName = pseudowireName;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public String getIfceName() {
        return ifceName;
    }

    public void setIfceName(String ifceName) {
        this.ifceName = ifceName;
    }


}
