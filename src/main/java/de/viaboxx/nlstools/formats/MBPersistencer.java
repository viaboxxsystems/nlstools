package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundles;

import java.io.File;
import java.io.InputStream;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 29.12.2010<br>
 * Time: 16:41:46<br>
 * viaboxx GmbH, 2010
 */
public abstract class MBPersistencer {
    public abstract void save(MBBundles obj, File target) throws Exception;

    protected void mkdirs(File target) {
        File dir = target.getParentFile();
        if (dir != null && !dir.exists()) dir.mkdirs();
    }

    public abstract MBBundles load(File source) throws Exception;
    public abstract MBBundles load(InputStream source) throws Exception;

    public static MBPersistencer forName(String name) {
        if (name.endsWith(".xls")) {
            return new MBExcelPersistencer();
        } else if (name.endsWith(".xml")) {
            return new MBXMLPersistencer();
        } else if (name.endsWith(".js")) {
            return new MBJSONPersistencer(true);
        } else if (name.endsWith(".mem")) {
            return new MBInMemoryPersistencer();
        } else {
            throw new IllegalArgumentException("Format type not supported: " + name);
        }
    }

    public static MBPersistencer forFile(String aFile) {
        return forName(aFile.toLowerCase());
    }

    public static MBPersistencer forFile(File aFile) {
        return forName(aFile.getName().toLowerCase());
    }

    public static MBBundles loadFile(File aFile) throws Exception {
        return forFile(aFile).load(aFile);
    }

    public static void saveFile(MBBundles obj, File aFile) throws Exception {
        obj.sort();
        forFile(aFile).save(obj, aFile);
    }
}
