package net.yura.swing;

import junit.framework.TestCase;

/**
 * WARNING!! this is not technically a UI test, but its grouped with other UI tests as
 * its very strange and does not work on a lot of setups (e.g. running in java 1.8)
 *
 *
 * WARNING!! this class only work in new version of java, in java 1.8 this test will fail
 * @author yura
 */
public class TextUtilTest extends TestCase {

    public void testEmptyString() {
        String empty = "";
        assertEquals(0, TextUtil.graphemeCount(empty));
        assertEquals("", TextUtil.subGrapheme(empty, 0, 0));
    }

    public void testSingleLetter() {
        String s = "A";
        assertEquals(1, TextUtil.graphemeCount(s));
        assertEquals("A", TextUtil.subGrapheme(s, 0, 1));
        assertEquals("", TextUtil.subGrapheme(s, 0, 0));
    }

    public void testMultipleLetters() {
        String s = "Hello";
        assertEquals(5, TextUtil.graphemeCount(s));
        assertEquals("He", TextUtil.subGrapheme(s, 0, 2));
        assertEquals("llo", TextUtil.subGrapheme(s, 2, 5));
        assertEquals("Hello", TextUtil.subGrapheme(s, 0, 5));
    }

    public void testSpacesAndPunctuation() {
        String s = "Hi, there!";
        assertEquals(10, TextUtil.graphemeCount(s));
        assertEquals("Hi,", TextUtil.subGrapheme(s, 0, 3));
        assertEquals(" ", TextUtil.subGrapheme(s, 3, 4));
        assertEquals("there!", TextUtil.subGrapheme(s, 4, 10));
    }

    public void testSimpleEmoji() {
        String s = "\uD83D\uDE0A\uD83D\uDE02"; // ??
        assertEquals(2, TextUtil.graphemeCount(s));
        assertEquals("\uD83D\uDE0A", TextUtil.subGrapheme(s, 0, 1)); // ?
        assertEquals("\uD83D\uDE02", TextUtil.subGrapheme(s, 1, 2)); // ?
        assertEquals("\uD83D\uDE0A\uD83D\uDE02", TextUtil.subGrapheme(s, 0, 2)); // ??
    }

    public void testMixedTextAndEmoji() {
        String s = "A\uD83D\uDC4B" + "B\ud83c\uddfa\ud83c\udde6" + "\u0061\u0300\u0301\u0300\u0301\u0300\u0301\u0300\u0301"; // A?B??à???????
        assertEquals(5, TextUtil.graphemeCount(s));
        assertEquals("A", TextUtil.subGrapheme(s, 0, 1));
        assertEquals("\uD83D\uDC4BB", TextUtil.subGrapheme(s, 1, 3)); // ?B
        assertEquals("\ud83c\uddfa\ud83c\udde6\u0061\u0300\u0301\u0300\u0301\u0300\u0301\u0300\u0301", TextUtil.subGrapheme(s, 3, 5)); // ??à???????
        assertEquals("A\uD83D\uDC4BB\ud83c\uddfa\ud83c\udde6\u0061\u0300\u0301\u0300\u0301\u0300\u0301\u0300\u0301", TextUtil.subGrapheme(s, 0, 5)); // A?B??à???????
    }

    public void testEdgeCases() {
        String s = "X\uD83D\uDE0AY"; // X?Y
        assertEquals(3, TextUtil.graphemeCount(s));
        assertEquals("", TextUtil.subGrapheme(s, 0, 0));
        assertEquals("Y", TextUtil.subGrapheme(s, 2, 3));
        assertEquals("X\uD83D\uDE0AY", TextUtil.subGrapheme(s, 0, 3));
    }

    public void testComplexEmoji() {
        String s = "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83C\uDDEB\uD83C\uDDF7"; // ?????????
        assertEquals(2, TextUtil.graphemeCount(s));
        assertEquals("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66", TextUtil.subGrapheme(s, 0, 1)); // ???????
        assertEquals("\uD83C\uDDEB\uD83C\uDDF7", TextUtil.subGrapheme(s, 1, 2)); // ??
        assertEquals("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66\uD83C\uDDEB\uD83C\uDDF7", TextUtil.subGrapheme(s, 0, 2)); // ?????????
    }

    public void testLongComplexEmojiSequence() {
        String s = "\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC8B\u200D\uD83D\uDC68" +
                   "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66" +
                   "\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67"; 
        // ????????????????????
        assertEquals(3, TextUtil.graphemeCount(s));
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC8B\u200D\uD83D\uDC68",
                     TextUtil.subGrapheme(s, 0, 1)); // ????????
        assertEquals("\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66",
                     TextUtil.subGrapheme(s, 1, 2)); // ?????
        assertEquals("\uD83D\uDC69\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC67",
                     TextUtil.subGrapheme(s, 2, 3)); // ???????
        assertEquals("\uD83D\uDC69\u200D\u2764\uFE0F\u200D\uD83D\uDC8B\u200D\uD83D\uDC68" +
                     "\uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC66",
                     TextUtil.subGrapheme(s, 0, 2)); // ?????????????
    }
}
