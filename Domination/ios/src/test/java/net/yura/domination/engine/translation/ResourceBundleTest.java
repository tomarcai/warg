package net.yura.domination.engine.translation;

import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mobile.RiskMiniIO;
import org.junit.Test;
import java.util.ResourceBundle;
import static org.junit.Assert.*;

public class ResourceBundleTest {

    @Test
    public void testLanguages() throws Exception {
        RiskUtil.streamOpener = new RiskMiniIO();

        // TODO get this automatically
        String[] locales = {"da", "en", "fr", "lv", "pt", "sk", "tr", "ca", "de", "es", "gl", "nl", "pt_PT", "sr", "uk", "cs", "el", "fi", "it", "pl", "ru", "sv", "zh"};

        for (String locale : locales) {
            testLanguage(locale);
        }

        System.out.println("DONE - testLanguages");
        //assertEquals(4, 2 + 2);
    }

    public void testLanguage(String name) throws Exception {
        System.out.println("CHECKING " + name);

        try {
            //Locale.setDefault(Locale.forLanguageTag(name));
            TranslationBundle.setLanguage(name);
            ResourceBundle resourceBundle = TranslationBundle.getBundle();
            assertTrue("no keys for " + name, resourceBundle.keySet().size() > 10);

            MapTranslator mapTranslator = new MapTranslator();
            mapTranslator.setMap(RiskGame.getDefaultMap());
            mapTranslator.setCards(RiskGame.getDefaultCards());
        }
        catch (Exception ex) {
            throw new Exception("can not load language " + name, ex);
        }
    }
}
