package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundles;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 02.05.2011<br>
 * Time: 14:53:59<br>
 * License: Apache 2.02010
 */
public class MBInMemoryPersistencer extends MBPersistencer {
    private static Map<String, MBBundles> memory = Collections.synchronizedMap(new HashMap());

    @Override
    public void save(MBBundles obj, File target) throws Exception {
        memory.put(target.getName(), obj);
    }

    @Override
    public MBBundles load(File source) throws Exception {
        return getCopy(source.getName());
    }

    @Override
    public MBBundles load(InputStream source) throws Exception {
        return getCopy(source.toString());
    }

    public MBBundles getCopy(String name) {
        MBBundles bundles = memory.get(name);
        if (bundles != null) {
            bundles = bundles.copy();
        }
        return bundles;
    }

    public static void clear() {
        memory.clear();
    }
}
