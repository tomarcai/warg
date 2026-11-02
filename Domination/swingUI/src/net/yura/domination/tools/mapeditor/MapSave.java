package net.yura.domination.tools.mapeditor;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.guishared.RiskUIUtil;

public class MapSave {

        public static void saveMapWithImages(RiskGame myMap,
                File mapFile, File cardsFile, File imageMapFile, File imagePicFile,
                String mapName, String cardsName, String imageMapName, String imagePicName,
                BufferedImage imgPic, String imgPicFormat, File imgFile, // source of image pic data, can be one or the other
                BufferedImage imgMap, String imgMapFormat) throws Exception {
  
            saveMap(myMap,
                mapFile, cardsFile, imageMapFile, imagePicFile,
                mapName, cardsName, imageMapName, imagePicName);

            saveImage(imgMap, imgMapFormat, imageMapFile);

            if (imgFile != null) {
                if (imgFile.equals(imagePicFile)) {
                    // do nothing
                    System.out.println("no change in pic, no save needed: " + imgFile);
                }
                else {
                    RiskUtil.copy(imgFile, imagePicFile);
                }
            }
            else {
                saveImage(imgPic, imgPicFormat, imagePicFile);
            }
	}
        
        public static void saveMap(RiskGame myMap,
                File mapFile, File cardsFile, File imageMapFile, File imagePicFile,
                String mapName, String cardsName, String imageMapName, String imagePicName) throws Exception {

            if (cardsFile != null) {
                String cardsBuffer = buildCardsFile(myMap, cardsName);
                saveMap(cardsBuffer, new FileOutputStream(cardsFile));
            }

	    String buffer = buildMapFile(myMap, mapName, cardsName, imageMapName, imagePicName);
            saveMap(buffer, new FileOutputStream(mapFile));
        }

        // ####################################################### MAKE MAP FILE
        public static String buildMapFile(RiskGame myMap, String mapName, String cardsName, String imageMapName, String imagePicName) throws Exception {

            String n = System.getProperty("line.separator");
            
            StringBuffer buffer = new StringBuffer();

            buffer.append("; map: ");
            buffer.append(mapName);
            buffer.append(n);

            buffer.append("; Made with yura.net ");
            buffer.append(RiskUtil.GAME_NAME);
            buffer.append(" ");
	    buffer.append(RiskUtil.RISK_VERSION);
            buffer.append(n);

            buffer.append("; OS: ");
            buffer.append(RiskUIUtil.getOSString());
            buffer.append(n);
            buffer.append(n); // empty line

//            String name = myMap.getMapName();
//            if (name!=null) {
//                buffer.append("name ");
//                buffer.append(name);
//                buffer.append(n);
//            }
//            int version = myMap.getVersion();
//            if (version!=1) { // 1 is the default, we do not need to save that
//                buffer.append("ver ");
//                buffer.append(version);
//                buffer.append(n);
//            }
//            if (name!=null || version!=1) {
//                buffer.append(n); // in case we put a name or a version, add a extra empty line
//            }

            Map properties = myMap.getProperties();
            if (!properties.isEmpty()) {
                Iterator keyvals = properties.entrySet().iterator();
                while (keyvals.hasNext()) {
                    Map.Entry entry = (Map.Entry)keyvals.next();
                    buffer.append( entry.getKey() );
                    buffer.append(' ');
                    buffer.append( entry.getValue() );
                    buffer.append(n);
                }
                buffer.append(n);
            }

            buffer.append("[files]");
            buffer.append(n);

            buffer.append("pic ");
            buffer.append(imagePicName);
            buffer.append(n);
            buffer.append("map ");
            buffer.append(imageMapName);
            buffer.append(n);
            buffer.append("crd ");
            buffer.append(cardsName);
            buffer.append(n);

            String prv = myMap.getPreviewPic();
            // only allow preview if the name matches the name of the map
            if (prv != null && RiskUtil.getFileNameWithoutExtension(mapName).equals(RiskUtil.getFileNameWithoutExtension(prv))) {
                buffer.append("prv ");
                buffer.append(prv);
                buffer.append(n);
            }

            buffer.append(n);
            buffer.append("[continents]");
            buffer.append(n);

            Continent[] continents = myMap.getContinents();

            for (int i = 0; i < continents.length; i++) {

                Continent c = continents[i];

                buffer.append(c.getIdString());
                buffer.append(" ");
                buffer.append(c.getArmyValue());
                buffer.append(" ");
		buffer.append( ColorUtil.getStringForColor( c.getColor() ) );
                buffer.append(n);
            }

            buffer.append(n);
            buffer.append("[countries]");
            buffer.append(n);

            Country[] countries = myMap.getCountries();

            for (int i = 0; i < countries.length; i++) {

                Country c = countries[i];

                int color = c.getColor();

		if (color != (i+1)) { throw new Exception("country missmatch with pos/id/color: "+c); }

                buffer.append(String.valueOf(color));
                buffer.append(" ");
                buffer.append(c.getIdString());
                buffer.append(" ");
		buffer.append( getStringForContinent(c.getContinent(), myMap));
                buffer.append(" ");
		buffer.append( c.getX() );
                buffer.append(" ");
		buffer.append( c.getY() );
                buffer.append(n);
            }


            buffer.append(n);
            buffer.append("[borders]");
            buffer.append(n);


            for (int i = 0; i < countries.length; i++) {

                Country c = countries[i];

		buffer.append(String.valueOf(i+1));


                List ney = c.getNeighbours();
                for (int j = 0; j < ney.size(); j++) {

                    Country n1 = (Country)ney.get(j);

                    buffer.append(" ");
		    buffer.append(String.valueOf( n1.getColor() ) );
                }

                buffer.append(n);

            }
            return buffer.toString();
        }

