package net.yura.domination.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Observer;
import java.util.ResourceBundle;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskIO;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.RiskGame;

public class TestUtil {

    public static void setupMapsForTest() throws Exception {
        // we must set the maps folder for map loading to work
        //RiskUIUtil.mapsdir = new File("game/Domination/maps").toURI().toURL();

        final File gameDir = new File(new File(System.getProperty("user.dir"), "game"), RiskUtil.GAME_NAME);
        final File mapsDir = new File(gameDir, "maps");

        RiskUtil.streamOpener = new RiskIO() {
            public InputStream openStream(String name) throws IOException {
                return new FileInputStream(new File(gameDir, name));
            }
            public InputStream openMapStream(String name) throws IOException {
                return new FileInputStream(new File(mapsDir, name));
            }
            public ResourceBundle getResourceBundle(Class c, String n, Locale l) {
                return ResourceBundle.getBundle(c.getPackage().getName() + "." + n, l);
            }
            public void openURL(URL url) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public void openDocs(String doc) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public void saveGameFile(String name, RiskGame obj) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public InputStream loadGameFile(String file) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public void getMap(String filename, Observer observer) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public java.io.OutputStream saveMapFile(String fileName) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public void renameMapFile(String oldName, String newName) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public boolean deleteMapFile(String mapName) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }

    public static Risk newRisk() throws Exception {
        setupMapsForTest();

        // this may or may not reload the ai wait time from game.ini file
        Risk risk = new Risk();

        // we want test to run quickly
        AIManager.setWait(0);
        Risk.setShowDice(false);

        return risk;
    }

    public static RiskGame newRiskGame() throws Exception {
        setupMapsForTest();

        return new RiskGame();
    }

    public static RiskGame createBasicMap(int noCountries) throws Exception {

        RiskGame map = TestUtil.newRiskGame();
        map.setupNewMap();

        Continent continent = new Continent("meow", "Meow", 5, 0);
        map.setContinents(new Continent[] { continent });

        Country[] countries = new Country[noCountries];
        for (int c = 0; c < noCountries; c++) {
            countries[c] = new Country(c + 1, "meow" + c, "Meow " + c, continent, 50 + 50 * c, 50);
            continent.addTerritoriesContained(countries[c]);
            if (c != 0) {
                countries[c].addNeighbour(countries[c - 1]);
                countries[c - 1].addNeighbour(countries[c]);
            }
            map.getCards().add(new Card(((c%3)==0) ? Card.CAVALRY : (  ((c%3)==1) ? Card.INFANTRY : Card.CANNON  ), countries[c]));
        }
        map.setCountries(countries);
        map.getCards().add(new Card(Card.WILDCARD, null));
        map.getCards().add(new Card(Card.WILDCARD, null));

        return map;
    }

    public static File getScriptFile(String mode) {
        File resFolder = new File("../res/test_scripts");
        return new File(resFolder, mode.replace(' ', '-') + ".log"); // RiskFileFilter.RISK_LOG_FILES
    }
}
