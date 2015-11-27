package de.viaboxx.nlstools.tasks;

import de.viaboxx.nlstools.formats.MBPersistencer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.File;

/**
 * Description: Read bundles from a file and save it to another.
 * This task can be used to convert between different formats (XML/Excel/JSON) <br>
 * User: roman.stumm<br>
 * Date: 30.12.2010<br>
 * Time: 13:43:41<br>
 * License: Apache 2.02010
 */
public class ConvertBundlesTask extends Task {
    private File from;
    private File to;
    private String options = "";

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    @Override
    public void execute() throws BuildException {
        if (from == null) {
            throw new BuildException("'from' file missing");
        }
        if (to == null) {
            throw new BuildException("'to' file missing");
        }
        if (!from.exists()) {
            throw new BuildException("FileNotFound: 'from' file: " + from.getPath() + " does not exist.");
        }
        try {
            this.log("Convert " + from.getPath() + " ==> " + to.getPath());
            MBPersistencer.saveFile(MBPersistencer.loadFile(from), to, options);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(
                    "Bundle file conversion from " + from.getPath() + " to " + to.getPath() + " failed!", e);
        }
    }

    public File getFrom() {
        return from;
    }

    public void setFrom(File from) {
        this.from = from;
    }

    public File getTo() {
        return to;
    }

    public void setTo(File to) {
        this.to = to;
    }
}
