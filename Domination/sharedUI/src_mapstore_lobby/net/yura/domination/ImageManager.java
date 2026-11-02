package net.yura.domination;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import javax.microedition.lcdui.Image;
import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.components.Component;

/**
 * for desktop version the Map Preview Image memory cache:
 * @see net.yura.domination.lobby.client.RiskMap
 */
public class ImageManager {

    // this can get called from multiple threads, and WeakHashMap is NOT thread-safe
    public final Map images = Collections.synchronizedMap(new WeakHashMap());
    public final int w, h;

    public ImageManager(int width, int height) {
        w = width;
        h = height;
    }

    private void put(Object key, LazyIcon icon) {
        images.put(key, icon);
    }
    public LazyIcon get(Object key) {
        LazyIcon icon = (LazyIcon)images.get(key);
        // if we found it, it may be using a different key, just to make sure, put it back with this key
        if (icon != null) {
            put(key, icon);
        }
        return icon;
    }

    /**
     * limitation: a key can ONLY have 1 size of icon for it
     */
    public Icon newIcon(Object key) {
        LazyIcon icon = new LazyIcon(w, h);
        put(key, icon);
        return icon;
    }

    public void gotImg(Object key, Image img) {
        LazyIcon icon = get( key );
        if (icon != null) {
            if (img!=null) {
                icon.setImage(img);
            }
            else {
                System.out.println("ERROR: got null response for key: " + key);
                images.remove(key); // we got a response but there was some error and no image
            }
        }
        else {
            System.out.println("ERROR: gotImg, but have no LazyIcon for key: " + key + " in " + images);
            Thread.dumpStack();
        }
    }

    /**
     * just like ImageIcon, but the image is drawn scaled to fit the whole icon size
     */
    public static class LazyIcon extends Icon {

        Image img;
        public LazyIcon(int w,int h) {
            width = w;
            height = h;
        }
        
        public void setImage(Image img) {
            this.img = img;
        }

        public Image getImage() {
            return img;
        }
        
        public void paintIcon(Component c, Graphics2D g, int x, int y) {
            if (img != null) {
                g.drawScaledImage(img, x, y, width, height);
            }
        }
    }
}
