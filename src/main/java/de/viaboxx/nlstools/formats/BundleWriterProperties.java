package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.util.FileUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.*;
import java.util.Properties;
import java.util.Set;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 15.06.2007 <br/>
 * Time: 09:44:05 <br/>
 * License: Apache 2.0
 */
public class BundleWriterProperties extends BundleWriter {
    private boolean merged = true;

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    /**
     * charset (e.g. UTF-8) of the .properties Files - if they do not use the default charset (e.g. Grails i18n files)
     * By default, the ISO 8859-1 character encoding is used (see javadoc of java.util.Properties)
     */
    private String charset = null;

    public BundleWriterProperties(Task task, String configFile, MBBundle currentBundle, String outputPath,
                                  FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType, allowedLocales);
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    protected String suffix() {
        return fileType == FileType.XML ? ".xml" : ".properties";
    }

    protected String getPropertiesHeader(String locale) {
        return " THIS FILE HAS BEEN GENERATED AUTOMATICALLY - DO NOT ALTER!\r\n" +
                "#\r\n" + "# resource bundle: " + getCurrentBundle().getBaseName() +
                "\r\n" + "# locale: " + locale + "\r\n" + "# interface: " +
                getCurrentBundle().getInterfaceName() + "\r\n" + "#";
    }

    protected void writeOutputFilePerLocale(String locale)
            throws IOException {
        String propfile = getFileName(locale);
        task.log("writing resource file " + propfile, Project.MSG_INFO);
        mkdirs(propfile);

        if (getCharset() == null || fileType.equals(FileType.XML)) {
            FileOutputStream stream = new FileOutputStream(propfile);
            try {
                String header = getPropertiesHeader(locale);
                writeProperties(stream, locale, header);
            } finally {
                stream.close();
            }
        } else {
            Writer writer = FileUtils.openFileWriter(new File(propfile), getCharset());
            try {
                String header = getPropertiesHeader(locale);
                Properties p = createProperties(locale, merged);
                p.store(writer, header);
            } finally {
                writer.close();
            }
        }
    }

    protected void writeProperties(OutputStream stream, String aLocale, String header)
            throws IOException {
        Properties p = createProperties(aLocale, merged);
        if (fileType.equals(FileType.XML)) {
            p.storeToXML(stream, header);
        } else {
            p.store(stream, header);
        }
    }
}
