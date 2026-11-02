package net.yura.swingme.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import net.yura.mobile.logging.Logger;
import net.yura.mobile.util.Properties;

/**
 * @author Yura Mamyrin
 */
public class CoreUtil {

    /**
     * @see net.yura.mobile.gui.layout.XULLoader#getPropertyText(java.lang.String, boolean) 
     */
    public static Properties wrap(final ResourceBundle res) {
        return new Properties() {
            public String getProperty(String key) {
                try {
                    return res.getString(key);
                }
                catch (Exception ex) {
                    // sometimes this method is used by the XULLoader, but sometimes it is used directly
                    // from code, thats why for those cases we should not ever return null, as a string is expected
                    Logger.warn("String not found " + key, ex);
                    return "???"+key+"???";
                }
            }
        };
    }

    /**
     * allows loading {@link Preferences} into {@link net.yura.mobile.gui.layout.XULLoader#setFormData(Hashtable)}
     */
    public static Hashtable<String, String> asHashtable(final Preferences prefs) {
        return new Hashtable<String, String>(0) {
            @Override
            public synchronized Enumeration<String> keys() {
                try {
                    return Collections.enumeration(Arrays.asList(prefs.keys()));
                }
                catch (BackingStoreException ex) {
                    throw new IllegalStateException("unable to get keys", ex);
                }
            }

            @Override
            public synchronized String get(Object key) {
                return prefs.get(String.valueOf(key), null);
            }

            @Override
            public synchronized String put(String key, String value) {
                throw new UnsupportedOperationException("type unknown");
            }
        };
    }
}
