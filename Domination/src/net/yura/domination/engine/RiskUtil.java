package net.yura.domination.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.PushbackInputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.translation.MapTranslator;

public class RiskUtil {

        public static final Object SUCCESS = "SUCCESS";
        public static final Object ERROR = "ERROR";

	public static final String RISK_VERSION_URL;
	public static final String RISK_LOBBY_URL;
//	public static final String RISK_POST_URL; // look in Grasshopper.jar now
	public static final String GAME_NAME;
	public static final String RISK_VERSION;
//	private static final String DEFAULT_MAP;

        private static boolean oldVersion;

        private static final Logger logger = Logger.getLogger(RiskUtil.class.getName());
	public static RiskIO streamOpener;

        /**
         * @see java.nio.charset.StandardCharsets#UTF_8
         */
        public final static Charset UTF_8 = Charset.forName("UTF-8");
        
	static {

		Properties settings = new Properties();

		InputStream settingsData = RiskUtil.class.getResourceAsStream("settings.ini");
		try {
			settings.load(settingsData);
		}
		catch (Exception ex) {
			throw new RuntimeException("can not find settings.ini file!",ex);
		}
		finally {
		    close(settingsData);
		}

		RISK_VERSION_URL = settings.getProperty("VERSION_URL");
		RISK_LOBBY_URL = settings.getProperty("LOBBY_URL");
//		RISK_POST_URL = settings.getProperty("POST_URL");
		GAME_NAME = settings.getProperty("name");
		//DEFAULT_MAP = settings.getProperty("defaultmap");
		RISK_VERSION = settings.getProperty("version");

		String dmap = settings.getProperty("defaultmap");
		String dcards = settings.getProperty("defaultcards");

		RiskGame.setDefaultMapAndCards( dmap , dcards );
	}

	public static void close(Closeable obj) {
	    try {
	        if (obj != null) {
                    obj.close();
                }
            }
            catch (Exception ex) {
                logger.info("not able to close " + obj);
            }
        }

	public static InputStream openMapStream(String a) throws IOException {
            if (a == null) {
                throw new NullPointerException("map filename is null");
            }
            return streamOpener.openMapStream(a);
	}

	public static InputStream openStream(String a) throws IOException {
            return streamOpener.openStream(a);
	}

	public static ResourceBundle getResourceBundle(Class c,String n,Locale l) {
            return streamOpener.getResourceBundle(c, n, l);
	}

	public static void openURL(URL url) throws Exception {
            streamOpener.openURL(url);
	}

	public static void openDocs(String docs) throws Exception {
            streamOpener.openDocs(docs);
	}
        public static void saveFile(String file, RiskGame aThis) throws Exception {
            streamOpener.saveGameFile(file, aThis);
        }
        public static InputStream getLoadFileInputStream(String file) throws Exception {
            return streamOpener.loadGameFile(file);
        }

        public static void printStackTrace(Throwable ex) {
            printStackTrace(null, ex);
        }
        
        public static void printStackTrace(String msg, Throwable ex) {
            logger.log(Level.WARNING, msg, ex);
        }

        public static void donate() throws Exception {
		openURL(new URL("http://domination.sourceforge.net/donate.shtml"));
	}

        public static void patreon() throws Exception {
                openURL(new URL("http://domination.sourceforge.net/patreon.shtml"));
        }

	public static void donatePayPal() throws Exception {
		openURL(new URL("https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=yura%40yura%2enet&item_name="+GAME_NAME+"%20Donation&no_shipping=0&no_note=1&tax=0&currency_code=GBP&lc=GB&bn=PP%2dDonationsBF&charset=UTF%2d8"));
	}

        public static BufferedReader readMap(InputStream in) throws IOException {

            PushbackInputStream pushback = new PushbackInputStream(in,3);

            int first = pushback.read();
            if (first == 0xEF) {
                int second = pushback.read();
                if (second == 0xBB) {
                    int third = pushback.read();
                    if (third == 0xBF) {
                        return new BufferedReader(new InputStreamReader( pushback, "UTF-8" ) );
                    }
                    pushback.unread(third);
                }
                pushback.unread(second);
            }
            pushback.unread(first);

            return new BufferedReader(new InputStreamReader( pushback, "ISO-8859-1" ) );
        }

