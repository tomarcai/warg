package net.yura.domination.tools.mapeditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.RiskGame;
import net.yura.mobile.io.kxml2.KXmlParser;

/**
 * @author Yura Mamyrin
 */
public class TegMapLoader {

    static class ImgMapFilter extends RGBImageFilter {
        int color;
        public ImgMapFilter(int color) {
            this.color = color;
        }
        public int filterRGB(int x, int y, int rgb) {
            int alpha = ColorUtil.getAlpha(rgb);
            return alpha > 127 ? color : 0;
        }
    }

    static class ImgPicFilter extends RGBImageFilter {
        public int filterRGB(int x, int y, int rgb) {
            int alpha = ColorUtil.getAlpha(rgb);
            return alpha > 127 ? rgb : 0;
        }
    }

    public void load(File xmlfile, RiskGame map, MapEditor editor) throws Exception {

        BufferedImage board=null,imgMap=null;

        File dir = xmlfile.getParentFile();

        InputStream input = new FileInputStream(xmlfile);

        KXmlParser parser = new KXmlParser();
        parser.setInput(input, null);
        parser.nextTag();
        // read start tag

        int continentId=0;
        int countryId=0;
        Point armiesPos = null;
        
        while (parser.nextTag() != KXmlParser.END_TAG) {
            String name = parser.getName();
            if ("continent".equals(name)) {
                String continentName = parser.getAttributeValue(null, "name");

                int continentX = Integer.parseInt( parser.getAttributeValue(null, "pos_x") );
                int continentY = Integer.parseInt( parser.getAttributeValue(null, "pos_y") );

                int myContinentId = convertContinentId(continentId);

                Continent continent = map.getContinents()[myContinentId];
                
                continent.setName(continentName);
                continent.setIdString(continentName.replace(' ', '_'));
                
                while (parser.nextTag() != KXmlParser.END_TAG) {
                    String name2 = parser.getName();

                    if ("country".equals(name2)) {

                        String countryName = parser.getAttributeValue(null, "name");
                        String file = parser.getAttributeValue(null, "file");

                        int pos_x = Integer.parseInt( parser.getAttributeValue(null, "pos_x") );
                        int pos_y = Integer.parseInt( parser.getAttributeValue(null, "pos_y") );
                        String army_x = parser.getAttributeValue(null, "army_x");
                        int armyX = (army_x==null||"".equals(army_x))?0:Integer.parseInt( army_x );
                        String army_y = parser.getAttributeValue(null, "army_y");
                        int armyY = (army_y==null||"".equals(army_y))?0:Integer.parseInt( army_y );

                        BufferedImage countryImage = ImageIO.read( new File(dir, file) );

                        Graphics g = board.getGraphics();
                        drawTegCountry(g, countryImage, continentX+pos_x, continentY+pos_y);
                        g.dispose();

                        // m3 map for unkown reasons has armies_pos in old coordinates of m2 map
                        if (armiesPos != null && new Rectangle(continentX+pos_x, continentY+pos_y, countryImage.getWidth(), countryImage.getHeight()).contains(armiesPos)) {
                            armiesPos = null;
                        }

                        Color color = new Color(countryId+1, countryId+1, countryId+1);
                        FilteredImageSource filteredSrc = new FilteredImageSource(countryImage.getSource(), new ImgMapFilter(color.getRGB()));
                        Image image = Toolkit.getDefaultToolkit().createImage(filteredSrc);

                        Graphics g2 = imgMap.getGraphics();
                        g2.drawImage(image, continentX+pos_x, continentY+pos_y, null);
                        g2.dispose();

                        Country country = map.getCountries()[countryId];

                        country.setName(countryName);
                        country.setIdString(countryName.replace(' ', '_'));
                        country.setX( continentX+pos_x+  (countryImage.getWidth()/2)  +(armyX/2) );
                        country.setY( continentY+pos_y+  (countryImage.getHeight()/2)  +(armyY/2) );

                        if (country.getContinent() != continent) {
                            throw new IllegalStateException("continent does not match for " + country + " " +country.getContinent() + "!=" + continent);
                        }
                        
                        countryId++;
                    }
                    // read end tag
                    parser.skipSubTree();
                }
                continentId++;
            }
            else {
                
                if ("board".equals(name) || "map".equals(name)) {
                    String file = parser.getAttributeValue(null, "file");
                    
                    File boardFile = new File(dir, file);
                    
                    board = MapEditor.makeRGBImage( ImageIO.read(boardFile) );
                    imgMap = MapEditor.newImageMap(board.getWidth(), board.getHeight());
                }
                else if ("armies_pos".equals(name)) {
                    //<armies_pos x="358" y="24" background="false" dragable="false"/>
                    String x = parser.getAttributeValue(null, "x");
                    String y = parser.getAttributeValue(null, "y");
                    String background = parser.getAttributeValue(null, "background");
                    String dragable = parser.getAttributeValue(null, "dragable");
                    // in 'modern' theme there is not enough room for armies info
                    if (background != null && dragable != null) {
                        armiesPos = new Point(Integer.parseInt(x), Integer.parseInt(y));
                    }
                }

                // read end tag
                parser.skipSubTree();
            }
        }

        if (armiesPos != null) {
            Graphics g2 = board.getGraphics();
            int yOffset = g2.getFontMetrics().getAscent();
            int xOffset = g2.getFontMetrics().stringWidth(" 9 ");
            g2.setColor(Color.BLACK);

            for (Continent continent : map.getContinents()) {
                // some maps use different colors for the continents, so this may be wrong
                //g2.setColor(new Color(continent.getColor()));
                g2.drawString(" " + continent.getArmyValue(), armiesPos.x, armiesPos.y + yOffset);
                g2.drawString(continent.getName(), armiesPos.x + xOffset, armiesPos.y + yOffset);
                yOffset = yOffset + g2.getFontMetrics().getHeight();
            }
            g2.dispose();
        }

        editor.setImagePic(board, null, false);
        editor.setImageMap(imgMap);
    }

    int convertContinentId(int tegId) {
        switch(tegId) {
            case 0: return 1; // South-America
            case 1: return 0; // North-America
            case 2: return 3; // Africa
            case 3: return 5; // Australia/Oceania
            case 4: return 2; // Europe
            case 5: return 4; // Asia
            default: throw new IllegalArgumentException("strange teg id " + tegId);
        }
    }

    private void drawTegCountry(Graphics g, BufferedImage countryImage, int x, int y) {
        if (countryImage.getAlphaRaster() != null) {
            g.drawImage(countryImage, x, y, null);
        }
        else {
            FilteredImageSource filteredSrc = new FilteredImageSource(countryImage.getSource(), new ImgPicFilter());
            Image image = Toolkit.getDefaultToolkit().createImage(filteredSrc);
            g.drawImage(image, x, y, null);
        }
    }
}
