package net.yura.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MediaTracker;
import java.awt.Shape;
import javax.swing.Icon;
import javax.swing.ScrollPaneConstants;
import java.awt.geom.RoundRectangle2D;

/**
 * WARNING!! this most likely does not support disabled auto-icon as it is not an ImageIcon
 */
public class ArcCornerIcon implements Icon {

    private final String corner;
    private final Icon base;

    public ArcCornerIcon(Icon base, String corner) {
        this.base = base;
        this.corner = corner;
    }

    @Override
    public void paintIcon(Component cmpnt, Graphics grphcs, int x, int y) {

        int w = getIconWidth();
        int h = getIconHeight();
        int r = Math.min(w, h);
        int xOffset = - r/2;
        int yOffset = - r/2;
        if (ScrollPaneConstants.UPPER_LEFT_CORNER.equals(corner)) {
            xOffset = 0;
            yOffset = 0;
        }
        else if (ScrollPaneConstants.UPPER_RIGHT_CORNER.equals(corner)) {
            xOffset = -r;
            yOffset = 0;
        }
        else if (ScrollPaneConstants.LOWER_LEFT_CORNER.equals(corner)) {
            xOffset = 0;
            yOffset = -r;
        }
        else if (ScrollPaneConstants.LOWER_RIGHT_CORNER.equals(corner)) {
            xOffset = -r;
            yOffset = -r;
        }

        Shape oldClip = grphcs.getClip();
        if (grphcs instanceof Graphics2D) {
            ((Graphics2D)grphcs).clip(new RoundRectangle2D.Double(x + xOffset, y + yOffset, w + r, h + r, r * 2, r * 2));
        }

        // fix for java 1.5/1.6/1.7, if image is in ABORTED state, if we try and paint it
        // it will call back to the ImageObserver/component, and if that component does not recognise it
        // the component will just return false and say it does not care about this image,
        // so it will never get loaded and so never get painted
        if (base instanceof javax.swing.ImageIcon) {
            if (((javax.swing.ImageIcon)base).getImageLoadStatus() == MediaTracker.ABORTED) {
                // setting this to null forces it to try and paint it right away
                cmpnt = null;
            }
        }

        base.paintIcon(cmpnt, grphcs, x, y);

        grphcs.setClip(oldClip);
    }

    @Override
    public int getIconWidth() {
        return base.getIconWidth();
    }

    @Override
    public int getIconHeight() {
        return base.getIconHeight();
    }
}
