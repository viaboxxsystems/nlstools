package de.viaboxx.nls

import org.apache.commons.lang3.ArrayUtils

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
     * combine bundleName and code
     * @return bundleName + "#" + code
     */
    String getBundleAndCode() {
        return bundleName + "#" + code
    }

    String getClassBundleAndCode() {
        return getClass().simpleName + '{' + bundleAndCode + '}'
    }

    /**
     * combine simpleName of class and code
     * @return getClass ( ) .simpleName + "." + code
     */
    String getClassDotCode() {
        return getClass().simpleName + "." + code
    }

    String classDotCode(Object[] args) {
        return getClassDotCode() + ArrayUtils.toString(args)
    }

    String classDotCode(Collection args) {
        return getClassDotCode() + ArrayUtils.toString(args as Object[])
    }
}