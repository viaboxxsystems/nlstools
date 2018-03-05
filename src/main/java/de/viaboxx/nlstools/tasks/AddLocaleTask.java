package de.viaboxx.nlstools.tasks;

import de.viaboxx.nlstools.formats.MBPersistencer;
import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Takes an XML bundle and adds new entries for the specified locales.
 * <br>NEW (29.12.2010):<br>
 * * Can handle XML and Excel files.<br>
 *
 * @author Simon Tiffert
 */
public class AddLocaleTask extends Task {
    private File from, to;
    private String locales;

    /**
     * The xml file with path name to read from
     *
     * @return
     */
    public File getFrom() {
        return from;
    }

    public void setFrom(File from) {
        this.from = from;
    }

    /**
     * The xml file with the path name to write into
     *
     */
    public File getTo() {
        return to;
    }

    public void setTo(File to) {
        this.to = to;
    }

    /**
     * semicolon separated locale names
     *
     */
    public String getLocales() {
        return locales;
    }

    public void setLocales(String locales) {
        this.locales = locales;
    }

    public void execute() {
        MBBundles loadedBundles;
        log("Reading Bundles from " + from, Project.MSG_INFO);
        // try to load the bundles of the file
        try {
            MBPersistencer persistencer = MBPersistencer.forFile(from);
            loadedBundles = persistencer.load(from);

            // if bundles exist
            if (loadedBundles != null) {
                setLocales(MergeLocaleTask.localesString(loadedBundles, getLocales()));
                for (MBBundle bundle : loadedBundles.getBundles()) {
                    for (MBEntry entry : bundle.getEntries()) {
                        // divide the locale string
                        StringTokenizer tokens = MergeLocaleTask.tokenize(locales);
                        while (tokens.hasMoreTokens()) {
                            String locale = tokens.nextToken();

                            // check if the defined locale already exists
                            boolean newLocale = true;
                            for (MBText text : entry.getTexts()) {
                                if (text.getLocale().equals(locale)) {
                                    newLocale = false;
                                }
                            }

                            // don't overwrite or duplicate an existing locale
                            if (newLocale) {
                                // create a new locale entry with an empty value
                                MBText text = new MBText();
                                text.setLocale(locale);
                                text.setValue("");
                                entry.getTexts().add(text);
                            }
                        }
                    }
                }
            }

            // write the combined locales into a file
            if(loadedBundles != null) loadedBundles.sort();
            persistencer.save(loadedBundles, to);
            log("Writing to XML file " + to, Project.MSG_INFO);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }
}
