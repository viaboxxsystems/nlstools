package de.viaboxx.nlstools.tasks;

import de.viaboxx.nlstools.formats.MBPersistencer;
import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Compares new (e.g. changed by customer) and old (e.g. stuff without customer changes) locale files and
 * lists keys and bundles that are missing in the new translation.
 *
 * @see ListChangesTask to compare translation values
 *      <p/>
 *      <br/>NEW (29.12.2010):<br/>
 *      * Can handle XML and Excel files.
 *      <p/>
 *      <br/>
 *      Sample usage:
 *      &lt;compareBundles
 *      original=&quot;original/main-default.xml&quot;
 *      newer=&quot;new/main-default.xml&quot;
 *      results=&quot;compare-results.txt&quot;
 *      /&gt;
 */
public class CompareBundlesTask extends Task {
    private File original, newer, results;

    @Override
    public void execute() throws BuildException {
        MBBundles originalBundles, newBundles;
        List<String> missingKeys = new ArrayList<String>();
        List<String> missingBundles = new ArrayList<String>();
        try {
            originalBundles = MBPersistencer.loadFile(original);
            newBundles = MBPersistencer.loadFile(newer);
            for (MBBundle originalBundle : originalBundles.getBundles()) {
                MBBundle newBundle = newBundles.getBundle(originalBundle.getBaseName());
                if (newBundle == null) {
                    missingBundles.add(originalBundle.getBaseName());
                } else {
                    for (MBEntry originalEntry : originalBundle.getEntries()) {
                        MBEntry newEntry = newBundle.getEntry(originalEntry.getKey());
                        if (newEntry == null) {
                            missingKeys.add(originalEntry.getKey());
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }
        try {
            Writer writer = new FileWriter(results);
            writer.append("# Comparison of ").append(original.getAbsolutePath()).append(" (original) and ")
                    .append(newer.getAbsolutePath()).append(" (new version)");
            if (missingBundles.size() > 0) {
                writer.append("# Missing bundles (").append(String.valueOf(missingKeys.size())).append("):\n");
                for (String missingBundle : missingBundles) {
                    writer.append(missingBundle);
                    writer.append("\n");
                }
            } else {
                writer.append("# No bundles missing.\n");
            }
            if (missingKeys.size() > 0) {
                writer.append("Missing keys (").append(String.valueOf(missingKeys.size())).append("):\n");
                for (String missingKey : missingKeys) {
                    writer.append(missingKey);
                    writer.append("\n");
                }
            } else {
                writer.append("No missing keys.\n");
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
}
