package net.yura.swingme.core;

import net.yura.mobile.gui.Graphics2D;
import net.yura.mobile.gui.components.Button;
import net.yura.mobile.gui.components.TextArea;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.gui.plaf.Style;

public class MultilineButton extends Button {

    // TODO this is kind of specific to Domination main menu
    /**
     * this is NOT the same as {@link #getMaxWidth()} as that is the components max width.
     */
    private final int maxTextLabelWidth = XULLoader.adjustSizeToDensity(110);
    private final int maxNoLines = 2;
    private final int pressedOffset = XULLoader.adjustSizeToDensity(2);

    private int[] lineBreaks = null;

    @Override
    public void setText(String a) {
        lineBreaks = null;
        super.setText(a);
    }

    @Override
    public void setSize(int width, int height) {
        lineBreaks = null;
        super.setSize(width, height);
    }

    @Override
    protected int getTextWidth(String string) {
        setupLineBreaks();
        int tw = 0;
        int start = 0;
        int lines = getNoLines();
        for (int c = 0; c < lines; c++) {
            int end = c == (lines - 1) ? string.length() : lineBreaks[c];
            int w = super.getTextWidth(string.substring(start, end));
            start = end;
            if (w > tw) {
                tw = w;
            }
        }
        return tw;
    }

    @Override
    protected int getTextHeight(String string) {
        setupLineBreaks();
        return getNoLines() * super.getTextHeight(string);
    }

    @Override
    protected void paintText(Graphics2D g, int x, int y, int textWidth, int availableTextWidth) {
        g.setColor(getForeground());
        g.setFont(getFont());
        if ((getCurrentState() & Style.FOCUSED) != 0) {
            x = x + pressedOffset;
            y = y + pressedOffset;
        }

        int lineHeight = super.getTextHeight(string);
        int start = 0;
        int lines = getNoLines();
        for (int c = 0; c < lines; c++) {
            int end = c == (lines - 1) ? string.length() : lineBreaks[c];

            String line = string.substring(start, end).trim();
            //if (line.charAt(line.length() - 1) == ' ') { // trim empty char from the end
            //    line = line.substring(0, line.length() - 1);
            //}

            int w = super.getTextWidth(line);

            g.drawString(line, x + (availableTextWidth - w) / 2, y + c * lineHeight);

            start = end;
        }
    }

    private int getNoLines() {
        return Math.min(lineBreaks.length + 1, maxNoLines);
    }

    private void setupLineBreaks() {
        if (lineBreaks == null) {
            lineBreaks = TextArea.getLines(string, font, 0, maxTextLabelWidth, maxTextLabelWidth);
        }
    }
}
