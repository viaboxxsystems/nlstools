package de.viaboxx.nlstools.tasks;

import de.viaboxx.nlstools.formats.MBPersistencer;
import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.*;

/**
 * This task takes two bundles and is able to merge new locales in one file into the existing bundle.
 * This is useful if the translation is not a complete version of the locale bundle and you can't just
 * replace the file. If there are translations missing, it inserts empty messages for that key.
 * Example:
 * <pre>
 * &lt;taskdef name="mergeLocale">
 * classname="de.viaboxx.nlstools.tasks.MergeLocaleTask">
 * &lt;classpath refid="maven.test.classpath"/>
 * &lt;/taskdef>
 *
 * &lt;mergeLocale
 * from="src/main/bundles/Common.xml"
 * with="src/main/bundles/Common_de_DE.xml"
 * locales="de_DE;en,it" (if empty, process ALL locales)
 * to="src/main/bundles/Common_de_DE.xml"/>
 * </pre>
 */
public class MergeLocaleTask extends Task {
    private File from, with, to;
    private String locales;
    private String bundleNames;
    protected MBBundles loadedBundles;
    protected MBBundles translatedBundles;

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
     * @return
     */
    public File getTo() {
        return to;
    }

    public void setTo(File to) {
        this.to = to;
    }

    /**
     * The xml file with new translated locales
     *
     * @return
     */
    public File getWith() {
        return with;
    }

    public void setWith(File xmlWithNewLocale) {
        this.with = xmlWithNewLocale;
    }

    /**
     * merge specific bundles only. if empty (default), merge all bundles.
     * bundle names separated by , or ;
     *
     * @return
     */
    public String getBundleNames() {
        return bundleNames;
    }

    public void setBundleNames(String bundleNames) {
        this.bundleNames = bundleNames;
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

        // try to load the bundles of the file
        try {
            loadExecute();
            processExecute();
            outputExecute();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public static StringTokenizer tokenize(String input) {
        return new StringTokenizer(input, ",;");
    }

    public static String localesString(Set<String> locales) {
        List<String> list = new ArrayList<String>(locales);
        Collections.sort(list);
        StringBuilder buf = new StringBuilder();
        for (String each : list) {
            buf.append(each);
            buf.append(",");
        }
        return buf.toString();
    }

    public static String localesString(MBBundles bundles, String locales) {
        if (locales == null || locales.length() == 0) {
            return localesString(bundles.locales());
        } else {
            return locales;
        }
    }

    protected void processExecute() {
        // if bundles exist
        Set<String> namesFilter = new HashSet<String>();
        if (!StringUtils.isEmpty(bundleNames)) {
            StringTokenizer tokens = tokenize(bundleNames);
            while (tokens.hasMoreTokens()) {
                namesFilter.add(tokens.nextToken());
            }
        } else {
            namesFilter = Collections.emptySet();
        }
        if (loadedBundles != null) {
            setLocales(localesString(loadedBundles, getLocales()));
            for (MBBundle bundle : loadedBundles.getBundles()) {
                if (!namesFilter.isEmpty()) {
                    if (!namesFilter.contains(bundle.getBaseName())) {
                        getProject().log("Skipped " + bundle.getBaseName());
                        continue; // skip
                    } else {
                        getProject().log("Merging " + bundle.getBaseName());
                    }
                }
                for (MBEntry entry : bundle.getEntries()) {
                    // divide the locale string
                    StringTokenizer tokens = MergeLocaleTask.tokenize(locales);
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
    }

    protected void outputExecute() throws Exception {
        // write the combined locales into a file
        MBPersistencer.saveFile(loadedBundles, to);
        log("Writing to bundles to file " + to, Project.MSG_INFO);
    }

    protected void loadExecute() throws Exception {
        log("Reading Bundles from " + from, Project.MSG_INFO);
        loadedBundles = MBPersistencer.loadFile(from);

        log("Reading Bundles from " + with, Project.MSG_INFO);
        translatedBundles = MBPersistencer.loadFile(with);
    }

    protected MBText findMBTextForLocale(String key, String locale, MBBundles bundles) {
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
