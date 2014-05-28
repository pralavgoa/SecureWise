package edu.ucla.wise.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

public class FileUtils {

    public static String convertInputStreamToString(InputStream inputStream, Charset charset) throws IOException {
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer, charset);
        return writer.toString();
    }
}
