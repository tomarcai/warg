package net.yura.domination.engine;

import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;

public class ColorUtilTest extends TestCase {

    public ColorUtilTest(String testName) {
        super(testName);
    }

    public void testIsColorLight() {
        System.out.println("testIsColorLight");

        assertEquals(true, ColorUtil.isColorLight(ColorUtil.RED));
        assertEquals(true, ColorUtil.isColorLight(ColorUtil.GREEN));
        assertEquals(true, ColorUtil.isColorLight(ColorUtil.MAGENTA));
        assertEquals(true, ColorUtil.isColorLight(ColorUtil.CYAN));
        assertEquals(true, ColorUtil.isColorLight(ColorUtil.ORANGE));
        assertEquals(true, ColorUtil.isColorLight(ColorUtil.PINK));
        assertEquals(true, ColorUtil.isColorLight(ColorUtil.LIGHT_GRAY));
        assertEquals(true, ColorUtil.isColorLight(ColorUtil.WHITE));

        assertEquals(false, ColorUtil.isColorLight(ColorUtil.DARK_GRAY));
        assertEquals(false, ColorUtil.isColorLight(ColorUtil.BLUE));
        assertEquals(false, ColorUtil.isColorLight(ColorUtil.BLACK));
    }
}
