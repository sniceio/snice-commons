/**
 * 
 */
package io.snice.preconditions;

/**
 * Contains common checks for null etc.
 *
 * Pattern:
 *
 * <ul>
 *     <li>All assertXXXX will throw a {@link IllegalArgumentException}</li>
 *     <li> All checkXXXX-methods will check the boolean expression and if not
 *     true, throw an {@link IllegalArgumentException}</li>
 *     <li>All ensureXXXX-methods is essentially the same as assertXXXX but they also
 *     return the passed in value if it is valid. This for a more fluent API, in case that is
 *     what you're looking for.</li>
 * </ul>
 *
 * @author jonas@jonasborjesson.com
 */
public final class PreConditions {

    private PreConditions() {
        // nothing to be created. All methods are static...
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ------------------------- Assert Methods -----------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * Ensure that the reference is indeed null.
     *
     * @return
     * @throws IllegalArgumentException in case the value isn't null
     */
    public static <T> void assertNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference != null) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static <T> void assertNull(final T reference) throws IllegalArgumentException {
        assertNull(reference, "Value must be null");
    }

    public static <T> T assertNotNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static <T> T assertNotNull(final T reference) throws IllegalArgumentException {
        return assertNotNull(reference, "Value cannot be null");
    }

    public static String assertNotEmpty(final String reference, final String msg) throws IllegalArgumentException {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static void assertArgument(final boolean expression, final String msg) throws IllegalArgumentException {
        if (!expression) {
            throw new IllegalArgumentException(msg);
        }
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ---------------------- Assert Methods for Arrays ---------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * If you work a lot with arrays you may find yourself needing to check that the offset and lengths, which
     * is something you typically use when working with raw arrays, are within the bounds of the array itself.
     *
     * @param array
     * @param offset
     * @param length
     * @throws IllegalArgumentException
     */
    public static void assertArray(final byte[] array, final int offset, final int length, final String msg) throws IllegalArgumentException {
        if (array == null) {
            throw new IllegalArgumentException(msg);
        }

        assertArrayBoundaries(array.length, offset, length, msg);
    }

    public static void assertArray(final byte[] array) throws IllegalArgumentException {
        if (array == null) {
            throw new IllegalArgumentException("The byte array cannot be null");
        }

        assertArray(array, 0, array.length);
    }

    private static void assertArrayBoundaries(final int arraySize, final int offset, final int length, final String msg) throws IllegalArgumentException {
        if (length > arraySize || offset < 0 || (offset != 0 && offset >= arraySize) || offset + length > arraySize || length < 0 ) {
            throw new IllegalArgumentException(msg);
        }
    }

    public static void assertArray(final byte[] array, final int offset, final int length) throws IllegalArgumentException {
        assertArray(array, offset, length, "The byte-array and the offset and/or length does not match up");
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ------------------------- Ensure Methods -----------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    public static <T> T ensureNotNull(final T reference, final String msg) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    public static <T> T ensureNotNull(final T reference) throws IllegalArgumentException {
        if (reference == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        return reference;
    }

    public static String ensureNotEmpty(final String reference, final String msg) throws IllegalArgumentException {
        if (reference == null || reference.isEmpty()) {
            throw new IllegalArgumentException(msg);
        }
        return reference;
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------
    // ------------------------- Check Methods ------------------------------
    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    /**
     * Check if a string is empty, which includes null check.
     * 
     * @param string
     * @return true if the string is either null or empty
     */
    public static boolean checkIfEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean checkIfNotEmpty(final String string) {
        return !checkIfEmpty(string);
    }


    /**
     * If our reference is null then return a default value instead.
     * 
     * @param reference the thing to check.
     * @param defaultValue the default value to return if the above reference is null.
     * @return the reference if not null, otherwise the default value. Note, if your default value
     *         is null as well then you will get back null, since that is what you asked. Chain with
     *         {@link #assertNotNull(Object, String)} if you want to make sure you have a non-null
     *         value for the default value.
     */
    public static <T> T ifNull(final T reference, final T defaultValue) {
        if (reference == null) {
            return defaultValue;
        }
        return reference;
    }

}
