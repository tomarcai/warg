package net.yura.domination.mapstore;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import net.yura.domination.engine.RiskUtil;

/**
 * @author Yura Mamyrin
 */
public class GetMap extends Observable implements MapServerListener {

    MapServerClient client;
    String filename;

    /**
     * Should always use {@link #getMap(java.lang.String, java.util.Observer) }
     */
    private GetMap() { }

    // TODO this can get called, while another is still running
    // steps:
    // 1: user clicks on the game to see it
    // 2: map starts to download, but takes a while
    // 3: user clicks back, closes the lobby
    // 4: user reopens the lobby
    // 5: user clicks open same game, and same map starts downloading
    // 6: both maps finish downloading and crash during file rename
    public static void getMap(String filename, Observer gml) {
        GetMap get = new GetMap();
        get.filename = filename;
        get.addObserver(gml);
        get.client = new MapServerClient(get);
        get.client.start();
        get.client.makeRequestXML(MapServerClient.MAP_PAGE, "mapfile", filename);
    }

    public void gotResultMaps(String url, List maps) {
        if (maps.size() == 1) {
            Map themap = (Map)maps.get(0);
            client.downloadMap(MapPreview.getURL(MapPreview.getContext(url), themap.mapUrl));
        }
        else {
            String error = "wrong number of maps on server: " + maps.size() + " for map: " + filename;
            System.err.println("invalid responce from MapStore: " + error);
            onError(error);
        }
    }

    private void onError(String error) {
        // all errors come here including network errors, so we just log the info
        // any serious error would have already been logged as such by the MapServerClient
        System.out.println("GetMap Error: " + error);
        notifyListeners(RiskUtil.ERROR);
    }

    private void notifyListeners(Object arg) {
        client.kill();
        setChanged();
        notifyObservers(arg);
    }

    public void downloadFinished(String mapUID) {
        notifyListeners(RiskUtil.SUCCESS);
    }

    public void onXMLError(String string) {
        onError(string);
    }

    public void onDownloadError(String string) {
        onError(string);
    }

    public void gotResultCategories(String url, List categories) { }
    public void publishImg(Object param) { }
}
