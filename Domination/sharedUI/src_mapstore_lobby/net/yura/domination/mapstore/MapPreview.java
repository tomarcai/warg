package net.yura.domination.mapstore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.WeakHashMap;
import javax.microedition.lcdui.Image;
import net.yura.cache.Cache;
import net.yura.domination.ImageManager;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.io.FileUtil;
import net.yura.mobile.logging.Logger;
import net.yura.mobile.util.ImageUtil;

public class MapPreview {

    public static final String PREVIEW_FILE_PREFIX = "preview/";

    /**
     * this is a weak cache, it only keep a object if someone else holds it or a key
     * needs to be synchronizedMap or we get endless loop in WeakHashMap: http://www.adam-bien.com/roller/abien/entry/endless_loops_in_unsychronized_weakhashmap
     * 
     * java Map: localfilename -> WeakReference of game 'Map' objects
     */
    private static final java.util.Map localMapsCache = Collections.synchronizedMap(new WeakHashMap());

    private static Cache repo;

    static {
        try {
            repo = new Cache("net.yura.domination");
        }
        catch (Throwable ex) {
            System.err.println("[MapChooser] no cache: "+ex);
        }
    }

    /**
     * WARNING! this may be called from 2 threads at the same time
     * e.g. MapUpdateService.init and MapChooser.actionPerformed."local".run
     */
    public static Map createMap(String file) {

        WeakReference wr = (WeakReference)localMapsCache.get(file);
        if (wr!=null) {
            Map map = (Map)wr.get();
            if (map!=null) {
                return map;
            }
        }

        java.util.Map info = RiskUtil.loadInfo(file, false);

        Map map = new Map();
        map.setMapUrl( file );

        String name = (String)info.get("name");
        if (name == null) {
            String filename = file.toLowerCase().endsWith(".map") ? RiskUtil.getFileNameWithoutExtension(file) : file;
            name = JavaCompatUtil.replaceAll(filename, "_", " ");
        }
        map.setName(name);
        map.setDescription( (String)info.get("comment") );

        String prv = (String)info.get("prv");
        if (prv!=null) {
            prv = PREVIEW_FILE_PREFIX + prv;
            if (!fileExists(prv)) {
                Logger.info("MAP ERROR: preview file specifies but not found: " + prv);
                prv=null;
            }
        }

        if (prv==null) {
            prv = (String)info.get("pic");
        }
        map.setPreviewUrl( prv );

        String ver = (String)info.get("ver");
        map.setVersion(ver == null ? "1" : ver);

        localMapsCache.put(file, new WeakReference(map));

        return map;
    }

    public static boolean haveLocalMap(String mapUID) {
        if (localMapsCache.containsKey(mapUID)) {
            return true;
        }
        return fileExists(mapUID);
    }

    public static boolean fileExists(String fileUID) {

        InputStream file=null;
        try {
            file = RiskUtil.openMapStream(fileUID);
        }
        catch (Exception ex) { } // not found?
        finally{
            FileUtil.close(file);
        }

        return (file != null); // we already have this file
    }

    public static void clearFromCache(String mapUID) {
        localMapsCache.remove(mapUID);
    }

    public static void cache(String url, byte[] data) {
        if (repo!=null && !repo.containsKey( url ) ) {
            repo.put( url , data );
        }
    }

    /**
     * @return map data or null if not saved in cache
     */
    public static InputStream getRemoteMapPreview(String url) {
        return repo != null ? repo.get(url) : null;
    }

    /**
     * can return null if unable to load image
     * @return the bytes of the map preview, could be the preview file itself or a cached generated preview
     */
    public static InputStream getLocalMapPreview(String url) {
        InputStream in;

        if (url.startsWith(PREVIEW_FILE_PREFIX)) {
            in = null;
            try {
                in = RiskUtil.openMapStream(url);

                if (in == null) {
                    throw new IllegalStateException("local preview stream null " + url);
                }
            }
            catch (Exception ex) {
                Logger.warn("cant open " + url, ex);
            }
        }
        else {
            in = repo != null ? repo.get(url) : null;

            if (in == null) {
                try {
                    System.out.println("[MapChooser] ### Going to re-encode img: "+url);
                    InputStream min = RiskUtil.openMapStream(url);
                    Image img = createImage(min);
                    img = ImageUtil.scaleImage(img, 150, 94);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ImageUtil.saveImage(img, out);
                    img = null; // drop the small image as soon as we can
                    byte[] bytes = out.toByteArray();
                    out = null; // drop the OutputStream as soon as we can
                    if (bytes.length == 0) {
                        throw new IllegalStateException("img failed to save " + url);
                    }
                    cache(url,bytes);
                    // TODO we should only cache if we are sure it can be opened as a image
                    in = new ByteArrayInputStream(bytes);
                }
                catch (OutOfMemoryError err) { // what can we do?
                    Logger.info("cant resize " + url, err);
                }
                catch (Exception ex) {
                    Logger.warn("cant resize " + url, ex);
                }
            }
        }

        return in;
    }

