package net.yura.domination.mapstore;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.ButtonGroup;
import net.yura.mobile.gui.DesktopPane;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.Component;
import net.yura.mobile.gui.components.Label;
import net.yura.mobile.gui.components.List;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.components.Panel;
import net.yura.mobile.gui.components.RadioButton;
import net.yura.mobile.gui.components.TextComponent;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.gui.plaf.LookAndFeel;
import net.yura.mobile.gui.plaf.SynthLookAndFeel;
import net.yura.mobile.io.ClipboardManager;
import net.yura.mobile.logging.Logger;
import net.yura.mobile.util.Properties;
import net.yura.swingme.core.CoreUtil;

/**
 * @author Yura Mamyrin
 */
public class MapChooser implements ActionListener,MapServerListener {

    private Properties resBundle = CoreUtil.wrap(TranslationBundle.getBundle());

    private XULLoader loader;
    private ActionListener al;
    protected MapServerClient client;

    private java.util.List<String> localMaps;
    private Set<String> allowedMaps;
    private List list;

    public static void loadThemeExtension() {
        MiniLobbyClient.loadThemeExtension("/ms_tabbar.xml");
        MiniLobbyClient.loadThemeExtension();
    }

    public MapChooser(ActionListener al, java.util.List<String> localMaps, Set<String> allowedMaps) {
        this.al = al;
        this.localMaps = localMaps;
        this.allowedMaps = allowedMaps;

        try {
            loader = XULLoader.inflate( new InputStreamReader(Application.getResourceAsStream("/ms_maps.xml"), RiskUtil.UTF_8), this, resBundle);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }

        Panel TabBar = (Panel)loader.find("TabBar");
        if (TabBar != null) {

            int count = 0;
            if (allowedMaps != null) {
                for (String localMap : localMaps) {
                    if (allowedMaps.contains(localMap)) {
                        count++;
                    }
                }
            }

            // if we have all the allowed maps already, no point showing download UI.
            if (allowedMaps != null && count == allowedMaps.size()) {
                TabBar.setVisible(false);
            }
            else {
                java.util.List buttons = TabBar.getComponents();
                Icon on = new Icon("/ms_bar_on.png");
                Icon off = new Icon("/ms_bar_off.png");
                int w = off.getIconWidth() / buttons.size();
                for (int c=0;c<buttons.size();c++) {
                    RadioButton b = (RadioButton)buttons.get(c);
                    Icon oni = on.getSubimage(c*w, 0, w, off.getIconHeight());
                    Icon offi = off.getSubimage(c*w, 0, w, off.getIconHeight());

                    b.setIcon(offi);
                    b.setSelectedIcon(oni);
                    b.setRolloverIcon(offi);
                    b.setRolloverSelectedIcon(oni);

                    b.setToolTipText( b.getText() );

                    b.setText("");
                    b.setMargin(0);
                }
            }
        }

        list = (List)loader.find("ResultList");
        if (Application.getPlatform() == Application.PLATFORM_ME4SE) {
            list.setDoubleClick(true);
        }
        MapRenderer r = new MapRenderer(this);
        list.setCellRenderer( r );
        list.setFixedCellHeight( Math.max( XULLoader.adjustSizeToDensity(100) , r.getFixedCellHeight() ) );
        list.setFixedCellWidth(10); // will streach

        client = new MapServerClient(this);
        client.start();

        activateGroup("MapView");

        MapUpdateService.getInstance().addObserver( (BadgeButton)loader.find("updateButton") );
    }

    public void destroy() {

        MapUpdateService.getInstance().deleteObserver( (BadgeButton)loader.find("updateButton") );

        client.kill();
        client=null;
    }

    public void publishImg(Object key) {
            if (client!=null) { // if we have shut down, dont need to do anything
                list.repaint();
            }
    }


    void makeRequestForMap(String key, String value) {
        client.makeRequestXML(MapServerClient.MAP_PAGE, key, value);
    }

