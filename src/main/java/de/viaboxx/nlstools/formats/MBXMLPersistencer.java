package de.viaboxx.nlstools.formats;

import com.thoughtworks.xstream.XStream;
import de.viaboxx.nlstools.model.*;
import de.viaboxx.nlstools.util.FileUtils;

import java.io.*;

/**
 * Description: <br>
 * User: roman.stumm <br>
 * Date: 14.06.2007 <br>
 * Time: 15:25:25 <br>
 * License: Apache 2.0
 */
public class MBXMLPersistencer extends MBPersistencer {
    public static final XStream xstream;
    private boolean noTexts = false;

    static {
        xstream = new XStream();
        configure(xstream);
    }

    static void configure(XStream xstream) {
        Class[] types = new Class[]{MBBundle.class, MBBundles.class, MBEntry.class, MBFile.class, MBText.class};
        // security config
        XStream.setupDefaultSecurity(xstream);
        xstream.allowTypes(types);
        xstream.allowTypeHierarchy(MBText.class);

        // annotation processing
        xstream.processAnnotations(types);
        xstream.registerConverter(new MBTextConverter());
    }

    public void save(MBBundles obj, File target) throws IOException {
        mkdirs(target);
        Writer out = FileUtils.openFileWriterUTF8(target);
        if (noTexts) {
            obj = obj.copy();
            obj.removeEntries();
        }
        try {
            xstream.toXML(obj, out);
        } finally {
            out.close();
        }
    }

    @Override
    public MBPersistencer withOptions(String options) {
        if (options != null && options.contains("-no-texts")) noTexts = true;
        return super.withOptions(options);    // call super!
    }


    public MBBundles load(File source) throws IOException, ClassNotFoundException {
        Reader reader = FileUtils.openFileReaderUTF8(source);
        try {
            return load(reader);
        } finally {
            reader.close();
        }
    }

    public MBBundles load(Reader reader) throws IOException, ClassNotFoundException {
        return (MBBundles) xstream.fromXML(reader);
    }

    public MBBundles load(InputStream in) throws IOException, ClassNotFoundException {
        return (MBBundles) xstream.fromXML(in);
    }

    public static XStream getXstream() {
        return xstream;
    }
}
