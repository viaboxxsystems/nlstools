package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import org.apache.tools.ant.Task;

import java.io.PrintWriter;
import java.util.Set;

/**
 * Description: <br>
 * <p>
 * Date: 24.11.15<br>
 * </p>
 */
public class BundleWriterGroovyEnum extends BundleWriterJavaInterface {
    public BundleWriterGroovyEnum(Task task, String configFile,
                                  MBBundle currentBundle,
                                  String outputPath, FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType, allowedLocales);
    }

    @Override
    protected String suffix() {
        return ".groovy";
    }

    protected boolean isKeysContained() {
        return true;
    }

    protected boolean isKeyNotContained() {
        return false;
    }

    protected void writeType(PrintWriter pw) {
        pw.print("enum ");
        pw.print(getIClass());
        pw.print(" implements Msg");
    }

    protected void writeKeyValue(PrintWriter pw, String key, String value) {
        pw.print("   ");
        pw.print(key);
        pw.print("('");
        pw.print(value);
        pw.println("'),");
    }

    protected void writeStaticIntro(PrintWriter pw) {
        String str = getIPackage();
        if (str != null && str.length() > 0) {
            pw.print("package ");
            pw.println(str);
        }
        pw.println();
        pw.println("import de.viaboxx.nls.Msg\n");
        writeDoNotAlter(pw);
        writeType(pw);
        pw.println(" {");
    }

    protected void writeStaticOutro(PrintWriter pw) {
        pw.println();
        pw.println("  private " + getIClass() + "(String code) {");
        pw.println("    this.code = code");
        pw.println("  }\n");
        pw.println("  static String getBUNDLE_NAME() {");
        pw.println("    return \"" + currentBundle.getBaseName() + "\"");
        pw.println("  }");
        if (currentBundle.getSqldomain() != null && currentBundle.getSqldomain().length() > 0) {
            pw.println();
            pw.println("  static String getSQL_DOMAIN() {");
            pw.println("    return \"" + currentBundle.getSqldomain() + "\"");
            pw.println("  }");
        }
        pw.println("}");
        writeDoNotAlter(pw);
    }
}
