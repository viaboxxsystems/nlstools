package de.viaboxx.nlstools.formats;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.util.FileUtils;

import java.io.*;

/**
 * Description: Load/Save JSON Format with XStream<br>
 * User: roman.stumm <br>
 * Date: 14.06.2007 <br>
 * Time: 15:29:50 <br>
 * License: Apache 2.0
 */
public class MBJSONPersistencer extends MBPersistencer {
    public static final XStream xstream;
    public static final XStream xstream_pretty;

    private boolean pretty;
    private boolean noTexts = false;

    static {
        xstream = new XStream(new JettisonMappedXmlDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        MBXMLPersistencer.configure(xstream);

        xstream_pretty = new XStream(new JsonHierarchicalStreamDriver());
        xstream_pretty.setMode(XStream.NO_REFERENCES);
        MBXMLPersistencer.configure(xstream_pretty);
    }

    public MBJSONPersistencer(boolean pretty) {
        this.pretty = pretty;
    }

    @Override
    public MBPersistencer withOptions(String options) {
        if (options != null && options.contains("-no-texts")) noTexts = true;
        return super.withOptions(options);    // call super!
    }

    public void save(MBBundles object, File file) throws Exception {
        mkdirs(file);
        if (noTexts) {
            object = object.copy();
            object.removeEntries();
        }
        saveObject(object, file);
    }

    public void saveString(String json, File target) throws IOException {
        Writer out = FileUtils.openFileWriterUTF8(target);
        try {
            out.write(json);
        } finally {
            out.close();
        }
    }

    public void saveObject(Object obj, File target) throws Exception {
        Writer out = FileUtils.openFileWriterUTF8(target);

        try {
            if (pretty) {
                xstream_pretty.toXML(obj, out);
            } else {
                xstream.toXML(obj, out);
            }
        } finally {
            out.close();
        }
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

}
