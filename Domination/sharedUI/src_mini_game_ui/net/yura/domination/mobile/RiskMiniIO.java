package net.yura.domination.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.Observer;
import java.util.ResourceBundle;
import javax.microedition.io.Connector;
import net.yura.domination.engine.RiskIO;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.mobile.gui.Application;
import net.yura.mobile.logging.Logger;
import net.yura.util.Service;

/**
 * @author Yura Mamyrin
 */
public class RiskMiniIO implements RiskIO {

    static {
        // hack for AI to work on android
        if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
            Service.SERVICES_LOCATION = "assets/services/";
        }
    }

    public InputStream openStream(String name) throws IOException {
        return Connector.openInputStream("file:///android_asset/"+name);
    }

    public InputStream openMapStream(String name) throws IOException {
        return MiniUtil.openMapStream(name);
    }

    public ResourceBundle getResourceBundle(Class c, String n, Locale l) {
        return ResourceBundle.getBundle(c.getPackage().getName()+"."+n, l );
    }

    public void openURL(URL url) throws Exception {
        // TODO should we be using this method? maybe we should just call platformRequest
        boolean success = Application.openURL(url.toString());
        if (!success) {
            throw new Exception("unable to open url: " + url);
        }
    }

    public void openDocs(String doc) throws Exception {
        boolean success = Application.openURL("file:///android_asset/" + doc );
        if (!success) {
            throw new Exception("unable to open doc: " + doc);
        }
    }

    public InputStream loadGameFile(String file) throws Exception {
        return new FileInputStream(file);
    }

    public void saveGameFile(String file, RiskGame obj) throws Exception {
        obj.saveGame(new FileOutputStream(file));
    }

    public OutputStream saveMapFile(String fileName) throws Exception {
        return RiskUtil.getOutputStream( MiniUtil.getSaveMapDir(), fileName);
    }

    public void getMap(String filename, Observer observer) {
        net.yura.domination.mapstore.GetMap.getMap(filename, observer);
    }

    public void renameMapFile(String oldName, String newName) {
        File mapsDir = MiniUtil.getSaveMapDir();
        File oldFile = new File(mapsDir, oldName);
        File newFile = new File(mapsDir, newName);
        try {
            RiskUtil.rename(oldFile, newFile);
        }
        catch (RuntimeException ex) {
            Logger.info("maps dir: " + mapsDir + " files list: " + Arrays.asList(mapsDir.list()));
            throw ex;
        }
    }

    public boolean deleteMapFile(String mapUID) {
        File mapFile = new File(MiniUtil.getSaveMapDir(), mapUID);
        if (mapFile.exists()) {
            return mapFile.delete();
        }
        return false;
    }
}