    public static String getFileUID(String mapUrl) {
        int i = mapUrl.lastIndexOf('/');
        return (i >= 0) ? mapUrl.substring(i + 1) : mapUrl;
    }

    public static String getURL(String context,String mapUrl) {

        if (mapUrl.indexOf(':')<0 && context!=null) { // we do not have a full URL, so we pre-pend the context
            if (mapUrl.startsWith("/")) {
                mapUrl = context.substring(0, context.indexOf('/', "http://.".length()) ) + mapUrl;
            }
            else {
                mapUrl = context + mapUrl;
            }
        }

        return mapUrl;
    }

    public static String getContext(String url) {
        if (url!=null) {
            int i = url.lastIndexOf('/');
            if (i> "http://.".length() ) {
                url = url.substring(0, i+1);
            }
        }
        return url;
    }

    /**
     * @see net.yura.domination.engine.RiskUIUtil#read(java.io.InputStream)
     */
    public static Image createImage(InputStream in) throws IOException {
        try {
            Image img = Image.createImage(in);
            if (img == null) {
                throw new IOException("Image.createImage returned null");
            }
            return img;
        }
        finally {
            FileUtil.close(in);
        }
    }






    // this is a weak cache, it only keep a object if someone else holds it or a key
    private static final ImageManager iconCache = new ImageManager( XULLoader.adjustSizeToDensity(150),XULLoader.adjustSizeToDensity(94) ); // 150x94

    /**
     * WARNING! this method may get called from multiple threads at the same time!
     */
    public static Icon getLocalIconForMap(Map map) {
        Icon icon = getIconForMapOrCategory(map, null, map.getPreviewUrl(), null);
        if (icon == null) {
            throw new RuntimeException("could not find local icon for " + map + " " + map.getPreviewUrl());
        }
        return icon;
    }

    /**
     * @return true if icon is in the cache, or false and {@see MapServerListener#publishImg(java.lang.Object)} will be called later.
     */
    public static boolean getRemoteImage(Object key, String url, MapServerClient c) {
        InputStream in = MapPreview.getRemoteMapPreview(url);
        if (in != null) {
            try {
                gotImg(key, in);
                return true;
            }
            catch (Exception ex) {
                Logger.warn("can not load image in cache " + key + " " + url, ex);
            }
        }
        // can be null when shut down
        if (c != null) c.getImage(url, key);
        return false;
    }
    
    /**
     * WARNING! this method may get called from multiple threads at the same time!
     * @param key can be a {@link Map} or a {@link Category}
     */
    public static Icon getIconForMapOrCategory(Object key,String context,String iconUrl,MapServerClient c) {
        Icon aicon = iconCache.get( key );
        if (aicon == null) {
            aicon = iconCache.newIcon(key);

            String url = getURL(context, iconUrl);

            // if this is a remote file (starts with http:// or https://)
            if (url.indexOf("://") > 0) {
                getRemoteImage(key, url, c);
            }
            // if this is a locale file
            else {
                InputStream in = MapPreview.getLocalMapPreview(url);

                if (in != null) {
                    gotImg(key, in);
                }
            }
        }
        return aicon;
    }

    public static Icon getRemoteIconForMap(String mapUID, MapServerClient mapServerClient) {
        Icon aicon = iconCache.get(mapUID);
        if (aicon == null) {
            aicon = iconCache.newIcon(mapUID);
            mapServerClient.makeRequestXML(MapServerClient.MAP_PAGE, "mapfile", mapUID);
        }
        return aicon;
    }

    public static void gotImgFromServer(Object obj,String url, byte[] data,MapServerListener msl) {
        try {
            gotImg(obj, new ByteArrayInputStream(data) );

            if (msl!=null) {
                msl.publishImg(obj);
            }
        }
        catch (RuntimeException ex) {
            // there was some error with this image
            //ImageManager.gotImg(obj, null); // clear the lazy image, so we can try again
            // not needed as its a week ref and will clear soon enough anyway

            System.err.println("error in image from server with url: "+url);
            throw ex;
        }

        // only cache if publish works fine
        cache(url, data);
    }

    private static void gotImg(Object obj,InputStream in) {
        try {
            Image img = createImage(in);
            iconCache.gotImg(obj, img);
        }
        catch (OutOfMemoryError err) {
            Logger.info("cant load " + obj, err); // nothing we can do here
        }
        catch (Exception ex) {
            throw new RuntimeException("failed to decode img "+obj, ex);
        }
    }
}
