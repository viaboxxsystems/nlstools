package de.viaboxx.nls

import junit.framework.TestCase

/**
 * Description: unit testing Msg trait<br>
 */
class MsgTest extends TestCase {
    public void testCombineMethods() {
        assertEquals("fakeBundle#Label1", FakeMsg.Label1.bundleAndCode)
        assertEquals("FakeMsg{fakeBundle#Label1}", FakeMsg.Label1.classBundleAndCode)
        assertEquals("FakeMsg.Label1", FakeMsg.Label1.classDotCode)
        assertEquals("FakeMsg.Label1{1,test}", FakeMsg.Label1.classDotCode([1, "test"] as Object[]))
        assertEquals("FakeMsg.Label1{1,test}", FakeMsg.Label1.classDotCode([1, "test"]))
    }
}
