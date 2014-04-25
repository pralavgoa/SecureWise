package edu.ucla.wise.client.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import edu.ucla.wise.commons.SurveyorApplication;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class TemplateUtils {
    public static String getHtmlFromTemplate(Map<String, Object> mapOfParameters, String templateFileName)
            throws IOException, TemplateException {
        Configuration cfg = SurveyorApplication.getInstance().getHtmlTemplateConfiguration();
        Template template = cfg.getTemplate(templateFileName);
        StringWriter stringWriter = new StringWriter();
        template.process(mapOfParameters, new PrintWriter(stringWriter));
        String response = stringWriter.toString();
        return response;
    }
}
