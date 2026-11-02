// Yura Mamyrin, Group D

package net.yura.domination.ui.flashgui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.yura.domination.audio.GameSound;
import net.yura.domination.guishared.RiskUIUtil;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import net.yura.swing.GraphicsUtil;
import net.yura.domination.engine.translation.TranslationBundle;

/**
 * <p> Move Dialog for FlashGUI </p>
 * @author Yura Mamyrin
 */
public class MoveDialog extends JDialog {

	private GameFrame gui;
	private boolean tacmove;

	private Image Move;
	private Image MoveBack;

	private BufferedImage c1img;
	private BufferedImage c2img;
	private Country country1;
	private Country country2;

	private int move;
	private int csrc;
	private int cdes;

	private Color color;

	private JSlider slider;
	private java.util.ResourceBundle resb;
	private JButton cancel;
	private final static Polygon arrow;
	private movePanel movepanel;

        static {
		int x=110;
		int y=40;
		int xCoords[] = {x+60, x+130, x+130, x+200, x+130, x+130, x+60};
		int yCoords[] = {y+40,  y+40,  y+20,  y+60, y+100,  y+80, y+80};
		arrow = GraphicsUtil.newPolygon(xCoords, yCoords);
        }
        
	public MoveDialog(Frame parent, boolean modal) {
		super(parent, modal);
		gui = (GameFrame)parent;
		Move = RiskUIUtil.getUIImage(this.getClass(),"move.jpg");
		MoveBack = GraphicsUtil.getSubimage(Move, 0, 0, 480, 330);
		initGUI();
		setResizable(false);
		pack();
	}

	/**
         * This method is called from within the constructor to initialize the form.
	 * Initialises the GUI
	 */
	private void initGUI() {
		resb = TranslationBundle.getBundle();

		setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);

		Dimension d = GraphicsUtil.newDimension(480, 330);

		movepanel = new movePanel();
		movepanel.setPreferredSize(d);
		movepanel.setMinimumSize(d);
		movepanel.setMaximumSize(d);
		movepanel.setLayout(null);

		slider = new JSlider();
		movepanel.add(slider);

		int w=88;
		int h=31;

		cancel = new JButton(resb.getString("move.cancel"));
		NewGameFrame.sortOutButton( cancel, GraphicsUtil.getSubimage(Move, 484, 5, w, h), GraphicsUtil.getSubimage(Move, 480, 72, w, h) ,GraphicsUtil.getSubimage(Move, 480, 41, w, h) );
		GraphicsUtil.setBounds(cancel, 50, 250, w, h);
		cancel.setActionCommand("cancel");

		JButton moveall = new JButton(resb.getString("move.moveall"));
		NewGameFrame.sortOutButton( moveall, GraphicsUtil.getSubimage(Move, 196, 250, w, h) ,GraphicsUtil.getSubimage(Move, 480, 134, w, h), GraphicsUtil.getSubimage(Move, 480, 103, w, h) );
		GraphicsUtil.setBounds(moveall, 196, 250, w, h);
		moveall.setActionCommand("all");

		JButton moveButton = new JButton(resb.getString("move.move"));
		NewGameFrame.sortOutButton(moveButton, GraphicsUtil.getSubimage(Move, 342, 250, w, h), GraphicsUtil.getSubimage(Move, 480, 196, w, h), GraphicsUtil.getSubimage(Move, 480, 165, w, h) );
		GraphicsUtil.setBounds(moveButton, 343, 250, w, h);
		moveButton.setActionCommand("move");



		w=35;
		h=25;

		JButton b1 = new JButton(resb.getString("move.min"));
		NewGameFrame.sortOutButton( b1, GraphicsUtil.getSubimage(Move, 25, 192, w, h), GraphicsUtil.getSubimage(Move, 480, 252, w, h), GraphicsUtil.getSubimage(Move, 515, 252, w, h) );
		GraphicsUtil.setBounds(b1, 25, 192, w, h);
		b1.setActionCommand("b1");

