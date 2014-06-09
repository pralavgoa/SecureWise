package edu.ucla.wise.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class SQLTemplateUtil {

    private final Configuration cfg;

    public SQLTemplateUtil(Configuration cfg) {
        this.cfg = cfg;
    }

    public String getSQLFromTemplate(Map<String, Object> mapOfParameters, String templateFileName) throws IOException,
            TemplateException {
        Template template = this.cfg.getTemplate(templateFileName);
        StringWriter stringWriter = new StringWriter();
        template.process(mapOfParameters, new PrintWriter(stringWriter));
        String response = stringWriter.toString();
        return response;
    }
}
