package de.viaboxx.nlstools.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.*;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:21:50 <br/>
 * License: Apache 2.0
 */
@XStreamAlias("bundle")
public class MBBundle implements Cloneable {
    @XStreamAsAttribute
    private String baseName;
    @XStreamAsAttribute
    private String interfaceName;
    @XStreamAsAttribute
    private String sqldomain;
    @XStreamImplicit
    private List<MBEntry> entries = new ArrayList();

    public void sort() {
        if (entries != null) Collections.sort(entries);
        for (MBEntry each : getEntries()) {
            each.sort();
        }
    }

    public List<MBEntry> getEntries() {
        if (entries == null) entries = new ArrayList();
        return entries;
    }

    public void setEntries(List<MBEntry> entries) {
        this.entries = entries;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getSqldomain() {
        return sqldomain;
    }

    public void setSqldomain(String sqldomain) {
        this.sqldomain = sqldomain;
    }

    public MBEntry getEntry(String key) {
        for (MBEntry each : entries) {
            if ((key == null && each.getKey() == null) ||
                (key != null && key.equals(each.getKey()))) {
                return each;
            }
        }
        return null; // not found
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MBBundle mbBundle = (MBBundle) o;

        return !(baseName != null ? !baseName.equals(mbBundle.baseName) : mbBundle.baseName != null) &&
            !(entries != null ? !entries.equals(mbBundle.entries) : mbBundle.entries != null) &&
            !(interfaceName != null ? !interfaceName.equals(mbBundle.interfaceName) :
                mbBundle.interfaceName != null) &&
            !(sqldomain != null ? !sqldomain.equals(mbBundle.sqldomain) : mbBundle.sqldomain != null);

    }

    public MBBundle copy() {
        try {
            MBBundle copy = (MBBundle) clone();
            copy.setEntries(new ArrayList(getEntries().size()));
            for (MBEntry entry : getEntries()) {
                copy.getEntries().add(entry.copy());
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public void removeEntries() {
        getEntries().clear();
    }


    public Set<String> locales() {
        Set<String> locales = new HashSet<String>();
        for (MBEntry entry : getEntries()) {
            locales.addAll(entry.locales());
        }
        return locales;
    }
}