        /**
         * gets the info for a map or cards file
         * in the case of map files it will get the "name" "crd" "prv" "pic" "map" and any "comment" and number of "countries"
         * and for cards it will have a "missions" that will contain the String[] of all the missions
         */
	public static Map loadInfo(String fileName, boolean cards) {

            Map info = new HashMap();

            for (int c=0;true;c++) {

                BufferedReader bufferin=null;

                try {

                        bufferin = RiskUtil.readMap(RiskUtil.openMapStream(fileName));
                        List misss = null;
                        MapTranslator mapTranslator = null;

                        if (cards) {
                            mapTranslator = new MapTranslator();
                            mapTranslator.setCards(fileName);
                            misss = new ArrayList();
                        }

                        String input = bufferin.readLine();
                        String mode = null;

                        while(input != null) {

                                if (input.equals("")) {
                                        // do nothing
                                        //System.out.print("Nothing\n"); // testing
                                }
                                else if (input.charAt(0)==';') {
                                    String comment = (String)info.get("comment");
                                    String com = input.substring(1).trim();
                                    if (comment==null) {
                                        comment = com;
                                    }
                                    else {
                                        comment = comment +"\n"+com;
                                    }
                                    info.put("comment", comment);
                                }
                                else {

                                        if (input.charAt(0)=='[' && input.charAt( input.length()-1 )==']') {
                                                mode="newsection";
                                        }

                                        if ("files".equals(mode)) {

                                                int space = input.indexOf(' ');

                                                if (space < 0) {
                                                    throw new RuntimeException("invalid file " + input);
                                                }

                                                String fm = input.substring(0,space);
                                                String val = input.substring(space+1);

                                                info.put( fm , val);

                                        }
                                        else if ("borders".equals(mode)) {
                                                // we dont care about anything in or after the borders section
                                                break;
                                        }
                                        else if ("countries".equals(mode)) {
                                                int space = input.indexOf(' ');

                                                if (space < 0) {
                                                    throw new RuntimeException("invalid country " + input);
                                                }

                                                info.put("countries", Integer.parseInt(input.substring(0, space)));
                                        }
                                        else if ("missions".equals(mode)) {

                                                StringTokenizer st = new StringTokenizer(input);
                                            
                                                String description=mapTranslator.getTranslatedMissionName(st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken()+"-"+st.nextToken());

                                                if (description==null) {

                                                        StringBuffer d = new StringBuffer();

                                                        while (st.hasMoreElements()) {

                                                                d.append( st.nextToken() );
                                                                d.append( " " );
                                                        }

                                                        description = d.toString();

                                                }

                                                misss.add( description );

                                        }
                                        else if ("newsection".equals(mode)) {

                                                mode = input.substring(1, input.length()-1); // set mode to the name of the section

                                        }
                                        else if (mode == null) {
                                            if (input.indexOf(' ')>0) {
                                                info.put( input.substring(0,input.indexOf(' ')) , input.substring(input.indexOf(' ')+1) );
                                            }
                                        }
                                        // if "continents" or "cards" then just dont do anything in those sections

                                }

                                input = bufferin.readLine(); // get next line
                        }

                        if (cards) {
                            info.put("missions", (String[])misss.toArray(new String[misss.size()]) );
                            misss = null;
                        }

                        break;
                }
                catch (IOException ex) {
                        RiskUtil.printStackTrace("Error trying to load: " + fileName, ex);
                        if (c < 5) { // retry
                                try { Thread.sleep(1000); } catch(Exception ex2) { }
                        }
                        else { // give up
                                break;
                        }
                }
                catch (RuntimeException ex) {
                    System.out.println("Error trying to load: " + fileName);
                    throw ex;
                }
                finally {
                    if (bufferin!=null) {
                        try { bufferin.close(); } catch(Exception ex2) { }
                    }
                }
            }

            return info;
	}

        /**
         * WARNING!!! this method can take a long time!!! up to 6 seconds!
         */
        public static String createRandomUniqueAddress() {

		String randomString = "#"+String.valueOf( Math.round(Math.random()*Long.MAX_VALUE) );

		try {
			//if (RiskUtil.checkForNoSandbox()) {
                        try {
				String hostname = getLocalHost().getHostName();
				hostname = JavaCompatUtil.replaceAll(hostname, " ", ""); // on Mac hostname can have a space
				return hostname + randomString;
			}
			//else {
                        catch(Throwable th) {
				return "sandbox" + randomString;
			}
/*

			//InetAddress localAddr = InetAddress.getLocalHost();

			//myAddress = localAddr.getHostAddress();

			myAddress=null;
			Enumeration ifaces = NetworkInterface.getNetworkInterfaces();

			search:
			while (ifaces.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface)ifaces.nextElement();
				//System.out.println(ni.getName() + ":");

				Enumeration addrs = ni.getInetAddresses();

				while (addrs.hasMoreElements()) {
					InetAddress ia = (InetAddress)addrs.nextElement();
					//System.out.println(" " + ia.getHostAddress());


					String tmpAddr = ia.getHostAddress();
					if (!tmpAddr.equals("127.0.0.1")) {

						myAddress = tmpAddr;
						break search;

					}


				}
			}

			if (myAddress==null) {
				throw new Exception("no IP found");
			}
*/

		}
		catch (Exception e) { // if network has not been setup
			return "nonet" + randomString;
		}
        }
        
