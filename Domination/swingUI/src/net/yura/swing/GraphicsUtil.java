package net.yura.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MediaTracker;
import java.awt.Polygon;
import java.awt.Toolkit;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.List;
import javax.swing.plaf.basic.BasicGraphicsUtils;

public class GraphicsUtil {

    /**
     * how many swing pixels there are in 1 device independent pixel
     */
    public static final double density = getDisplayDensity();

    /**
     * how many sub-pixels there are in 1 swing pixel
     */
    public static final double scale = getScale();

    public static int scale(int i) {
        return (int) (i * density / scale);
    }

    public static boolean insideButton(int x, int y, int bx, int by, int bw, int bh) {
        return x >= GraphicsUtil.scale(bx) && x < GraphicsUtil.scale(bx + bw) && y >= GraphicsUtil.scale(by) && y < GraphicsUtil.scale(by + bh);
    }

    public static void setBounds(Component comp, int x, int y, int w, int h) {
        comp.setBounds(scale(x), scale(y), scale(w), scale(h));
    }

    public static Dimension newDimension(int width, int height) {
        return new Dimension(scale(width), scale(height));
    }

    public static Insets newInsets(int top, int left, int bottom, int right) {
        return new Insets(scale(top), scale(left), scale(bottom), scale(right));
    }

    public static Polygon newPolygon(int[] xCoords, int[] yCoords) {
        Polygon polygon = new Polygon();
        for (int c = 0; c < xCoords.length; c++) {
            polygon.addPoint(scale(xCoords[c]), scale(yCoords[c]));
        }
        return polygon;
    }

    public static RoundRectangle2D newRoundRectangle(int x, int y, int w, int h, int arcw, int arch) {
        return new RoundRectangle2D.Float(scale(x), scale(y), scale(w), scale(h), scale(arcw), scale(arch));
    }
    
    public static FlowLayout newFlowLayout(int align) {
        return new FlowLayout(align, scale(5), scale(5));
    }

    public static void drawImage(Graphics g, Image img, int x, int y, ImageObserver observer) {
        g.drawImage(img,
                scale(x),
                scale(y),
                scale(img.getWidth(observer)),
                scale(img.getHeight(observer)),
                observer);
    }

    public static void drawImage(Graphics g, Image img, int x, int y, int w, int h, ImageObserver observer) {
        g.drawImage(img,
                scale(x),
                scale(y),
                scale(w),
                scale(h),
                observer);
    }

    public static void drawImage(Graphics g, Image img,
            int dx1, int dy1, int dx2, int dy2,
            int sx1, int sy1, int sx2, int sy2,
            ImageObserver observer) {
        g.drawImage(img,
                scale(dx1),
                scale(dy1),
                scale(dx2),
                scale(dy2),
                sx1, sy1, sx2, sy2, observer);
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

        drawImage(g, img, centreX - w/2, centreY - h/2, w, h, observer);
    }

    public static void fillRect(Graphics g, int x, int y, int width, int height) {
        g.fillRect(scale(x), scale(y), scale(width), scale(height));
    }

    public static void fillArc(Graphics g, int x, int y, int width, int height, int startAngle, int arcAngle) {
        g.fillArc(scale(x), scale(y), scale(width), scale(height), startAngle, arcAngle);
    }

    public static void fillOval(Graphics g, int x, int y, int width, int height) {
        g.fillOval(scale(x), scale(y), scale(width), scale(height));
    }

    public static void drawString(Graphics g, String string, int x, int y) {
        g.drawString(string, scale(x), scale(y));
    }

    public static void drawStringCenteredAt(Graphics g, String text, int x, int y) {
        drawStringCenteredAt(g, text, '\0', x, y);
    }

    public static void drawStringCenteredAt(Graphics g, String text, char ch, int x, int y) {
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        BasicGraphicsUtils.drawString(g, text, ch, GraphicsUtil.scale(x) - metrics.stringWidth(text) / 2, GraphicsUtil.scale(y));
    }

    private static double getDisplayDensity() {
        try {
            return ((Double)Class.forName("javax.microedition.midlet.ApplicationManager")
                    .getMethod("getDisplayDensity").invoke(null)).doubleValue();
        }
        catch (Throwable th) { }
        return 1;
    }
    
