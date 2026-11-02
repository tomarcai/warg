// Yura Mamyrin

package net.yura.domination.ui.flashgui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import net.yura.domination.audio.GameSound;
import net.yura.domination.engine.Risk;
import net.yura.domination.guishared.RiskUIUtil;
import net.yura.domination.engine.core.StatType;
import net.yura.swing.GraphicsUtil;
import net.yura.domination.guishared.StatsPanel;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.swing.ArcCornerIcon;
import net.yura.swing.ImageIcon;

/**
 * <p> Statistics Dialog for FlashGUI </p>
 * @author Yura Mamyrin
 */
public class StatsDialog extends JDialog implements ActionListener {

        // we can only have 12 stats with the current UI, there is a total of 14, so we skip a couple
        private static final StatType[] STAT_TYPES = {
            StatType.COUNTRIES,
            StatType.ARMIES,
            StatType.KILLS,
            StatType.CASUALTIES,
            StatType.REINFORCEMENTS,
            StatType.CONTINENTS,
            StatType.CONNECTED_EMPIRE,
            StatType.ATTACKS,
            // skip RETREATS
            StatType.COUNTRIES_WON,
            StatType.COUNTRIES_LOST,
            // skip ATTACKED
            StatType.CARDS,
            StatType.DICE};

        private Image Back;
	private Risk myrisk;
	private StatsPanel graph;
	private java.util.ResourceBundle resb;
        private ButtonGroup group;

	public StatsDialog(Frame parent, boolean modal, Risk r) {
		super(parent, modal);
		myrisk = r;
		Back = RiskUIUtil.getUIImage(this.getClass(),"graph.jpg");
		initGUI();
		RiskUIUtil.setMinimumSize(this, getPreferredSize());
		pack();
	}

	/** This method is called from within the constructor to initialize the form. */

	/**
	 * Initialises the GUI
	 */
	private void initGUI() {

		resb = TranslationBundle.getBundle();

		setTitle( resb.getString("swing.tab.statistics") );

		JPanel thisgraph = new JPanel() {

                    @Override
                    public void doLayout() {

                        int sides = GraphicsUtil.scale(50);
                        int bottom = GraphicsUtil.scale(150);
                        graph.setBounds(sides, sides, getWidth() - sides * 2, getHeight() - sides - bottom);

                        int leftStart = (getWidth() - GraphicsUtil.scale(642)) / 2;
                        int x=leftStart;
                        int y=getHeight() - sides - GraphicsUtil.scale(67);
                        int w=GraphicsUtil.scale(107);
                        int h=GraphicsUtil.scale(33);

                        for (int c = 0; c < STAT_TYPES.length; c++) {
                                AbstractButton button = ((AbstractButton)getComponent(c));
                                button.setBounds(x, y, w, h);
                                x = x + w;

                                // when we have done half, move on to 2nd row
                                if (c == (STAT_TYPES.length / 2) - 1) {
                                        x = leftStart;
                                        y = y + h;
                                }
                        }
                    }
                };
                thisgraph.setBorder( new FlashBorder(
                        GraphicsUtil.getSubimage(Back, 100, 0, 740, 50),
                        GraphicsUtil.getSubimage(Back, 0, 0, 50, 400),
                        GraphicsUtil.getSubimage(Back, 100, 182, 740, 150),
                        GraphicsUtil.getSubimage(Back, 50, 0, 50, 400)
                        ) );

		Dimension d = GraphicsUtil.newDimension(740, 600);
		thisgraph.setPreferredSize(d);
		thisgraph.setMinimumSize(d);
		thisgraph.setMaximumSize(d);

		thisgraph.setLayout(null);

                group = new ButtonGroup();

		int x=149;
		int y=50;
		int w=107;
		int h=33;

                for (int c=0;c<STAT_TYPES.length;c++) {
                        StatType statType = STAT_TYPES[c];
                        thisgraph.add(makeButton(statType.getName(), x, y, w, h, statType.ordinal()));
                        x=x+w;

                        // when we have done half, move on to 2nd row
                        if (c == (STAT_TYPES.length/2)-1) {
                                x=149;
                                y=y+h;
                        }
                }

                ((AbstractButton)thisgraph.getComponent(0)).setSelected(true);

		graph = new StatsPanel(myrisk);
		

		thisgraph.add(graph);

		getContentPane().add(thisgraph);

		addWindowListener(
                    new java.awt.event.WindowAdapter() {
                        public void windowClosing(java.awt.event.WindowEvent evt) {
                            exitForm();
                        }
                    }
		);

	}

	public void actionPerformed(ActionEvent a) {
            GameSound.INSTANCE.playSound(GameSound.BUTTON);
            showGraph(StatType.fromOrdinal(Integer.parseInt(a.getActionCommand())));
	}

        public void setVisible(boolean b) {
            super.setVisible(b);
            if (b) {
                showGraph(StatType.fromOrdinal(Integer.parseInt(group.getSelection().getActionCommand())));
            }
        }

        public void showGraph(StatType statType) {
		graph.repaintStats( statType );
		graph.repaint();
        }

	/**
	 * Closes the GUI
	 */
	private void exitForm() {
		((GameFrame)getParent()).displayGraph();
	}

        private AbstractButton makeButton(String a, int x,int y,int w,int h,int s) {

                AbstractButton statbutton = new JToggleButton(resb.getString("swing.toolbar."+a));
                statbutton.setActionCommand(s+"");
                statbutton.addActionListener( this );
                group.add(statbutton);

                Icon nornal = new ImageIcon(GraphicsUtil.getSubimage(Back, x, y + 283, w, h));
                Icon hover = new ImageIcon(GraphicsUtil.getSubimage(Back, x, y, w, h));
                Icon pressed = new ImageIcon(GraphicsUtil.getSubimage(Back, x, y + 66, w, h));

                switch (s) {
                    case 0:
                        nornal = new ArcCornerIcon(nornal, ScrollPaneConstants.UPPER_LEFT_CORNER);
                        hover = new ArcCornerIcon(hover, ScrollPaneConstants.UPPER_LEFT_CORNER);
                        pressed = new ArcCornerIcon(pressed, ScrollPaneConstants.UPPER_LEFT_CORNER);
                        break;
                    case 5:
                        nornal = new ArcCornerIcon(nornal, ScrollPaneConstants.UPPER_RIGHT_CORNER);
                        hover = new ArcCornerIcon(hover, ScrollPaneConstants.UPPER_RIGHT_CORNER);
                        pressed = new ArcCornerIcon(pressed, ScrollPaneConstants.UPPER_RIGHT_CORNER);
                        break;
                    case 6:
                        nornal = new ArcCornerIcon(nornal, ScrollPaneConstants.LOWER_LEFT_CORNER);
                        hover = new ArcCornerIcon(hover, ScrollPaneConstants.LOWER_LEFT_CORNER);
                        pressed = new ArcCornerIcon(pressed, ScrollPaneConstants.LOWER_LEFT_CORNER);
                        break;
                    case 13:
                        nornal = new ArcCornerIcon(nornal, ScrollPaneConstants.LOWER_RIGHT_CORNER);
                        hover = new ArcCornerIcon(hover, ScrollPaneConstants.LOWER_RIGHT_CORNER);
                        pressed = new ArcCornerIcon(pressed, ScrollPaneConstants.LOWER_RIGHT_CORNER);
                        break;
                    default:
                        break;
                }

                NewGameFrame.sortOutButton(statbutton, nornal, hover, pressed);

                return statbutton;
        }
}
