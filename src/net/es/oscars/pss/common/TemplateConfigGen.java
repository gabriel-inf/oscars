package net.es.oscars.pss.common;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import net.es.oscars.pss.PSSException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateConfigGen {
    private String templateDir = "";

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public String getTemplateDir() {
        return templateDir;
    }

    @SuppressWarnings("rawtypes")
    protected String getConfig(Map root, String templateFileName) throws PSSException {
        String config = "";
        Template temp = null;
        Configuration cfg = new Configuration();
        try {
            cfg.setDirectoryForTemplateLoading(new File(templateDir));
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Writer out = new StringWriter();
            temp = cfg.getTemplate(templateFileName);
            temp.process(root, out);
            out.flush();
            config = out.toString();
        } catch (IOException e) {
            throw new PSSException(e.getMessage());
        } catch (TemplateException e) {
            throw new PSSException(e.getMessage());
        }
        return config;
    }
}
