// Yura Mamyrin

package net.yura.domination.ui.swinggui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.guishared.RiskUIUtil;
import net.yura.grasshopper.submitter.BugSubmitter;

/**
 * @author Yura Mamyrin
 */
public class BugsPanel extends JPanel implements ActionListener, SwingGUITab {

        private static final Logger logger = Logger.getLogger(BugsPanel.class.getName());
    
	private JToolBar toolbar;
	private JTextArea text;
	private JTextField from;
        private SwingGUIPanel gui;

	public BugsPanel(SwingGUIPanel sgp) {
                gui = sgp;
		setName( "Report Bugs" );
		setOpaque(false);
		toolbar = new JToolBar();
		toolbar.setRollover(true);
		toolbar.setFloatable(false);

		JButton send = new JButton("\u2332 SEND MESSAGE");
		send.setActionCommand("send");
		send.addActionListener(this);
		toolbar.add(send);

		text = new JTextArea();
		from = new JTextField();

		setLayout( new BorderLayout() );

		JPanel top = new JPanel();
		top.setLayout( new BorderLayout() );
		top.setOpaque(false);

		top.add( new JLabel("type your bug/suggestion to yura and hit send at the top"), BorderLayout.NORTH );
		top.add( new JLabel("your Email:") , BorderLayout.WEST );
		top.add( from );

		add( top, BorderLayout.NORTH );
		add( new JScrollPane(text) );
	}

	public void actionPerformed(ActionEvent a) {
            if ("send".equals(a.getActionCommand())) {
                
                String recipient = "yura@yura.net";
                String subject = RiskUtil.GAME_NAME + " " + RiskUtil.RISK_VERSION +" SwingGUI "+ TranslationBundle.getBundle().getLocale().toString() + " Suggestion";
                String body = text.getText();

                try {
                    // This code is a lot like the Alert Service in Lobby
                    Map<String, String> map = new HashMap();
                    map.put("recipient", recipient);
                    map.put("subject", subject);
                    map.put("email", from.getText());
                    map.put("text", body);
                    map.put("OS", RiskUIUtil.getOSString());
                    map.put("lobbyID", net.yura.lobby.mini.MiniLobbyClient.getMyUUID());
                    map.put("env_report", "REMOTE_HOST,HTTP_USER_AGENT");
                    BugSubmitter.doPost(BugSubmitter.FORM_MAIL_URL, map);
                    JOptionPane.showMessageDialog(this, "SENT!");
                }
                catch (Throwable ex) {
                    try {
                        sendEmailWithNativeClient(recipient, subject, body);

                        logger.log(Level.WARNING, "error with grasshopper doPost", ex);
                    }
                    catch (Throwable ex2) {
                        JOptionPane.showMessageDialog(this, "Error Sending: " + ex + "\nPlease email " + recipient + " about this issue.");

                        logger.log(Level.WARNING, "error with grasshopper doPost", ex);
                        logger.log(Level.WARNING, "error with native mailto", ex2);
                    }
                }
            }
	}

        public static void sendEmailWithNativeClient(String recipient, String subject, String body) throws Exception {
            
            body = body + "\n\n"
                    + "OS: " + RiskUIUtil.getOSString() + "\n"
                    + "ID: " + net.yura.lobby.mini.MiniLobbyClient.getMyUUID();

            // for some reason + does not get decoded, so we set it back to a space
            URL url = new URL("mailto:" + recipient
                    + "?subject=" + JavaCompatUtil.replaceAll(URLEncoder.encode(subject, "UTF-8"), "+", "%20")
                    + "&body=" + JavaCompatUtil.replaceAll(URLEncoder.encode(body, "UTF-8"), "+", "%20"));

            RiskUtil.openURL(url);
        }

	public JToolBar getToolBar() {
		return toolbar;
	}
	public JMenu getMenu() {
		return null;
	}
}
