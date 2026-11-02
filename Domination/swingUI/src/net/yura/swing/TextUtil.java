package net.yura.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.BreakIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextUtil {

    private static final Font backupFont;
    
    static {
        Font font = null;
        try {
            font = Font.createFont(Font.TRUETYPE_FONT, TextUtil.class.getResourceAsStream("symbols-backup.ttf"));
        }
        catch (Exception ex) {
            Logger.getLogger(TextUtil.class.getName()).log(Level.INFO, "unable to load backup font", ex);
        }
        backupFont = font;
    }

    public static void drawString(Graphics2D g2, String text, int x, int y) {
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(text);

        float penX = x;
        StringBuilder buffer = new StringBuilder();
        Font font = g2.getFont();
        FontMetrics fm = g2.getFontMetrics();

        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            String cluster = text.substring(start, end);

            if (font.canDisplayUpTo(cluster) != -1 && backupFont != null) { // cant display char
                // flush text buffer
                if (buffer.length() > 0) {
                    String txt = buffer.toString();
                    g2.drawString(txt, penX, y);
                    penX += fm.stringWidth(txt);
                    buffer.setLength(0);
                }

                Font f2 = backupFont.deriveFont((float)font.getSize());
                g2.setFont(f2);
                g2.drawString(cluster, penX, y);
                g2.setFont(font);
                
                penX += g2.getFontMetrics(f2).stringWidth(cluster);
            }
            else {
                buffer.append(cluster);
            }
        }

        // flush remaining text
        if (buffer.length() > 0) {
            g2.drawString(buffer.toString(), penX, y);
        }
    }
    
    
    public static void drawStringCenteredAt(Graphics g, String text, int centerX, int startY, int wrapWidth, Color outline) {
        AttributedString as = new AttributedString(text);
        as.addAttribute(TextAttribute.FONT, g.getFont());

        AttributedCharacterIterator aci = as.getIterator();
        FontRenderContext frc = ((Graphics2D)g).getFontRenderContext();
            
        LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);

        lbm.setPosition( 0 );

        TextLayout tl;
        while (lbm.getPosition() < text.length()) {
            tl = lbm.nextLayout(wrapWidth);
            float textX = (float)(centerX - tl.getBounds().getWidth() / 2);
            float textY = startY += tl.getAscent();
            if (outline == null) {
                tl.draw((Graphics2D)g, textX, textY);
            }
            else {
                drawStringWithOutline((Graphics2D)g, tl, textX, textY, outline);
            }
            startY += tl.getDescent() + tl.getLeading();
        }
    }
    
    public static void drawStringWithOutline(Graphics2D g2, TextLayout tl, float x, float y, Color outlineColor) {
        Color textColor = g2.getColor();
        AffineTransform transform = new AffineTransform();
        transform.setToTranslation(x, y);
        Shape outline = tl.getOutline(transform);
        Stroke oldStroke = g2.getStroke();

        g2.setStroke(new BasicStroke(GraphicsUtil.scale(2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(outlineColor);
        g2.draw(outline);

        g2.setColor(textColor);
        g2.setStroke(oldStroke);
        g2.fill(outline);
    }
    
    public static void drawStringWithOutline(Graphics2D g2, String text, int x, int y, Color outlineColor) {
        FontRenderContext frc = g2.getFontRenderContext();
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(text);

        float penX = x;
        StringBuilder buffer = new StringBuilder();
        Font font = g2.getFont();
        FontMetrics fm = g2.getFontMetrics();

        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {

            String cluster = text.substring(start, end);

            boolean unableToDisplay = font.canDisplayUpTo(cluster) != -1 && backupFont != null;
            boolean isEmojiCluster = isEmojiCluster(cluster);
            if (unableToDisplay || isEmojiCluster) {
                // flush text buffer
                if (buffer.length() > 0) {
                    TextLayout layout = new TextLayout(buffer.toString(), g2.getFont(), frc);
                    drawStringWithOutline(g2, layout, penX, y, outlineColor);
                    penX += layout.getAdvance();
                    buffer.setLength(0);
                }

                if (unableToDisplay) {
                    Font f2 = backupFont.deriveFont((float)font.getSize());
                    TextLayout layout = new TextLayout(cluster, f2, frc);
                    drawStringWithOutline(g2, layout, penX, y, outlineColor);
                    penX += g2.getFontMetrics(f2).stringWidth(cluster);
                }
                else {
                    // draw emoji cluster
                    g2.drawString(cluster, penX, y);
                    penX += fm.stringWidth(cluster);
                }
            }
            else {
                buffer.append(cluster);
            }
        }

        // flush remaining text
        if (buffer.length() > 0) {
            TextLayout layout = new TextLayout(buffer.toString(), g2.getFont(), frc);
            drawStringWithOutline(g2, layout, penX, y, outlineColor);
        }
    }
    
    /**
     * WARNING!!! code generated by AI, may not be correct!
     */
    private static boolean isEmojiCluster(String cluster) {
        if (cluster == null || cluster.length() == 0) return false;

        for (int i = 0; i < cluster.length(); ) {
            char c1 = cluster.charAt(i);
            int cp = c1;
            if (Character.isHighSurrogate(c1) && i + 1 < cluster.length()) {
                char c2 = cluster.charAt(i + 1);
                if (Character.isLowSurrogate(c2)) {
                    cp = Character.toCodePoint(c1, c2);
                }
            }

            if ((cp >= 0x1F300 && cp <= 0x1F5FF) || // Misc Symbols & Pictographs
                (cp >= 0x1F600 && cp <= 0x1F64F) || // Emoticons
                (cp >= 0x1F680 && cp <= 0x1F6FF) || // Transport & Map
                (cp >= 0x1F900 && cp <= 0x1F9FF) || // Supplemental Symbols
                (cp >= 0x1FA70 && cp <= 0x1FAFF) || // Extended-A
                (cp >= 0x1F1E6 && cp <= 0x1F1FF) || // Regional indicators
                cp == 0x200D ||                     // ZWJ
                cp == 0xFE0F                        // VS16
            ) {
                return true;
            }

            i += ((cp >= 0x10000) ? 2 : 1);
        }

        return false;
    }

    
    
    
    /**
     * this is not perfect as many fonts do not obey the unicode grapheme rules, but its the best option.
     * counting Glyphs {@link java.awt.font.GlyphVector#getNumGlyphs()} gives very large counts for emoji
     */
    public static int graphemeCount(String text) {
        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(text);
        int count = 0;
        while (it.next() != BreakIterator.DONE) count++;
        return count;
    }

    public static String subGrapheme(String text, int beginIndex, int endIndex) {
        if (beginIndex == 0 && endIndex == 0) return "";

        BreakIterator it = BreakIterator.getCharacterInstance();
        it.setText(text);

        int grapheme = 0;
        int startOffset = beginIndex == 0 ? 0 : -1;

        while (true) {
            int position = it.next();
            if (position == BreakIterator.DONE) {
                throw new IllegalArgumentException("bad index for string \"" + text +"\" beginIndex=" + beginIndex + " endIndex=" + endIndex + " length=" + graphemeCount(text));
            }
            grapheme++;
            if (grapheme == beginIndex) {
                startOffset = position;
            }
            if (grapheme == endIndex) {
                return text.substring(startOffset, position);
            }
        }
    }
}
