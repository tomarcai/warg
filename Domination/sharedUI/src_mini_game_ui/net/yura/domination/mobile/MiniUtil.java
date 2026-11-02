package net.yura.domination.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.core.StatType;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.KeyEvent;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.io.FileUtil;
import javax.microedition.io.file.FileConnection;

public class MiniUtil {

    public static void showAbout() {
        if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
            Application.openURL("nativeNoResult://net.yura.domination.android.AboutActivity");
        }
        else {
            showPlatformIndependentAbout();
        }
    }

    private static void showPlatformIndependentAbout() {

        ResourceBundle resb = TranslationBundle.getBundle();

        String text = getAboutHtml();

        Button credits = new Button(resb.getString("about.tab.credits"));
        credits.setActionCommand("credits");
        credits.setName("SmallButton");
        Button license = new Button(resb.getString("about.tab.license"));
        license.setActionCommand("license");
        license.setName("SmallButton");
        Button changelog = new Button(resb.getString("about.tab.changelog"));
        changelog.setActionCommand("changelog");
        changelog.setName("SmallButton");
        Button ok = new Button(resb.getString("about.okbutton"));
        ok.setMnemonic( KeyEvent.KEY_END );
        ok.setName("SmallButton");

        OptionPane.showOptionDialog(new ActionListener() {
            public void actionPerformed(String actionCommand) {
                try {
                    if ("license".equals(actionCommand)) {
                        RiskUtil.openDocs("gpl.txt");
                    }
                    else if ("changelog".equals(actionCommand)) {
                        RiskUtil.openDocs("ChangeLog.txt");
                    }
                    else if ("credits".equals(actionCommand)) {
                        RiskUtil.openDocs("help/game_credits.htm");
                    }
                }
                catch(Exception e) {
                    OptionPane.showMessageDialog(null,"Unable to open info: "+e.getMessage(),"Error", OptionPane.ERROR_MESSAGE);
                }
            }
        } ,text, "\ud83c\uddfa\ud83c\udde6 \ud83c\udff3\ufe0f\u200d\ud83c\udf08 " + resb.getString("about.title") + " \ud83c\udff3\ufe0f\u200d\ud83c\udf08 \ud83c\uddfa\ud83c\udde6", 0, OptionPane.INFORMATION_MESSAGE,
        null, new Button[] {credits,license,changelog,ok} , ok);

    }

    public static String getAboutHtml() {
        ResourceBundle resb = TranslationBundle.getBundle();
        int year = Calendar.getInstance().get(Calendar.YEAR);
        String copyright = resb.getString("about.copyright").replaceAll("\\{0\\}", String.valueOf(year) );
        String author = resb.getString("about.author") + " Yura Mamyrin (yura@yura.net)";
        String c1="#DA4437",c2="#F6971D",c3="#F5EA3B",c4="#65AF45",c5="#4284F3",c6="#7E3793";

        String externalMapDirUrl = getExternalMapDir();

        String version = DominationMain.version;
        String versionName = System.getProperty("versionName");
        if (version != null && versionName != null && !version.equals(versionName)) {
            version = versionName + " (" + version + ")";
        }

        String displayInfo;

        if (Application.getPlatform() == Application.PLATFORM_IOS) {
            displayInfo = "<p>Screen scale: " + System.getProperty("display.scale");
        }
        else {
            // display.size = small/normal/large/...
            displayInfo = "<p>DPI: " + System.getProperty("display.dpi") + " Density: " + System.getProperty("display.density");
        }
        DesktopPane dp = DesktopPane.getDesktopPane();
        displayInfo += " Size: " + System.getProperty("display.size") + "<br/>window: " +dp.getWidth() + "x" + dp.getHeight() + "</p>";

        return "<html>" +
                "<div style=\"" +
// start CSS
"background-image: -webkit-gradient(linear, left top, left bottom, " +
    "color-stop(0%,   "+c1+"), color-stop(16.7%, "+c1+"), " +
    "color-stop(16.7%,"+c2+"), color-stop(33.3%, "+c2+"), " +
    "color-stop(33.3%,"+c3+"), color-stop(50%,   "+c3+"), " +
    "color-stop(50%,  "+c4+"), color-stop(66.7%, "+c4+"), " +
    "color-stop(66.7%,"+c5+"), color-stop(83.3%, "+c5+"), " +
    "color-stop(83.3%,"+c6+"), color-stop(100%,  "+c6+")" +
");"+
"height:30px;" +
// end CSS
                "\"></div>"+
                "<h3>yura.net "+RiskUtil.GAME_NAME+"</h3>"+
                "<p>"+DominationMain.product+" "+resb.getString("about.version")+" "+version + (RiskUtil.isOldVersion()?" OLD VERSION":"") + ("true".equals( System.getProperty("debug") )?" DEBUG":"")+"</p>"+
                "<p>"+"Game Engine: "+" "+RiskUtil.RISK_VERSION +"</p>"+
                "<p>"+author+"</p>"+
                "<p>"+copyright+"</p>"+
               // "<p>"+ resb.getString("about.comments") +"</p>"+
                displayInfo +
                "<p>Locale: "+Locale.getDefault()+" use: "+resb.getLocale()+"</p>"+
                "<p>Device: " + System.getProperty("microedition.platform") + (Application.getPlatform() == Application.PLATFORM_ME4SE ? " (java " + System.getProperty("java.vm.version") + ")" : "") + "</p>"+
                (externalMapDirUrl == null ? "" : "<p>ExternalMapDir=<a href=\"" + externalMapDirUrl + "\">" + getLabelForUrl(externalMapDirUrl) + "</a></p>") +
                // e.g. file:///storage/emulated/0/Domination%20Maps/
                "</html>";
    }

    private static String getLabelForUrl(String url) {
        if (url.startsWith("file:/")) {
            return JavaCompatUtil.replaceAll(url, "%20", " ");
        }
        try {
            FileConnection fileConnection = FileUtil.getReadFileConnection(url);
            return fileConnection.getName();
        }
        catch (Exception ex) {
            return url;
        }
    }

    public static String getStatsLabel(StatType statType, Player player) {
        String note;
        if (statType == StatType.COUNTRIES) {
            note = " (" + player.getNoTerritoriesOwned() + ")";
        }
        else if (statType == StatType.CARDS) {
            note = " (" + player.getCards().size() + ")";
        }
        else {
            note = "";
        }
        return player.getName() + note;
    }

    public static final String mapsdir = "file:///android_asset/maps/";

    /**
     * Gets a list of files from the maps folder
     * @param extension can be "map" or "cards"
     */
    public static List getFileList(String extension) {
        List result = new java.util.Vector();
        String dotExtension = "." + extension;

        String externalMapDirUrl = getExternalMapDir();
        if (externalMapDirUrl != null) {
            Enumeration<String> files = FileUtil.getDirectoryFiles(externalMapDirUrl);
            if (files != null) {
                while (files.hasMoreElements()) {
                    String name = files.nextElement();
                    if (name.endsWith(dotExtension)) {
                        result.add(name);
                    }
                }
            }
        }

        Enumeration en = FileUtil.getDirectoryFiles(mapsdir);
        while (en.hasMoreElements()) {
            String file = (String)en.nextElement();
            if (file.endsWith(dotExtension) && !result.contains(file)) {
                result.add( file );
            }
        }


        String[] list = getSaveMapDir().list();
        for (int c=0;c<list.length;c++) {
            String file = list[c];
            if (file.endsWith(dotExtension) && !result.contains(file)) {
                result.add( file );
            }
        }

        return result;
    }

    public static InputStream openMapStream(String name) throws IOException {
        try {
            String externalMapDirUrl = getExternalMapDir();
            if (externalMapDirUrl != null) {
                FileConnection fileConnection = FileUtil.getReadFileConnection(externalMapDirUrl + name);
                if (fileConnection.exists()) {
                    return fileConnection.openInputStream();
                }
            }
        }
        catch (Exception ex1) {
            RiskUtil.printStackTrace(ex1);
            // ignore if we cant find map in external dir.
        }

        try {
            File userMaps = MiniUtil.getSaveMapDir();
            File newFile = new File(userMaps, name);
            return new FileInputStream(newFile);
        }
        catch (Exception ex) {
            try {
                return FileUtil.getInputStreamFromFileConnector(mapsdir + name);
            }
            catch (Exception ex2) {
                // in case its a specific IOException, e.g. FileNotFoundException just throw that
                if (ex instanceof IOException) {
                    throw (IOException) ex;
                }

                IOException exception = new IOException(ex2.toString());
                exception.initCause(ex); // in android 1.6
                throw exception;
            }
        }
    }

    /**
     * This is really only used for android, as on desktop and iOS you can access the main maps folder
     */
    private static String externalMapsDirUrl;
    public static String getExternalMapDir() {
        if (externalMapsDirUrl != null) {
            return externalMapsDirUrl;
        }

        String ExternalStorageDirectory = System.getProperty("ExternalStorageDirectory");
        if (ExternalStorageDirectory != null) {
            File userMaps = new File(new File(ExternalStorageDirectory), RiskUtil.GAME_NAME + " Maps");
            //if (!userMaps.isDirectory() && !userMaps.mkdirs()) { // if it does not exist and i cant make it
            //    return null; // if we can not make it,
            //}

            externalMapsDirUrl = userMaps.toURI().toString();
            return externalMapsDirUrl;
        }
        return null;
    }
    public static void setExternalMapDir(String url) {
        externalMapsDirUrl = url;
    }

    private static File mapsDir;
    public static File getSaveMapDir() {

        if (mapsDir != null) {
            return mapsDir;
        }

        File userHome = new File( System.getProperty("user.home") );
        File userMaps = new File(userHome, "maps");
        if (!userMaps.isDirectory() && !userMaps.mkdirs()) { // if it does not exist and i cant make it
            throw new RuntimeException("can not create dir "+userMaps);
        }

        mapsDir = userMaps;
        return userMaps;
    }

    private static File savesDir;
    public static File getSaveGameDir() {

        if (savesDir!=null) {
            return savesDir;
        }

        File userHome = new File( System.getProperty("user.home") );
        File userMaps = new File(userHome, "saves");
        if (!userMaps.isDirectory() && !userMaps.mkdirs()) { // if it does not exist and i cant make it
            throw new RuntimeException("can not create dir "+userMaps);
        }

        savesDir = userMaps;
        return userMaps;
    }

    public static String getSaveGameName(RiskGame game) {
        String file = game.getMapFile();
        if (file.endsWith(".map")) {
            file = file.substring(0, file.length() - 4);
        }
        return file;
    }

    public static void openHelp() {
        try {
            RiskUtil.openDocs("help/rules.htm");
        }
        catch(Exception e) {
            OptionPane.showMessageDialog(null,"Unable to open manual: "+e.getMessage(),"Error", OptionPane.ERROR_MESSAGE);
        }
    }
}
