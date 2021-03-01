

package filemanager.fileexplorer.misc;

import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * Simple static methods to be called at the start of your own methods to verify
 * correct arguments and state.
 */
public class Preconditions {
    public static void checkArgument(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkArgumentNotNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void checkArgumentEquals(String expected, @Nullable String actual, String message) {
        if (!TextUtils.equals(expected, actual)) {
            throw new IllegalArgumentException(String.format(message, String.valueOf(expected),
                    String.valueOf(actual)));
        }
    }

    public static void checkState(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }


    /**
     * Ensures that an object reference passed as a parameter to the calling
     * method is not null.
     *
     * @param reference an object reference
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }

    /**
     * Ensures that an object reference passed as a parameter to the calling
     * method is not null.
     *
     * @param reference an object reference
     * @param errorMessage the exception message to use if the check fails; will
     *     be converted to a string using {@link String#valueOf(Object)}
     * @return the non-null reference that was validated
     * @throws NullPointerException if {@code reference} is null
     */
    public static <T> T checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
        return reference;
    }
}