    private static double getScale() {
        try {
            return ((Double)Class.forName("javax.microedition.midlet.ApplicationManager")
                    .getMethod("getScale").invoke(null)).doubleValue();
        }
        catch (Throwable th) { }
        return 1;
    }

    /**
     * A version of BufferedImage.getSubimage that works on ALL image types, not just BufferedImage
     *
     * @see sun.awt.image.MultiResolutionToolkitImage
     */
    public static Image getSubimage(Image img, int x, int y, int width, int height) {
        // java 9+ (works for both toolkit images and custom loaded BaseMultiResolutionImage)
        try {
            Class multiResolutionImageClass = Class.forName("java.awt.image.MultiResolutionImage");
            if (multiResolutionImageClass.isInstance(img)) {
                int baseWidth = img.getWidth(null);
                List<Image> images = (List<Image>)multiResolutionImageClass.getMethod("getResolutionVariants").invoke(img);
                Image[] scaledImages = new Image[images.size()];
                for (int c = 0; c < images.size(); c++) {
                    Image i = images.get(c);
                    double scale = i.getWidth(null) / (double)baseWidth;
                    // can not use recursion here as some images return themselves as a variant
                    scaledImages[c] = getSubimageImpl(i, (int)(x * scale), (int)(y * scale), (int)(width * scale), (int)(height * scale));
                }
                return newBaseMultiResolutionImage(scaledImages);
            }
        }
        catch (Throwable ex) {
            // failed to handle MultiResolutionImage
        }

        // java 8, ONLY on macOS when using specific image names '@2x' and loading with Toolkit.getImage
        try {
            Class multiResolutionImageClass = Class.forName("sun.awt.image.MultiResolutionImage");
            if (multiResolutionImageClass.isInstance(img)) {
                int baseWidth = img.getWidth(null);
                List<Image> images = (List<Image>)multiResolutionImageClass.getMethod("getResolutionVariants").invoke(img);
                Image[] scaledImages = new Image[images.size()];
                for (int c = 0; c < images.size(); c++) {
                    // sometimes images.get(0) == img, we dont want to get stuck recursive, so we call getSubimageFilter directly
                    Image i = images.get(c);
                    double scale = i.getWidth(null) / (double)baseWidth;
                    scaledImages[c] = getSubimageImpl(i, (int)(x * scale), (int)(y * scale), (int)(width * scale), (int)(height * scale));
                }
                Class baseMultiResolutionImageClass = Class.forName("sun.awt.image.MultiResolutionToolkitImage");
                return (Image)baseMultiResolutionImageClass.getConstructor(Image.class, Image.class).newInstance(scaledImages[0], scaledImages[1]);
            }
        }
        catch (Throwable ex) {
            // failed to handle MultiResolutionImage
        }

        return getSubimageImpl(img, x, y, width, height);
    }

    /**
     * @see BufferedImage#getSubimage(int, int, int, int)
     */
    private static Image getSubimageImpl(Image img, int x, int y, int width, int height) {
        if (img instanceof BufferedImage) {
            return ((BufferedImage)img).getSubimage(x, y, width, height);
        }

        ImageFilter filter = new CropImageFilter(x, y, width, height);
        ImageProducer prod = new FilteredImageSource(img.getSource(), filter);
        Image croppedImg = Toolkit.getDefaultToolkit().createImage(prod);

        // for unknown crazy reasons, this is needed or image has width and height as -1
        waitForImage(croppedImg);

        return croppedImg;
    }

    /**
     * only works on java 9+ otherwise throws Exception
     */
    public static Image newBaseMultiResolutionImage(Image[] images) throws Exception {
        Class baseMultiResolutionImageClass = Class.forName("java.awt.image.BaseMultiResolutionImage");
        return (Image)baseMultiResolutionImageClass.getConstructor(images.getClass()).newInstance((Object)images);
    }

    private static MediaTracker mediaTracker = new MediaTracker( new Component(){} );
    public static void waitForImage(Image img) {
        mediaTracker.addImage(img, 1);
        try {
            mediaTracker.waitForID(1);
        }
        catch(InterruptedException e) {
            System.out.println("Loading of the image was interrupted" );
        }
        int status = mediaTracker.statusID(1, true);
        if (status != MediaTracker.COMPLETE) {
            System.out.println("Image status NOT COMPLETE " + status + " " +img);
        }
        mediaTracker.removeImage(img);
    }
}
