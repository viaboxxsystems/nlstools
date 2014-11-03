package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.util.Properties;
import java.util.Set;

/**
 * Description: write bundles in json format as required by angular-localization, see examples at
 * <a href="https://github.com/doshprompt/angular-localization">angular-localization on github</a><br>
 * <p>
 * Date: 03.11.14<br>
 * </p>
 *
 * @since 2.6
 */
public class BundleWriterAngularJS extends BundleWriterJson {

    public BundleWriterAngularJS(Task task, String configFile,
                                 MBBundle currentBundle, String outputPath,
                                 String outputFile, FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, outputFile, fileType, allowedLocales);
        setFlexLayout(true);
    }

    protected void writeOutputFilePerLocale(String locale) throws Exception {
        String jsfile = getFileName(locale);
        mkdirs(jsfile);
        task.log("writing angular json file " + jsfile, Project.MSG_INFO);
        Properties mergedProperties = createProperties(locale, isMerged());
        MBJSONPersistencer writer = new MBJSONPersistencer(true);
        JSONObject map = new JSONObject(mergedProperties);
        writer
            .saveString((fileType == FileType.JS_ANGULAR_PRETTY) ? map.toString(2) : map.toString(), new File(jsfile));
    }

    protected String suffix() {
        return ".json";
    }
}
