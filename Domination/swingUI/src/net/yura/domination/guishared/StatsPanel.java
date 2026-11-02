// Yura Mamyrin

package net.yura.domination.guishared;

import net.yura.swing.GraphicsUtil;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.plaf.UIResource;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.core.StatType;

/**
 * Statistics Graphs Panel
 * @author Yura Mamyrin
 */
public class StatsPanel extends JPanel {

    //private int spX;
    //private int spY;
    private Risk risk;
    private BufferedImage graph;
    private StatType lastStatType;
    private final Set<Player> hiddenPlayers = new HashSet<Player>();

    public StatsPanel(Risk r) {
	//spX=x;
	//spY=y;
	risk=r;
	//Dimension size = new Dimension(spX , spY);
	//setPreferredSize(size);
	//setMinimumSize(size);
	//setMaximumSize(size);

        Font font = getFont();
        Font newFont = font.deriveFont((float) (font.getSize() * GraphicsUtil.scale));
        if (newFont instanceof UIResource) {
            // for some crazy reason, on mac, deriveFont returns a UIResource, even though it specifically should not
            // as UIResource causes the font to be reset on theme changes, we want to avoid this if possible
            System.out.println("ERROR: deriveFont returned a UIResource " + newFont);
            newFont = new Font(font.getName(), font.getStyle(), (int)(font.getSize2D() * GraphicsUtil.scale));
        }

        setFont(newFont);

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }
        });

        // whenever this panel stops being shown (dialog closed, tab switched away from, etc.)
        // reset which players are hidden, so it always starts fresh next time it is shown
        addHierarchyListener(new HierarchyListener() {
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing()) {
                    hiddenPlayers.clear();
                }
            }
        });
    }

    private void maybeShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showHidePlayersMenu(e);
        }
    }

    private void showHidePlayersMenu(MouseEvent e) {
        RiskGame game = risk.getGame();
        if (game == null || game.getState() == RiskGame.STATE_NEW_GAME) {
            return;
        }

        List players = game.getPlayersStats();

        JPopupMenu menu = new JPopupMenu();
        for (int i = 0; i < players.size(); i++) {
            final Player p = (Player) players.get(i);
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem(p.getName(), !hiddenPlayers.contains(p));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    if (item.isSelected()) {
                        hiddenPlayers.remove(p);
                    }
                    else {
                        hiddenPlayers.add(p);
                    }
                    repaintStats();
                }
            });
            menu.add(item);
        }

        menu.show(e.getComponent(), e.getX(), e.getY());
    }

    public void paintComponent(Graphics g) {
	//super.paintComponent(g);
	if (graph != null) {
	    g.drawImage(graph, 0, 0, getWidth(), getHeight(), this);
	}
	else {
	    g.fillRect(0,0,getWidth(),getHeight());
	}
    }

    public void repaintStats() {
        if (risk.getGame() == null || risk.getGame().getState() == RiskGame.STATE_NEW_GAME) {
            return;
        }

        if (lastStatType != null) {
            repaintStats(lastStatType);
        }
        repaint();
    }
    
    public void repaintStats(StatType a) {
        lastStatType = a;

        double scale = GraphicsUtil.scale;

	BufferedImage tempgraph = new BufferedImage((int)(getWidth() * scale), (int)(getHeight() * scale), BufferedImage.TYPE_INT_RGB ); // spX, spY

	List players = risk.getGame().getPlayersStats();

	int maxValue = 0;
        int maxTurns = 0;
	for (int i = 0; i < players.size(); i++) {

	    Player p = (Player)players.get(i);

	    double[] pstats= p.getStatistics(a);

            int max = pstats.length;
            if ( max > maxTurns) {
                maxTurns = max;
	    }

	    double sum=0;

	    for (int j = 0; j < pstats.length; j++) {
	      if (a.isSummable()) {
		  sum += pstats[j];
	      }
	      else {
                  if (pstats[j] > maxValue) {
		    maxValue = (int)pstats[j];
                  }
	      }
	    }

            if (sum > maxValue) {
                maxValue = (int)sum;
            }
        }

	// adds a space at the top and to the right of the graph
	maxValue++;
	maxTurns++;

	Graphics2D g2 = tempgraph.createGraphics();
        g2.setFont(getFont());

	int xOffset = scale(35); // offset from the left
	int yOffset = scale(25); // offset from the bottom

	// size of devision
	gridSizeX = (tempgraph.getWidth()-xOffset-(float)scale(20)) /maxTurns; // the 20 is the right offset
	gridSizeY = (tempgraph.getHeight()-yOffset-(float)scale(20)) /maxValue; // the 20 is the top offset

	// the co-ords of the Zero Zero
	ZeroX = xOffset;
	ZeroY = tempgraph.getHeight()-yOffset;

	int bob = (int)Math.round(g2.getFontMetrics().getHeight()/gridSizeY);

	// draw -- lines and numbers
	for (int i = 0; i <= maxValue ; i++) {
	    if ( i == maxValue || bob == 0 || ( i % bob )==0 ) {
		g2.setColor(Color.gray);
		g2.drawLine(ZeroX,(int)(ZeroY-(i*gridSizeY)),(int)(maxTurns*gridSizeX)+ZeroX,(int)(ZeroY-(i*gridSizeY)));
		g2.setColor(Color.white);
		String label = String.valueOf(i);
                int labelWidth = g2.getFontMetrics().stringWidth(label);
		g2.drawString(label, ZeroX-(scale(6) + labelWidth), (int)(ZeroY-(i*gridSizeY) + scale(5)));
	    }
	}

	int fred = (int)Math.round(g2.getFontMetrics().stringWidth(String.valueOf(maxTurns))/gridSizeX);

	// draw | lines and numbers
	for (int i = 0; i <= maxTurns ; i++) {
	    g2.setColor(Color.gray);
	    g2.drawLine((int)(ZeroX + (i*gridSizeX)),ZeroY,(int)(ZeroX +i*gridSizeX), (int)( ZeroY-( maxValue *gridSizeY) ) );
	    if ( i == maxTurns || fred == 0 || ( i % fred )==0 ) {
		g2.setColor(Color.white);
                int labelHeight = g2.getFontMetrics().getHeight();
		g2.drawString(String.valueOf(i),(int)(i*gridSizeX + ZeroX-scale(3)),ZeroY + labelHeight);
	    }
        }

	g2.setColor(Color.white);
	g2.drawLine(ZeroX, ZeroY, (int)(ZeroX+(maxTurns*gridSizeX)), ZeroY); // -
	g2.drawLine(ZeroX, ZeroY, ZeroX, (int)(ZeroY-( maxValue*gridSizeY)) ); // |

	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	// set hints
	BasicStroke bs = new BasicStroke( 2.0f,BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER );
	g2.setStroke(bs);

	//draw each player graph.
	for (int i = 0; i < players.size(); i++) {
	    Player p = (Player)players.get(i);
            if (!hiddenPlayers.contains(p)) {
                drawPlayerGraph(a, p, g2);
            }
	}

	g2.dispose();

	graph = tempgraph;
    }
    
    private int scale(int n) {
        return (int) (GraphicsUtil.density * n);
    }

    private int ZeroX;
    private int ZeroY;
    private double gridSizeX;
    private double gridSizeY;

    private void drawPlayerGraph(StatType a, Player p, Graphics2D g) {

	double[] PointToDraw = p.getStatistics(a);
	g.setColor(new Color( p.getColor() ) );
        Image icon = RiskUIUtil.getIconForColor(p.getColor());
        int iconSize = scale(15);

	double oldPoint = Double.NaN;
	double newPoint = 0;
	int i;

	for (i = 0; i < PointToDraw.length; i++) {
            
            if (a.isSummable()) {
                newPoint += PointToDraw[i];
            }
            else {
                newPoint = PointToDraw[i];
            }

            // only draw the line if the last and next points are real numbers
            if (!Double.isNaN(newPoint)) {
                int x1,y1;
                if (!Double.isNaN(oldPoint)) {
                    x1 = (int)(ZeroX + i*gridSizeX);
                    y1 = (int)(ZeroY-(oldPoint*gridSizeY));

                }
                else {
                    x1 = (int)(ZeroX +(i+1)*gridSizeX) - 3;
                    y1 = (int)(ZeroY-(newPoint*gridSizeY));
                }
                int x2 = (int)(ZeroX +(i+1)*gridSizeX);
                int y2 = (int)(ZeroY-(newPoint*gridSizeY));
                Color color = g.getColor();
                Stroke stroke = g.getStroke();
                if (Color.BLACK.equals( color )) {
                    g.setColor(Color.WHITE);
                    g.setStroke( new BasicStroke(3) );
                    g.drawLine(x1,y1,x2,y2);
                    g.setColor(color);
                    g.setStroke(stroke);
                }
                g.drawLine(x1,y1,x2,y2);
                if (!Double.isNaN(oldPoint) && icon != null) {
                    // we dont need to re-scale, so do NOT use GraphicsUtil.drawImageInRect here
                    drawImageInRect(g, icon, x1 - iconSize/2, y1 - iconSize/2, iconSize, iconSize, this);
                }
            }

            oldPoint = newPoint;
	}

        int x = (int)(ZeroX + i*gridSizeX);
        int y = (int)(ZeroY - (oldPoint*gridSizeY)+11);

        Color color = g.getColor();

        if (Color.BLACK.equals( color )) {
            g.setColor(Color.WHITE);
            g.drawString(p.getName(),x+1,y+1);
            g.drawString(p.getName(),x-1,y-1);
            g.drawString(p.getName(),x+1,y-1);
            g.drawString(p.getName(),x-1,y+1);
            g.setColor(color);
        }

        String playerName = p.getName();
        int textWidth = g.getFontMetrics().stringWidth(playerName);
	g.drawString(playerName, Math.min(x, (int)(getWidth() * GraphicsUtil.scale) - textWidth), y);
    }

    public static void drawImageInRect(Graphics g, Image img, int xPos, int yPos, int maxW, int maxH, ImageObserver observer) {

        int centreX = xPos + (maxW / 2);
        int centreY = yPos + (maxH / 2);
        int w = img.getWidth(observer);
        int h = img.getHeight(observer);

        if (w > maxW || h > maxH) {
            double scale = Math.min(maxW/(double)w,maxH/(double)h);
            w = (int)( scale * w );
            h = (int)( scale * h );
        }

        g.drawImage(img, centreX - w/2, centreY - h/2, w, h, observer);
    }
}
