package de.viaboxx.nlstools.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Description: <br/>
 * User: roman.stumm <br/>
 * Date: 14.06.2007 <br/>
 * Time: 15:21:57 <br/>
 * License: Apache 2.0
 */
@XStreamAlias("entry")
public class MBEntry implements Comparable, Cloneable {
    @XStreamAsAttribute
    private String key;
    private String description;  // comment field
    @XStreamImplicit(itemFieldName = "text")
    private List<MBText> texts = new ArrayList<MBText>();

    public void sort() {
        if (texts != null) Collections.sort(texts);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MBText> getTexts() {
        if (texts == null) texts = new ArrayList<MBText>();
        return texts;
    }

    public void setTexts(List<MBText> texts) {
        this.texts = texts;
    }

    public MBText getText(String locale) {
        for (MBText text : getTexts()) {
            if (locale != null && locale.equals(text.getLocale())) {
                return text;
            }
        }
        return null;
    }

    public MBText findExampleText(String exampleLocale) {
        if (StringUtils.isEmpty(exampleLocale)) {
            for (MBText each : getTexts()) {
                if (each.getValue() != null && each.getValue().length() > 0) {
                    return each;
                }
            }
        } else {
            return getText(exampleLocale);
        }
        return null;
    }

    public boolean isReview(String reviewLocale) {
        if (texts == null || texts.isEmpty()) return true;
        if (reviewLocale == null) {
            for (MBText each : texts) {
                if (each.isReview() ||
                        (!each.isUseDefault() && StringUtils.isEmpty(each.getValue())))
                    return true;
            }
            return false;
        } else {
            MBText text = getText(reviewLocale);
            return (text == null || text.isReview() ||
                    (!text.isUseDefault() && StringUtils.isEmpty(text.getValue())));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MBEntry mbEntry = (MBEntry) o;
        return !(description != null ? !description.equals(mbEntry.description) : mbEntry.description != null) &&
                !(key != null ? !key.equals(mbEntry.key) : mbEntry.key != null) &&
                !(texts != null ? !texts.equals(mbEntry.texts) : mbEntry.texts != null);
    }

    public int compareTo(Object o) {
        if (o instanceof MBEntry) {
            if (getKey() != null) {
                return getKey().compareTo(((MBEntry) o).getKey());
            } else {
                return 1;
            }
        }
        return -1;
    }

    public MBEntry copy() {
        try {
            MBEntry entry = (MBEntry) clone();
            entry.setTexts(new ArrayList<MBText>(getTexts().size()));
            for (MBText text : getTexts()) {
                entry.getTexts().add(text.copy());
            }
            return entry;
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }
}
