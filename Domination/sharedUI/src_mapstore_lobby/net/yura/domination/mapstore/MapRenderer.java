package net.yura.domination.mapstore;

import java.util.ResourceBundle;
import javax.microedition.lcdui.Image;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.gui.Animation;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.cellrenderer.DefaultListCellRenderer;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.ProgressBar;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.gui.plaf.Style;

/**
 * @author Yura Mamyrin
 */
public class MapRenderer extends DefaultListCellRenderer {

    private static final int TOTAL_LINES_OF_TEXT = 5;

    String line1,line2;

    private ProgressBar bar = new ProgressBar();
    private Component list;

    private String context;

    Image play,download;
    Icon loading;

    MapChooser chooser;
    Map map;
    
    ResourceBundle resb = TranslationBundle.getBundle();

    public MapRenderer(MapChooser chooser) {

        this.chooser = chooser;
        setName("ListRendererCollapsed"); // get rid of any padding

        bar.workoutPreferredSize();
        //add(bar); // YURA do we need this???

        play = Application.createImage("/ms_play.png");
        download = Application.createImage("/ms_download.png");

        loading = new Icon("/ms_icon_loading.png");

    }

    public void animate() {
        bar.animate();

        if (list!=null) {
            list.repaint();
        }
    }

    public void setContext(String c) {
        context = c;
    }

    public String getContext() {
        return context;
    }

    public Component getListCellRendererComponent(Component list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);

        this.list = list;

        line2 = null; // reset everything
        map = null;
        String iconUrl=null;

        if (value instanceof Category) {
            Category category = (Category)value;

            line1 = category.getName();

            iconUrl = category.getIconURL();
        }
        else if (value instanceof Map) {
            map = (Map)value;

            line1 = map.getName();

            String author = map.getAuthorName();
            if (author!=null && !"".equals(author)) {
                line2 = resb.getString("mapchooser.by").replaceAll("\\{0\\}", author);
            }
            String description = map.getDescription();
            if (description!=null && !"".equals(description)) {
                line2 = (line2==null?"":line2+"\n")+description;
            }

            if (line2!=null) {
                line2 = getFirstLines(line2,TOTAL_LINES_OF_TEXT-1);
            }

            iconUrl = map.getPreviewUrl();
        }
        // else just do nothing

        //iconUrl = "http://www.imagegenerator.net/clippy/image.php?question="+map.getName();

        if (iconUrl!=null) {
            setIcon( MapPreview.getIconForMapOrCategory(value,context,iconUrl,chooser.client) );
        }
        else {
            System.out.println("[MapRenderer] No PreviewUrl for map or category: "+value);
        }

        return c;
    }

    public void paintComponent(Graphics2D g) {

        Icon icon = getIcon();
        if (icon==null || icon.getImage()==null) {
            setIcon(loading);
        }

        super.paintComponent(g); // paint the icon

        int textx = padding+getIcon().getIconWidth()+gap;

        g.setFont( font );
        g.setColor( getForeground() );
        g.drawString(line1, textx, (line2!=null)?padding:(getHeight()-font.getHeight())/2);

        if (line2!=null) {
            int state = getCurrentState();
            // if NOT focused or selected
            if ( (state&Style.FOCUSED)==0 && (state&Style.SELECTED)==0 ) {
                list.setState(Style.DISABLED);
                g.setColor(list.getForeground());
                list.setState(-1);
            }
            g.drawString(line2, textx , padding + getFont().getHeight() + gap);
        }

        if (map!=null) {

            int gap = XULLoader.adjustSizeToDensity(5);

            String mapUID = MapPreview.getFileUID( map.getMapUrl() );

            if ( chooser.client.isDownloading( mapUID ) ) { // we need to check for this first as we may have it and also be updating it

                // position spinner in top right corner
                int x = getWidth()-bar.getWidth()-gap;
                int y = gap;

                g.translate(x, y);

                bar.paintComponent(g);

                g.translate(-x, -y);


                // its ok to register more than once
                Animation.registerAnimated(this);
            }
            else if ( chooser.willDownload( map ) ) {
                g.drawImage(download, getWidth()-download.getWidth()-gap, gap);
            }
            else {
                g.drawImage(play, getWidth()-play.getWidth()-gap, gap);
            }

            int w = getWidth();
            if (w >= XULLoader.adjustSizeToDensity(768)) {
                if (map.getMapWidth() > 0) {
                    String size = "Size: " + map.getMapWidth() + "x" + map.getMapHeight();
                    g.drawString(size, textx + w / 2, padding);
                }
                if (map.getNumberOfDownloads() != null) {
                    String downloads = JavaCompatUtil.replaceAll(resb.getString("mapchooser.map.numberOfDownloads"), "{0}", map.getNumberOfDownloads());
                    g.drawString(downloads, textx + w / 2 , padding + getFont().getHeight() + this.gap);
                }
            }
        }

        setIcon(icon);
    }

    public int getFixedCellHeight() {
        return padding*2 + Math.max(loading.getIconHeight(), getFont().getHeight()*TOTAL_LINES_OF_TEXT+gap );
    }

    /**
     * @see net.yura.lobby.mini.MiniLobbyClient#getLastLines(String, int)
     */
    public static String getFirstLines(String input,int lines) {
        int lastchar=0;
        for (int c=0;c<lines;c++) {
            int newline = input.indexOf('\n', lastchar);
            if (newline<0) {
                return input;
            }
            lastchar = newline+1;
        }
        return input.substring(0, lastchar-1)+"...";
    }
}
