package de.viaboxx.nlstools;

import de.viaboxx.nlstools.formats.MBExcelPersistencer;
import de.viaboxx.nlstools.formats.MBXMLPersistencer;
import de.viaboxx.nlstools.model.MBBundles;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

/**
 * Description: <br>
 * User: roman.stumm<br>
 * Date: 29.12.2010<br>
 * Time: 15:07:56<br>
 * License: Apache 2.02010
 */
public class MBExcelPersistencerTest extends TestCase {
    private MBExcelPersistencer persistencer = new MBExcelPersistencer();

    public MBExcelPersistencerTest(String name) {
        super(name);
    }

    public void testWriteExcel() throws ClassNotFoundException, IOException {
        File target = new File("target/excelWritten.xls");
        MBBundles bundles = (MBBundles) new MBXMLPersistencer().load(new File("example/example.xml"));
        persistencer.save(bundles, target);
        assertTrue(target.exists());

        MBBundles bundles2 = persistencer.load(target);
        assertEquals(bundles, bundles2);
    }

    public void testReadExcel() throws IOException, ClassNotFoundException {
        File source = new File("example/excelExample.xls");

        MBBundles bundles = persistencer.load(source);
        assertNotNull(bundles);

        File target = new File("target/excelWritten2.xls");
        persistencer.save(bundles, target);

        MBBundles bundles2 = persistencer.load(target);
        assertEquals(bundles, bundles2);
    }
}
