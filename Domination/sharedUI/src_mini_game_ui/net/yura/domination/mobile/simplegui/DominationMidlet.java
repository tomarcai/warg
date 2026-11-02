package net.yura.domination.mobile.simplegui;

import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.mobile.RiskMiniIO;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.plaf.LookAndFeel;
import net.yura.mobile.gui.plaf.SynthLookAndFeel;
import net.yura.util.Service;

public class DominationMidlet extends Application {

    @Override
    public void initialize(DesktopPane rootpane) {

        LookAndFeel lookandfeel=null;
        try {
                if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
                        lookandfeel = (SynthLookAndFeel)Class.forName("net.yura.android.plaf.AndroidLookAndFeel").newInstance();
                }
                //else if (Midlet.getPlatform() == Midlet.PLATFORM_BLACKBERRY) {
                //      lookandfeel = (LookAndFeel)Class.forName("net.yura.blackberry.plaf.BlackBerryLookAndFeel").newInstance();
                //}
        }
        catch (Throwable ex) {
                RiskUtil.printStackTrace(ex);
        }
        if (lookandfeel==null) {
                lookandfeel = new net.yura.mobile.gui.plaf.nimbus.NimbusLookAndFeel(16);
        }
        rootpane.setLookAndFeel( lookandfeel );






        RiskUtil.streamOpener = new RiskMiniIO();

        Risk risk = new Risk("luca.map","risk.cards");



        GameFrame mainMenu = new GameFrame(risk);

        mainMenu.setMaximum(true);
        mainMenu.setVisible(true);
    }

    @Override
    public DesktopPane makeNewRootPane() {
        return new DesktopPane(this, -1, null);
    }
}