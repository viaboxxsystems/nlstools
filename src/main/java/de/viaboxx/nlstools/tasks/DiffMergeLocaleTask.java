package de.viaboxx.nlstools.tasks;

import de.viaboxx.nlstools.formats.MBPersistencer;
import de.viaboxx.nlstools.formats.MBXMLPersistencer;
import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import org.apache.tools.ant.Project;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Description: Merge-Task that can detect Merge-Conflicts by comparison of 'from'
 * with a origin-source-version and a current-source-version.
 * Can write conflicts to a 'conflicts' bundle.<br>
 * User: roman.stumm<br>
 * Date: 02.05.2011<br>
 * Time: 15:35:39<br>
 * License: Apache 2.02010
 */
public class DiffMergeLocaleTask extends MergeLocaleTask {
    /**
     * origin:    the file that is the original 'source' version, 'from' is based on
     * current:   is the current 'source' (probably containing newer changes that are neither in 'origin' nor in 'from'
     * conflicts: the file where to store the conflicts
     */
    private File origin, current, conflicts;

    // originBundles     => A) source version of the bundles on which 'translatedBundles' is based on (older than loadedBundles)
    // translatedBundles => B) file with new translation from translator
    // loadedBundles     => C) current source version of the bundles
    // to                => D) the name of the file to be saved (maybe equal to 'loadedBundles' if you want to replace immediately)
    // conflictsBundles  => E) new bundles to store conflicts in

    private MBBundles originBundles;
    private MBBundles conflictsBundles;

    protected void processExecute() {
        // if bundles exist
        if (loadedBundles != null) {
            setLocales(localesString(loadedBundles, getLocales()));
            for (MBBundle bundle : loadedBundles.getBundles()) {
                if (!isBundleToProcess(bundle)) {
                    getProject().log("Skipped " + bundle.getBaseName());
                    continue; // skip
                } else {
                    getProject().log("Merging " + bundle.getBaseName());
                }
                for (MBEntry entry : bundle.getEntries()) {
                    // divide the locale string
                    StringTokenizer tokens = MergeLocaleTask.tokenize(getLocales());
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
                        MBText originText = findMBTextForLocale(entry.getKey(), locale, originBundles);
                        if (translatedText != null) {
                            if (originText != null &&
                                !originText.equals(translatedText) &&
                                !translatedText.equals(tmpText) &&
                                !originText.equals(tmpText)) {
                                MBBundle conflictBundle = getConflictBundle(bundle);
                                MBEntry conflictEntry = entry.copy();
                                conflictEntry.setKey(conflictEntry.getKey() + ".transl");
                                conflictEntry.getTexts().add(translatedText);
                                conflictBundle.getEntries().add(conflictEntry);

                                conflictEntry = entry.copy();
                                conflictEntry.setKey(conflictEntry.getKey() + ".current");
                                conflictEntry.getTexts().add(tmpText);
                                conflictBundle.getEntries().add(conflictEntry);
                            } else if (originText != null &&
                                originText.equals(translatedText) &&
                                !originText.equals(tmpText)) {
                                MBBundle conflictBundle = getConflictBundle(bundle);
                                MBEntry conflictEntry = entry.copy();
                                conflictEntry.setKey(conflictEntry.getKey() + ".origin");
                                conflictEntry.getTexts().add(originText);
                                conflictBundle.getEntries().add(conflictEntry);

                                conflictEntry = entry.copy();
                                conflictEntry.setKey(conflictEntry.getKey() + ".transl");
                                conflictEntry.getTexts().add(translatedText);
                                conflictBundle.getEntries().add(conflictEntry);

                                conflictEntry = entry.copy();
                                conflictEntry.setKey(conflictEntry.getKey() + ".current");
                                conflictEntry.getTexts().add(tmpText);
                                conflictBundle.getEntries().add(conflictEntry);
                            } else {
                                tmpText.setValue(translatedText.getValue());
                            }
                        }
                    }
                }
            }
        }
    }

    private MBBundle getConflictBundle(MBBundle bundle) {
        if (conflictsBundles == null) {
            conflictsBundles = new MBBundles();
        }
        MBBundle conflictBundle = conflictsBundles.getBundle(bundle.getBaseName());
        if (conflictBundle == null) {
            conflictBundle = bundle.copy();
            conflictBundle.getEntries().clear();
            conflictsBundles.getBundles().add(conflictBundle);
        }
        return conflictBundle;
    }

    @Override
    protected void loadExecute() throws Exception {
        super.loadExecute();
        if (getOrigin() != null) {
            log("Reading Bundles from " + getOrigin(), Project.MSG_INFO);
            originBundles = MBPersistencer.loadFile(getOrigin());
        }
    }

    @Override
    protected void outputExecute() throws Exception {
        if (conflictsBundles != null) {
            if (conflicts != null) {
                MBPersistencer.saveFile(conflictsBundles, conflicts);
                log("Merged and writing conflict bundles to " + conflicts, Project.MSG_INFO);
            } else {
                log("Merged with conflicts found: \n" + MBXMLPersistencer.getXstream().toXML(conflictsBundles),
                    Project.MSG_INFO);
            }
        } else {
            if (conflicts != null) {
                log("Merged without conflicts - writing empty bundles to " + conflicts, Project.MSG_INFO);
                MBPersistencer.saveFile(new MBBundles(), conflicts);
            } else {
                log("Merged without conflicts", Project.MSG_INFO);
            }
        }
    }

    public File getOrigin() {
        return origin;
    }

    public void setOrigin(File origin) {
        this.origin = origin;
    }

    public File getConflicts() {
        return conflicts;
    }

    public void setConflicts(File conflicts) {
        this.conflicts = conflicts;
    }

    public File getCurrent() {
        return current;
    }

    public void setCurrent(File current) {
        this.current = current;
    }
}
