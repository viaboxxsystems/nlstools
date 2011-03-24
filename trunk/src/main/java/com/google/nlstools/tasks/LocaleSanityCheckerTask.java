package com.google.nlstools.tasks;

import com.google.nlstools.formats.MBPersistencer;
import com.google.nlstools.model.MBBundle;
import com.google.nlstools.model.MBBundles;
import com.google.nlstools.model.MBEntry;
import com.google.nlstools.model.MBText;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.IOException;

/**
 * Checks localisation files for obvious errors like missing translations for single entries.
 * This task is locale-specific, so using it include telling which locale to analyze.
 * <p/>
 * Usage sample:
 * &lt;sanityCheck
 * locale=&quot;fr_FR&quot;
 * includeReview=&quot;false&quot;
 * from=&quot;complete/main-default.xml&quot;
 * to=&quot;sanity-check-FR.xml&quot;
 * /&gt;
 */
public class LocaleSanityCheckerTask extends Task {

    private File from, to;
    private String locale;
    private boolean includeReview = false;

    @Override
    public void execute() throws BuildException {
        if (locale == null) {
            throw new BuildException("locale parameter is needed!");
        }
        if (to.exists()) {
            throw new BuildException("Output file already exists:" + to.getAbsolutePath());
        }
        try {
            if (!to.createNewFile()) {
                throw new BuildException("Could not create result file:" + to.getAbsolutePath());
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
        if (!to.canWrite()) {
            throw new BuildException("Cannot write to output file:" + to.getAbsolutePath());
        }


        boolean foundMissing = false;
        MBBundles missingTranslations = new MBBundles();
        MBBundles originalBundles;
        try {
            originalBundles = MBPersistencer.loadFile(from);
            for (MBBundle bundle : originalBundles.getBundles()) {
                MBBundle missingBundle = new MBBundle();
                missingBundle.setBaseName(bundle.getBaseName());
                missingBundle.setInterfaceName(bundle.getInterfaceName());
                missingBundle.setSqldomain(bundle.getSqldomain());
                boolean needsBundle = false;

                for (MBEntry entry : bundle.getEntries()) {
                    MBText text = entry.getText(locale);
                    if (isMissing(text)) {
                        if (!needsBundle) {
                            missingTranslations.getBundles().add(missingBundle);
                            needsBundle = true;
                            foundMissing = true;
                        }
                        missingBundle.getEntries().add(entry);
                    }
                }
            }
        } catch (Exception e) {
            throw new BuildException(e);
        }

        if (foundMissing) {
            try {
                MBPersistencer.saveFile(missingTranslations, to);
            } catch (Exception e) {
                throw new BuildException(e);
            }
            log("missing translations saved as " + to.getPath());
        } else {
            log("no missing translations found.");
        }

    }

    private boolean isMissing(MBText text) {
        return text == null ||
                (isIncludeReview() && text.isReview()) ||
                (text.getValue() == null || text.getValue().equals(""));
    }

    public File getFrom() {
        return from;
    }

    public void setFrom(File from) {
        this.from = from;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public File getTo() {
        return to;
    }

    public void setTo(File to) {
        this.to = to;
    }

    public boolean isIncludeReview() {
        return includeReview;
    }

    public void setIncludeReview(boolean includeReview) {
        this.includeReview = includeReview;
    }
}
