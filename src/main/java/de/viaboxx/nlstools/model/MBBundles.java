package de.viaboxx.nlstools.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:21:45 <br/>
 * License: Apache 2.0
 */
@XStreamAlias("bundles")
public class MBBundles implements Cloneable {
    @XStreamImplicit
    private List<MBBundle> bundles = new ArrayList<MBBundle>();

    public void sort() {
        for (MBBundle each : getBundles()) {
            each.sort();
        }
    }

    public List<MBBundle> getBundles() {
        if (bundles == null) bundles = new ArrayList<MBBundle>();
        return bundles;
    }

    public void setBundles(List<MBBundle> bundles) {
        this.bundles = bundles;
    }

    public MBBundle getBundle(String baseName) {
        for (MBBundle each : bundles) {
            if ((baseName == null && null == each.getBaseName()) ||
                (baseName != null && baseName.equals(each.getBaseName()))) {
                return each;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MBBundles mbBundles = (MBBundles) o;

        return !(bundles != null ? !bundles.equals(mbBundles.bundles) : mbBundles.bundles != null);
    }


    public MBText findMBTextForLocale(String key, String locale) {
        for (MBBundle bundle : getBundles()) {
            for (MBEntry entry : bundle.getEntries()) {
                if (entry.getKey().equals(key)) {
                    for (MBText text : entry.getTexts()) {
                        if (text.getLocale().equals(locale)) {
                            return text;
                        }
                    }
                }
            }
        }
        return null;
    }

    public MBBundles copy() {
        try {
            MBBundles copy = (MBBundles) clone();
            copy.setBundles(new ArrayList<MBBundle>(getBundles().size()));
            for (MBBundle bundle : getBundles()) {
                copy.getBundles().add(bundle.copy());
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public void removeEntries() {
        for (MBBundle bundle : getBundles()) {
            bundle.removeEntries();
        }
    }

    public Set<String> locales() {
        HashSet<String> locales = new HashSet<String>();
        for(MBBundle bundle : getBundles()) {
            locales.addAll(bundle.locales());
        }
        return locales;
    }
}
