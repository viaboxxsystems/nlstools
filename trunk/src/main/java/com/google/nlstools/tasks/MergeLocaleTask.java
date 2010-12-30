package com.google.nlstools.tasks;

import com.google.nlstools.formats.MBPersistencer;
import com.google.nlstools.model.MBBundle;
import com.google.nlstools.model.MBBundles;
import com.google.nlstools.model.MBEntry;
import com.google.nlstools.model.MBText;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.StringTokenizer;

/**
 * This task takes two bundles and is able to merge new locales in one file into the existing bundle.
 * This is useful if the translation is not a complete version of the locale bundle and you can't just
 * replace the file. If there are translations missing, it inserts empty messages for that key.
 * Example:
 * <pre>
 * &lt;taskdef name="mergeLocale">
 * classname="com.google.nlstools.tasks.MergeLocaleTask">
 * &lt;classpath refid="maven.test.classpath"/>
 * &lt;/taskdef>
 * <p/>
 * &lt;mergeLocale
 * from="src/main/bundles/Common.xml"
 * with="src/main/bundles/Common_de_DE.xml"
 * locales="de_DE"
 * to="src/main/bundles/Common_de_DE.xml"/>
 * </pre>
 */
public class MergeLocaleTask extends Task {
    private String from, with, to, locales;

    /**
     * The xml file with path name to read from
     *
     * @return
     */
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * The xml file with the path name to write into
     *
     * @return
     */
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    /**
     * The xml file with new translated locales
     *
     * @return
     */
    public String getWith() {
        return with;
    }

    public void setWith(String xmlWithNewLocale) {
        this.with = xmlWithNewLocale;
    }

    /**
     * semicolon separated locale names
     *
     * @return
     */
    public String getLocales() {
        return locales;
    }

    public void setLocales(String locales) {
        this.locales = locales;
    }

    public void execute() {
        MBBundles loadedBundles;
        MBBundles translatedBundles;

        // try to load the bundles of the file
        try {
            log("Reading Bundles from " + from, Project.MSG_INFO);
            loadedBundles = MBPersistencer.loadFile(new File(from));

            log("Reading Bundles from " + with, Project.MSG_INFO);
            translatedBundles = MBPersistencer.loadFile(new File(with));

            // if bundles exist
            if (loadedBundles != null) {
                for (MBBundle bundle : loadedBundles.getBundles()) {
                    for (MBEntry entry : bundle.getEntries()) {
                        // divide the locale string
                        StringTokenizer tokens = new StringTokenizer(locales, ";");
                        while (tokens.hasMoreTokens()) {
                            String locale = tokens.nextToken();
                            MBText tmpText = null;

                            // check if the defined locale already exists
                            for (MBText text : entry.getTexts()) {
                                if (text.getLocale().equals(locale)) {
                                    tmpText = text;
                                }
                            }

                            if (tmpText == null) {
                                tmpText = new MBText();
                                tmpText.setLocale(locale);
                                tmpText.setValue("");
                                entry.getTexts().add(tmpText);
                            }

                            MBText translatedText = findMBTextForLocale(entry.getKey(), locale, translatedBundles);
                            if (translatedText != null) {
                                tmpText.setValue(translatedText.getValue());
                            }
                        }
                    }
                }
            }

            // write the combined locales into a file
            MBPersistencer.saveFile(loadedBundles, new File(to));
            log("Writing to bundles to file " + to, Project.MSG_INFO);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    private MBText findMBTextForLocale(String key, String locale, MBBundles bundles) {
        if (bundles != null) {
            for (MBBundle bundle : bundles.getBundles()) {
                for (MBEntry entry : bundle.getEntries()) {
                    if (entry.getKey().equals(key)) {
                        for (MBText text : entry.getTexts()) {
                            if (text.getLocale().equals(locale)) {
                                return text;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
