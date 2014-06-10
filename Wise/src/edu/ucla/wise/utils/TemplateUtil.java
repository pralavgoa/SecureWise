package edu.ucla.wise.utils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

public class TemplateUtil {
    public static Configuration createTemplateConfiguration(String rootFolderPath, String templateRootPath)
            throws IOException {
        Configuration cfg = new Configuration();
        cfg.setDirectoryForTemplateLoading(new File(rootFolderPath + templateRootPath));
        cfg.setIncompatibleImprovements(new Version(1, 0, 0));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.US);
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        return cfg;
    }
}
