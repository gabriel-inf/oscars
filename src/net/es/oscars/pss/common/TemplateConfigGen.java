package net.es.oscars.pss.common;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import org.apache.log4j.Logger;

import net.es.oscars.pss.PSSException;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
/**
 * a base class for Freemarker-based template config generators
 * 
 * @author haniotak
 *
 */
public class TemplateConfigGen {
    private String templateDir = "";
    private Logger log = Logger.getLogger(TemplateConfigGen.class);

    public void setTemplateDir(String templateDir) {
        this.templateDir = templateDir;
    }

    public String getTemplateDir() {
        return templateDir;
    }

    @SuppressWarnings("rawtypes")
    protected String getConfig(Map root, String templateFileName) throws PSSException {
        log.debug("getConfig.start");
        String config = "";
        Template temp = null;
        Configuration cfg = new Configuration();
        try {
            log.debug("templateDir: "+templateDir);
            File dir = new File(templateDir);
            log.debug(dir.getAbsolutePath());
            cfg.setDirectoryForTemplateLoading(dir);
            cfg.setObjectWrapper(new DefaultObjectWrapper());
            Writer out = new StringWriter();
            temp = cfg.getTemplate(templateFileName);
            temp.process(root, out);
            out.flush();
            config = out.toString();
        } catch (IOException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        } catch (TemplateException e) {
            log.error(e);
            throw new PSSException(e.getMessage());
        }
        log.debug("getConfig.end");
        return config;
    }
}