    public void actionPerformed(String actionCommand) {
        if ("local".equals(actionCommand)) {
            mainCatList(actionCommand);

            new Thread() {
                @Override
                public void run() {
                    java.util.List riskmaps = new java.util.Vector(localMaps.size());
                    for (int c = 0; c < localMaps.size(); c++) {
                        final String file = localMaps.get(c);

                        // if one map is corrupted, we dont want to block all map loading
                        try {
                            // we create a Map object for every localy stored map
                            Map map = MapPreview.createMap(file);
                            riskmaps.add(map);
                        }
                        catch (Exception ex) {
                            Logger.warn("error creating map: " + file, ex);

                            // create placeholder so map can be deleted
                            Map map = new Map();
                            map.setMapUrl(file);
                            map.setName(file);
                            riskmaps.add(map);

                            // we are not in UI thread here, in Desktop mode we may not have a UI at all yet
                            OptionPane.showConfirmDialog(new ActionListener() {
                                public void actionPerformed(String actionCommand) {
                                    if ("ok".equals(actionCommand)) {
                                        RiskUtil.streamOpener.deleteMapFile(file);
                                    }
                                }
                            }, "Error in file: " + file + ". Delete?", "Map Error", OptionPane.OK_CANCEL_OPTION);

                        }
                    }

                    // we want to sort by name for the local map list
                    Collections.sort(riskmaps, new Comparator() {
                        public int compare(Object t1, Object t2) {
                            Map m1 = (Map)t1;
                            Map m2 = (Map)t2;
                            return String.CASE_INSENSITIVE_ORDER.compare(m1.getName(), m2.getName());
                        }
                    });

                    setListData( null, riskmaps );
                }
            }.start();
        }
        else if ("catagories".equals(actionCommand)) {
            mainCatList(actionCommand);

            client.makeRequestXML( MapServerClient.CATEGORIES_PAGE , (String)null, (String)null);
        }
        else if ("top25".equals(actionCommand)) {
            mainCatList(actionCommand);

            activateGroup("Top25View");
        }
        else if ("search".equals(actionCommand)) {
            mainCatList(actionCommand);

            actionPerformed("doMapSearch");
        }
        else if ("update".equals(actionCommand)) {
            mainCatList(actionCommand);

            java.util.List<Map> mapsToUpdate = MapUpdateService.getInstance().mapsToUpdate;

            Component updateAll = loader.find("updateAll");
            if (mapsToUpdate.isEmpty()) {
                updateAll.setVisible(false);
                show("AllUpToDate");
            }
            else {
                updateAll.setVisible(true);
                // take a copy of the update vector so if we do update, they dont just disappear from screen
                // otherwise we may get array index out of bounds, as the list is updated during paint
                // also after updating, we may want to actaully select the map
                setListData(MapServerClient.MAP_PAGE, new java.util.Vector(mapsToUpdate));
            }
        }
        else if ("updateall".equals(actionCommand)) {
            java.util.List<Map> mapsToUpdate = MapUpdateService.getInstance().mapsToUpdate;
            synchronized(mapsToUpdate) {
                for (int c=0;c<mapsToUpdate.size();c++) {
                    click(mapsToUpdate.get(c));
                }
            }
        }
        else if ("TOP_NEW".equals(actionCommand)) {
            clearList();
            makeRequestForMap("sort","TOP_NEW" );
        }
        else if ("TOP_RATINGS".equals(actionCommand)) {

            if ( "true".equals( System.getProperty("debug") ) ) {
                System.err.println("no ratings :-(");
            }

            clearList();
            makeRequestForMap("sort","TOP_RATINGS" );
        }
        else if ("TOP_DOWNLOADS".equals(actionCommand)) {
            clearList();
            makeRequestForMap("sort","TOP_DOWNLOADS" );
        }
        else if ("listSelect".equals(actionCommand)) {

            Object value = list.getSelectedValue();
            if (value instanceof Category) {
                Category cat = (Category)value;
                clearList();
                makeRequestForMap("category",cat.getId() );
            }
            else if (value instanceof Map) {
                Map map = (Map)value;
                click(map);
            }
            //else value is null coz the list is empty
        }
        else if ("mapInfo".equals(actionCommand)) {
            Object value = list.getSelectedValue();
            if (value instanceof Map) {
                Map map = (Map)value;
                OptionPane.showMessageDialog(null, getMapInfo(map), map.toString(), OptionPane.INFORMATION_MESSAGE);
            }
        }
        else if ("sameAuthor".equals(actionCommand)) {
            Object value = list.getSelectedValue();
            // TODO
            // TODO on android its too hard to get to the right click menu, as you need to hold and wait
            // TODO does not make sense for categories
            // TODO results list still shows old title at top
            // TODO
            if (value instanceof Map) {
                Map map = (Map)value;
                if (map.getAuthorId() == null) {
                    client.makeRequestMap(MapServerClient.MAP_PAGE, MapPreview.getFileUID(map.getMapUrl()), new Observer() {
                        public void update(Observable o, Object map) {
                            if (map != null) {
                                client.makeRequestXML(MapServerClient.MAP_PAGE, "author", ((Map)map).getAuthorId());
                            }
                            else {
                                // this map is not on the map store, must be a bundled map
                                setListData(null, Collections.EMPTY_LIST);
                            }
                        }
                    });
                }
                else {
                    makeRequestForMap("author", map.getAuthorId());
                }
            }
        }
        else if ("copyId".equals(actionCommand)) {
            Object value = list.getSelectedValue();
            if (value instanceof Map) {
                ClipboardManager.getInstance().setText(((Map)value).getId());
            }
        }
        else if ("delMap".equals(actionCommand)) {
            Object value = list.getSelectedValue();
            if (value instanceof Map) {
                Map map = (Map)value;
                final String mapUID = MapPreview.getFileUID(map.getMapUrl());
                if (localMaps.contains(mapUID)) {
                    // we check with the server to see if this map can be deleted
                    // TODO this means we are unable to delete any maps when we are not connected to the internet
                    client.makeRequestMap(MapServerClient.MAP_PAGE, mapUID, new Observer() {
                        public void update(Observable o, Object map) {
                            if (map != null) {
                                try {
                                    java.util.Map mapinfo = RiskUtil.loadInfo(mapUID, false);
                                    String cardsFile = (String) mapinfo.get("crd");
                                    String prvFile = (String) mapinfo.get("prv");
                                    String picFile = (String) mapinfo.get("pic");
                                    String mapFile = (String) mapinfo.get("map");
                                    RiskUtil.streamOpener.deleteMapFile(mapFile);
                                    RiskUtil.streamOpener.deleteMapFile(picFile);
                                    if (prvFile != null) {
                                        RiskUtil.streamOpener.deleteMapFile(MapPreview.PREVIEW_FILE_PREFIX + prvFile);
                                    }
                                    if (!"risk.cards".equals(cardsFile) && !"nomission.cards".equals(cardsFile)) {
                                        RiskUtil.streamOpener.deleteMapFile(cardsFile);
                                    }
                                }
                                catch (Exception ex) {
                                    Logger.warn("unable to delete supplementary map files for: " + mapUID, ex);
                                }

                                if (RiskUtil.streamOpener.deleteMapFile(mapUID)) {
                                    localMaps.remove(mapUID);
                                    
                                    String context = ((MapRenderer)list.getCellRenderer()).getContext();
                                    if (context == null) {
                                        // if we are already looking at local maps, then reload the list
                                        // TODO this is not very good as jumps to the top of the list
                                        actionPerformed("local");
                                    }
                                    else {
                                        // if we are looking at server maps, repaint the download icon
                                        list.repaint();
                                    }
                                }
                                else {
                                    OptionPane.showMessageDialog(null, "could not delete this map", "error", 0);
                                }
                            }
                            else {
                                OptionPane.showMessageDialog(null, "can not delete bundled map", "message", 0);
                            }
                        }
                    });
                }
            }
        }
        else if ("defaultMap".equals(actionCommand)) {
            chosenMap( RiskGame.getDefaultMap() );
        }
        else if ("cancel".equals(actionCommand)) {
            al.actionPerformed(null);
        }
        else if ("doMapSearch".equals(actionCommand)) {
            TextComponent.closeNativeEditor();
            String text = ((TextComponent)loader.find("mapSearchBox")).getText();
            clearList();
            if (text != null && !"".equals(text)) {
                makeRequestForMap("search", text );
            }
            else {
                setListData(null, null);
            }
        }
        else {
            System.out.println("MapChooser unknown command "+actionCommand);
        }
    }

