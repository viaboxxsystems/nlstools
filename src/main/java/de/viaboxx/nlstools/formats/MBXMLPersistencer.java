package de.viaboxx.nlstools.formats;

import com.thoughtworks.xstream.XStream;
import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
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
    static final XStream xstream = new XStream();
    private boolean noTexts = false;

    static {
        configure(xstream);
    }

    static void configure(XStream xstream) {
        xstream.processAnnotations(MBBundle.class);
        xstream.processAnnotations(MBBundles.class);
        xstream.processAnnotations(MBEntry.class);
//        xstream.processAnnotations(MBText.class);
        xstream.registerConverter(new MBTextConverter());
    }

    public void save(MBBundles obj, File target) throws IOException {
        mkdirs(target);
        Writer out = FileUtils.openFileWriterUTF8(target);
        if(noTexts) {
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
        if(options != null && options.contains("-no-texts")) noTexts = true;
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
