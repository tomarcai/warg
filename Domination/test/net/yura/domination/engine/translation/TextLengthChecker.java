package net.yura.domination.engine.translation;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * As this is not a junit test, we can not end the class name with 'Test'
 */
public class TextLengthChecker {
    
    public static void main(String[] args) throws Exception {

        new TranslationBundleTest(null).setUp();

        Map<String, Locale> longestLocale = new HashMap();
        Map<String, Integer> longestText = new HashMap();

        Locale[] locales = TranslationBundleTest.getAppLocales();
        for (Locale locale : locales) {
            TranslationBundle.setLanguage(locale.toString());
            ResourceBundle resb = TranslationBundle.getBundle();

            for (String key : resb.keySet()) {
                String value = resb.getString(key);

                Integer length = longestText.get(key);
                if (length == null || value.length() > length) {
                    longestText.put(key, value.length());
                    longestLocale.put(key, locale);
                }
            }
        }

        Map<Locale, Integer> results = new HashMap();
        for (Locale locale :  longestLocale.values()) {
            Integer result = results.get(locale);
            if (result == null) {
                results.put(locale, 1);
            }
            else {
                results.put(locale, result + 1);
            }
        }

        // print out what locales have the longest text
        results.entrySet().stream()
                .sorted((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()))
                .forEachOrdered(entry -> System.out.println(" -> " + entry));

        // Top 5
        // -> it=34
        // -> nl=34
        // -> ca=44
        // -> el=69
        // -> fr=81
    }
}
