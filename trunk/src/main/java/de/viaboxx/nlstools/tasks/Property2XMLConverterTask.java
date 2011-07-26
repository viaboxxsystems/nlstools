package de.viaboxx.nlstools.tasks;

import de.viaboxx.nlstools.formats.MBPersistencer;
import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import de.viaboxx.nlstools.model.MBEntry;
import de.viaboxx.nlstools.model.MBText;
import de.viaboxx.nlstools.util.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.io.*;
import java.util.*;

/**
 * <p>Description: Create a bundles-xml (or excel) file fromProperty some plain property (or xml) files</p>
 *
 * @author Roman Stumm
 */
public class Property2XMLConverterTask extends Task {
    private boolean xml = false;
    private String fromProperty, to, locales;
    private String interfaceName = "";
    /**
     * charset (e.g. UTF-8) of the .properties Files - if they do not use the default charset (e.g. Grails i18n files)
     * By default, the ISO 8859-1 character encoding is used (see javadoc of java.util.Properties)
     */
    private String fromCharset = null;

    public String getFromCharset() {
        return fromCharset;
    }

    public void setFromCharset(String fromCharset) {
        this.fromCharset = fromCharset;
    }

    public boolean isXml() {
        return xml;
    }

    public void setXml(boolean xml) {
        this.xml = xml;
    }

    /**
     * name of the interface for writing into the xml error-sections file
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String aInterfaceName) {
        interfaceName = aInterfaceName;
    }

    /**
     * path/filename without _de_DE.properties /
     * each locale + .properties will be added to this string to find the properties source file to convert
     *
     * @return
     */
    public String getFromProperty() {
        return fromProperty;
    }

    public void setFromProperty(String aFromProperty) {
        fromProperty = aFromProperty;
    }

    /**
     * path/filename of the xml error-section file to be created
     *
     * @return
     */
    public String getTo() {
        return to;
    }

    public void setTo(String aToXML) {
        to = aToXML;
    }

    /**
     * all locales for which a resource bundle exists, separated by ;
     *
     * @return
     */
    public String getLocales() {
        return locales;
    }

    public void setLocales(String aLocales) {
        locales = aLocales;
    }

    public void execute() throws BuildException {
        MBBundles bundles = new MBBundles();
        MBBundle bundle = new MBBundle();
        bundles.getBundles().add(bundle);

        try {
            bundle.setInterfaceName(getInterfaceName());
            bundle.setBaseName(getInterfacePackage().replace('.', '/') + "/" +
                    getPropertyBaseName());

            StringTokenizer tokens = new StringTokenizer(getLocales(), ";");
            Map properties = new HashMap();
            Set allKeys = new HashSet();
            while (tokens.hasMoreTokens()) {
                String eachLocale = tokens.nextToken();
                if (eachLocale.equals("-")) eachLocale = "";   // - steht fuer die default-locale!!!
                Properties prop = new Properties();

                String fname = eachLocale.length() > 0
                        ? getFromProperty() + "_" + eachLocale
                        : getFromProperty();

                if (!xml) {
                    if (getFromCharset() == null) { // use default charset
                        InputStream stream = new FileInputStream(fname + ".properties");
                        prop.load(stream);
                        stream.close();
                    } else {
                        Reader reader = FileUtils.openFileReader(new File(fname+".properties"), getFromCharset());
                        prop.load(reader);
                        reader.close();
                    }
                } else {
                    InputStream stream = new FileInputStream(fname + ".xml");
                    prop.loadFromXML(stream);
                    stream.close();
                }
                properties.put(eachLocale, prop);
                allKeys.addAll(prop.keySet());
            }
            List allKeysList = new ArrayList(allKeys);
            Collections.sort(allKeysList);
            for (Object anAllKeysList : allKeysList) {
                String key = (String) anAllKeysList;
                MBEntry entry = new MBEntry();
                bundle.getEntries().add(entry);
                entry.setKey(key);
                tokens = new StringTokenizer(getLocales(), ";");
                while (tokens.hasMoreTokens()) {
                    String eachLocale = tokens.nextToken();
                    if (eachLocale.equals("-")) eachLocale = "";

                    Properties prop = (Properties) properties.get(eachLocale);
                    if (prop.containsKey(key)) {
                        MBText text = new MBText();
                        entry.getTexts().add(text);
                        text.setLocale(eachLocale);
                        text.setValue(prop.getProperty(key));
                    }
                }
            }
            MBPersistencer.saveFile(bundles, new File(getTo()));
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e);
        }
    }

    protected String getPropertyBaseName() {
        String fullName = getFromProperty();
        fullName = fullName.replace('\\', '/');
        int idx = fullName.lastIndexOf('/');
        if (idx < 0) return fullName;
        return fullName.substring(idx + 1);
    }

    protected String getInterfacePackage() {
        int idx = getInterfaceName().lastIndexOf('.');
        if (idx < 0) return "";
        return getInterfaceName().substring(0, idx);
    }
}
