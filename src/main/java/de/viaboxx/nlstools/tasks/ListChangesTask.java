package de.viaboxx.nlstools.tasks;

import de.viaboxx.nlstools.formats.BundleWriterExcel;
import de.viaboxx.nlstools.formats.MBPersistencer;
import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

/**
 * Print different values between two bundles.
 *
 * @see CompareBundlesTask
 *      <br/>NEW (29.12.2010):<br/>
 *      * Can handle XML and Excel files.<br/>
 *      <p/>
 *      <pre>
 *                      * checkedLocales: optional (default: all locales). a ; separated list of locales to compare
 *                      * original: excel or xml file to read (the ORIGINAL file)
 *                      * newer: excel or xml file to read (the NEWER file)
 *                      * ignoreMissingKeys: true/false (default: false)
 *                     <pre>
 *                     Example:
 *                     <pre>
 *                      <listChanges ignoreMissingKeys="true"
 *                     locales="en_US;de_DE"
 *                     original="i18n/main-default.xml" newer="i18n/main-default.xls" results="results.txt"/>
 *                     </pre>
 */
public class ListChangesTask extends Task {

    private File original, newer, results;
    private String locales;
    private boolean ignoreMissingKeys = false;

    @Override
    public void execute() throws BuildException {
        List<String> diffs = new ArrayList<String>();
        try {
            MBBundles originalBundles = MBPersistencer.loadFile(original);
            MBBundles newBundles = MBPersistencer.loadFile(newer);

            HashSet<String> locales = new HashSet();
            if (this.locales == null || this.locales.length() == 0) {
                for (MBBundle each : originalBundles.getBundles()) {
                    locales.addAll(new BundleWriterExcel(each).getLocalesUsed());
                }
            } else {
                StringTokenizer localesTokens = new StringTokenizer(this.locales, ",;");
                while (localesTokens.hasMoreTokens()) {
                    String locale = localesTokens.nextToken();
                    locales.add(locale);
                }
            }
            List<String> localeOrdered = new ArrayList(locales);
            Collections.sort(localeOrdered);
            for (String locale : localeOrdered) {
                log("Checking locale:" + locale);
                for (MBBundle originalBundle : originalBundles.getBundles()) {
                    for (MBEntry originalEntry : originalBundle.getEntries()) {
                        MBText newText = newBundles == null ? null :
                                newBundles.findMBTextForLocale(originalEntry.getKey(), locale);
                        String originalTextValue =
                                originalEntry.getText(locale) == null ? "" : originalEntry.getText(locale).getValue();
                        String newTextValue = newText == null ? "" : newText.getValue();
                        if (!(ignoreMissingKeys && newText == null) && !originalTextValue.equals(newTextValue)) {
                            String diffDescription =
                                    "(" + locale + ") key= " + originalEntry.getKey() + "- ORIGINAL= '" +
                                            originalTextValue + "' <-> ";
                            if (newText == null) {
                                diffDescription += "NEW: null";
                            } else {
                                diffDescription += "NEW: '" + newTextValue + "'";
                            }
                            log(diffDescription);
                            diffs.add(diffDescription);
                        }
                    }
                }
            }
            Writer writer = new FileWriter(results);
            for (String diff : diffs) {
                writer.append(diff).append("\n");
            }
            writer.close();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    public File getOriginal() {
        return original;
    }

    public void setOriginal(File original) {
        this.original = original;
    }

    public File getNewer() {
        return newer;
    }

    public void setNewer(File newer) {
        this.newer = newer;
    }

    public File getResults() {
        return results;
    }

    public void setResults(File results) {
        this.results = results;
    }

    public String getLocales() {
        return locales;
    }

    public void setLocales(String locales) {
        this.locales = locales;
    }

    public boolean isIgnoreMissingKeys() {
        return ignoreMissingKeys;
    }

    public void setIgnoreMissingKeys(boolean ignoreMissingKeys) {
        this.ignoreMissingKeys = ignoreMissingKeys;
    }
}
