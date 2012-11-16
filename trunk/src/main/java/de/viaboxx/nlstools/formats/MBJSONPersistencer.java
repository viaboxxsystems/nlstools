package de.viaboxx.nlstools.formats;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Description: Load/Save JSON Format with XStream<br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:29:50 <br/>
 * Copyright: Viaboxx GmbH
 */
public class MBJSONPersistencer extends MBPersistencer {
    static final XStream xstream;
    static final XStream xstream_pretty;

    private boolean pretty;

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

    public void save(MBBundles object, File file) throws Exception {
        mkdirs(file);
        saveObject(object, file);
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
            return (MBBundles) xstream.fromXML(reader);
        } finally {
            reader.close();
        }
    }

}