		JButton b4 = new JButton(resb.getString("move.max"));
		NewGameFrame.sortOutButton( b4, GraphicsUtil.getSubimage(Move, 25, 192, w, h), GraphicsUtil.getSubimage(Move, 480, 252, w, h), GraphicsUtil.getSubimage(Move, 515, 252, w, h) );
		GraphicsUtil.setBounds(b4, 420, 192, w, h);
		b4.setActionCommand("b4");



		w=25;
		h=25;

		JButton b2 = new JButton(resb.getString("move.minus"));
		NewGameFrame.sortOutButton( b2, GraphicsUtil.getSubimage(Move, 60, 192, w, h), GraphicsUtil.getSubimage(Move, 480, 227, w, h), GraphicsUtil.getSubimage(Move, 505, 227, w, h) );
		GraphicsUtil.setBounds(b2, 60, 192, w, h);
		b2.setActionCommand("b2");

		JButton b3 = new JButton(resb.getString("move.plus"));
		NewGameFrame.sortOutButton( b3, GraphicsUtil.getSubimage(Move, 60, 192, w, h), GraphicsUtil.getSubimage(Move, 480, 227, w, h), GraphicsUtil.getSubimage(Move, 505, 227, w, h) );
		GraphicsUtil.setBounds(b3, 395, 192, w, h);
		b3.setActionCommand("b3");


		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                                String actionCommand = e.getActionCommand();

                                // seem to get bug reports that indicate that this must sometimes be happening
                                if (!MoveDialog.this.isVisible()) {
                                    Logger.getLogger(MoveDialog.class.getName()).info("WARNING! actionPerformed but window not Visible! " + actionCommand + " " + e);
                                    return;
                                }

