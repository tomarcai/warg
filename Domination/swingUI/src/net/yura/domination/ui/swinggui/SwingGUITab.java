package net.yura.domination.ui.swinggui;

import javax.swing.JToolBar;
import javax.swing.JMenu;

public interface SwingGUITab {

    /**
     * This method is called whenever we switch to this Tab
     */
    public JToolBar getToolBar();
    public JMenu getMenu();
    public String getName();
}
