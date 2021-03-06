package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import de.viaboxx.nlstools.util.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Description: <br>
 * User: roman.stumm <br>
 * Date: 15.06.2007 <br>
 * Time: 12:14:13 <br>
 * License: Apache 2.0
 */
public class BundleWriterJavaInterface extends BundleWriter {
    protected int indentNum = 0;
    protected int indentSize = 2;
    private String exampleLocale;

    public BundleWriterJavaInterface(Task task, String configFile, MBBundle currentBundle, String outputPath,
                                     FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType, allowedLocales);
    }

    public String getExampleLocale() {
        return exampleLocale;
    }

    public void setExampleLocale(String exampleLocale) {
        this.exampleLocale = exampleLocale;
    }

    protected String suffix() {
        return ".java";
    }

    /**
     * generate the current bundle's interface
     */
    public void writeOutputFiles() throws Exception {
        // now write the interface
        if (currentBundle.getInterfaceName() == null) return;
        String iffile = getInterfaceFileName();
        task.log("writing interface to: " + iffile, Project.MSG_INFO);
        mkdirs(iffile);
        Writer out = FileUtils.openFileWriterUTF8(new File(iffile));
        PrintWriter pw = new PrintWriter(out);
        try {
            writeStaticIntro(pw);
            if (isKeyNotContained()) {
                pw.println(
                    "// keys not contained (small interface). see .xml source file for possible keys");
            } else if (isKeysContained()) {
                writeConstants(pw, getCurrentBundle());
            }
            writeStaticOutro(pw);
        } finally {
            pw.close();
        }
    }

    protected boolean isKeysContained() {
        return FileType.JAVA_FULL == fileType || FileType.JAVA_FULL_ENUM_KEYS == fileType;
    }

    protected boolean isKeyNotContained() {
        return FileType.JAVA_SMALL == fileType || FileType.JAVA_ENUM_KEYS == fileType;
    }

    String getIPackage() {
        String inf = currentBundle.getInterfaceName();
        int pidx = inf.lastIndexOf('.');
        if (pidx < 0) {
            return "";
        }
        return inf.substring(0, pidx);
    }

    protected String getIClass() {
        String inf = currentBundle.getInterfaceName();
        int pidx = inf.lastIndexOf('.');
        if (pidx < 0) {
            return inf;
        }
        return inf.substring(pidx + 1);
    }

    protected String getInterfaceFileName() {
        StringBuilder fileName = new StringBuilder(getInterfacePathName());
        fileName.append("/");
        fileName.append(getIClass());
        fileName.append(suffix());
        return fileName.toString();
    }

    protected String getInterfacePathName() {
        String fileName = getOutputPath();
        char lastChar = fileName.charAt(fileName.length() - 1);
        if (lastChar != '\\' && lastChar != '/') {
            fileName += "/";
        }
        fileName += getIPackage().replace('.', '/');
        return fileName;
    }

    /**
     * Write the static beginning of the interface file.
     *
     * @param pw writer to write to
     */
    protected void writeStaticIntro(PrintWriter pw) {
        String str = getIPackage();
        if (str != null && str.length() > 0) {
            pw.print("package ");
            pw.print(str);
            pw.println(";");
        }
        pw.println();
        writeDoNotAlter(pw);
        writeType(pw);

        pw.println(" {");
        printIndent(pw).print("String _BUNDLE_NAME = \"");
        pw.print(currentBundle.getBaseName());
        pw.println("\";");
        pw.println();
        indentNum += indentSize;
    }

    protected void writeType(PrintWriter pw) {
        pw.print("public interface ");
        pw.print(getIClass());
    }

    protected void writeDoNotAlter(PrintWriter pw) {
        pw.println("/**");
        pw.print(" * contains keys of resource bundle ");
        pw.print(currentBundle.getBaseName());
        pw.println('.');
        pw.println(" * THIS FILE HAS BEEN GENERATED AUTOMATICALLY - DO NOT ALTER!");
        pw.println(" */");
    }

    /**
     * Write the static end of the interface file.
     *
     * @param pw writer to write to
     */
    protected void writeStaticOutro(PrintWriter pw) {
        pw.println("}");
        indentNum -= indentSize;
        writeDoNotAlter(pw);
    }

    /**
     * Write the constants to the interface file.
     *
     * @param pw      writer to write to
     * @param aBundle to read from
     */
    protected void writeConstants(PrintWriter pw, MBBundle aBundle) {
        Iterator<MBEntry> iter = aBundle.getEntries().iterator();
        boolean enumerateNames =
            (fileType == FileType.JAVA_ENUM_KEYS || fileType == FileType.JAVA_FULL_ENUM_KEYS);
        List<String> allNames = enumerateNames ? new ArrayList<String>() : null;
        while (iter.hasNext()) {
            MBEntry eachEntry = iter.next();
            String keyName = eachEntry.getKey();
            Iterator<MBText> texts = eachEntry.getTexts().iterator();
            printEntryComment(pw, eachEntry, texts);
            String theKey = createKeyName(keyName);
            if (enumerateNames) {
                allNames.add(theKey);
            }
            writeKeyValue(pw, theKey, keyName);
        }
        if (enumerateNames) {
            writeNameEnumeration(pw, allNames);
        }
    }

    protected void printEntryComment(PrintWriter pw, MBEntry eachEntry, Iterator<MBText> texts) {
        printIndent(pw).print("/** ");
        if (eachEntry.getDescription() != null && eachEntry.getDescription().length() > 0) {
            pw.print(eachEntry.getDescription());
            pw.print("\n  ");
        }
        if (eachEntry.getAliases() != null && !eachEntry.getAliases().isEmpty()) {
            pw.print(eachEntry.getAliases());
            pw.print("\n  ");
        }
        while (texts.hasNext()) {
            MBText theText = texts.next();
            String lang = theText.getLocale();
            pw.print("{");
            pw.print(lang);
            pw.print("} ");
        }
        MBText xmpl = eachEntry.findExampleText(getExampleLocale());
        if (xmpl != null) {
            pw.print(" | ");
            pw.print(xmpl.getLocale());
            pw.print(" = ");
            pw.print(StringEscapeUtils.escapeXml11(xmpl.getValue()));
        }
        pw.println(" */");
    }
    
    protected void writeKeyValue(PrintWriter pw, String key, String value) {
        printIndent(pw).print("String ");
        pw.print(key);
        pw.print(" = \"");
        pw.print(value);
        pw.println("\";");
    }

    protected String createKeyName(String keyName) {
        return keyName.replace('.', '_');
    }

    /**
     * Add an array of constant names to the interface for quick enumeration purposes.
     *
     * @param pw       writer to write to
     * @param allNames to iterate over and read from
     */
    private void writeNameEnumeration(PrintWriter pw, List<String> allNames) {
        printIndent(pw).print("  String[] _ALL_KEYS = {");
        for (Iterator<String> i = allNames.iterator(); i.hasNext(); ) {
            pw.print(i.next());
            if (i.hasNext()) {
                pw.print(", ");
            }
        }
        pw.println("};");
    }

    /**
     * true when generation is neccessary, false when up-to-date
     *
     * @throws java.io.FileNotFoundException
     */
    @Override
    protected boolean needsNewFiles() throws FileNotFoundException {
        if (currentBundle.getInterfaceName() == null) return false;
        File outfile = new File(getInterfaceFileName());
        if (!outfile.exists()) {
            return true;
        }
        File infile = new File(configFile);
        if (!infile.exists()) {
            throw new FileNotFoundException(infile + " not found");
        }
        return (infile.lastModified() > outfile.lastModified());
    }

    protected PrintWriter printIndent(PrintWriter pw) {
        for (int i = 0; i < indentNum; i++) {
            pw.print(" ");
        }
        return pw;
    }

}