    public void click(Map map) {
        String fileUID = MapPreview.getFileUID( map.getMapUrl() );

        String context = ((MapRenderer)list.getCellRenderer()).getContext();

        if (context!=null) { // we have a context, this means this is a remote map

            if (client.isDownloading(fileUID)) { // we may be doing a update

                OptionPane.showMessageDialog(null, "already downloading", "message", 0);
            }
            else if (localMaps.contains(fileUID)) {

                java.util.Map info = RiskUtil.loadInfo(fileUID, false);

                String ver = (String)info.get("ver");

                if (map.needsUpdate(ver)) {
                    // update needed!!!

                    client.downloadMap( MapPreview.getURL(context, map.mapUrl ) );
                    list.repaint();
                    return;
                }

                String pic = (String)info.get("pic");
                String crd = (String)info.get("crd");
                String imap = (String)info.get("map");
                String prv = (String)info.get("prv");

                if ( !MapPreview.fileExists(pic) || !MapPreview.fileExists(crd) || !MapPreview.fileExists(imap) || (prv!=null && !MapPreview.fileExists(MapPreview.PREVIEW_FILE_PREFIX + prv)) ) {
                    // we are missing a file, need to re-download this map

                    client.downloadMap( MapPreview.getURL(context, map.mapUrl ) );
                    list.repaint();
                    return;

                }

                // so we already have this map, just fire event to load it
                chosenMap(fileUID);
            }
            else {
                client.downloadMap( MapPreview.getURL(context, map.mapUrl ) );
                list.repaint();
            }
        }
        else { // this is a local map, we will fire the event right away that we got it
            chosenMap(fileUID);
        }
    }

