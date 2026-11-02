package net.yura.domination.ui.swinggui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.guishared.RiskUIUtil;
import net.yura.util.Service;

public class PLAF {

    private SwingGUIPanel ui;
    private ButtonGroup lookAndFeelRadioGroup;
    private String lookAndFeel;

    Action setLookAndFeelAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent ae) {
            ButtonModel model = lookAndFeelRadioGroup.getSelection();
            String lookAndFeelName = model.getActionCommand();
            try {
                setLookAndFeel(lookAndFeelName);
            }
            catch (Exception ex) {
                RiskUtil.printStackTrace(ex);
                ui.showError("unable To Change LookAndFeel to " + lookAndFeelName);
            }
        }
    };
    
    public PLAF(SwingGUIPanel ui) {
        this.ui = ui;
        ui.getJMenuBar().add(createLookAndFeelMenu());
    }

    private JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("L&F");
        menu.setName("lookAndFeel");

        // Look for toolkit look and feels first
        UIManager.LookAndFeelInfo lookAndFeelInfos[] = UIManager.getInstalledLookAndFeels();
        lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        lookAndFeelRadioGroup = new ButtonGroup();
        for(UIManager.LookAndFeelInfo lafInfo: lookAndFeelInfos) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName()));
        }

        //try {
            // Now load any look and feels defined externally as service via java.util.ServiceLoader (java 1.6+)
            //java.util.ServiceLoader<LookAndFeel> LOOK_AND_FEEL_LOADER = java.util.ServiceLoader.load(LookAndFeel.class);
            //LOOK_AND_FEEL_LOADER.iterator();
            //for (LookAndFeel laf : LOOK_AND_FEEL_LOADER) {
            //    menu.add(createLookAndFeelItem(laf.toString(), laf.getClass().getName()));
            //}
        //}
        //catch (Throwable th) {
            Iterator<LookAndFeel> it = Service.providers(LookAndFeel.class);
            while (it.hasNext()) {
                LookAndFeel laf = it.next();
                menu.add(createLookAndFeelItem(laf.toString(), laf.getClass().getName()));
            }
        //}
        
        menu.addSeparator();
        JMenuItem fontUp = new JMenuItem("Font up");
        fontUp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                font(true);
            }
        });
        menu.add(fontUp);
        JMenuItem fontDown = new JMenuItem("Font down");
        fontDown.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                font(false);
            }
        });
        menu.add(fontDown);
        
        return menu;
    }

    private JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();

        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        try {
            lafItem.setHideActionText(true); // java 1.6+
        }
        catch (Throwable th) {}
        lafItem.setAction(setLookAndFeelAction);
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);

        return lafItem;
    }

    private void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException,
        InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {

        String oldLookAndFeel = this.lookAndFeel;

        if (oldLookAndFeel != lookAndFeel) {
            UIManager.setLookAndFeel(lookAndFeel);
            this.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            ui.firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }

    private void updateLookAndFeel() {
        
        Window[] windows;
        try {
            windows = Window.getWindows(); // only JAVA-1.6
        }
        catch (Throwable th) {
            windows = Frame.getFrames();
        }

        for(Window window : windows) {
            SwingUtilities.updateComponentTreeUI(window);
        }
        
        for(Component demoPanel : ui.getJTabbedPane().getComponents()) {
            SwingGUITab tab = (SwingGUITab)demoPanel;
            SwingUtilities.updateComponentTreeUI(tab.getToolBar());
        }
    }
    
    private void font(boolean up) {
        int szIncr = up ? 1 : -1; // Value to increase the size by
        RiskUIUtil.fontSize(szIncr);
        updateLookAndFeel();
    }
}