        private static void saveImage(BufferedImage im, String formatName, File output) throws Exception {
	    if ( !ImageIO.write( im , formatName , output ) ) {
		// this should NEVER happen
		throw new Exception("unable to save image files! " + output + " format=" + formatName);
	    }
        }

        private static void saveMap(String text, OutputStream outputStream) throws IOException {
            Writer output = null;
	    try {

                boolean utf8 = false;
                for (int c=0,l=text.length();c<l;c++) {
                    char ch = text.charAt(c);
                    if (ch >= 256) {
                        utf8 = true;
                        break;
                    }
                }

                if (utf8) {
                    outputStream.write(0xEF);
                    outputStream.write(0xBB);
                    outputStream.write(0xBF);
                    output = new BufferedWriter( new OutputStreamWriter(outputStream,"UTF-8") );
                    output.write("; 1.1.0.7+ (UTF-8)");
                    output.write( System.getProperty("line.separator") );
                }
                else {
                    output = new BufferedWriter( new OutputStreamWriter(outputStream,"ISO-8859-1") );
                }

		output.write( text );
            }
	    finally {
		if (output != null) output.close();
	    }
        }
    
        // ####################################################### MAKE CARDS FILE
        public static String buildCardsFile(RiskGame myMap, String cardsName) throws Exception {

            String n = System.getProperty("line.separator");
            
	    StringBuffer cardsBuffer = new StringBuffer();

	    cardsBuffer.append("; cards: ");
	    cardsBuffer.append(cardsName);
	    cardsBuffer.append(n);

	    cardsBuffer.append("; Made with yura.net ");
            cardsBuffer.append(RiskUtil.GAME_NAME);
            cardsBuffer.append(" ");
	    cardsBuffer.append(RiskUtil.RISK_VERSION);
	    cardsBuffer.append(n);
            
            cardsBuffer.append("; OS: ");
            cardsBuffer.append( RiskUIUtil.getOSString() );
	    cardsBuffer.append(n);

	    cardsBuffer.append(n);
	    cardsBuffer.append("[cards]");
	    cardsBuffer.append(n);

	    List cards = myMap.getCards();

            for (int i = 0; i < cards.size(); i++) {

                Card c = (Card)cards.get(i);

		cardsBuffer.append(c.getName());

		if (c.getCountry() != null) {
			int color = c.getCountry().getColor();
			cardsBuffer.append(" ");
			cardsBuffer.append(String.valueOf(color));
		}

		cardsBuffer.append(n);
	    }

	    List missions = myMap.getMissions();

	    if (missions.size()>0) {

	    	cardsBuffer.append(n);
	    	cardsBuffer.append("; destroy x occupy x x continents x x x");
	    	cardsBuffer.append(n);
	    	cardsBuffer.append("; destroy (Player) occupy (int int) continents (Continent Continent Continent)");
	    	cardsBuffer.append(n);
	    	cardsBuffer.append("[missions]");
	    	cardsBuffer.append(n);

		for (int i = 0; i < missions.size(); i++) {
			Mission m = (Mission)missions.get(i);
			cardsBuffer.append(getMissionString(m, myMap));
			cardsBuffer.append(n);
		}
	    }

            return cardsBuffer.toString();
        }

        public static String getMissionString(Mission m, RiskGame context) {
            StringBuffer cardsBuffer = new StringBuffer();
            
            if (m.getPlayer()!=null) {
                    cardsBuffer.append( m.getPlayer().getName().substring(6,7) ); // PLAYER1
            }
            else {
                    cardsBuffer.append("0");
            }

            cardsBuffer.append("\t");
            cardsBuffer.append(String.valueOf( m.getNoofcountries() ));
            cardsBuffer.append(" ");
            cardsBuffer.append(String.valueOf( m.getNoofarmies() ));
            cardsBuffer.append("\t");
            cardsBuffer.append( getStringForContinent(m.getContinent1(), context) );
            cardsBuffer.append(" ");
            cardsBuffer.append( getStringForContinent(m.getContinent2(), context) );
            cardsBuffer.append(" ");
            cardsBuffer.append( getStringForContinent(m.getContinent3(), context) );
            cardsBuffer.append("\t");
            cardsBuffer.append(m.getDiscription());

            return cardsBuffer.toString();
        }

        public static String getStringForContinent(Continent c, RiskGame context) {

		if (c == null) {
			return "0";
		}
		if (c == RiskGame.ANY_CONTINENT) {
			return "*";
		}

		Continent[] continents = context.getContinents();

		for (int i = 0; i < continents.length; i++) {
			if (continents[i] == c) {
				return String.valueOf(i+1);
			}
		}

		throw new RuntimeException();
	}
}
