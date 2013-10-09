package net.es.oscars.topoUtil.util;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

public class TemplateLoader {
    public static String populateTemplate(Map root, String templateDir, String templateFile) throws IOException, TemplateException {
        String config = "";
        Template temp = null;
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File(templateDir));
        cfg.setObjectWrapper(new DefaultObjectWrapper());
        Writer out = new StringWriter();
        temp = cfg.getTemplate(templateFile);
        temp.process(root, out);
        out.flush();
        config = out.toString();

        return config;
    }
}
