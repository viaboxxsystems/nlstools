package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import de.viaboxx.nlstools.model.MBBundles;
import org.apache.tools.ant.Task;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 29.12.2010<br>
 * Time: 15:25:42<br>
 * License: Apache 2.02010
 */
public class BundleWriterExcel extends BundleWriter {
    private MBBundles bundles;
    private File target;

    public BundleWriterExcel(MBBundles bundles, File target) {
        this(null);
        this.bundles = bundles;
        this.target = target;
    }

    public BundleWriterExcel(MBBundle currentBundle) {
        super(null, null, currentBundle, null, null, null);
    }

    public BundleWriterExcel(Task task, String configFile, MBBundle currentBundle, String outputPath,
                             FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType, allowedLocales);
    }

    @Override
    protected String suffix() {
        return ".xls";
    }

    @Override
    public List<String> getLocalesUsed() {
        return super.getLocalesUsed();    // call super!
    }

    public void writeOutputFiles() throws Exception {
        bundles.sort();
        new MBExcelPersistencer().save(bundles, target);
    }

}