    private void chosenMap(String mapName) {
        selectedMap = mapName;
        al.actionPerformed(null);
    }

    public void mainCatList(String actionCommand) {
        Enumeration group = ((ButtonGroup)loader.getGroups().get("MapView")).getElements();
        while (group.hasMoreElements()) {
            Button button = (Button)group.nextElement();
            String action = button.getActionCommand();
            Component panel = loader.find(action+"Bar");
            panel.setVisible( action.equals(actionCommand) );
        }

        clearList();
    }

    void clearList() {
        show("Loading");
    }

    public Panel getRoot() {
        return ((Panel)loader.getRoot());
    }

    private String selectedMap;
    public String getSelectedMap() {
        return selectedMap;
    }

    public void gotResultCategories(String url,java.util.List items) {
        setListData(url, items);
    }

    public void gotResultMaps(String url, java.util.List maps) {
	setListData(url, maps);
    }

    public void onXMLError(String error) {
        ((Label)loader.find("errorMessage")).setText(error);
        show("Error");
    }

    /**
     * currently this is for download or image errors
     */
    public void onDownloadError(String error) {
        // TODO make this better
        OptionPane.showMessageDialog(null, error , "Error!", OptionPane.ERROR_MESSAGE);
    }

    public void downloadFinished(String download) {

        if ( !this.localMaps.contains( download ) ) {
            this.localMaps.add( download );
        }

        if (((Button)loader.find("updateButton")).isSelected() && MapUpdateService.getInstance().mapsToUpdate.isEmpty()) {
            loader.find("updateAll").setVisible(false);
            getRoot().revalidate();
            getRoot().repaint();
            //show("AllUpToDate"); // allow user to select map even after update
        }

        //else {
            // this must have been a update or re-download, no need to show message
            //OptionPane.showMessageDialog(null, "got map, but we already have it "+download, "error", 0);
        //}
    }

    /**
     * @param items items to display or null for empty screen or empty list for 'no matches' message
     */
    private void setListData(String url,java.util.List items) {
        ((MapRenderer) list.getCellRenderer()).setContext(MapPreview.getContext(url));

        java.util.Vector result;
        if (items == null) {
            result = new java.util.Vector(0);
        }
        else if (allowedMaps == null) {
            result = JavaCompatUtil.asVector(items);
        }
        else {
            result = new java.util.Vector();
            for (Object item : items) {
                if (item instanceof Map) {
                    if (allowedMaps.contains(MapPreview.getFileUID(((Map) item).getMapUrl()))) {
                        result.add(item);
                    }
                }
                else {
                    result.add(item);
                }
            }
        }

        list.setListData(result);
        boolean showNoMatch = items != null && result.isEmpty();
        show(showNoMatch ? "NoMatches" : "ResultList");
    }

    private void show(String name) {

        Component loading = loader.find("Loading");
        Component noMatches = loader.find("NoMatches");
        Component allUpToDate = loader.find("AllUpToDate");
        Component error = loader.find("Error");

        list.setSelectedIndex(-1);
        if (list.getSize()>0) {
            list.ensureIndexIsVisible(0);
        }

        list.setVisible( "ResultList".equals(name) );
        noMatches.setVisible( "NoMatches".equals(name) );
        allUpToDate.setVisible( "AllUpToDate".equals(name) );
        loading.setVisible( "Loading".equals(name) );
        error.setVisible( "Error".equals(name) );

        getRoot().revalidate();
        getRoot().repaint();
    }

    private void activateGroup(String string) {
        String mincat = ((ButtonGroup)loader.getGroups().get(string)).getSelection().getActionCommand();
        actionPerformed(mincat);
    }

    public boolean willDownload(Map map) {

        String mapUID = MapPreview.getFileUID( map.getMapUrl() );

        // if we dont have a local file with the same uid
        if (!localMaps.contains(mapUID)) {
            return true;
        }

        return MapUpdateService.getInstance().mapsToUpdate.contains(map);
    }
    
    public static String getMapInfo(Map map) {
        String author = map.getAuthorName();
        String noOfDownloads = map.getNumberOfDownloads();
        return "By: " + (author == null ? "?" : author) + "\n" +
        JavaCompatUtil.replaceAll(TranslationBundle.getBundle().getString("mapchooser.map.numberOfDownloads"), "{0}", (noOfDownloads == null ? "?" : noOfDownloads)) + "\n" +
        "Version: " + map.getVersion() + "\n" +
        map.getDescription();
    }
}