        private static InetAddress localHost;
        /**
         * WARNING! this method can take a LONG time on macOS!
         */
        public static InetAddress getLocalHost() throws UnknownHostException {
            if (localHost == null) {
                // on macOS this method can take a long time
                long start = System.currentTimeMillis();
                // TODO WARNING!! on iOS this can cause the popup of "app would like to find and connect to devices on your local network"
                localHost = InetAddress.getLocalHost();
                System.out.println("getLocalHost took " + (System.currentTimeMillis() - start) + "milliseconds");
            }
            return localHost;
        }

        /**
         * This is only non empty when loading from random file locations in the MapEditor
         */
        public static String getContext(String f) {
                int slash = f.lastIndexOf('\\');
                if (slash >= 0) {
                    return f.substring(0, slash + 1);
                }
                slash = f.lastIndexOf('/');
                if (slash >= 0) {
                    return f.substring(0, slash + 1);
                }
                return "";
        }

        public static void saveGameLog(File logFile, RiskGame game) throws IOException {
            FileWriter fileout = new FileWriter(logFile);
            BufferedWriter buffer = new BufferedWriter(fileout);
            PrintWriter printer = new PrintWriter(buffer);
            List commands = game.getCommands();
            int size = commands.size();
            for (int line = 0; line < size; line++) {
                printer.println(commands.get(line));
            }
            printer.close();
        }

        public static OutputStream getOutputStream(File dir,String fileName) throws Exception {
            File outFile = new File(dir,fileName);
            // as this could be dir=.../maps fileName=preview/file.jpg
            // we need to make sure the preview dir exists, and if it does not, we must make it
            File parent = outFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) { // if it does not exist and i cant make it
                throw new RuntimeException("can not create dir "+parent);
            }

            // we always save new data into new files
            // even though many examples show this, it is not needed
            //if (!outFile.createNewFile()) {
            //    logger.info("WARNING! getOutputStream " + outFile + " createNewFile returned false!");
            //}

