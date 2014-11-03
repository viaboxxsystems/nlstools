package de.viaboxx.nlstools.tasks;

import de.viaboxx.nlstools.formats.*;
import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * <p>Description: Generates the interface with message constants and property files
 * (resource bundles) from the descriptions in a xml file.</p>
 * <p/>
 * <p/>
 * <br>Values for:<br>
 * <pre>
 * writeProperties = true (default=false)
 *      (=.properties), false, xml (=.xml)
 * writeJson = true  (default=false)
 *      (=compressed .js file), false, pretty (formatted .js file)
 *      angular (for angular-localization, see https://github.com/doshprompt/angular-localization)
 *      angular_pretty (for angular-localization prettyprinted, see https://github.com/doshprompt/angular-localization)
 * writeInterface = true  (default=false)
 *      (keys + bundle name), false, small_enum, small (bundle name only), Flex (ActionScript class), smallFlex (bundle name only)
 *      enum (keys + bundle name + enum of all keys), small_enum (bundle name + enum of all keys)
 *
 * This utility can generate:
 * Properties: .properties, .xml
 * JSON: .js
 * SQL-Script: .sql
 * Interface: .java
 * </pre>
 * <p/>
 * <strong>Configuration:</strong>
 * <pre>
 * bundles = the XML (or Excel)-bundles input file(s), separated by ;
 * sourcePath = to write .java interface to
 * propertyPath = to write .properties/.xml to
 * toCharset = (optional, default: null) the charset, e.g. UTF-8, to write properties files
 * jsonPath = to write .js to
 * jsonFile = null or the hard-coded json file name
 * sqlScriptDir = to write .sql to
 * flexLayout=true: output format is "de_DE/path/bundle.properties"  (Adobe Flex directory format)
 * flexLayout=false: output format is "path/bundle_de_DE.properties" (default, java style)
 * preserveNewlines=true: newlines will *not* be escaped when writing properties files
 * preserveNewlines=false: (default) newlines will be escaped when writing properties files
 * allowedLocales: ';'-separated list of locales that should be written, all other locales will NOT be written, if empty, all locales are written.
 * overwrite = (default false)
 * deleteOldFiles = (default true)
 * merged (has effect when writing .properties files for json files)
 * merged = (default true) - true: also include properties from parent-bundles
 *                  (to keep the properties small, set merged=false - requires fallback at runtime of your app)
 *                      - false: only own properties, parent properties not included in output
 *                      (if you want the java.util.ResourceBundle to look'em up)
 * debugMode = (default false) ignores translations and sets the key as label
 * exampleLocale = a locale, e.g. 'en' (default: the first available locale) optional. to force the flex/java interface to use this locale's text in their example comment
 * </pre>
 * <strong>Example:</strong>
 * <pre>
 * &lt;taskdef name="msgbundle" classname="de.viaboxx.nlstools.tasks.MessageBundleTask">
 * &lt;classpath refid="maven.test.classpath"/>
 * &lt;/taskdef>
 *
 * &lt;msgbundle overwrite="true" bundles="src/main/bundles/Customer.xml;../utilities/base/src/main/bundles/Common.xml"
 * writeProperties="true"
 * writeJson="true"
 * jsonPath="src\main\webapp\js"
 * jsonFile="i18n"
 * propertyPath="src\main\webapp\WEB-INF\classes"/>
 * </pre>
 *
 * @author Roman Stumm
 */
public class MessageBundleTask extends Task {
    protected String bundles;
    protected String sourcePath = ".";
    protected String propertyPath = ".";
    protected String jsonPath = ".";
    protected String jsonFile = null;

    private String sqlScriptDir = null;

    private boolean overwrite = false, deleteOldFiles = true;
    private String writeProperties = "false";
    private String writeJson = "false";
    private String writeInterface = "false";
    private boolean debugMode = false;
    private Boolean flexLayout;
    private boolean preserveNewlines = false;
    private boolean merged = true;

    /**
     * charset (e.g. UTF-8) of the .properties Files - if they do not use the default charset (e.g. Grails i18n files)
     * By default, the ISO 8859-1 character encoding is used (see javadoc of java.util.Properties)
     */
    private String toCharset = null;

    private MBBundles parsedBundles;
    private String xmlConfigBundle;

    private Set<String> allowedLocales;
    private String exampleLocale;

    public String getToCharset() {
        return toCharset;
    }

    public void setToCharset(String toCharset) {
        this.toCharset = toCharset;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = getProject().resolveFile(jsonPath).getPath();
    }

    public boolean isFlexLayout() {
        return flexLayout != null && flexLayout;
    }

    public void setFlexLayout(boolean flexLayout) {
        this.flexLayout = flexLayout;
    }

    public boolean isMerged() {
        return merged;
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    /**
     * if true, delete old .properties files before generating new ones.
     *
     * @param aDeleteOldFiles - (default = true)
     */
    public void setDeleteOldFiles(boolean aDeleteOldFiles) {
        deleteOldFiles = aDeleteOldFiles;
    }

    public String getWriteProperties() {
        return writeProperties;
    }

    public void setWriteProperties(String aWriteProperties) {
        writeProperties = aWriteProperties;
    }

    public String getWriteJson() {
        return writeJson;
    }

    public void setWriteJson(String writeJson) {
        this.writeJson = writeJson;
    }

    /**
     * true, false, small, Flex, smallFlex when the interface file shall be generated
     */
    public String getWriteInterface() {
        return writeInterface;
    }

    public void setWriteInterface(String aWriteInterface) {
        writeInterface = aWriteInterface;
    }

    /**
     * true when the files shall be generated, even if they are up-to-date
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean aOverwrite) {
        overwrite = aOverwrite;
    }

    /**
     * true when the label shall be the same as the key
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * the path+name of the xml files (; separated)
     */
    public String getBundles() {
        return bundles;
    }

    public void setBundles(String aConfigFile) {
        bundles = aConfigFile;
    }

    public void setSourcePath(String aSourcePath) {
        sourcePath = getProject().resolveFile(aSourcePath).getPath();
    }

    /**
     * the root path to store the property files
     */
    public String getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(String aPropertyPath) {
        propertyPath = getProject().resolveFile(aPropertyPath).getPath();
    }

    public void execute() throws BuildException {
        try {
            for (MBBundle o : loadBundles().getBundles()) {
                handleInterface(o);
                handleProperties(o);
                handleJson(o);
                handleSql(o);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }

    private void handleSql(MBBundle o) throws Exception {
        final BundleWriter.FileType fileType;
        if (o.getSqldomain() == null) {
            fileType = BundleWriter.FileType.NO;
        } else {
            fileType = BundleWriter.FileType.SQL;
        }
        executeBundleWriter(
            new BundleWriterSql(this, getXMLConfigBundle(), o, getSqlScriptDir(), fileType, allowedLocales));
    }

    private void handleInterface(MBBundle o) throws Exception {
        final BundleWriter.FileType fileType;
        if (getWriteInterface().equalsIgnoreCase("small")) {
            fileType = BundleWriter.FileType.JAVA_SMALL;
        } else if (getWriteInterface().equalsIgnoreCase("false")) {
            fileType = BundleWriter.FileType.NO;
        } else if (getWriteInterface().equalsIgnoreCase("small_enum")) {
            fileType = BundleWriter.FileType.JAVA_ENUM_KEYS;
        } else if (getWriteInterface().equalsIgnoreCase("enum")) {
            fileType = BundleWriter.FileType.JAVA_FULL_ENUM_KEYS;
        } else if (getWriteInterface().equalsIgnoreCase("Flex")) {
            BundleWriterFlexClass writer = new BundleWriterFlexClass(this, getXMLConfigBundle(), o, sourcePath,
                BundleWriter.FileType.FLEX_FULL, allowedLocales);
            writer.setExampleLocale(getExampleLocale());
            executeBundleWriter(writer);
            return;
        } else if (getWriteInterface().equalsIgnoreCase("smallFlex")) {
            BundleWriterFlexClass writer = new BundleWriterFlexClass(this, getXMLConfigBundle(), o, sourcePath,
                BundleWriter.FileType.FLEX_SMALL, allowedLocales);
            writer.setExampleLocale(getExampleLocale());
            executeBundleWriter(writer);
            return;
        } else {
            fileType = BundleWriter.FileType.JAVA_FULL;
        }
        BundleWriterJavaInterface writer =
            new BundleWriterJavaInterface(this, getXMLConfigBundle(), o, sourcePath, fileType, allowedLocales);
        writer.setExampleLocale(getExampleLocale());
        executeBundleWriter(writer);
    }

    private void handleProperties(MBBundle o) throws Exception {
        BundleWriter.FileType fileType;
        if (getWriteProperties().equalsIgnoreCase("false")) {
            fileType = BundleWriter.FileType.NO;
        } else if (getWriteProperties().equalsIgnoreCase("xml")) {
            fileType = BundleWriter.FileType.XML;
        } else {
            fileType = BundleWriter.FileType.PROPERTIES;
        }
        BundleWriterProperties writer = new BundleWriterProperties(this, getXMLConfigBundle(), o,
            getPropertyPath(), fileType, allowedLocales);
        writer.setMerged(isMerged());
        if (getToCharset() != null) {
            writer.setCharset(getToCharset());
        }
        executeBundleWriter(writer);
    }

    private void handleJson(MBBundle o) throws Exception {
        BundleWriter.FileType fileType;
        if (getWriteJson().equalsIgnoreCase("false")) {
            fileType = BundleWriter.FileType.NO;
        } else if (getWriteJson().equalsIgnoreCase("pretty")) {
            fileType = BundleWriter.FileType.JS_PRETTY;
        } else if (getWriteJson().equalsIgnoreCase("angular")) {
            fileType = BundleWriter.FileType.JS_ANGULAR;
        } else if (getWriteJson().equalsIgnoreCase("angular_pretty")) {
            fileType = BundleWriter.FileType.JS_ANGULAR_PRETTY;
        } else {
            fileType = BundleWriter.FileType.JS;
        }
        BundleWriterJson writer = BundleWriterJson.build(this, getXMLConfigBundle(), o, getJsonPath(),
            getJsonFile(), fileType, allowedLocales);
        writer.setMerged(isMerged());
        executeBundleWriter(writer);
    }

    public String getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(String jsonFile) {
        this.jsonFile = jsonFile;
    }

    private void executeBundleWriter(BundleWriter writer) throws Exception {
        if (flexLayout != null) writer.setFlexLayout(isFlexLayout());
        writer.setOverwrite(overwrite);
        writer.setDeleteOldFiles(deleteOldFiles);
        writer.setDebugMode(debugMode);
        writer.execute();
    }

    /**
     * read/parse XML file
     */
    protected MBBundles loadBundles() throws Exception {
        if (parsedBundles == null) {
            StringTokenizer tokens = new StringTokenizer(getBundles(), ";");
            while (tokens.hasMoreTokens()) {
                String bundlesFileName = getProject().resolveFile(tokens.nextToken()).getPath();
                log("Reading bundles from " + bundlesFileName, Project.MSG_INFO);
                MBBundles loadedBundles = MBPersistencer.loadFile(new File(bundlesFileName));
                if (parsedBundles == null) {
                    parsedBundles = loadedBundles;
                    xmlConfigBundle = bundlesFileName;
                } else {
                    mergeBundles(loadedBundles);
                }
                if (preserveNewlines) {
                    for (MBBundle bundle : parsedBundles.getBundles()) {
                        for (MBEntry mbEntry : bundle.getEntries()) {
                            for (MBText mbText : mbEntry.getTexts()) {
                                mbText.setValue(mbText.getValue().replace("\\n", "\n"));
                            }
                        }
                    }
                }

            }
        }
        return parsedBundles;
    }

    private String getXMLConfigBundle() {
        return xmlConfigBundle;
    }

    private void mergeBundles(MBBundles loadedBundles) {
        log("Merge bundles ...", Project.MSG_VERBOSE);
        for (MBBundle bundle : loadedBundles.getBundles()) {
            // Enhancement NYI - duplettencheck
            for (MBBundle parsedBundle : parsedBundles.getBundles()) {
                parsedBundle.getEntries().addAll(bundle.getEntries());
            }
        }
    }

    public String getSqlScriptDir() {
        return sqlScriptDir;
    }

    public void setSqlScriptDir(String aSqlScriptDir) {
        sqlScriptDir = getProject().resolveFile(aSqlScriptDir).getPath();
    }

    public boolean isPreserveNewlines() {
        return preserveNewlines;
    }

    public void setPreserveNewlines(boolean preserveNewlines) {
        this.preserveNewlines = preserveNewlines;
    }

    public void setAllowedLocales(String allowedLocales) {
        this.allowedLocales = new HashSet<String>((Arrays.asList(allowedLocales.split(";"))));

    }

    public String getExampleLocale() {
        return exampleLocale;
    }

    public void setExampleLocale(String exampleLocale) {
        this.exampleLocale = exampleLocale;
    }
}