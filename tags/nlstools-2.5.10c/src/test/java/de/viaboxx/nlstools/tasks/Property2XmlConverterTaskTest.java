package de.viaboxx.nlstools.tasks;

import junit.framework.TestCase;

/**
 * Description: <br>
 * <p>
 * User: roman.stumm<br>
 * Date: 16.11.12<br>
 * Time: 15:34<br>
 * viaboxx GmbH, 2012
 * </p>
 */
public class Property2XmlConverterTaskTest extends TestCase {
    /**
     * test fix for issue 1:
     * Use of illegal Chars for Excel Sheet Name & creation of target file fails because directory is not being created
     */
    public void testConvertToExcel() {
        Property2XMLConverterTask task = new Property2XMLConverterTask();
        task.setFromProperty("src/test/resources/sample/Keys");
        task.setTo("target/Keys.xls");
        task.setLocales("de;en");
        task.setInterfaceName("foo.Keys");
        task.execute();
    }
}
