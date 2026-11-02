package net.yura.domination.engine.translation;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Locale;
import java.util.ResourceBundle;
import junit.framework.TestCase;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.test.TestUtil;


public class TranslationBundleTest extends TestCase {

    public TranslationBundleTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        TestUtil.setupMapsForTest();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAllPlaceholders() throws Exception {
        ResourceBundle defaultResourceBundle = RiskUtil.getResourceBundle(TranslationBundle.class, "Risk", Locale.ENGLISH);

        Locale[] locales = getAppLocales();
        for (Locale locale : locales) {
            TranslationBundle.setLanguage(locale.toString());
            ResourceBundle resb = TranslationBundle.getBundle();

            for (String key : defaultResourceBundle.keySet()) {
                String defaultValue = defaultResourceBundle.getString(key);
                if (defaultValue.contains("{0}")) {
                    String localeValue = resb.getString(key);
                    if (!localeValue.contains("{0}")) {
                        fail("no placeholder found for " + key +" in " +locale);
                    }
                }
            }
        }
    }
    
    /**
     * to update the compiled note, run:
     * sed -i 's+48.0/java1.4+49/java1.5+g' src/net/yura/domination/engine/translation/Risk*.properties
     */
    public void testSystemInfoString() {
        System.out.println("testSystemInfoString");

        //Locale[] locales = Locale.getAvailableLocales();
        Locale[] locales = getAppLocales();

        if (locales.length < 5) {
            fail("number too small");
        }

        for (Locale locale : locales) {

            TranslationBundle.setLanguage(locale.toString());
            ResourceBundle resb = TranslationBundle.getBundle();

            String text = resb.getString("about.infopanel");
            String[] split = text.split("\\n");

            if (split.length != 14) {
                fail("error in " + locale);
            }
            //if (!split[6].equals(" Screen: ")) {
            //    fail("error in " + locale + " >" + split[6] + "< ");
            //}
            
            String compiled = resb.getString("about.compiledfor");
            if (!compiled.contains("49/java1.5")) {
                fail("error2 in " + locale);
            }
            
            String playOnline = resb.getString("mainmenu.globe.playonline");
            int underlineIndex = playOnline.indexOf("<u>");
            if (underlineIndex > 0) {
                String o = playOnline.substring(underlineIndex + 3, playOnline.indexOf("</u>"));
                if (!o.equalsIgnoreCase("o")) {
                    fail("wrong letter underline " + o + " in " + locale);
                }
            }
        }
        System.out.println("testSystemInfoString PASS");
    }

    public static Locale[] getAppLocales() {
        File translation = new File("../src/net/yura/domination/engine/translation");
        
        System.out.println("looking in " + translation.getAbsolutePath());
        
        String[] files = translation.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.startsWith("Risk") && name.endsWith(".properties");
            }
        });
        Locale[] locales = new Locale[files.length];
        int c = 0;
        for (String file : files) {
            locales[c++] = getLocale(file);
        }
        return locales;
    }

    public static Locale getLocale(String fileName) {
        int index = fileName.indexOf('_');
        if (index < 0) {
            return TranslationBundle.getLocale("");
        }
        return TranslationBundle.getLocale(fileName.substring(index + 1, fileName.lastIndexOf('.')));
    }
}
