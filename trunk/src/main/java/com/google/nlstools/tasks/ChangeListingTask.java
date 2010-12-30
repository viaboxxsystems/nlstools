package com.google.nlstools.tasks;

import com.google.nlstools.formats.BundleWriterExcel;
import com.google.nlstools.formats.MBPersistencer;
import com.google.nlstools.model.MBBundle;
import com.google.nlstools.model.MBBundles;
import com.google.nlstools.model.MBEntry;
import com.google.nlstools.model.MBText;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.*;

/**
 * Print different values between two bundles.
 *
 * @see com.google.nlstools.tasks.CompareLocalesTask
 *      <br/>NEW (29.12.2010):<br/>
 *      * Can handle XML and Excel files.<br/>
 *      <p/>
 *      <pre>
 *                      * checkedLocales: optional (default: all locales). a ; separated list of locales to compare
 *                      * originalXML: excel or xml file to read (the ORIGINAL file)
 *                      * newXML: excel or xml file to read (the NEWER file)
 *                      * ignoreMissingKeys: true/false (default: false)
 *                     <pre>
 *                     Example:
 *                     <pre>
 *                      <changeListing ignoreMissingKeys="true"
 *                     checkedLocales="en_US;de_DE"
 *                     originalXML="i18n/main-default.xml" newXML="i18n/main-default.xls" results="results.txt"/>
 *                     </pre>
 */
public class ChangeListingTask extends Task {

    private File originalXML, newXML, results;
    private String checkedLocales;
    private boolean ignoreMissingKeys = false;

    @Override
    public void execute() throws BuildException {
        List<String> diffs = new ArrayList<String>();
        try {
            MBBundles originalBundles = MBPersistencer.loadFile(originalXML);
            MBBundles newBundles = MBPersistencer.loadFile(newXML);

            HashSet<String> locales = new HashSet();
            if (checkedLocales == null || checkedLocales.length() == 0) {
                for (MBBundle each : originalBundles.getBundles()) {
                    locales.addAll(new BundleWriterExcel(each).getLocalesUsed());
                }
            } else {
                StringTokenizer localesTokens = new StringTokenizer(checkedLocales, ";");
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

    public File getOriginalXML() {
        return originalXML;
    }

    public void setOriginalXML(File originalXML) {
        this.originalXML = originalXML;
    }

    public File getNewXML() {
        return newXML;
    }

    public void setNewXML(File newXML) {
        this.newXML = newXML;
    }

    public File getResults() {
        return results;
    }

    public void setResults(File results) {
        this.results = results;
    }

    public String getCheckedLocales() {
        return checkedLocales;
    }

    public void setCheckedLocales(String checkedLocales) {
        this.checkedLocales = checkedLocales;
    }

    public boolean isIgnoreMissingKeys() {
        return ignoreMissingKeys;
    }

    public void setIgnoreMissingKeys(boolean ignoreMissingKeys) {
        this.ignoreMissingKeys = ignoreMissingKeys;
    }
}
