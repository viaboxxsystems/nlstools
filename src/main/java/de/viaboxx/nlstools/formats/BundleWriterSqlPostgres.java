package de.viaboxx.nlstools.formats;

import de.viaboxx.nlstools.model.MBBundle;
import org.apache.tools.ant.Task;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

/**
 * Description: <br>
 * <p>
 * Date: 24.11.15<br>
 * </p>
 */
public class BundleWriterSqlPostgres extends BundleWriterSqlOracle {
    public BundleWriterSqlPostgres(Task task, String configFile,
                                   MBBundle currentBundle, String outputPath,
                                   FileType fileType, Set<String> allowedLocales) {
        super(task, configFile, currentBundle, outputPath, fileType, allowedLocales);
    }

    protected void writeSeqNextVal(String domain, Writer fw) throws IOException {
        fw.write("-- CREATE SEQUENCE SEQ_NLSBundle " +
            "  INCREMENT 1 " +
            "  MINVALUE 1 " +
            "  START 1 " +
            "  CACHE 10;\n");
        fw.write("INSERT INTO NLSBUNDLE (ID, DOMAIN) SELECT NEXTVAL('SEQ_NLSBundle'),'" +
            domain + "';\n");
    }
}
