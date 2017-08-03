package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import org.apache.tools.ant.Task;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Description: generate a typescript class with for the bundle keys<br>
 */
public class BundleWriterTypeScript extends BundleWriterJavaInterface {
    public BundleWriterTypeScript(Task task, String configFile,
                                  MBBundle currentBundle,
                                  String outputPath, FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType, allowedLocales);
    }

    protected String suffix() {
        return ".ts";
    }

    protected String getInterfaceFileName() {
        return getInterfacePathName() + "/" +
            transformToFileName(getIClass()) +
            suffix();
    }

    private String transformToFileName(String className) {
        String[] parts = splitCamelCase(className);
        StringBuilder result = new StringBuilder();
        boolean dot = false;
        for (String part : parts) {
            if (dot) result.append(".");
            result.append(part.toLowerCase());
            dot = true;
        }
        return result.toString();
    }

    // DashboardBundle => Dashboard Bundle
    // from https://stackoverflow.com/questions/7593969/regex-to-split-camelcase-or-titlecase-advanced
    public static String[] splitCamelCase(String name) {
        return name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
    }

    protected void writeStaticIntro(PrintWriter pw) {
//        String str = getIPackage();
//        if (str != null && str.length() > 0) {
//            pw.print("package ");
//            pw.print(str);
//            pw.println(";");
//        }
//        pw.println();
        writeDoNotAlter(pw);
        writeType(pw);

        pw.println(" {");
//        pw.print("  String _BUNDLE_NAME = \"");
//        pw.print(currentBundle.getBaseName());
//        pw.println("\";");
        pw.println();
        indentNum += indentSize;
    }

    protected boolean isKeysContained() {
        return true;
    }

    protected void writeType(PrintWriter pw) {
        pw.print("export class ");
        pw.print(getIClass());
    }

    protected void writeKeyValue(PrintWriter pw, String key, String value) {
        printIndent(pw).print("static ");
        pw.print(key);
        pw.print(": string = \"");
        pw.print(value);
        pw.println("\";");
    }

    protected void writeConstants(PrintWriter pw, MBBundle bundle) {
        try {
            Properties map = new Properties();
            JSONObject slot = new JSONObject(map);
            for (String locale : bundle.locales()) {
                BundleWriterNg2Translate.addGroupedEntries(locale, slot, bundle, true, debugMode, task);

            }
            printEntries(pw, bundle, slot, "");
        } catch (JSONException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void printEntries(PrintWriter pw, MBBundle bundle, JSONObject slot, String context) throws JSONException {
        Iterator it = slot.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            Object value = slot.get(key);
            if (value instanceof String) {
                MBEntry eachEntry = bundle.getEntry(context + key);
                Iterator<MBText> texts = eachEntry.getTexts().iterator();
                printEntryComment(pw, eachEntry, texts);
                String theKey = nonReservedWord(createKeyName(key));
                String theValue = bundle.getBaseName().replace('/', '.') + "." + context + key;
                writeKeyValue(pw, theKey, theValue);
            } else {
                printIndent(pw).print("static ");
                indentNum += indentSize;
                pw.print(nonReservedWord(key));
                pw.println(" = class { ");
                String context2 = context + key + ".";
                printEntries(pw, bundle, (JSONObject) value, context2);  // recursion
                indentNum -= indentSize;
                printIndent(pw).println("};");
            }
        }
    }

    private String nonReservedWord(String word) {
        if (word.equals("new")) return "new_";
        if (word.equals("delete")) return "delete_";
        if (word.equals("class")) return "class_";
        if (word.equals("export")) return "export_";
        return word;
    }
}
