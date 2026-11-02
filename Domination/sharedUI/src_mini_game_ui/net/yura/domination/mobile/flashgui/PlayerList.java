package net.yura.domination.mobile.flashgui;

import android.graphics.ColorMatrix;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.Risk;
import net.yura.domination.mobile.PicturePanel;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.List;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.domination.engine.core.Player;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.gui.plaf.Style;
import java.util.Collection;
import javax.microedition.lcdui.Image;

/**
 * @author Yura
 */
public class PlayerList extends List {

    final ColorMatrix invert = new ColorMatrix(new float[] {
            -1.0f, 0, 0, 0, 255, //red
            0, -1.0f, 0, 0, 255, //green
            0, 0, -1.0f, 0, 255, //blue
            0, 0, 0, 1.0f, 0 //alpha
    });

    private Risk risk;

    public PlayerList() {
        setLayoutOrientation(HORIZONTAL);
        setCellRenderer(new DefaultListCellRenderer() {
            int playerType;
            Image human,ai_easy,ai_average,ai_hard;
            {
                setName("PlayerListRenderer"); // ListRendererCollapsed
                padding = XULLoader.adjustSizeToDensity(4);
                human = Application.createImage("/type_human.png");
                ai_easy = Application.createImage("/type_ai_easy.png");
                ai_average = Application.createImage("/type_ai_average.png");
                ai_hard = Application.createImage("/type_ai_hard.png");
            }
            @Override
            public void setValue(Object obj) {
                Player player = (Player) obj;
                setBackground(player.getColor());
                setToolTipText(player.getName());
                playerType = player.getType();
            }

            @Override
            public void paintComponent(Graphics2D g) {
                super.paintComponent(g);

                int color = getBackground();
                Image img = PicturePanel.getIconForColor(color);

                if (img != null) {
                    int iconSize = getWidth() - padding;
                    int h = iconSize;
                    int w = (int)(img.getWidth()*(h/(double)img.getHeight()));
                    g.drawScaledImage(img, (getWidth()-w)/2, (getHeight()-h)/2, w, h);
                }

                Image typeIcon;
                switch (playerType) {
                    case Player.PLAYER_HUMAN:
                    case Player.PLAYER_AI_CRAP:
                        typeIcon = human;
                        break;
                    case Player.PLAYER_AI_EASY:
                        typeIcon = ai_easy;
                        break;
                    case Player.PLAYER_AI_AVERAGE:
                        typeIcon = ai_average;
                        break;
                    case Player.PLAYER_AI_HARD:
                        typeIcon = ai_hard;
                        break;
                    default:
                        typeIcon = null;
                        break;
                }
                int foreground = ColorUtil.getTextColorFor(color);
                if (typeIcon != null) {
                    boolean doInvert = foreground == ColorUtil.WHITE;
                    if (doInvert) {
                        g.getGraphics().setColorMatrix(invert);
                    }
                    int iconSize = getWidth() / 2;
                    g.drawScaledImage(typeIcon, (getWidth()-iconSize)/2, (getHeight()-iconSize)/2, iconSize, iconSize);
                    if (doInvert) {
                        g.getGraphics().setColorMatrix(null);
                    }

                }

                if ((getCurrentState() & Style.FOCUSED) != 0) {
                    int padding = XULLoader.adjustSizeToDensity(5);
                    g.setColor(foreground);

                    int oldStroke = g.getGraphics().getStrokeWidth();
                    g.getGraphics().setStrokeWidth(Math.max(1, XULLoader.adjustSizeToDensity(1)));

                    g.drawRect(padding, padding, getWidth() - padding * 2, getHeight() - padding * 2);
                    
                    g.getGraphics().setStrokeWidth(oldStroke);
                }
            }
        });
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(String actionCommand) {
                final Player player = (Player) getSelectedValue();
                if (player != null) {
/*
                    // ColorPickerActivity has too many problems, the size of the window is too random on too many devices
                    if (Application.getPlatform() == Application.PLATFORM_ANDROID) {
                        DominationMain.openURL("native://net.yura.domination.android.ColorPickerActivity", new DominationMain.ActivityResultListener() {
                            @Override
                            public void onActivityResult(Object data) {
                                int color = (Integer) data;
                                setPlayerColor(player, color);
                            }

                            @Override
                            public void onCanceled() {
                                // dont care
                            }
                        });
                    }
*/
                    final ColorPicker colorPicker = new ColorPicker();
                    colorPicker.showDialog(new ActionListener() {
                        @Override
                        public void actionPerformed(String actionCommand) {
                            if (ColorPicker.CMD_OK.equals(actionCommand)) {
                                setPlayerColor(player, colorPicker.getSelectedColor());
                            }
                        }
                    }, player.getName());
                }
            }
        });
    }

    private void setPlayerColor(Player player, int color) {
        if (player.getColor() != color) {
            Player playerWithColor = getPlayerByColor(color);
            if (playerWithColor == null) {
                player.setColor(color);
                repaint();
                //risk.parser("delplayer "+player);
                //risk.parser("newplayer "+risk.getType(player.getType())+" "+color+" "+player.getName());
            }
            else {
                playerWithColor.setColor(player.getColor());
                player.setColor(color);
                repaint();
                //risk.parser("delplayer "+player);
                //risk.parser("delplayer "+playerWithColor);
                //risk.parser("newplayer "+risk.getType(playerWithColor.getType())+" "+player.getColor()+" "+playerWithColor.getName());
                //risk.parser("newplayer "+risk.getType(player.getType())+" "+color+" "+player.getName());
            }
        }
    }

    private Player getPlayerByColor(int color) {
        for (Player player : (Collection<Player>)risk.getGame().getPlayers()) {
            if (player.getColor() == color) {
                return player;
            }
        }
        return null;
    }

    @Override
    protected void workoutMinimumSize() {

        int size = XULLoader.adjustSizeToDensity(75);
        // (10 is padding of 5 from the xml layout * 2)
        int screen = (DesktopPane.getDesktopPane().getWidth() - XULLoader.adjustSizeToDensity(10)) / 6;
        size = size > screen ? screen : size;
        setFixedCellWidth(size);
        setFixedCellHeight(size);

        super.workoutMinimumSize();
    }

    public void setGame(Risk risk) {
        this.risk = risk;
    }

    @Override
    public int getSize() {
        return risk.getGame().getNoPlayers();
    }

    @Override
    public Object getElementAt(int index) {
        return risk.getGame().getPlayers().get(index);
    }

}