				if ("cancel".equals(actionCommand)) {
					exitForm();
				}
				else if ("all".equals(actionCommand)) {
					if (tacmove) {
                                                GameSound.INSTANCE.playSound(GameSound.MOVE_TACTICAL);
						gui.go("movearmies " +country1.getColor()+ " " +country2.getColor()+ " " + (csrc-1) );
					}
					else {
                                                GameSound.INSTANCE.playSound(GameSound.MOVE_ARMIES);
						gui.go("move " + (csrc-1) );
					}
					//exitForm();
				}
				else if ("move".equals(actionCommand)) {
					if (tacmove) {
                                                GameSound.INSTANCE.playSound(GameSound.MOVE_TACTICAL);
						gui.go("movearmies " +country1.getColor()+ " " +country2.getColor()+ " " + move );
					}
					else {
                                                GameSound.INSTANCE.playSound(GameSound.MOVE_ARMIES);
						gui.go("move " + move);
					}
					//exitForm();
				}
				else if ("b1".equals(actionCommand)) {
					slider.setValue( slider.getMinimum() );
				}
				else if ("b2".equals(actionCommand)) {
					slider.setValue( move-1 );
				}
				else if ("b3".equals(actionCommand)) {
					slider.setValue( move+1 );
				}
				else if ("b4".equals(actionCommand)) {
					slider.setValue( slider.getMaximum() );
				}
			}
		};


		cancel.addActionListener( al );
		moveall.addActionListener( al );
		moveButton.addActionListener( al );

		b1.addActionListener( al );
		b2.addActionListener( al );
		b3.addActionListener( al );
		b4.addActionListener( al );

		movepanel.add(b1);
		movepanel.add(b2);
		movepanel.add(b3);
		movepanel.add(b4);

		movepanel.add(cancel);
		movepanel.add(moveall);
		movepanel.add(moveButton);

		getContentPane().add(movepanel);

		addWindowListener(
			new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					if (tacmove) {
						exitForm();
					}
				}
			}
		);
	}

	public void setup(boolean tm, int m, int a, int b, BufferedImage ai, BufferedImage bi, Country country1, Country country2, Color c) {

		tacmove=tm;

		c1img = ai;
		c2img = bi;

		this.country1 = country1;
		this.country2 = country2;

		move=m;

		csrc=a;
		cdes=b;

		color=c;


		// set title
		if (tacmove) {
			setTitle(resb.getString("move.title.tactical"));
			cancel.setVisible(true);
		}
		else {
			setTitle(resb.getString("move.title.captured"));
			cancel.setVisible(false);
		}


		// all this coz for some reason u cant reuse a JSlider, the labels start getting painted wrong
		movepanel.remove(slider);

		slider = new JSlider(move,csrc-1,move);


		int spacig = Math.round( (csrc-1)/10f );

		if (spacig==0) {
			slider.setMajorTickSpacing(1);
		}
		else {
			slider.setMajorTickSpacing( spacig );
			slider.setMinorTickSpacing(1);
		}

		slider.setPaintTicks( true );
		slider.setPaintLabels( true );
		slider.setSnapToTicks( true );
		slider.setOpaque( false );

		slider.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					move = slider.getValue();
					movepanel.repaint();
				}
			}
		);

		GraphicsUtil.setBounds(slider, 90, 180, 300, 50);

		movepanel.add(slider);
	}

	public void exitForm() {
		setVisible(false);
	}

	class movePanel extends JPanel {

		/**
		 * Paints the graphic
		 * @param g Graphics
		 */
		public void paintComponent(Graphics g) {

			GraphicsUtil.drawImage(g, MoveBack, 0, 0, this);

			if (tacmove) {
				GraphicsUtil.drawImage(g, GraphicsUtil.getSubimage(Move, 480, 0, 98, 41), 46, 245, this);
			}

			paintMove(g, 
                                c1img, c2img,
                                color, color,
                                gui.getCountryCapital(country1), gui.getCountryCapital(country2),
                                country1.getName(), country2.getName(),
                                csrc - move, cdes + move, gui);

                        g.setColor( RiskUIUtil.getTextColorFor(color) );
			GraphicsUtil.drawStringCenteredAt(g, java.lang.Integer.toString(move), 240, 104);
		}
	}

        static void paintMove(Graphics g,
                        BufferedImage c1img,BufferedImage c2img,
                        Color playerColor1, Color playerColor2,
                        Player capital1,Player capital2,
                        String name1, String name2,
                        int noa1i, int noa2i, GameFrame gf) {

                Graphics2D g2 = (Graphics2D)g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GraphicsUtil.drawImageInRect(g2, c1img, 30, 30, 200, 140, null);
                GraphicsUtil.drawImageInRect(g2, c2img, 250, 30, 200, 140, null);

                g2.setColor( Color.black );

                GraphicsUtil.drawStringCenteredAt(g, name1, 130, 40);
                GraphicsUtil.drawStringCenteredAt(g, name2, 350, 40);

                gf.drawArmy(g2, playerColor1, noa1i, 130, 100, 20, capital1);
                //g2.setColor(color1);
                //GraphicsUtil.fillOval(g, 120 , 90 , 20, 20);
                //g2.setColor(RiskUIUtil.getTextColorFor(color1));
                //GraphicsUtil.drawStringCenteredAt(g, String.valueOf(noa1i), 130, 105);

                gf.drawArmy(g2, playerColor2, noa2i, 350, 100, 20, capital2);
                //g2.setColor(color2);
                //GraphicsUtil.fillOval(g, 340 , 90 , 20, 20);
                //g2.setColor(RiskUIUtil.getTextColorFor(color2));
                //GraphicsUtil.drawStringCenteredAt(g, String.valueOf(noa2i), 350, 105);

                g2.setColor(new Color(playerColor1.getRed(), playerColor1.getGreen(), playerColor1.getBlue(), 150) );
                g2.fillPolygon( arrow );
        }
}
