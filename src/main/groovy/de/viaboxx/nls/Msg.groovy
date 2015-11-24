package de.viaboxx.nls
/**
 * Description: Message trait for generated groovy-enums for
 * multi-bundle support<br>
 * <p>
 * Date: 23.11.15<br>
 * </p>
 */
trait Msg {
    /**
     * the real code in the referenced ResourceBundle.
     */
    String code

    /**
     * the ResourceBundle basename of the enum class
     * @return
     */
    abstract static String getBUNDLE_NAME()

    /**
     * access to the ResourceBundle name on the enum instance
     */
    String getBundleName() {
        return BUNDLE_NAME
    }

    /**
     * example of a method in the trait to combine bundleName and value
     * @return
     */
    String getBundleAndCode() {
        return bundleName + "#" + code
    }
}