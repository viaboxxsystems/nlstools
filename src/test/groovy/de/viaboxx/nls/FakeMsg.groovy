package de.viaboxx.nls

/**
 * Description: enum only for testing Msg trait<br>
 */
enum FakeMsg implements Msg {
    Label1('Label1'),
    Label2('Label2')

    private FakeMsg(String code) {
      this.code = code
    }

    static String getBUNDLE_NAME() {
      return "fakeBundle"
    }
}
