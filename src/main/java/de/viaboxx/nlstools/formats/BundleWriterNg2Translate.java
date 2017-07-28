package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.codehaus.jettison.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Description: write one file named {locale}.json that contains
 * all bundles and is splitted into sub-maps per bundleName.part and key.part.
 * This conforms to the ng2translate format for Angular2<br>
 *
 * @see BundleWriterTypeScript to generate a typescript class with for the bundle keys
 */
public class BundleWriterNg2Translate extends BundleWriterJson {
    private boolean bundlesGenerated = false;

    public BundleWriterNg2Translate(Task task, String configFile,
                                    String outputPath,
                                    String outputFile, FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, null, outputPath, outputFile, fileType, allowedLocales);
    }

    protected boolean needsNewFiles() throws FileNotFoundException {
        return !bundlesGenerated;
    }

    protected void writeOutputFiles() throws Exception {
        deleteFiles();
        Iterator locales = getLocalesUsed().iterator();
        while (locales.hasNext()) {
            String locale = (String) locales.next();
            if (allowedLocales.isEmpty() || allowedLocales.contains(locale)) {
                writeOutputFilePerLocale(locale);
            }
        }
        bundlesGenerated = true;
    }

    protected List<String> getLocalesUsed() {
        if (myUsedLocales == null) {
            Set<String> locales = new HashSet<String>();
            for (MBBundle bundle : bundles.getBundles()) {
                locales.addAll(getLocalesUsed(bundle));
            }
            List<String> result = new ArrayList<String>(locales);
            Collections.sort(result);
            myUsedLocales = result;
        }
        return myUsedLocales;
    }

    protected String suffix() {
        return ".json";
    }

    protected void deleteFiles() {
        if (deleteOldFiles) {
            // try to delete only if directory exists
            File dir = new File(getOutputPath());
            if (fileType != FileType.NO && dir.exists()) {
                deleteFiles(dir.getPath() + "/*");
            }
        }
    }

    protected void writeOutputFilePerLocale(String locale) throws Exception {
        String jsfile = getFileName(locale);
        File outFile = new File(getOutputPath(), jsfile);
        mkdirs(outFile.getPath());
        task.log("writing ng2translate json file " + jsfile, Project.MSG_INFO);
        Properties properties = new Properties();
        for (MBBundle bundle : bundles.getBundles()) {
            List<String> path = split(bundle.getBaseName());
            Properties slot = prepareSlot(properties, path);
            addGroupedEntries(locale, slot, bundle, isMerged(), debugMode, task);
        }
        MBJSONPersistencer writer = new MBJSONPersistencer(true);
        JSONObject map = new JSONObject(properties);
        writer.saveString(map.toString(2), outFile);
    }

    public static void addGroupedEntries(String locale, Properties slot, MBBundle bundle, boolean merged,
                                         boolean debugMode, Task task) {
        Properties bundleProperties = createProperties(locale, bundle, merged, debugMode, task);
        for (Map.Entry entry : bundleProperties.entrySet()) {
            List<String> keys = split((String) entry.getKey());
            Properties targetSlot = slot;
            if (keys.size() > 1) {
                targetSlot = prepareSlot(slot, keys.subList(0, keys.size() - 1));
            }
            String key = keys.get(keys.size() - 1); // last part of keys
            String value = (String) entry.getValue();
            targetSlot.put(key, value);
        }
    }

    private static Properties prepareSlot(Properties properties, List<String> path) {
        Properties slot = properties;
        for (String each : path) {
            Object slot2 = slot.get(each);
            if (slot2 == null) {
                slot2 = new Properties();
                slot.put(each, slot2);
            }
            slot = (Properties) slot2;
        }
        return slot;
    }

    public static List<String> split(String baseOrKey) {
        StringTokenizer tokens = new StringTokenizer(baseOrKey, "/.");
        List<String> result = new ArrayList<String>(tokens.countTokens() - 1);
        String prevToken = null;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (prevToken != null) result.add(prevToken);
            prevToken = token;
        }
        if (prevToken != null) result.add(prevToken);
        return result;
    }

    protected String getFileName(String locale) {
        return locale + suffix();
    }
}