            return new FileOutputStream( outFile );
        }

        public static void rename(File oldFile,File newFile) {
            if (newFile.exists() && !newFile.delete()) {
                throw new RuntimeException("can not del dest file: "+newFile);
            }
            if (!oldFile.renameTo(newFile)) {
                try {
                    copy(oldFile, newFile);
                    if (!oldFile.delete()) {
                        // this is not so bad, but still very strange
                        logger.warning("can not del source file: " + oldFile);
                    }
                }
                catch(Exception ex) {
                    throw new RuntimeException("rename failed: from: "+oldFile+" to: "+newFile,ex);
                }
            }
        }

        /**
         * this method must NEVER throw an exception, even if everything goes very very wrong
         */
        public static String getNewVersionCheck() {
            try {
                URL url = new URL(RiskUtil.RISK_VERSION_URL);

                BufferedReader bufferin=new BufferedReader( new InputStreamReader(url.openStream()) );
                List buffer = new ArrayList();
                String input = bufferin.readLine();

                while(input != null) {
                        buffer.add(input);
                        input = bufferin.readLine(); // get next line
                }

                String[] newversion = (String[])buffer.toArray( new String[buffer.size()] );
                
                if (newversion[0].startsWith("RISKOK ")) {

                        String v = newversion[0].substring(7, newversion[0].length() );

                        if (!v.equals(RiskUtil.RISK_VERSION)) {
                                oldVersion = true;

                                for (int c=1;c<newversion.length;c++) {
                                        v = v+"\n"+newversion[c];
                                }
                
                                return v;
                        }
                }
            }
            catch (Throwable e) {
                logger.info("version check fail " + e);
            }

            return null;
        }

    public static void setOldVersion() {
        oldVersion = true;
    }

    public static boolean isOldVersion() {
        return oldVersion;
    }

    public static String asHTML(Continent[] continents) {
/*
            for (Continent continent:continents) {
                Element tr;
                table.addChild(tr = new Element("tr",
                        new Element("td",continent.getName()),
                        new Element("td"," - "),
                        new Element("td",String.valueOf(continent.getArmyValue()))
                ));
                tr.setAttribute(null, "style", "background-color:"+ ColorUtil.getHexForColor(continent.getColor())+"; color:"+ColorUtil.getHexForColor(ColorUtil.getTextColorFor(continent.getColor()))+";" );
            }
*/
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html><table>");

        for (int c = 0; c < continents.length; c++) {
            Continent continent = continents[c];

            buffer.append("<tr style=\"background-color: ");
            buffer.append(ColorUtil.getHexForColor(continent.getColor()));
            buffer.append("; color:");
            buffer.append(ColorUtil.getHexForColor(ColorUtil.getTextColorFor(continent.getColor())));
            buffer.append("\"><td>");
            buffer.append(continent.getName());
            buffer.append("</td><td> - </td><td>");
            buffer.append(continent.getArmyValue());
            buffer.append("</td></tr>");
        }
        buffer.append("</table></html>");
        return buffer.toString();
    }

    public static void copy(File src, File dest) throws IOException {
     
            if(src.isDirectory()){
     
                    //if directory not exists, create it
                    if(!dest.exists()){
                       dest.mkdir();
                       logger.info("Directory copied from " + src + "  to " + dest);
                    }
     
                    //list all the directory contents
                    String files[] = src.list();
     
                    for (int c=0;c<files.length;c++) {
                       //construct the src and dest file structure
                       File srcFile = new File(src, files[c]);
                       File destFile = new File(dest, files[c]);
                       //recursive copy
                       copy(srcFile,destFile);
                    }
     
            }else{
                    //if file, then copy it
                    //Use bytes stream to support all file types
                    InputStream in = new FileInputStream(src);
                    OutputStream out = new FileOutputStream(dest); 
     
                    byte[] buffer = new byte[1024];
     
                    int length;
                    //copy the file content in bytes 
                    while ((length = in.read(buffer)) > 0){
                       out.write(buffer, 0, length);
                    }
     
                    in.close();
                    out.close();
                    logger.info("File copied from " + src + " to " + dest);
            }
    }

    public static String getAtLeastOne(StringTokenizer stringT) {
        StringBuilder text = new StringBuilder(stringT.nextToken());
        while ( stringT.hasMoreTokens() ) {
            text.append(' ');
            text.append(stringT.nextToken());
        }
        return text.toString();
    }

    /**
     * @see net.yura.domination.guishared.RiskFileFilter#getExtension(java.io.File)
     * @see net.yura.domination.tools.mapeditor.MapsTools#getExtension(java.io.File)
     */
    public static String getFileNameWithoutExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        // if file starts with a dot, this is a hidden file, not an extension
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }

    public static String EMOJI_HUMAN_ALIVE = "\ud83e\uddd1"; // living human: \ud83d\udc64
    public static String EMOJI_HUMAN_DEAD = "\ud83d\udc80";
    public static String EMOJI_AI_ALIVE = "\ud83e\udd16"; // living computer: \ud83d\udcbb
    public static String EMOJI_AI_DEAD = "\ud83d\uddd1";
    // for a card old android devices only support color emoji \ud83c\udfb4 and \ud83c\udccf
    public static String SYMBOL_CARD = "\ud83c\udca0"; // old card: "\ud83c\udcad" does not work in java 1.5
    public static String SYMBOL_FLAG = "\u2691"; // red emoji flag \ud83d\udea9
    public static String SYMBOL_ARMY = "\uc6c3"; // happy face: \u263a
    public static String SYMBOL_EXTRA_ARMIES = "+";

    public static String[][] getDashboardText(RiskGame game) {
        List<Player> players = game.getPlayers();
        String[][] dashboard = new String[players.size()][];

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);

            String emoji = p.isAlive() ? (p.getType() == Player.PLAYER_HUMAN ? EMOJI_HUMAN_ALIVE : EMOJI_AI_ALIVE) : (p.getType() == Player.PLAYER_HUMAN ? EMOJI_HUMAN_DEAD : EMOJI_AI_DEAD);

            int extra = game.getSetupDone() ? game.getExtraArmiesForPlayer(p) : p.getExtraArmies();

            dashboard[i] = new String[] { emoji + " " + p.getName(), SYMBOL_CARD + p.getCards().size(), SYMBOL_FLAG + p.getNoTerritoriesOwned(), SYMBOL_ARMY + p.getNoArmies(), SYMBOL_EXTRA_ARMIES + extra };
        }
        return dashboard;
    }

    /**
     * @see net.yura.mobile.util.Url#toHashtable(String)
     */
    public static Map<String, String> queryAsMap(String query) {
        Map<String,String> map = new HashMap();
        for (String p : query.split("&")) {
            String[] kv = p.split("=", 2);
            map.put(urlDecode(kv[0]), urlDecode(kv[1]));
        }
        return map;
    }

    public static String urlDecode(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8");
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
