package net.yura.domination.lobby.client;

import java.awt.Component;
import java.awt.Graphics;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import javax.swing.Icon;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.lobby.mini.MapPreviewClient;
import net.yura.domination.mapstore.Map;
import net.yura.domination.mapstore.MapPreview;
import net.yura.domination.mapstore.MapUpdateService;
import net.yura.swing.GraphicsUtil;

/**
 * for mobile version the Map Preview Image memory cache:
 * @see net.yura.domination.ImageManager
 */
public class RiskMap {

    private static final Logger logger = Logger.getLogger(RiskMap.class.getName());

    // TODO we create this, but never shut it down!!
    private static MapPreviewClient mapPreviewClient = new MapPreviewClient() {
        @Override
        public void publishMap(Map map) {
            setMapMetaData(map);
        }

        @Override
        public void publishImg(String mapUID) {
            RiskMap riskMap = getRiskMap(mapUID);
            // warning, items can be added to list while this is being called
            // we can NOT use new java for loop as it will throw ConcurrentModificationException
            for (int c = 0; c < riskMap.components.size(); c++) {
                riskMap.components.get(c).repaint();
            }
            riskMap.components = null;
        }
    };

    // by default WeakHashMap is not thread safe
    private static java.util.Map<String, RiskMap> mapUIDToIcon = Collections.synchronizedMap(new WeakHashMap());

    private final String mapUID;

    // lazy loaded fields
    private Map lazyMapMetadata;
    private net.yura.mobile.gui.Icon lazyMapIcon;
    private String[] lazyMapMissions;

    private final java.util.Map<Long, Icon> swingIconMap = new HashMap();
    private List<Component> components = new ArrayList();

    private RiskMap(String mapUID) {
        this.mapUID = mapUID;
    }

    public static void setMapMetaData(Map map) {
        getRiskMap(MapPreview.getFileUID(map.getMapUrl())).lazyMapMetadata = map;
    }

    public static RiskMap getRiskMap(final String mapUID) {
        RiskMap icon  = mapUIDToIcon.get(mapUID);
        if (icon == null) {
            icon = new RiskMap(mapUID);
            mapUIDToIcon.put(mapUID, icon);
        }
        return icon;
    }

    /**
     * @param comp can be either the component with the icon, or the list itself in the case of a renderer
     */
    public Icon getIcon(int w, int h, Component comp) {
        final int width = GraphicsUtil.scale(w);
        final int height = GraphicsUtil.scale(h);
        long id = ByteBuffer.allocate(8).putInt(width).putInt(height).getLong(0);
        Icon icon = swingIconMap.get(id);
        if (icon == null) {
            icon = new Icon() {
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    if (lazyMapIcon == null) {
                        lazyMapIcon = mapPreviewClient.getIconForMap(mapUID);

                        // if the lazy icon we got for the very first time is already set,
                        // publishImg will never get called, so we dont need 'components'.
                        if (lazyMapIcon.getImage() != null) {
                            components = null;
                        }
                    }
                    javax.microedition.lcdui.Image img = lazyMapIcon.getImage();
                    if (img != null) { // the icon has been loaded, so we can draw it
                        g.drawImage(img._image, x, y, width, height, c);
                    }
                }

                public int getIconWidth() {
                    return width;
                }

                public int getIconHeight() {
                    return height;
                }
            };
            swingIconMap.put(id, icon);
        }

        List<Component> comps = components;
        if (comps != null) {
            comps.add(comp);
        }
        return icon;
    }

    public boolean isLocalMap() {
        // map.getMapUrl().lastIndexOf('/') < 0 this is not a good check, as remote file does not always need to have a /
        return MapPreview.haveLocalMap(mapUID) && !MapUpdateService.getInstance().contains(mapUID);
        // OR in applet mode, but we dont care any more as no one uses applets.
    }

    @Override
    public String toString() {
        return lazyMapMetadata == null ? mapUID : lazyMapMetadata.getName();
    }

    public String getID() {
        return mapUID;
    }

    public String[] getMissions() {
        if (lazyMapMissions != null) {
            return lazyMapMissions;
        }

        java.util.Map mapinfo = RiskUtil.loadInfo(mapUID, false);
        String cardsFile = (String) mapinfo.get("crd");
        if (cardsFile != null) {
            java.util.Map cardsinfo = RiskUtil.loadInfo(cardsFile, true);
            lazyMapMissions = (String[]) cardsinfo.get("missions");
        }
        else {
            logger.warning("no crd file in " + mapUID);
            // should not happen
            lazyMapMissions = new String[0];
        }
        return lazyMapMissions;
    }
    
    /**
     * @Nullable: This method may or may not return a MapStore.Map object, it depends on if the icon has been requested and returned.
     */
    Map getMap() {
        return lazyMapMetadata;
    }
}
