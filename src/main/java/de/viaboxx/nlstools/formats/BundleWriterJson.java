package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.tasks.MessageBundleTask;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.Properties;
import java.util.Set;

/**
 * Description: <br>
 * User: roman.stumm <br>
 * Date: 15.06.2007 <br>
 * Time: 09:44:11 <br>
 * License: Apache 2.0
 */
public class BundleWriterJson extends BundleWriter {
    private final String outputFile;
    private boolean merged = true;

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public boolean isMerged() {
        return merged;
    }

    public BundleWriterJson(Task task, String configFile,
                            MBBundle currentBundle, String outputPath, String outputFile,
                            FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType, allowedLocales);
        this.outputFile = outputFile;
    }

    protected void writeOutputFilePerLocale(String locale) throws Exception {
        String jsfile = getFileName(locale);
        mkdirs(jsfile);
        task.log("writing json file " + jsfile, Project.MSG_INFO);
        Properties mergedProperties = createProperties(locale, merged);
        MBJSONPersistencer writer =
            new MBJSONPersistencer(fileType == FileType.JS_PRETTY);
        writer.saveObject(mergedProperties, new File(jsfile));
    }

    @Override
    protected StringBuilder buildOutputFileNameBase() {
        StringBuilder fileName = new StringBuilder();
        fileName.append(getOutputPath());
        fileName.append("/");
        if (outputFile == null) {
            fileName.append(getCurrentBundle().getBaseName());
        } else {
            fileName.append(outputFile);
        }
        return fileName;
    }

    protected String suffix() {
        return ".js";
    }

    public static BundleWriterJson build(MessageBundleTask task, String configFile, MBBundle currentBundle,
                                         String outputPath, String outputFile, FileType fileType,
                                         Set<String> allowedLocales) {
        switch (fileType) {
            case NG2_TRANSLATE:
                return new BundleWriterNg2Translate(task, configFile, outputPath, outputFile, fileType,
                    allowedLocales);
            case JS_ANGULAR:
            case JS_ANGULAR_PRETTY:
                return new BundleWriterAngularJS(task, configFile, currentBundle, outputPath, outputFile, fileType,
                    allowedLocales);
            default:
                return new BundleWriterJson(task, configFile, currentBundle, outputPath, outputFile, fileType,
                    allowedLocales);
        }
    }
}
