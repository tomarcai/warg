// Yura Mamyrin, Group D

package net.yura.domination.ui.flashgui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JDialog;
import javax.swing.JPanel;
import net.yura.domination.guishared.RiskUIUtil;
import net.yura.swing.GraphicsUtil;
import net.yura.swing.TextUtil;

/**
 * Mission Dialog for FlashGUI
 * @author Yura Mamyrin
 */
public class MissionDialog extends JDialog implements MouseListener {

    private Image mission;
    private String text;

    /**
     * Creates a mission dialog
     * @param parent Frame
     * @param modal boolean
     */
    public MissionDialog(Frame parent, boolean modal) {
        super(parent, modal);
	mission = RiskUIUtil.getUIImage(this.getClass(),"mission.jpg");
        initGUI();

        //setResizable(false);
        RiskUIUtil.setMinimumSize(this, GraphicsUtil.newDimension(150, 230));

        pack();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initGUI() {

        // set title
        setTitle("");

	Dimension d = GraphicsUtil.newDimension(150, 230);

	missionPanel missionpanel = new missionPanel();
	missionpanel.setPreferredSize(d);
	missionpanel.setMinimumSize(d);
	missionpanel.setMaximumSize(d);
	missionpanel.addMouseListener(this);

        // 150 x 230
        int side = 40;
        missionpanel.setBorder( new FlashBorder(
            GraphicsUtil.getSubimage(mission, 0, 0, 150, 60),
            GraphicsUtil.getSubimage(mission, 0, 60, side, 230 -60 -40),
            GraphicsUtil.getSubimage(mission, 0, 230 - 40, 150, 40),
            GraphicsUtil.getSubimage(mission, 150 - side, 60, side, 230 -60 -40)
            ) );
        
        
	getContentPane().add(missionpanel);

        addWindowListener(
            new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent evt) {
                    exitForm();
                }
            }
	);
    }
    
    public void setMission(String text) {
        	this.text = text;
    }

    /**
     * Closes the Dialog
     */
    private void exitForm() {
	setVisible(false);
	dispose();
    }

    class missionPanel extends JPanel {

    /**
     * Paints graphic
     * @param g Graphics
     */
	public void paintChildren(Graphics g) {

	    //GraphicsUtil.drawImage(g, mission, 0, 0, this);

            GraphicsUtil.drawImage(g, mission,
                    40, 60, RiskUIUtil.unscale(getWidth()) -40, RiskUIUtil.unscale(getHeight()) -40,
                    40, 60, 150 -40, 230 -40, this);

	    Graphics2D g2 = (Graphics2D)g;

	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // do not let font go bellow 10,
            // on hi res windows, default fontsize is 10, and 10-2=8 looks tiny
            int fontSize = Math.max(10, g.getFont().getSize() - 2); // 13 - 2 = 11
            
	    Font font = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, fontSize);
            g.setFont(font);
            g2.setColor( GameFrame.UI_COLOR );

            TextUtil.drawStringCenteredAt(g, text, getWidth() / 2, GraphicsUtil.scale(70), getWidth() - GraphicsUtil.scale(50), Color.WHITE);
	}
    }

	//**********************************************************************
	//                     MouseListener Interface
	//**********************************************************************

        /**
         * WARNING! This method does not work on macOS High Sierra, java 1.8
         */
	public void mouseClicked(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
            exitForm();
	}
}
