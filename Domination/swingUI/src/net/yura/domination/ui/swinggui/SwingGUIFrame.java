// Yura Mamyrin, Group D

package net.yura.domination.ui.swinggui;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.yura.domination.engine.Risk;
import net.yura.domination.guishared.RiskUIUtil;
import net.yura.domination.guishared.AboutDialog;

/**
 * <p> Swing GUI Main Frame </p>
 * @author Yura Mamyrin
 */
public class SwingGUIFrame {

	public static void main(final String[] argv) {
            
                // sets label in top left of menu bar
                System.setProperty( "apple.awt.application.name", SwingGUIPanel.product );
                
                // supports macOS dark mode, this does NOT currently work
                //System.setProperty( "apple.awt.application.appearance", "system" );
            
		RiskUIUtil.parseArgs(argv);

                // always use the system menu bar for menus on macOS
                System.setProperty("apple.laf.useScreenMenuBar", "true");

                final Risk r = new Risk();

                // before we create any UI, we want to load up all settings
                Preferences prefs = SwingGUIPanel.getUIPreferences();
                RiskUIUtil.loadSettingsFromPrefs(prefs);

                RiskUIUtil.initAudio(r);

		final SwingGUIPanel sg = new SwingGUIPanel( r );

                // allow drop of game save file
                sg.setDropTarget(new DropTarget() {
                    @Override
                    public synchronized void drop(DropTargetDropEvent event) {
                        try {
                            if (!event.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                                event.rejectDrop();
                                return;
                            }

                            event.acceptDrop(DnDConstants.ACTION_COPY);
                            
                            List<File> files = (List<File>) event.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                            if (files.size() != 1) {
                                event.rejectDrop();
                                return;
                            }

                            File file = files.get(0);
                            if (file.getName().endsWith(".save")) {
                                sg.go("loadgame " + file.getAbsolutePath());
                            }
                            else {
                                event.rejectDrop();
                                return;
                            }

                            event.dropComplete(true);

                        } catch (Exception e) {
                            event.dropComplete(false);
                            e.printStackTrace();
                        }
                    }
                });
                
                
		final JFrame gui = new JFrame();
                // old docs: https://developer.apple.com/library/archive/technotes/tn2007/tn2196.html
                //gui.getRootPane().putClientProperty("apple.awt.brushMetalLook", true);

                // get rid of line between title bar and jtoolbar
                gui.getRootPane().putClientProperty( "apple.awt.transparentTitleBar", true);

		gui.setContentPane( sg );
                gui.setJMenuBar( sg.getJMenuBar() );
		gui.setTitle( SwingGUIPanel.product );
		gui.setIconImage(Toolkit.getDefaultToolkit().getImage( AboutDialog.class.getResource("icon.gif") ));
                gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {

                        // we HAVE to run pack() from UI thread or we get a deadlock on jdk1.8 Ubuntu20.04.1
                        // because pack() first takes the AWTTreeLock then calls ... JViewport.reshape
                        // that fires AtkWrapper.emitSignal(ac, AtkSignal.OBJECT_VISIBLE_DATA_CHANGED, null)
                        // that calls at org.GNOME.Accessibility.AtkUtil.invokeInSwing(AtkUtil.java:68)
                        // and tells a component to revalidate that tries to get the AWTTreeLock again
                        gui.pack();

                        RiskUIUtil.center(gui);
                        RiskUIUtil.setMinimumSize(gui, gui.getPreferredSize());

                        gui.setVisible(true);

                        // dirty hack, for JDK19 on MacOS Arm64
                        // for some reason without this, the main window SOMETIEMS does not repaint.
                        gui.paint(gui.getGraphics());

                        RiskUIUtil.openFile(argv,r);
                    }
                });

                // use main thread to check for map updates
		sg.checkForUpdates();
	}
}
