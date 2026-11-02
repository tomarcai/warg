package net.yura.domination.mobile.flashgui;

import java.util.List;
import java.util.Locale;
import java.io.File;
import javax.microedition.lcdui.Image;
import net.yura.domination.audio.GameSound;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mobile.MiniUtil;
import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.border.BackgroundBorder;
import net.yura.mobile.gui.border.Border;
import net.yura.mobile.gui.border.EdgeToEdgeBorder;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.FileChooser;
import net.yura.mobile.gui.components.Frame;
import net.yura.mobile.gui.components.Menu;
import net.yura.mobile.gui.components.MenuBar;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.Window;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.io.FileUtil;
import net.yura.mobile.util.Properties;
import net.yura.mobile.util.Url;

/**
 * @author Yura Mamyrin
 */
public class MainMenu extends Frame implements ActionListener {

    // shares res
    Properties resb = GameWindow.resb;
    public Risk myrisk;
    MiniFlashRiskAdapter controller;

    XULLoader mainMenu;

    // main menu res
    FileChooser chooser;

    public static final Border background;
    static {
        Image bkg = Application.createImage("/war_blood.png");
        background = bkg == null ? null : new EdgeToEdgeBorder(new BackgroundBorder(bkg));
    }

    public MainMenu(Risk risk,MiniFlashRiskAdapter controller) {
        myrisk = risk;
        this.controller = controller;

        //setTitle( resb.getProperty("mainmenu.title") );
        setUndecorated(true);

        setMaximum(true);

        setBorder(background);
        setBackground( 0x00FFFFFF );
    }

    public void openMainMenu() {
	mainMenu = GameWindow.getPanel("/mainmenu.xml",this);

        setContentPane((Panel) mainMenu.getRoot());
        revalidate();

        setVisible(true);
        moveToBack();
    }

    public void setPlayGamesSingedIn(final boolean in) {
        DesktopPane.invokeLater(new Runnable() {
            @Override
            public void run() {
                // as this can make changes to the currently visible UI, we need to run it on the UI thread
                XULLoader mm = mainMenu;
                if (mm!=null) {
                    Button showAchievements = (Button)mm.find("showAchievements");
                    Button signIn = (Button)mm.find("signIn");
                    Button signOut = (Button)mm.find("signOut");
                    if (showAchievements != null) { showAchievements.setVisible(true); }
                    if (signIn != null) { signIn.setVisible(!in); }
                    if (signOut != null) { signOut.setVisible(in); }
                    Window window = mm.getRoot().getWindow();
                    window.revalidate();
                    window.repaint();
        	}
            }
        });
    }

    private void moveToBack() {
        // we want to always be at the bottom of the stack
        // so move anything bellow us to be above us
        List windows = getDesktopPane().getAllFrames();
        for (int c=1;c<windows.size();c++) {
            getDesktopPane().setSelectedFrame((Window)windows.get(0));
        }
    }

    @Override
    public void actionPerformed(String actionCommand) {

            GameSound.INSTANCE.playSound("online".equals(actionCommand) ? GameSound.LOBBY_START : GameSound.MENU_BUTTON);

            if ("new game".equals(actionCommand)) {
                myrisk.parser("newgame");
            }
            else if ("load game".equals(actionCommand)) {

                FileUtil fileSystemView = new FileUtil() {
                    @Override
                    public String getRoots() {
                        return MiniUtil.getSaveGameDir() + "/";
                    }
                    @Override
                    public java.util.Vector listFiles(String dir, int filter, boolean recent) {
                        String[] saves = new File(dir).list();
                        List<String> result = new java.util.Vector();
                        for (int c = 0; c < saves.length; c++) {
                            if (saves[c].endsWith(DominationMain.SAVE_EXTENSION)) {
                                result.add(saves[c]);
                            }
                        }
                        return JavaCompatUtil.asVector(result);
                    }

                    @java.lang.Override
                    public void deleteFile(String path) {
                        new File(path).delete();
                    }
                };

                chooser = new FileChooser(fileSystemView);

                Panel contentPane = chooser.getContentPane();
                // hide address bar
                ((Component)contentPane.getComponents().get(0)).setVisible(false);

                MenuBar menuBar = chooser.getMenuBar();
                for (int c = 0; c < menuBar.getComponentCount(); c++) {
                    Component comp = (Component) menuBar.getItems().get(c);
                    if (comp instanceof Menu) {
                        menuBar.remove(comp);
                        break;
                    }
                }

                chooser.showDialog(this, "doLoad", resb.getProperty("mainmenu.loadgame.loadbutton"), resb.getProperty("mainmenu.loadgame.loadbutton"));
            }
            else if ("doLoad".equals(actionCommand)) {

                String file = chooser.getSelectedFile();
                chooser = null;

                if (file.endsWith( DominationMain.SAVE_EXTENSION )) {
                    myrisk.parser("loadgame " + file);
                }
                // else ignore file
            }
            else if (FileChooser.NO_FILE_SELECTED.equals(actionCommand)) {
                chooser = null;
            }
            else if ("manual".equals(actionCommand)) {
                MiniUtil.openHelp();
            }
            else if ("about".equals(actionCommand)) {
                MiniUtil.showAbout();
            }
            else if ("quit".equals(actionCommand)) {
                DominationMain.quit();
            }
            else if ("donate".equals(actionCommand)) {
                try {
                    RiskUtil.donate();
                }
                catch (Exception ex) {
                    RiskUtil.printStackTrace(ex);
                }
            }
            else if ("online".equals(actionCommand)) {
        	controller.openLobby();
            }
            else if ("join game".equals(actionCommand)) {
                OptionPane.showMessageDialog(null,"not done yet","Error", OptionPane.ERROR_MESSAGE);
            }
            else if ("start server".equals(actionCommand)) {
                OptionPane.showMessageDialog(null,"not done yet","Error", OptionPane.ERROR_MESSAGE);
            }
            else if ("feedback".equals(actionCommand)) {
                String email = "yura@yura.net";
                String url = "mailto:" + email +
                        "?subject=" + Url.encode(
                                RiskUtil.GAME_NAME+" "+RiskUtil.RISK_VERSION+" "+
                                DominationMain.product+" "+DominationMain.version+" "+
                                Locale.getDefault()+" Feedback").replace("+", "%20") // platforms do not seem to support + char here
                        +"&body=" + Url.encode(
                                "\n\n\nDevice: "+System.getProperty("http.agent")+
                                "\nID: "+MiniLobbyClient.getMyUUID()).replace("+", "%20");

                try {
                    // RiskUtil.openURL(url);
                    // boolean success = Application.openURL(url);
                    // we do not want to trigger grasshopper when there is no mail app
                    Application.getInstance().platformRequest(url);
                }
                catch (Exception ex) {
                    OptionPane.showMessageDialog(null, "Please email " + email, "contact", OptionPane.INFORMATION_MESSAGE);
                }
            }
            else if ("signIn".equals(actionCommand)) {
        	DominationMain.getGooglePlayGameServices().beginUserInitiatedSignIn();
            }
            else if ("signOut".equals(actionCommand)) {
        	DominationMain.getGooglePlayGameServices().signOut();
        	setPlayGamesSingedIn(false);
            }
            else if ("showAchievements".equals(actionCommand)) {
        	DominationMain.getGooglePlayGameServices().showAchievements();
            }
            else {
                System.err.println("MainMenu unknown command: "+actionCommand);
            }
    }

}
