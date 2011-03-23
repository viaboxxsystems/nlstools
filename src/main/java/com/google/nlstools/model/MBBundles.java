package com.google.nlstools.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:21:45 <br/>
 * Copyright: Viaboxx GmbH
 */
@XStreamAlias("bundles")
public class MBBundles {
    @XStreamImplicit
    private List<MBBundle> bundles = new ArrayList();

    public void sort() {
       for (MBBundle each : bundles) {
           each.sort();
       }
    }

    public List<MBBundle> getBundles() {
        if (bundles == null) bundles = new ArrayList();
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
}
