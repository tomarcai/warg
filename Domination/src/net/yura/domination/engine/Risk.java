// Yura Mamyrin, Group D

package net.yura.domination.engine;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Mission;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.p2pclient.ChatClient;
import net.yura.domination.engine.p2pserver.ChatArea;
import net.yura.domination.engine.translation.TranslationBundle;

/**
 * <p> Main Risk Class </p>
 * @author Yura Mamyrin
 */
public class Risk extends Thread {

        public static final String STARTGAME_OPTION_MODE_DOMINATION = "domination";
        public static final String STARTGAME_OPTION_MODE_CAPITAL = "capital";
	public static final String STARTGAME_OPTION_MODE_SECRET_MISSION = "mission";
	public static final String STARTGAME_OPTION_CARD_INCREASING_SET = "increasing";
	public static final String STARTGAME_OPTION_CARD_FIXED_SET = "fixed";
	public static final String STARTGAME_OPTION_CARD_ITALIAN_LIKE_SET = "italianlike";
        public static final String STARTGAME_OPTION_AUTO_PLACE_ALL = "autoplaceall";
        public static final String STARTGAME_OPTION_RECYCLE_CARDS = "recycle";
        
        public static final String STARTGAME_OPTION_DEFENDER_DICE = "defenderdice=";

        private static final int DEFAULT_SHOW_DICE_SLEEP = 1000;
        private static final int DEFAULT_ROLL_DICE_SLEEP = 500;

        private static int SHOW_DICE_SLEEP = DEFAULT_SHOW_DICE_SLEEP;
        private static int ROLL_DICE_SLEEP = DEFAULT_ROLL_DICE_SLEEP;

        private static final Logger logger = Logger.getLogger(Risk.class.getName());

        protected RiskGame game;
	protected RiskController controller;

        OnlineRisk onlinePlayClient;
	private ChatArea p2pServer;

	private int p2pPort = 4444;

	protected String myAddress;

	// crashes on a mac too much
	//private SealedObject Undo;
	private ByteArrayOutputStream Undo = new ByteArrayOutputStream();

        /**
         * this is true ONLY for local games, in p2p and Lobby client it is false
         * and on the Lobby server even though {@link #onlinePlayClient} is null, this is FALSE!
         */
	protected boolean unlimitedLocalMode;
	private boolean autoplaceall;
	private boolean battle;
	private boolean replay;

	protected final List inbox = new java.util.Vector();

	protected ResourceBundle resb;
	protected Properties riskconfig;

        public static final String[] types = new String[] { "human","ai easy","ai easy","ai easy","ai average","ai average" };
        public static final String[] names = new String[] { "player","bob","fred","ted","yura","lala"};
        public static final String[] colors = new String[] { "cyan","green","magenta","red","blue","yellow"};

	public Risk() {
                // default Android value does not work
                // 10,000 gives StackOverflowError on android on default map
                // 100,000 gives StackOverflowError on android on the map "The Keep"
                // 1,000,000 gives StackOverflowError on android on the map "The Keep" if you place all the troops
                // 10,000,000 very rarly gives crash
                // 100,000,000 crashes on "Castle in the Sky" map on Android (CURRENT VALUE)
                // 1,000,000,000 still crashes on "Castle in the Sky" (also crashes 32bit java SE)
                // 10,000,000,000 still crashes on "Castle in the Sky" (also crashes 32bit java SE)
                // 100,000,000,000 still crashes on "Castle in the Sky" (also crashes 32bit java SE)
                // 1,000,000,000,000 crashes the whole Android JVM, FUCK FUCK FUCK
		super(null,null,"DOMINATION-GAME-THREAD", 100000000);

		resb = TranslationBundle.getBundle();

                try {
                    String newName = System.getProperty("user.name");

                    if (newName==null || "".equals(newName.trim()) || "?".equals(newName.trim())) {
                        throw new Exception("bad user name: " + newName);
                    }
                    else {
                        for (int c=0;c<names.length;c++) {
                            if (names[c].equals(newName)) {
                                throw new Exception("name already in use: " + newName);
                            }
                        }
                    }
                    names[0] = newName;
		}
                catch(Throwable th) { }

		riskconfig = new Properties();

                // game setup defaults
                riskconfig.setProperty(RiskSettings.DEFAULT_AUTO_PLACE_ALL_KEY, "false");
                riskconfig.setProperty(RiskSettings.DEFAULT_RECYCLE_CARDS_KEY, "true");
                // player settings are only used at first launch or if java.util.prefs.Preferences fail to save
                for (int c=0;c<names.length;c++) {
                    riskconfig.setProperty("default.player" + (c + 1) + ".type", types[c]);
                    riskconfig.setProperty("default.player" + (c + 1) + ".color", colors[c]);
                    riskconfig.setProperty("default.player" + (c + 1) + ".name", names[c]);
                }

                // set default to display in UI connect dialog
		riskconfig.setProperty("p2p.host","localhost");

		try {
                    riskconfig.load( RiskUtil.openStream("game.ini") );
		}
		catch (Exception ex) {
                    // can not find file, no problem
		}

                // initalise static settings from game config file (on PC game only)
		RiskGame.setDefaultMapAndCards(
                        riskconfig.getProperty(RiskSettings.DEFAULT_MAP_KEY, RiskGame.getDefaultMap()),
                        riskconfig.getProperty(RiskSettings.DEFAULT_CARDS_KEY, RiskGame.getDefaultCards())
                );
                RiskGame.MAX_PLAYERS = Integer.parseInt(riskconfig.getProperty("game.players.max", String.valueOf(RiskGame.MAX_PLAYERS)));
		p2pPort = Integer.parseInt(riskconfig.getProperty("p2p.port", String.valueOf(p2pPort)));

                setShowDice(Boolean.parseBoolean(riskconfig.getProperty(RiskSettings.SHOW_DICE_KEY, String.valueOf(isShowDice()))));
                AIManager.setWait(Integer.parseInt(riskconfig.getProperty(RiskSettings.AI_WAIT_KEY, String.valueOf(AIManager.getWait()))));

		controller = new RiskController();

		start();
	}

        public Risk(String b,String c) {
		this();
		RiskGame.setDefaultMapAndCards(b,c);
	}

        public static void setShowDice(boolean show) {
            if (show) {
                SHOW_DICE_SLEEP = DEFAULT_SHOW_DICE_SLEEP;
                ROLL_DICE_SLEEP = DEFAULT_ROLL_DICE_SLEEP;
            }
            else {
                SHOW_DICE_SLEEP = 0;
                ROLL_DICE_SLEEP = 0;
            }
        }
        
        public static boolean isShowDice() {
            return SHOW_DICE_SLEEP > 0 || ROLL_DICE_SLEEP > 0;
        }

	public String getRiskConfig(String a) {
		return riskconfig.getProperty(a);
	}

	public void addRiskListener(RiskListener o) {
		controller.addListener(o);
		setHelp();
	}

	public void deleteRiskListener(RiskListener o) {
		controller.deleteListener(o);
	}

        private class GameCommand implements Runnable {
            public static final int UI_COMMAND = 1;
            public static final int NETWORK_COMMAND = 2;
            final int type;
            final String command;
            Object notifier;

            public GameCommand(int t,String c) {
                type = t;
                command = c;
            }

            public void run() {
                try {
                    if (type == GameCommand.UI_COMMAND) {
                        processFromUI(command);
                    }
                    else if (type == GameCommand.NETWORK_COMMAND) {
                        inGameParser(command);
                    }
                    else {
                        throw new RuntimeException();
                    }
                }
                finally {
                    if (notifier != null) {
                        synchronized (notifier) {
                            notifier.notifyAll();
                        }
                    }
                }
            }

            public String toString() {
                return (type == UI_COMMAND ? "UI" : "NETWORK") + " " + command;
            }
        }

	/**
	 * This parses the string, calls the relevant method and displays the correct error messages
	 * @param commandFromUI The string needed for parsing
	 */
	public void parser(String commandFromUI) {
            addToInbox(new GameCommand(GameCommand.UI_COMMAND, commandFromUI));
	}

        public void parserFromNetwork(String m) {
            addToInbox( new GameCommand(GameCommand.NETWORK_COMMAND, m ) );
	}

        public void parserAndWait(String uiCommand) throws InterruptedException {
            GameCommand gCommand = new GameCommand(GameCommand.UI_COMMAND, uiCommand );
            Object lock = new Object();
            gCommand.notifier = lock;
            synchronized(lock) {
                addToInbox(gCommand);
                lock.wait();
            }
        }

        private void addToInbox(Runnable m) {
		synchronized(inbox) {
			inbox.add(m);
			inbox.notify();
		}
        }

        public void kill() {
            synchronized(inbox) {
                running = false;
                inbox.notify();
            }
        }

        boolean running = true;
	public void run() {
            // we do this in the background thread as this may be slow
            myAddress = RiskUtil.createRandomUniqueAddress();
            
            Runnable message=null;
            while (running) {
		try {
			synchronized(inbox) {
				while ( inbox.isEmpty() ) {
                                        if (!running) return;

					try { inbox.wait(); }
					catch(InterruptedException e) {
                                                // TODO fix this, this is totally wrong
						System.err.println("InterruptedException in "+getName());
					}
				}

				message = (Runnable) inbox.remove(0);
			}

                        message.run();
                }
                catch (Exception ex) {
                        RiskGame game = this.game;
			logger.log(Level.WARNING, "ERROR processing: " + message + (game == null ? "" : " (gameState=" + game.getState() + ")"), ex);
                }
            }
        }

        private void processFromUI(String message) {

                if ( message.trim().length()==0 ) {
                        controller.sendMessage(">", false, false );
                        getInput();
                        return;
                }

                // Show version
                if (message.equals("ver")) {
                        controller.sendMessage(">" + message, false, false );
                        controller.sendMessage(RiskUtil.GAME_NAME+" Game Engine [Version " + RiskUtil.RISK_VERSION + "]", false, false );
                        getInput();
                }
                // take no action
                else if (message.startsWith("rem ")) {
                        controller.sendMessage(">" + message, false, false );
                        getInput();
                }
                // out of game commands
                else if (game==null) { // if no game
                        noGameParser(message);
                }
                // IN GAME COMMANDS
                else {
                        StringTokenizer StringT = new StringTokenizer(message);
                        String input=StringT.nextToken();
                        String output;

			// CLOSE GAME
			if (input.equals("closegame")) {
				if (!StringT.hasMoreTokens()) {
                                    closeGame();
                                    output=resb.getString("core.close.closed");
				}
				else {
                                    output=JavaCompatUtil.replaceAll( resb.getString( "core.error.syntax"), "{0}", "closegame");
                                }
			}
			// SAVE GAME
			else if (input.equals("savegame")) {
				if (StringT.countTokens() >= 1) {
				    if ( unlimitedLocalMode ) {

					String filename = RiskUtil.getAtLeastOne(StringT);

                                        try {
                                            RiskUtil.saveFile(filename,game);
                                            output=resb.getString( "core.save.saved");
                                        }
                                        catch (Exception ex) {
                                            Level level = Level.WARNING;
                                            if (ex.getMessage() != null && ex.getMessage().endsWith("open failed: ENOSPC (No space left on device)")) {
                                                level = Level.INFO;
                                            }

                                            logger.log(level, "error saving game to file: " + filename, ex);

                                            output=resb.getString( "core.save.error.unable")+" "+ex;
                                            showMessageDialog(output);
                                        }
				    }
				    else {
					output = resb.getString( "core.save.error.unable" );
				    }
				}
				else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "savegame filename"); }
			}
                        // REPLAY A GAME FROM THE GAME COMMANDS
			else if (input.equals("replay")) {
				if ( StringT.hasMoreTokens()==false ) {
				    if ( unlimitedLocalMode ) {
					try {
						List replayCommands = game.getCommands();
						saveGameToUndoObject();
						game = new RiskGame();
						replay = true;
						for (Iterator e = replayCommands.iterator(); e.hasNext();) {
                                                    String command = (String)e.next();
                                                    try {
							inGameParser(command);
                                                    }
                                                    catch (IllegalArgumentException error) {
                                                        // sometimes the user may have typed an invalid command, dont kill the whole replay
                                                        if (game.getCurrentPlayer() != null && game.getCurrentPlayer().getType() == Player.PLAYER_HUMAN) {
                                                            logger.log(Level.INFO, "invalid replay user command: " + command, error);
                                                        }
                                                        // if the invlid command came from the AI, this is a very serious error and we want to stop
                                                        else {
                                                            throw error;
                                                        }
                                                    }
                                                    //try{ Thread.sleep(1000); }
                                                    //catch(InterruptedException e){}
						}
						output="replay of game finished";
					}
					catch (Exception e) {
						output="error with replay "+e;
                                                logger.log(Level.WARNING, "error with replay ", e);
					}
                                        finally {
						replay = false;
                                        }
				    }
				    else {
					output="can only replay local games";
				    }
				}
				else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "replay"); }
			}
                        else if ( onlinePlayClient == null ) {
                                inGameParser( myAddress+" "+message );
                                output=null;
                        }
                        else {
                                // send to network
                                onlinePlayClient.sendUserCommand( message );
                                output=null;
                        }

                        if (output!=null) {
                            controller.sendMessage("game>" + message, false, false);
                            controller.sendMessage(output, false, false);
                            getInput();
                        }
                }
	}

        private void noGameParser(String message) {

                StringTokenizer StringT = new StringTokenizer( message );

                String input = StringT.nextToken();
                String output;

                controller.sendMessage(">" + message, false, false );

                // NEW GAME
                if (input.equals("newgame")) {

                        if (StringT.hasMoreTokens()==false) {

                                // already checked
                                //if (game == null) {

                                        try {
                                                // CREATE A GAME
                                                game = new RiskGame();

                                                // NO SERVER OR CLIENT IS STARTED

                                                unlimitedLocalMode = true;

                                                controller.newGame(true);
                                                setupPreviews( doesMapHaveMission() );

                                                output=resb.getString( "core.newgame.created");
                                        }
                                        catch (Exception e) {
                                                logger.log(Level.WARNING, "newgame error", e);
                                                output = resb.getString("core.newgame.error") + " " + e;
                                        }
                                //}
                                //else {
                                //	output=resb.getString( "core.newgame.alreadyloaded");
                                //}
                        }
                        else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "newgame"); }
                }
                // LOAD GAME
                else if (input.equals("loadgame")) {

                        if (StringT.countTokens() >= 1) {

                                // this is not needed here as u can only get into this bit of code if game == null
                                //if (game == null) {
                                        String filename = RiskUtil.getAtLeastOne(StringT);

                                        try {
                                                game = RiskGame.loadGame( filename );

                                                if (game == null) {
                                                        throw new Exception("no game");
                                                }

                                                unlimitedLocalMode = true;
                                                output=resb.getString( "core.loadgame.loaded");

                                                Player player = game.getCurrentPlayer();
                                                if ( player != null ) {
                                                        // the game is saved
                                                        saveGameToUndoObject();
                                                        output=output+ System.getProperty("line.separator") + resb.getString( "core.loadgame.currentplayer") + " " + player.getName();
                                                }

                                                if (game.getState()==RiskGame.STATE_NEW_GAME) {
                                                    controller.newGame(true);
                                                    setupPreviews( doesMapHaveMission() );
                                                }
                                                else {
                                                    controller.startGame(unlimitedLocalMode);
                                                }
                                        }
                                        catch (Throwable ex) {
                                                logger.log(filename.endsWith(".save") ? Level.WARNING : Level.INFO,"error loading game from file: "+filename,ex);

                                                output=resb.getString( "core.loadgame.error.load")+" "+ex;
                                                showMessageDialog(output);
                                        }
                                //}
                                //else {
                                //	output=resb.getString( "core.newgame.alreadyloaded");
                                //}
                        }
                        else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "loadgame filename"); }
                }
                else if (input.equals("join")) {

                        if (StringT.countTokens() == 1) {

                                // already checked
                                //if (game == null) {

                                        // CREATE A CLIENT
                                        try {
                                                onlinePlayClient = new ChatClient(this, myAddress, StringT.nextToken(), p2pPort);

                                                // CREATE A GAME
                                                game = new RiskGame();

                                                unlimitedLocalMode = false;

                                                controller.newGame(false);
                                                setupPreviews( doesMapHaveMission() );

                                                output=resb.getString( "core.join.created");
                                        }
                                        catch (UnknownHostException e) {
                                                game = null;
                                                output=resb.getString( "core.join.error.unknownhost");
                                        }
                                        catch (ConnectException e) {
                                                game = null;
                                                output=resb.getString( "core.join.error.connect");
                                        }
                                        catch (IllegalArgumentException e) {
                                                game = null;
                                                output=resb.getString( "core.join.error.nothostname");
                                        }
                                        catch (IOException e) {
                                                game = null;
                                                output=resb.getString("core.join.error.002") + " " + e;
                                        }
                                        catch (SecurityException e) { // java.security.AccessControlException is deprecated and marked for removal
                                                game = null;
                                                output = e + ":\n" + resb.getString("core.error.applet");
                                        }
                                        catch (Exception e) { // catch not being able to make a new game, so game is null
                                                game=null; // just in case ;-)
                                                output=resb.getString( "core.join.error.create")+" "+e;
                                        }


                                        if (game==null) {
                                                showMessageDialog(output);
                                        }

                                //}
                                //else {
                                //	output=resb.getString( "core.join.error.001");
                                //}
                        }
                        else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "join server"); }
                }
                // NEW SERVER
                else if (input.equals("startserver")) {

                        if (StringT.hasMoreTokens()==false) {

                                if ( p2pServer == null ) {

                                        // CREATE A SERVER
                                        try {
                                                p2pServer = new ChatArea(controller, p2pPort);

                                                output=resb.getString( "core.startserver.started");
                                                controller.serverState(true);
                                        }
                                        catch(Exception e) {
                                                p2pServer = null;
                                                output=resb.getString( "core.startserver.error")+" "+e;
                                                showMessageDialog(output);
                                        }
                                }
                                else {
                                        output=resb.getString( "core.startserver.error");
                                }
                        }
                        else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "startserver"); }
                }
                // KILL SERVER
                else if (input.equals("killserver")) {
                        if (StringT.hasMoreTokens()==false) {
                                if ( p2pServer != null ) {
                                        try {
                                                // shut down the server
                                                //if (chatter.serverSocket != null) {
                                                //	chatter.serverSocket.close();
                                                //	chatter=null;
                                                //}

                                                if (p2pServer != null) {
                                                        p2pServer.closeSocket();
                                                        p2pServer=null;
                                                }

                                                output=resb.getString( "core.killserver.killed");
                                                controller.serverState(false);
                                        }
                                        catch (Exception e) {
                                                output=resb.getString( "core.killserver.error")+" "+e.getMessage();
                                        }
                                }
                                else {
                                        output=resb.getString( "core.killserver.noserver");
                                }
                        }
                        else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "killserver"); }
                }
                // REPLAY A GAME FROM SCRIPT FILE
                else if (input.equals("play")) {

                        if (StringT.countTokens() >= 1) {
                                String filename = RiskUtil.getAtLeastOne(StringT);

                                try {
                                        URL url;
                                        // TODO dont think this can ever work as an applet anyway
                                        //if (Risk.applet==null) {
                                                url = (new File(filename)).toURI().toURL();
                                        //}
                                        //else {
                                        //	url = new URL( net.yura.domination.engine.Risk.applet.getCodeBase() , filename );
                                        //}
                                        BufferedReader bufferin=new BufferedReader(new InputStreamReader(url.openStream()));

                                        //create thread with bufferin
                                        class Replay extends Thread {

                                            private Risk risk;
                                            private BufferedReader bufferin;

                                            public Replay(Risk r, BufferedReader in) {
                                                super("Domination-Replay");
                                                risk=r;
                                                bufferin=in;
                                            }

                                            public void run() {
                                                int line = 0;
                                                try {
                                                    String input = bufferin.readLine();
                                                    while(input != null) {
                                                        line++;
                                                        //System.out.print(input+"\n");
                                                        try {
                                                            risk.inGameParser(input);
                                                        }
                                                        catch (IllegalArgumentException error) {
                                                            // sometimes the user may have typed an invalid command, dont kill the whole replay
                                                            if (game.getCurrentPlayer() != null && game.getCurrentPlayer().getType() == Player.PLAYER_HUMAN) {
                                                                logger.log(Level.INFO, "invalid script user command on line: " + line + " input: " + input, error);
                                                            }
                                                            // if the invlid command came from the AI, this is a very serious error and we want to stop
                                                            else {
                                                                throw error;
                                                            }
                                                        }

                                                        input = bufferin.readLine();
                                                    }
                                                }
                                                catch(Exception error) {
                                                    logger.log(Level.WARNING, "script error on line " + line, error);
                                                }
                                                finally {
                                                    RiskUtil.close(bufferin);
                                                }

                                                //set replay off
                                                replay = false;
                                                getInput();
                                            }
                                        }

                                        Thread replaythread = new Replay(this, bufferin);
                                        //set boolean that replay is on
                                        replay = true;
                                        
                                        // create a new local game
                                        game = new RiskGame();
                                        unlimitedLocalMode = true;
                                        controller.newGame(true);
                                        setupPreviews( doesMapHaveMission() );
                                        
                                        replaythread.start();
                                        output="playing \""+filename+"\"";
                                }
                                catch(Exception error) {
                                        output="unable to play \""+filename+"\" "+error;
                                }
                        }
                        else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "play filename"); }
                }
                else { // if there is no game and the command was unknown
                        output=resb.getString( "core.loadgame.nogame");
                }

                // if there was NO game

                controller.sendMessage(output, false, true );

                getInput();
        }


	/**
	 * This parses the string, calls the relavant method and displays the correct error messages
	 * @param message The string needed for parsing
	 */
	protected void inGameParser(final String message) {

		controller.sendDebug(message);

		boolean needInput=true;
		String output=null;

		StringTokenizer StringT = new StringTokenizer( message );
                final String Addr = StringT.nextToken();


                // ERROR is not related to the game
                if (!Addr.equals("ERROR")) {
                    game.addCommand(message);
                }


		if (Addr.equals("ERROR")) { // server has sent us a error

			String Pname = StringT.nextToken();

			while ( StringT.hasMoreTokens() ) {
				Pname = Pname +" "+ StringT.nextToken();
			}

			showMessageDialog(Pname);
		}
                else if (Addr.equals("LEAVE")) {

                    String id = StringT.nextToken();

                    // @todo, do we set needinput to flase, so that ai wont go twice, if its there go
                    // but then if there needinput was ignored coz this command was in the inbox, no needinput will get called

                    // if it is NOT there go the set needinput to false
                    if (game.getCurrentPlayer()!=null && !(game.getCurrentPlayer().getAddress().equals(id) && game.getCurrentPlayer().getType() == Player.PLAYER_HUMAN)) {
                            needInput = false;
                    }
                    // if this command stopped the last needInput from being called, then this will be screwed
                    // at worst AI or human wont get a chance to put any input in, game stalled

                    output = "someone has gone: ";

                    // get all the players and make all with the ip of the leaver become nutral
                    List leavers = game.getPlayers();

                    String newPlayerAddress=null;

                    // find the first player that is NOT playing on this computer
                    // this happens in the same way on each computer
                    for (int c=0; c< leavers.size() ; c++) {

                            if ( !((Player)leavers.get(c)).getAddress().equals(id) ) {

                                    newPlayerAddress = ((Player)leavers.get(c)).getAddress();
                                    break;
                            }
                    }


                    for (int c=0; c < leavers.size(); c++) {

                            Player patc = ((Player)leavers.get(c));

                            if ( patc.getAddress().equals(id) ) {

                                    if ( patc.getType() == Player.PLAYER_HUMAN ) {

                                            output = output + patc.getName()+" ";

                                            if (game.getState() == RiskGame.STATE_NEW_GAME ) {

                                                    // should never return false
                                                    if ( game.delPlayer( patc.getName() ) ) {

                                                            c--;

                                                            controller.delPlayer( patc.getName() );

                                                            patc = null;
                                                    }

                                            }
                                            else {

                                                    patc.setType( Player.PLAYER_AI_CRAP );

                                            }
                                    }

                                    if (patc!=null) {

                                            if (newPlayerAddress!=null) {
                                                    patc.setAddress( newPlayerAddress );
                                            }
                                            else {
                                                    // this means there are only spectators left
                                                    // so nothing really needs to be done
                                                    // game will stop, but hay there r no more players
                                            }
                                    }
                            }
                    }
                }
		else if (Addr.equals("DICE")) { // a server command

			int attSize = RiskGame.getNumber(StringT.nextToken());
			int defSize = RiskGame.getNumber(StringT.nextToken());

			output=resb.getString( "core.dice.rolling") + System.getProperty("line.separator") + resb.getString( "core.dice.results");

			int att[] = new int[ attSize ];
			output = output + " " + resb.getString( "core.dice.attacker");
			for (int c=0; c< attSize ; c++) {
				att[c] = RiskGame.getNumber(StringT.nextToken());
				output = output + " " + (att[c]+1);
			}

			int def[] = new int[ defSize ];
			output = output + " " + resb.getString( "core.dice.defender");
			for (int c=0; c< defSize ; c++) {
				def[c] = RiskGame.getNumber(StringT.nextToken());
				output = output + " " + (def[c]+1);
			}

			output = output + System.getProperty("line.separator");

                        Country attacker = game.getAttacker();
			int result[] = game.battle( att, def );

			if ( result[0]==1 ) {
				output = output + JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll(resb.getString( "core.dice.result")
									, "{0}", String.valueOf(result[2]) ) //defeated
									, "{1}", String.valueOf(result[1]) );//lost

				if (result[3] == 0) {
					int n = attacker.getArmies()-1;

					output=output + System.getProperty("line.separator") + resb.getString( "core.dice.notdefeated") + " ";

					if (n > 0) {
						if (n > 3) { n=3; }
						output=output + JavaCompatUtil.replaceAll(resb.getString( "core.dice.attackagain"), "{0}", "" + n);

//						Player attackingPlayer = ((Country)game.getAttacker()).getOwner();
//
//						if ( showHumanPlayerThereInfo( attackingPlayer ) ) {
//							controller.showDice(n, true);
//						}
					}
					else {
						output=output + resb.getString( "core.dice.noattackagain");
					}
				}
				else {
//                                      // not needed any more
//					Player attackingPlayer = ((Country)game.getAttacker()).getOwner();
//
//					if ( showHumanPlayerThereInfo( attackingPlayer ) ) {
//						controller.setSlider( game.getMustMove(), game.getAttacker().getColor(), game.getDefender().getColor() );
//					}

					output=output + System.getProperty("line.separator") + resb.getString( "core.dice.defeated") + " ";

					if ( result[3]==2 ) {
						output=output + resb.getString( "core.dice.eliminated") + " ";
					}

					// if there is only one amount of troops u can move
					if ( result[4] == result[5] ) {

						int noa = game.moveAll();

						int ma = game.moveArmies( noa );

						//Moved {0} armies to captured country.
						output=output + JavaCompatUtil.replaceAll(resb.getString( "core.dice.armiesmoved"), "{0}", String.valueOf(noa) );

						if (ma==2) {
							output=output + whoWon();
						}
					}
					else {
						//How many armies do you wish to move? ({0} to {1})
						output=output + JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll(resb.getString( "core.dice.howmanyarmies")
								, "{0}", String.valueOf(result[4]) )
								, "{1}", String.valueOf(result[5]) );
					}
				}

				if (battle) {
                                        boolean weAreAttacker = showHumanPlayerThereInfo(attacker.getOwner());
                                        boolean defenderWon = result[3] == 0 && attacker.getArmies() == 1;
                                        //boolean attackerWon = result[3] >= 1;
                                        //boolean playerEliminated = result[3] == 2;

					controller.showDiceResults(att, def, weAreAttacker, defenderWon ? -1 : result[3]);

					try{ Thread.sleep(SHOW_DICE_SLEEP); }
					catch(InterruptedException e){}
				}
			}
			else { output=resb.getString( "core.dice.error.unabletoroll"); }

			// ==1 this fixes the automove bug, when u need to trade after rolling and automove
			if ( game.getState()!=RiskGame.STATE_ROLLING && game.getState()!=RiskGame.STATE_DEFEND_YOURSELF) {
				closeBattle();
			}
		}
		else if (Addr.equals("PLAYER")) { // a server command

			int index = Integer.parseInt( StringT.nextToken() );

			Player p = game.setCurrentPlayer( index );

			controller.sendMessage("Game started", false, false);

			// moved to startgame for lobby
			//if ( chatSocket != null ) {
			//	controller.startGame(false);
			//}
			//else {
			//	controller.startGame(true);
			//}

			output=JavaCompatUtil.replaceAll(resb.getString( "core.player.randomselected"), "{0}", p.getName());

			if ( game.getGameMode()==RiskGame.MODE_SECRET_MISSION || autoplaceall==true ) {
				needInput=false;
			}
			else {
				saveGameToUndoObject();
			}
		}
		else if (Addr.equals("CARD")) { // a server command

			// if the player deserves a card
			if ( StringT.hasMoreTokens() ) {

				// get the cards
				//Vector cards = game.getCards();
				String name = StringT.nextToken();
				Card card = game.findCardAndRemoveIt( name );

				((Player)game.getCurrentPlayer()).giveCard( card );

				if ( showHumanCurrentPlayerInfo() ) {

					String cardName;

					if (name.equals(Card.WILDCARD)) {
						cardName = name;
					}
					else {
						cardName = card.getName() + " " + game.getCountryInt( Integer.parseInt(name) ).getName();
					}

                                        controller.playerGotCard();
					controller.sendMessage("You got a new card: \"" + cardName +"\"", false , false);
				}
			}

			Player newplayer = game.endGo();

			output = JavaCompatUtil.replaceAll(resb.getString( "core.player.newselected"), "{0}", newplayer.getName());

			// this is not a bug! (Easter egg)
			if ( unlimitedLocalMode && game.getSetupDone() && newplayer.getName().equals("Theo")) { newplayer.addArmies( newplayer.getExtraArmies() ); }

			saveGameToUndoObject();
		}
		else if (Addr.equals("PLACE")) { // a server command

			Country c = game.getCountryInt( Integer.parseInt( StringT.nextToken() ) );
			game.placeArmy( c ,1);
			controller.sendMessage(JavaCompatUtil.replaceAll( resb.getString( "core.place.oneplacedin"), "{0}", c.getName()) , false, false); // Display
			output=resb.getString( "core.place.autoplaceok");
		}
		else if (Addr.equals("PLACEALL")) { // a server command

			for (int c=0; c< game.getNoCountries() ; c++) {

				Country t = game.getCountryInt( Integer.parseInt( StringT.nextToken() ) );
				game.placeArmy( t ,1);
				controller.sendMessage(JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll( resb.getString("core.place.getcountry")
						, "{0}", ((Player)game.getCurrentPlayer()).getName())
						, "{1}", t.getName()) // Display
						, false, false);
				game.endGo();
			}

			// the game is saved
			saveGameToUndoObject();

			controller.sendMessage("Auto place all successful.", false, false);
			//New player selected: {0}.
			output= JavaCompatUtil.replaceAll( resb.getString( "core.player.newselected"), "{0}", ((Player)game.getCurrentPlayer()).getName());
		}
		else if (Addr.equals("MISSION")) { // a server command

			List m = new ArrayList(game.getMissions());
			List p = game.getPlayers();

			for (int c=0; c< p.size() ; c++) {
				int i = RiskGame.getNumber( StringT.nextToken() );
				((Player)p.get(c)).setMission( (Mission)m.get(i) );
				m.remove(i);
			}

			output=null;
			needInput=false;
		}
                else if (Addr.equals("RENAME")) {
                    Map<String, String> map = RiskUtil.queryAsMap(message.substring(Addr.length() + 1));

                    String oldName = map.get("oldName");
                    String newName = map.get("newName");
                    String newAddress = map.get("newAddress");
                    int newType = Integer.parseInt(map.get("newType"));

                    try {
                        renamePlayer(oldName,newName,newAddress,newType);
                    }
                    catch (Exception ex) {
                        logger.log(Level.WARNING, "rename error", ex);
                    }
                }
		else { // parse this normal cammand

			String echo = message.substring( Addr.length()+1 );

			if (game != null && game.getCurrentPlayer() != null && game.getState()!=RiskGame.STATE_GAME_OVER ) {

                                int type = game.getCurrentPlayer().getType();

                                String key;
                                if (type==Player.PLAYER_HUMAN) {
                                    key = "newgame.player.type.human";
                                }
                                else {
                                    key = "newgame.player.type."+ai.getCommandFromType(type)+"ai";
                                }

                                String typeString;
                                try {
                                    typeString = resb.getString(key);
                                }
                                catch (MissingResourceException ex) {
                                    // fallback just in case
                                    typeString = key;
                                }

                                controller.sendMessage( game.getCurrentPlayer().getName()+ "("+typeString+")>"+echo, false, false );
			}
			else {
				controller.sendMessage( "game>" + echo, false, false );
			}

			//if (StringT.hasMoreTokens()) {

			String input=StringT.nextToken();
			output="";

                        if (game.getState()==RiskGame.STATE_NEW_GAME) {

				if (input.equals("choosemap")) {

					if (StringT.countTokens() >= 1) {
						final String filename = RiskUtil.getAtLeastOne(StringT);

						try {
                                                    setMap(filename);
						}
						catch (final Exception e) {
                                                    // crap, we wanted to use this map, but we would not load it
                                                    // maybe we can download it from the server and then use it
                                                    RiskUtil.streamOpener.getMap(filename, new Observer() {
                                                        @Override
                                                        public void update(Observable observable, Object data) {
                                                            if (data == RiskUtil.SUCCESS) {
                                                                try {
                                                                    setMap(filename);
                                                                }
                                                                catch (Exception ex) {
                                                                    getMapError(ex.toString());
                                                                }
                                                            }
                                                            else {
                                                                getMapError(e.toString());
                                                            }
                                                        }
                                                    });
						}
                                                output = null; // we have nothing to output now

					}
					else  { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "choosemap filename"); }

				}
				else if (input.equals("choosecards")) {

					if (StringT.countTokens() >= 1) {
						String filename = RiskUtil.getAtLeastOne(StringT);

						try {

							boolean yesmissions = game.setCardsfile(filename);

							controller.showCardsFile( game.getCardsFile() , yesmissions );
							//New cards file selected: "{0}"
							output=JavaCompatUtil.replaceAll(resb.getString( "core.choosecards.chosen"), "{0}", filename);
						}
						catch (Exception e) {
							output=resb.getString( "core.choosecards.error.unable");
						}
					}
					else  { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "choosecards filename"); }

				}
				else if (input.equals("newplayer")) {

					if (StringT.countTokens()>=3) {

						String type=StringT.nextToken();
						if (type.equals("ai")) {
							type = type+" "+StringT.nextToken();
						}

						String c=StringT.nextToken();

						String name="";
						while ( StringT.hasMoreTokens() ) {
							name = name + StringT.nextToken();
							if ( StringT.hasMoreTokens() ) { name = name + " "; }
						}

						int t=getType(type);
						int color=ColorUtil.getColor( c );

						if ( color != 0 && t != -1 && !name.equals("") && (   (  unlimitedLocalMode && game.addPlayer(t, name, color, "LOCALGAME" ) ) || ( !unlimitedLocalMode && game.addPlayer(t, name, color, Addr)    )    ) ) {
							//New player created, name: {0} color: {1}
							output=JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll( resb.getString("core.newplayer.created")
										, "{0}", name)
										, "{1}", c);

							controller.addPlayer(t, name, color, Addr);
							//System.out.print("New player has Address: "+Addr+"\n");

						}
						else { output=resb.getString( "core.newplayer.error.unable"); }



					}
					else  { output=JavaCompatUtil.replaceAll( resb.getString( "core.error.syntax"), "{0}", "newplayer type (skill) color name"); }

				}
				else if (input.equals("delplayer")) {

					if (StringT.countTokens() >= 1) {
						String name=RiskUtil.getAtLeastOne(StringT);

						if ( game.delPlayer(name) ) {
							controller.delPlayer(name);
							output=JavaCompatUtil.replaceAll(resb.getString( "core.delplayer.deleted"), "{0}", name);
						}
						else { output=resb.getString( "core.delplayer.error.unable"); }
					}
					else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "delplayer name"); }

				}
				else if (input.equals("info")) {

					if (StringT.hasMoreTokens()==false) {

						output=resb.getString( "core.info.title") + "\n";

						List players = game.getPlayers();

						for (int a=0; a< players.size() ; a++) {

							output = output + resb.getString( "core.info.player") + " " + ((Player)players.get(a)).getName() +"\n";

						}

						output = output + resb.getString( "core.info.mapfile") + " "+ game.getMapFile() +"\n";
						output = output + resb.getString( "core.info.cardsfile") + " "+ game.getCardsFile() ;

					}
					else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "info"); }

				}
				else if (input.equals("autosetup")) {
					if (StringT.hasMoreTokens()==false) {
						if ( game.getPlayers().size() == 0) {
						    if (!replay) {
							for (int c = 1; c <= RiskGame.MAX_PLAYERS; c++) {
                                                                String playerType = riskconfig.getProperty("default.player"+c+".type");
                                                                String playerColor = riskconfig.getProperty("default.player"+c+".color");
                                                                String playerName = riskconfig.getProperty("default.player"+c+".name");
                                                                if (playerType != null && playerColor != null && playerName != null) {
                                                                    parser("newplayer " + playerType + " " + playerColor + " " + playerName);
                                                                }
							}
							output = resb.getString( "core.info.autosetup");
						    }
						    else {
							output = "replay mode, nothing done";
						    }
						}
						else {
							output = resb.getString( "core.info.autosetup.error");
						}
					}
					else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "autosetup"); }
				}
				else if (input.equals("startgame")) {
					if (StringT.countTokens() >= 2 && StringT.countTokens() <= 5) {

						int n=game.getPlayers().size();

						int newgame_type = -1;
						int newgame_cardType = -1;
                                                boolean maxFiveCards = true;
						boolean newgame_autoplaceall = false;
						boolean newgame_recycle = false;
                                                Integer defenderDice = null;
                                                boolean minimumThreeArmies = true;

						String crap = null;

						while (StringT.hasMoreTokens()) {
							String newOption = StringT.nextToken();
							if ( newOption.equals(STARTGAME_OPTION_MODE_DOMINATION) ) {
								newgame_type = RiskGame.MODE_DOMINATION;
							}
							else if ( newOption.equals(STARTGAME_OPTION_MODE_CAPITAL) ) {
								newgame_type = RiskGame.MODE_CAPITAL;
							}
							else if ( newOption.equals(STARTGAME_OPTION_MODE_SECRET_MISSION) ) {
								newgame_type = RiskGame.MODE_SECRET_MISSION;
							}
							else if ( newOption.equals(STARTGAME_OPTION_CARD_INCREASING_SET) ) {
								newgame_cardType = RiskGame.CARD_INCREASING_SET;
							}
							else if ( newOption.equals(STARTGAME_OPTION_CARD_FIXED_SET) ) {
								newgame_cardType = RiskGame.CARD_FIXED_SET;
							}
							else if ( newOption.equals(STARTGAME_OPTION_CARD_ITALIAN_LIKE_SET) ) {
								newgame_cardType = RiskGame.CARD_ITALIANLIKE_SET;
                                                                minimumThreeArmies = false;
                                                                maxFiveCards = false;
							}
							else if ( newOption.equals(STARTGAME_OPTION_AUTO_PLACE_ALL) ) {
								newgame_autoplaceall = true;
							}
							else if ( newOption.equals(STARTGAME_OPTION_RECYCLE_CARDS) ) {
								newgame_recycle = true;
							}
                                                        else if ( newOption.startsWith(STARTGAME_OPTION_DEFENDER_DICE) ) {
                                                                defenderDice = Integer.parseInt(newOption.substring(STARTGAME_OPTION_DEFENDER_DICE.length()));
                                                        }
							else {
								crap = newOption;
							}
						}

                                                if (crap == null) {
                                                    
                                                    defenderDice = defenderDice == null ? (newgame_cardType == RiskGame.CARD_ITALIANLIKE_SET ? 3 : 2) : defenderDice;
                                                    
                                                    // checks all the options are correct to start a game
                                                    if (newgame_type != -1 && newgame_cardType != -1 && n >= 2 && n <= RiskGame.MAX_PLAYERS && defenderDice >= 1 && defenderDice <= 3) {

                                                            autoplaceall = newgame_autoplaceall;

                                                            try {
                                                                    game.startGame(newgame_type, newgame_cardType, newgame_recycle, maxFiveCards, defenderDice, minimumThreeArmies);
                                                            }
                                                            catch (Exception e) {
                                                                    logger.log(Level.WARNING, "startgame error", e);
                                                            }
                                                    }

                                                    // this checks if the game was able to start or not
                                                    if (game.getState() != RiskGame.STATE_NEW_GAME ) {

                                                        controller.noInput();

                                                        controller.startGame(unlimitedLocalMode);

                                                        if (shouldGameCommand(Addr)) {
                                                            gameCommand(Addr, "PLAYER", String.valueOf( game.getRandomPlayer() ) );

                                                            // do that mission thing
                                                            if (game.getGameMode()== RiskGame.MODE_SECRET_MISSION ) {

                                                                    // give me a array of random numbers
                                                                    Random r = new Random();
                                                                    int a = game.getNoMissions();
                                                                    int b = game.getNoPlayers();

                                                                    StringBuffer outputa=new StringBuffer();
                                                                    for (int c=0; c< b ; c++) {
                                                                            if (outputa.length()!=0 ) {
                                                                                outputa.append(' ');
                                                                            }
                                                                            outputa.append( r.nextInt(a) );
                                                                            a--;
                                                                    }

                                                                    gameCommand(Addr, "MISSION", outputa.toString());
                                                            }

                                                            // do that autoplace thing
                                                            if ( game.getGameMode()==RiskGame.MODE_SECRET_MISSION || autoplaceall ) {

                                                                    List a = game.shuffleCountries();

                                                                    StringBuffer outputb=new StringBuffer();
                                                                    for (int c=0; c< a.size() ; c++) {
                                                                            if (outputb.length()!=0 ) {
                                                                                outputb.append(' ');
                                                                            }
                                                                            outputb.append( ((Country)a.get(c)).getColor() );
                                                                    }

                                                                    gameCommand(Addr, "PLACEALL", outputb.toString());
                                                            }
                                                        }

                                                        output=null;
                                                        needInput=false;
                                                    }
                                                    else if (newgame_type == RiskGame.MODE_SECRET_MISSION && game.getNoMissions() < game.getPlayers().size()) {
                                                        output="map does not have enough missions for players";
                                                    }
                                                    else {
                                                        output=resb.getString("core.start.error.players");
                                                    }
                                                }
                                                else {
                                                    output="unknown option: "+crap;
                                                }
					}
					else {
                                            output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "startgame gametype cardtype (autoplaceall recycle)");
                                        }
				}
				else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "newplayer, delplayer, startgame, choosemap, choosecards, info, autosetup"); }
                        }
                        else {
                            boolean aiPlayer = game.getCurrentPlayer().getType()!=Player.PLAYER_HUMAN;
                            try {
                                // UNDO
                                if (input.equals("undo")) {

                                    if(game.getState()!=RiskGame.STATE_GAME_OVER && aiPlayer) {
                                        throw new IllegalArgumentException("ai is trying to call undo");
                                    }

                                    if ( StringT.hasMoreTokens()==false ) {

                                        if ( unlimitedLocalMode ) {

                                            try {
                                                    // can not undo when defending yourself as it is not really your go
                                                    if (game.getState()!=RiskGame.STATE_DEFEND_YOURSELF && Undo!=null && Undo.size()!=0) {
                                                            //game = (RiskGame)Undo.getObject( nullCipher );
                                                            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(Undo.toByteArray()));
                                                            game = (RiskGame)in.readObject();

                                                            output =resb.getString( "core.undo.undone");
                                                    }
                                                    else {
                                                            output =resb.getString( "core.undo.error.unable");
                                                    }
                                            }
                                            catch (Exception e) {
                                                    logger.log(Level.WARNING, resb.getString( "core.loadgame.error.undo"),e);
                                            }
                                        }
                                        else {
                                            output = resb.getString( "core.undo.error.network");
                                        }
                                    }
                                    else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "undo"); }
                                }
                                else if (input.equals("showmission")) {
                                    if (StringT.hasMoreTokens()==false) {

                                            // only show to the right player!

                                            if ( showHumanCurrentPlayerInfo() ) {
                                                    output = resb.getString( "core.showmission.mission") + " " + getHumanPlayerMission();
                                            }
                                            else { output=resb.getString( "core.showmission.error"); }
                                    }
                                    else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "showmission"); }
                                }
                                else if (input.equals("showarmies")) {
                                    if (StringT.hasMoreTokens()==false) {

                                            if ( game.getState() != RiskGame.STATE_NEW_GAME) {

                                                    Country[] v = game.getCountries();

                                                    output=resb.getString( "core.showarmies.countries") + System.getProperty("line.separator");

                                                    for (int c=0; c< v.length ; c++) {

                                                            output = output + v[c].getColor() + " " + v[c].getName()+" - "; // Display

                                                            if ( v[c].getOwner() != null ) {
                                                                    output = output + ((Player)v[c].getOwner()).getName() +" ("+v[c].getArmies() +")";
                                                                    if (game.getGameMode() == 2 && game.getSetupDone() && game.getState() !=RiskGame.STATE_SELECT_CAPITAL) {

                                                                            List players = game.getPlayers();

                                                                            for (int a=0; a< players.size() ; a++) {

                                                                                    if ( ((Player)players.get(a)).getCapital() != null && ((Player)players.get(a)).getCapital() == v[c] ) {
                                                                                            output = output + " " + JavaCompatUtil.replaceAll( resb.getString( "core.showarmies.captial")
                                                                                                                    , "{0}", ((Player)players.get(a)).getName());
                                                                                    }

                                                                            }

                                                                    }

                                                                    output = output + System.getProperty("line.separator");

                                                            }
                                                            else {
                                                                    output = output + resb.getString( "core.showarmies.noowner") + System.getProperty("line.separator");
                                                            }

                                                    }

                                            }
                                            else { output=resb.getString( "core.showarmies.error.unable"); }
                                    }
                                    else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "showarmies"); }
                                }

                                else if (input.equals("showcards")) {
                                    if (StringT.hasMoreTokens()==false) {

                                            if ( showHumanCurrentPlayerInfo() ) {

                                                    List c = ((Player)game.getCurrentPlayer()).getCards();

                                                    if (c.size() == 0) {
                                                            output=resb.getString( "core.showcards.nocards");
                                                    }
                                                    else {
                                                            output=resb.getString( "core.showcards.youhave");

                                                            for (int a=0; a< c.size() ; a++) {

                                                                    if ( ((Card)c.get(a)).getName().equals(Card.WILDCARD) ) {
                                                                            output = output + " " + Card.WILDCARD; // resb.getString( "core.showcards.wildcard"); // dont use this as user needs to type it in
                                                                    }
                                                                    else {
                                                                            output = output + " \"" + ((Card)c.get(a)).getName() +" "+ ((Country)((Card)c.get(a)).getCountry()).getName() +" ("+((Country)((Card)c.get(a)).getCountry()).getColor()+")\""; // Display
                                                                    }

                                                            }
                                                    }

                                                    if(game.getCardMode()==RiskGame.CARD_FIXED_SET) {
                                                            output = output+"\n"+ resb.getString("cards.fixed");

                                                    }
                                                    else if(game.getCardMode()==RiskGame.CARD_ITALIANLIKE_SET) {
                                                            output = output+"\n"+ resb.getString("cards.italianlike");

                                                    }
                                                    else { // CARD_INCREASING_SET
                                                            output = output+"\n"+ JavaCompatUtil.replaceAll(resb.getString("cards.nexttrade"), "{0}", String.valueOf(getNewCardState()) );
                                                    }

                                            }
                                            else { output=resb.getString( "core.showcards.error.unable"); }
                                    }
                                    else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "showcards"); }
                                }
                                else if (input.equals("autoendgo")) {
                                    if (StringT.hasMoreTokens()==false) {
                                            String strSelected;

                                            if ( game.getCurrentPlayer().getAutoEndGo() ) {
                                                    strSelected = "core.autoendgo.on";
                                            }
                                            else {
                                                    strSelected = "core.autoendgo.off";
                                            }
                                            output = JavaCompatUtil.replaceAll(resb.getString( "core.autoendgo.setto"), "{0}", resb.getString( strSelected));
                                    }
                                    else if (StringT.countTokens() == 1) {

                                            String option = StringT.nextToken();
                                            if (option.equals("on") ) {
                                                    game.getCurrentPlayer().setAutoEndGo(true);
                                                    output = JavaCompatUtil.replaceAll(resb.getString( "core.autoendgo.setto"), "{0}", resb.getString( "core.autoendgo.on"));
                                            }
                                            else if (option.equals("off") ) {
                                                    game.getCurrentPlayer().setAutoEndGo(false);
                                                    output = JavaCompatUtil.replaceAll(resb.getString( "core.autoendgo.setto"), "{0}", resb.getString( "core.autoendgo.off"));
                                            }
                                            else { output=JavaCompatUtil.replaceAll(resb.getString( "core.autoendgo.error.unknown"), "{0}", option); }

                                    }
                                    else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "autoendgo on/off"); }
                                }
                                else if (input.equals("autodefend")) {
                                    if (StringT.hasMoreTokens()==false) {

                                            String strSelected;
                                            if ( game.getCurrentPlayer().getAutoDefend() ) {
                                                    strSelected = "core.autodefend.on";
                                            }
                                            else {
                                                    strSelected = "core.autodefend.on";
                                            }
                                            output = JavaCompatUtil.replaceAll(resb.getString( "core.autodefend.setto"), "{0}", resb.getString( strSelected));
                                    }
                                    else if (StringT.countTokens() == 1) {

                                            String option = StringT.nextToken();
                                            if (option.equals("on") ) {
                                                    game.getCurrentPlayer().setAutoDefend(true);
                                                    output = JavaCompatUtil.replaceAll(resb.getString( "core.autodefend.setto"), "{0}", resb.getString( "core.autodefend.on"));
                                            }
                                            else if (option.equals("off") ) {
                                                    game.getCurrentPlayer().setAutoDefend(false);
                                                    output = JavaCompatUtil.replaceAll(resb.getString( "core.autodefend.setto"), "{0}", resb.getString( "core.autodefend.off"));
                                            }
                                            else { output=JavaCompatUtil.replaceAll(resb.getString( "core.autodefend.error.unknown"), "{0}", option); }

                                    }
                                    else { output=JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "autodefend on/off"); }
                                }
                                else if (game.getState()==RiskGame.STATE_TRADE_CARDS) {

                                    if (input.equals("trade")) {
                                            if (StringT.countTokens()==3) {
                                                    // trade Japan wildcard Egypt
                                                    int noa=0;

                                                    Card cards[] = game.getCards(StringT.nextToken(),StringT.nextToken(),StringT.nextToken());

                                                    if (cards[0] != null && cards[1] != null && cards[2] != null) { // if the player DOES HAVE all the cards he chose
                                                            noa = game.trade(cards[0], cards[1], cards[2]);
                                                    }

                                                    if ( noa != 0 ) { // if the trade WAS SUCCESSFUL
                                                            output=JavaCompatUtil.replaceAll(resb.getString( "core.trade.traded"), "{0}", "" + noa);
                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.trade.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "trade card card card")); }
                                    }
                                    else if (input.equals("endtrade")) {
                                            if (StringT.hasMoreTokens()==false) {

                                                    if ( game.endTrade() ) {
                                                            output=resb.getString( "core.trade.endtrade");
                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.trade.end.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "endtrade")); }
                                    }
                                    else {
                                        throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "showcards, trade"+(game.canEndTrade()?", endtrade":"") ));
                                    }
                                }
                                else if (game.getState()==RiskGame.STATE_PLACE_ARMIES) {

                                    if (input.equals("placearmies")) {
                                            if (StringT.countTokens()==2) {
                                                    String country=StringT.nextToken();
                                                    int c=RiskGame.getNumber( country );
                                                    int num=RiskGame.getNumber( StringT.nextToken() );
                                                    Country t;

                                                    if (c != -1) {
                                                            t=game.getCountryInt(c);
                                                    }
                                                    else {
                                                            //YURA:LANG t=game.getCountryByName(country);
                                                            t=null;
                                                    }

                                                    if ( t != null && num != -1 && !(game.getGameMode() == 1 && t.getOwner() == null) && !(game.getGameMode() == RiskGame.MODE_SECRET_MISSION && t.getOwner() == null)) {

                                                            int result = game.placeArmy(t, num);

                                                            if (result!=0) {
                                                                    //{0} new army placed in: {1}
                                                                    output = JavaCompatUtil.replaceAll( JavaCompatUtil.replaceAll(resb.getString( "core.place.placed")
                                                                            , "{0}", String.valueOf(num) )
                                                                            , "{1}", t.getName() ); // Display

                                                                    if (result == 2) {
                                                                            output=output + whoWon();
                                                                    }

                                                            }
                                                            else { throw new IllegalArgumentException(resb.getString( "core.place.error.unable")); }
                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.place.error.invalid")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "placearmies country number")); }
                                    }
                                    else if (input.equals("autoplace")) {
                                            if (!StringT.hasMoreTokens()) {
                                                        if (shouldGameCommand(Addr)) {
                                                            gameCommand(Addr, "PLACE", String.valueOf( game.getRandomCountry() ) );
                                                        }
                                                        needInput=false;
                                                        output = null;
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "autoplace")); }
                                    }
                                    else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "showarmies, placearmies, autoplace")); }
                                }
                                else if (game.getState()==RiskGame.STATE_ATTACKING) {

                                    if (input.equals("attack")) {
                                            if (StringT.countTokens()==2) {

                                                    String arg1=StringT.nextToken();
                                                    String arg2=StringT.nextToken();
                                                    int a1=RiskGame.getNumber(arg1);
                                                    int a2=RiskGame.getNumber(arg2);

                                                    Country country1;
                                                    Country country2;

                                                    if (a1 != -1) {
                                                            country1=game.getCountryInt(a1);
                                                    }
                                                    else {
                                                            //YURA:LANG country1=game.getCountryByName(arg1);
                                                            country1=null;
                                                    }

                                                    if (a2 != -1) {
                                                            country2=game.getCountryInt(a2);
                                                    }
                                                    else {
                                                            //YURA:LANG country2=game.getCountryByName(arg2);
                                                            country2=null;
                                                    }

                                                    boolean a=game.attack(country1, country2);

                                                    if ( a ) {
                                                            //Attack {0} ({1}) with {2} ({3}). (You can use up to {4} dice to attack)
                                                            output = JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll(resb.getString( "core.attack.attacking")
                                                                                    , "{0}", country2.getName()) // Display
                                                                                    , "{1}", "" + country2.getArmies())
                                                                                    , "{2}", country1.getName()) // Display
                                                                                    , "{3}", "" + country1.getArmies())
                                                                                    , "{4}", "" + game.getNoAttackDice() );

    //							Player attackingPlayer = ((Country)game.getAttacker()).getOwner();
    //
    //							if ( showHumanPlayerThereInfo( attackingPlayer ) ) {
    //
    //								controller.showDice(a[1], true);
    //							}

                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.attack.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "attack country country")); }
                                    }
                                    else if (input.equals("endattack")) {
                                            if (StringT.hasMoreTokens()==false) {
                                                    if ( game.endAttack() ) {
                                                            output=resb.getString( "core.attack.end.ended");
                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.attack.end.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "endattack")); }
                                    }
                                    else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "attack, endattack")); }
                                }
                                else if (game.getState()==RiskGame.STATE_ROLLING) {

                                    if (input.equals("roll")) {

                                            if (StringT.countTokens()==1) {

                                                    int dice=RiskGame.getNumber( StringT.nextToken() );

                                                    if ( dice != -1 && game.rollA(dice) ) {

                                                            if ( battle ) {

                                                                    controller.setNODAttacker(dice);

                                                            }

                                                            int n = game.getNoDefendDice();

                                                            //Rolled attacking dice, {0} defend yourself! (you can use up to {1} dice to defend)
                                                            output = JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll(resb.getString( "core.roll.rolled")
                                                                                    , "{0}", ((Player)game.getCurrentPlayer()).getName())
                                                                                    , "{1}", "" + n);

    //							Player defendingPlayer = ((Country)game.getDefender()).getOwner();
    //
    //							if ( showHumanPlayerThereInfo(defendingPlayer) ) {
    //								controller.showDice(n, false);
    //							}

                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.roll.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "roll number")); }
                                    }
                                    else if (input.equals("retreat")) {
                                            if (StringT.hasMoreTokens()==false) {

                                                    if ( game.retreat() ) {
                                                            output=resb.getString( "core.retreat.retreated");
                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.retreat.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "retreat")); }
                                    }
                                    else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "roll, retreat")); }
                                }
                                else if (game.getState()==RiskGame.STATE_BATTLE_WON) {

                                    if (input.equals("move")) {
                                            if (StringT.countTokens()==1) {

                                                    String num = StringT.nextToken();
                                                    int noa;

                                                    if (num.equals("all")) {
                                                            noa=game.moveAll();
                                                    }
                                                    else {
                                                            noa=RiskGame.getNumber( num );
                                                    }

                                                    int mov=game.moveArmies(noa);

                                                    if ( mov != 0 ) {
                                                            //Moved {0} armies to captured country.
                                                            output = JavaCompatUtil.replaceAll(resb.getString( "core.move.moved"), "{0}", "" + noa);

                                                            if (mov == 2) {
                                                                    output=output + whoWon();
                                                            }
                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.move.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "move number")); }
                                    }
                                    else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "move")); }
                                }
                                else if (game.getState()==RiskGame.STATE_FORTIFYING) {

                                    if (input.equals("movearmies")) {
                                            if (StringT.countTokens()==3) {

                                                    String arg1=StringT.nextToken();
                                                    String arg2=StringT.nextToken();
                                                    int a1=RiskGame.getNumber(arg1);
                                                    int a2=RiskGame.getNumber(arg2);

                                                    Country country1;
                                                    Country country2;

                                                    if (a1 != -1) {
                                                            country1=game.getCountryInt(a1);
                                                    }
                                                    else {
                                                            //YURA:LANG country1=game.getCountryByName(arg1);
                                                            country1=null;
                                                    }

                                                    if (a2 != -1) {
                                                            country2=game.getCountryInt(a2);
                                                    }
                                                    else {
                                                            //YURA:LANG country2=game.getCountryByName(arg2);
                                                            country2=null;
                                                    }

                                                    int noa=RiskGame.getNumber( StringT.nextToken() );

                                                    if ( game.moveArmy(country1, country2, noa) ) {
                                                            //Moved {0} armies from {1} to {2}.
                                                            output = JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll(JavaCompatUtil.replaceAll(resb.getString( "core.tacmove.movedfromto")
                                                                            , "{0}", "" + noa)
                                                                            , "{1}", country1.getName()) // Display
                                                                            , "{2}", country2.getName()); // Display
                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.tacmove.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "movearmies country country number")); }
                                    }
                                    else if (input.equals("nomove")) {
                                            if (StringT.hasMoreTokens()==false) {
                                                    if ( game.noMove() ) {
                                                            output=resb.getString( "core.tacmove.no.nomoves");
                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.tacmove.no.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "nomove")); }
                                    }
                                    else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "movearmies, nomove")); }
                                }
                                else if (game.getState()==RiskGame.STATE_END_TURN) {

                                    if (input.equals("endgo")) {
                                            if (StringT.hasMoreTokens()==false) {

                                                    needInput=false;
                                                    output=null;

                                                    controller.sendMessage(resb.getString( "core.endgo.ended"), false , false);
                                                    DoEndGo();

                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "endgo")); }
                                    }
                                    else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "emdgo")); }
                                }
                                else if (game.getState()==RiskGame.STATE_GAME_OVER) {

                                    if (input.equals("continue")) {
                                            if (StringT.hasMoreTokens()==false) {

                                                    if ( game.continuePlay() ) {
                                                            output=resb.getString( "core.continue.successful");
                                                    }
                                                    else {
                                                            throw new IllegalArgumentException(resb.getString( "core.continue.error.unable"));
                                                    }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "continue")); }
                                    }
                                    else {
                                            //The game is over. {0} won! (current possible commands are: continue)
                                            throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.gameover.won"), "{0}", game.getCurrentPlayer().getName()));
                                    }
                                }
                                else if (game.getState()==RiskGame.STATE_SELECT_CAPITAL) {

                                    if (input.equals("capital")) {
                                            if (StringT.countTokens()==1) {

                                                    String strCountry = StringT.nextToken();
                                                    int nCountryId = RiskGame.getNumber(strCountry);
                                                    Country t;

                                                    if (nCountryId != -1) {
                                                            t = game.getCountryInt( nCountryId);
                                                    } else {
                                                            //YURA:LANG t = game.getCountryByName( strCountry);
                                                            t=null;
                                                    }

                                                    if ( t != null && game.setCapital(t) ) {
                                                            if ( showHumanCurrentPlayerInfo() ) {
                                                                    output=JavaCompatUtil.replaceAll(resb.getString( "core.capital.selected"), "{0}", t.getName()); // Display
                                                            }
                                                            else {
                                                                    output=resb.getString( "core.capital.hasbeenselected");
                                                            }
                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.capital.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "capital country")); }
                                    }
                                    else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "capital")); }
                                }
                                else if (game.getState()==RiskGame.STATE_DEFEND_YOURSELF) {

                                    if (input.equals("roll")) {

                                            if (StringT.countTokens()==1) {

                                                    int dice=RiskGame.getNumber( StringT.nextToken() );
                                                    if ( dice != -1 && game.rollD(dice) ) {

                                                            if ( battle ) {

                                                                    controller.setNODDefender(dice);

                                                                    try{ Thread.sleep(ROLL_DICE_SLEEP); }
                                                                    catch(InterruptedException e){}

                                                            }
                                                            // client does a roll, and this is not called
                                                            if ( shouldGameCommand(Addr) ) { // recursive call

                                                                    int[] attackerResults = game.rollDice( game.getAttackerDice() );
                                                                    int[] defenderResults = game.rollDice( game.getDefenderDice() );

                                                                    String serverRoll = "";

                                                                    serverRoll = serverRoll + attackerResults.length + " ";
                                                                    serverRoll = serverRoll + defenderResults.length + " ";

                                                                    for (int c=0; c< attackerResults.length ; c++) {
                                                                            serverRoll = serverRoll + attackerResults[c] + " ";
                                                                    }
                                                                    for (int c=0; c< defenderResults.length ; c++) {
                                                                            serverRoll = serverRoll + defenderResults[c] + " ";
                                                                    }

                                                                    gameCommand(Addr, "DICE", serverRoll );

                                                            }

                                                            output=null;
                                                            needInput=false;

                                                    }
                                                    else { throw new IllegalArgumentException(resb.getString( "core.roll.error.unable")); }
                                            }
                                            else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.syntax"), "{0}", "roll number")); }
                                    }
                                    else { throw new IllegalArgumentException(JavaCompatUtil.replaceAll(resb.getString( "core.error.incorrect"), "{0}", "roll")); }
                                }
                                else { throw new IllegalStateException(resb.getString( "core.error.unknownstate")); }
                            }
                            catch (IllegalArgumentException ex) {
                                if (aiPlayer) {
                                    logger.log(Level.WARNING,"bad command from ai: \""+message+"\" player="+game.getCurrentPlayer()+" state="+game.getState(),ex);
                                }
                                if (replay) {
                                    throw ex;
                                }
                                output=ex.getMessage();
                            }
			} // this was the end of "if there is somthing to pass" but not needed any more

                        updateBattleState();

		}// end of parse of normal command

		// give a output if there is one
		if (output!=null) {
			// give a output
			if (game==null) {
				controller.sendMessage(output, false, true );
			}
			else if ( game.getState()==RiskGame.STATE_NEW_GAME ) {
				controller.sendMessage(output, false, true );
			}
			else if ( game.getState()==RiskGame.STATE_GAME_OVER ) {
				controller.sendMessage(output, true, true );
			}
			else if (game.getState()==RiskGame.STATE_END_TURN) {
				if ( game.getCurrentPlayer().getAutoEndGo() ) {
					controller.sendMessage(output, false, false );
				}
				else {
					controller.sendMessage(output, true, true );
				}
			}
			else {// if player type is human or neutral or ai
				controller.sendMessage(output, true, true );
			}
		}

		// check to see if the players go should be ended
		if ( game != null && game.getState()==RiskGame.STATE_END_TURN && game.getCurrentPlayer().getAutoEndGo() ) {
			needInput=false;
			DoEndGo();
		}

		// ask for input if u need one
		if (needInput) {
			getInput();
		}
	}

	/**
	 * This deals with trying to find out what input is required for the parser
	 */
	public void getInput() {
                // if we have more commands we need to process, do not bother asking for input
                if (!inbox.isEmpty()) return;

                setHelp();

		if (game==null) {
			controller.needInput( -1 );
		}
		// work out what to do next
		else if ( game!=null && game.getCurrentPlayer()!=null && game.getState()!=RiskGame.STATE_GAME_OVER ) {// if player type is human or neutral or ai

                        updateBattleState();

			if (game.getState()==RiskGame.STATE_TRADE_CARDS) {
				controller.sendMessage(JavaCompatUtil.replaceAll(resb.getString( "core.input.newarmies"), "{0}", ((Player)game.getCurrentPlayer()).getExtraArmies() + "") , false, false);
				//controller.armiesLeft( ((Player)game.getCurrentPlayer()).getExtraArmies() , game.NoEmptyCountries() );
			}
			else if (game.getState()==RiskGame.STATE_PLACE_ARMIES) {
				controller.sendMessage(JavaCompatUtil.replaceAll(resb.getString( "core.input.armiesleft"), "{0}", ((Player)game.getCurrentPlayer()).getExtraArmies() + ""), false, false);
				//controller.armiesLeft( ((Player)game.getCurrentPlayer()).getExtraArmies() , game.NoEmptyCountries() );
			}

                        // check if the next command should come from this computer
			if (shouldGameCommand(game.getCurrentPlayer().getAddress())) {
				if (game.getState() == RiskGame.STATE_DEFEND_YOURSELF && game.getCurrentPlayer().getAutoDefend()) {
                                        // 1st param = online play will check that this matches our address
                                        // 2nd param = we MUST send this command using our address, so the p2p server can roll the dice on the same client
                                        gameCommand(myAddress, myAddress, getBasicPassiveGo());
				}
				// || ((Player)game.getCurrentPlayer()).getType()==Player.PLAYER_NEUTRAL
				else if ( ((Player)game.getCurrentPlayer()).getType()==Player.PLAYER_HUMAN ) {
					controller.needInput( game.getState() );
				}
				else {
					ai.play(this);
				}
			}
		}
		else {
			controller.needInput( game.getState() );
		}
	}

        // TODO is this thread safe???
        private void setMap(String filename) throws Exception {

            if (game.getState()==RiskGame.STATE_NEW_GAME) {

                boolean yesmissions = game.setMapfile(filename);

                setupPreviews(yesmissions);

                //New map file selected: "{0}" (cards have been reset to the default for this map)
                String output= JavaCompatUtil.replaceAll( resb.getString( "core.choosemap.mapselected"), "{0}", filename);

                controller.sendMessage(output, false , true);
            }
            else {
                // GetMap class used to call this method directly, now it takes a callback
                // This is almost certainly not needed any more

                // if we had failed to start the UI because of a currupt map file on android
                // we will try to re download the map, and so after downloading the map,
                // we should try and load the map into the UI again
                controller.startGame(unlimitedLocalMode); // prob not used any more
            }
        }
        private void getMapError(String exception) {

            String output = resb.getString( "core.choosemap.error.unable")+" "+exception;
            controller.sendMessage(output, false , true);
            showMessageDialog(output);
        }

	private void setupPreviews(boolean yesmissions) {
            controller.showMapPic( game );
            controller.showCardsFile( game.getCardsFile() , yesmissions );
	}

        private boolean doesMapHaveMission() {
            java.util.Map cardsinfo = RiskUtil.loadInfo( game.getCardsFile() ,true);
            String[] missions = (String[])cardsinfo.get("missions");
            return missions.length > 0;
        }

	public int getType(String type) {
            if ("human".equals(type)) {
                    return Player.PLAYER_HUMAN;
            }
            if (type != null && type.startsWith("ai ")) {
                String aiType = type.substring(3);
                try {
                    return ai.getTypeFromCommand(aiType);
                }
                catch (IllegalArgumentException ex) {
                    logger.log(Level.INFO, "can not find ai", ex);
                    return -1;
                }
            }
            return -1;
	}

        public String getType(int type) {
            if (type==Player.PLAYER_HUMAN) {
                return "human";
            }
            else {
                return "ai "+ai.getCommandFromType(type);
            }
	}

	/**
	 * return true ONLY if info of this Player p should be disclosed to this computer
	 */
	private boolean showHumanPlayerThereInfo(Player p) {
		return game.getState()==RiskGame.STATE_GAME_OVER || ( (p != null) && ( p.getType()==Player.PLAYER_HUMAN ) && ( unlimitedLocalMode || myAddress.equals( p.getAddress() ) ) );
	}

        /**
         * @return true if the secret information of the current player can be shown in the UI
         */
        public boolean showHumanCurrentPlayerInfo() {
            Player currentPlayer = game.getCurrentPlayer();
            if (showHumanPlayerThereInfo(currentPlayer)) {
                return true;
            }
            // if there is only one human left in this game, we can show their info to everyone
            if (getSingleLocalHumanPlayer() == currentPlayer) {
                return true;
            }

            for (Player player : (List<Player>)game.getPlayers()) {
                if (player.getType() == Player.PLAYER_HUMAN && player.isAlive()) {
                    return false;
                }
            }
            // all humans are dead
            return true;
        }

	/**
	 * Method that deals with an end of a player's turn
	 */
	void DoEndGo() {

                controller.noInput(); // definatly need to block input at the end of someones go
                String Addr = ((Player)game.getCurrentPlayer()).getAddress();

                if (shouldGameCommand(Addr)) {
                    //give them a card if they deserve one
                    gameCommand(Addr, "CARD", game.getDesrvedCard() );
                }
	}

        boolean shouldGameCommand(String Addr) {
            return !replay && (onlinePlayClient == null || myAddress.equals(Addr));
        }
        
        /**
         * should ONLY be called if {@link #shouldGameCommand(java.lang.String) } returns true
         */
        void gameCommand(String address, String gameCommandOrAddress, String options) {
            if (replay) {
                throw new IllegalStateException("game command sent during replay");
            }

            String fullCommand = gameCommandOrAddress + " " + options;
            if (onlinePlayClient == null) {
                    inGameParser(fullCommand);
            }
            else {
                    if (!address.equals(myAddress)) {
                        throw new IllegalStateException("trying to send game command when not my address " + address +" " + myAddress);
                    }

                    onlinePlayClient.sendGameCommand(fullCommand);
            }
        }

	public void setReplay(boolean a) {
		replay = a;
	}
        public boolean isReplay() {
            return replay;
        }

        final AIManager ai = new AIManager();

        public String getBasicPassiveGo() {
            return ai.getOutput(game,Player.PLAYER_AI_CRAP);
        }

        public String[] getAICommands() {
            return ai.getAICommands();
        }

        public String getCommandFromType(int type) {
            return ai.getCommandFromType(type);
        }

	public String whoWon() {
		Player winner = getWinner();

                boolean iWon = winner.getType() == Player.PLAYER_HUMAN && ( unlimitedLocalMode || myAddress.equals( winner.getAddress() ) );
                controller.gameOver(iWon);

		String text = System.getProperty("line.separator") +
			JavaCompatUtil.replaceAll(resb.getString("core.whowon.hehaswon"), "{0}", winner.getName());
		if ( game.getGameMode() == RiskGame.MODE_SECRET_MISSION ) {
			//There mission was: {0}
			text=text + System.getProperty("line.separator") +
				JavaCompatUtil.replaceAll(resb.getString( "core.whowon.mission"), "{0}", winner.getMission().getDiscription());
		}
		return text;
	}

	public Player getWinner() {
	    if (game.getState() == RiskGame.STATE_GAME_OVER) {
		return game.getCurrentPlayer();
	    }
	    return null;
	}

	/** Shows helpful tips in each game state */
	private void setHelp() {

		String help="";

		if ( game!=null && game.getCurrentPlayer() != null ) {

			String strId = null;

                        int type = game.getCurrentPlayer().getType();
                        if (type==Player.PLAYER_HUMAN) {
                            strId = "core.help.move.human";
                        }
                        else {
                            strId = "core.help.move.ai."+ai.getCommandFromType(type);
                        }
                        try {
                            help = JavaCompatUtil.replaceAll(resb.getString(strId), "{0}", game.getCurrentPlayer().getName()) +" ";
                        }
                        catch (MissingResourceException ex) {
                            // fallback just in case we dont have a string
                            help = strId+": ("+game.getCurrentPlayer().getName()+") ";
                        }
		}

		if (game == null) {
			help = resb.getString( "core.help.newgame");
		}
		else if (game.getState()==RiskGame.STATE_NEW_GAME) {
			help = resb.getString( "core.help.createplayers");
		}
		else if (game.getState()==RiskGame.STATE_TRADE_CARDS) {
			help = help + resb.getString( "core.help.trade");
		}
		else if (game.getState()==RiskGame.STATE_PLACE_ARMIES) {

			if ( game.getSetupDone() ) { help = help + resb.getString( "core.help.placearmies"); }

			else if ( game.NoEmptyCountries() ) { help = help + resb.getString( "core.help.placearmy"); }

			else { help = help + resb.getString( "core.help.placearmyempty"); }

		}
		else if (game.getState()==RiskGame.STATE_ATTACKING) {
			help = help + resb.getString( "core.help.attack");
		}
		else if (game.getState()==RiskGame.STATE_ROLLING) {
			help = help + resb.getString( "core.help.rollorretreat");
		}
		else if (game.getState()==RiskGame.STATE_BATTLE_WON) {
			help = help + resb.getString( "core.help.youhavewon");
		}
		else if (game.getState()==RiskGame.STATE_FORTIFYING) {
			help = help + resb.getString( "core.help.fortifyposition");
		}
		else if (game.getState()==RiskGame.STATE_END_TURN) {
			help = help + resb.getString( "core.help.endgo");
		}
		else if (game.getState()==RiskGame.STATE_GAME_OVER) {
			//the game is over, {0} has won! close the game to create a new one
			Player winrar = game.getCurrentPlayer();
                        // winner should never be null, but its better not to crash if it is
			help = JavaCompatUtil.replaceAll(resb.getString( "core.help.gameover"), "{0}", winrar==null ? "null" : winrar.getName());
		}
		else if (game.getState()==RiskGame.STATE_SELECT_CAPITAL) {
			help = help + resb.getString( "core.help.selectcapital");
		}
		else if (game.getState()==RiskGame.STATE_DEFEND_YOURSELF) {
			help = help + resb.getString( "core.help.defendyourself");
		}
		else {
			help = resb.getString( "core.help.error.unknownstate");
		}

		controller.setGameStatus( help );

	}

        boolean skipUndo; // sometimes on some JVMs this just does not work
	private void saveGameToUndoObject() {

            if (skipUndo) return;

            if ( unlimitedLocalMode ) {

                // the game is saved
                try {
                    synchronized (Undo) {
                        Undo.reset();
                        game.saveGame(Undo);
                    }
                }
                catch (OutOfMemoryError e) {
                    // what can we do :-(
                    Undo.reset(); // do not keep broken data, TODO, this does NOT clean up memory
                    skipUndo = true;
                    logger.log(Level.INFO,resb.getString("core.loadgame.error.undo"),e);
                }
                catch (Throwable e) {
                    Undo.reset(); // do not keep broken data, TODO, this does NOT clean up memory
                    skipUndo = true;
                    logger.log(Level.WARNING, resb.getString( "core.loadgame.error.undo"),e);
                }
            }
	}

	public byte[] getLastSavedState() {
	    return Undo.toByteArray();
	}

        public void setSkipUndo(boolean skip) {
            skipUndo = skip;
        }

	public void disconnected() {
		logger.info("disconnected from the server!!");

                controller.sendMessage(resb.getString("core.kicked.error.disconnected"), false, false);

                // if close the game, we may be right in the middle of processing a game command right now
                parser("closegame");
	}

        private void updateBattleState() {
            if ((game.getState()==RiskGame.STATE_ROLLING || game.getState()==RiskGame.STATE_DEFEND_YOURSELF) && !battle) {
                    Player attackingPlayer = game.getAttacker().getOwner();
                    Player defendingPlayer = game.getDefender().getOwner();
                    if ( showHumanPlayerThereInfo(attackingPlayer) || showHumanPlayerThereInfo(defendingPlayer) ) {
                            controller.openBattle( game.getAttacker().getColor() , game.getDefender().getColor() );
                            // we are opeing the battle at a strange point, when NODAttacker is already set, so we should update it on the battle
                            if (game.getState()==RiskGame.STATE_DEFEND_YOURSELF) {
                                controller.setNODAttacker(game.getAttackerDice());
                            }
                            battle=true;
                    }
            }
            // if someone retreats
            else if (game.getState()!=RiskGame.STATE_ROLLING && game.getState()!=RiskGame.STATE_DEFEND_YOURSELF) {
                    closeBattle();
            }
        }

	public void closeBattle() {
		if ( battle ) { controller.closeBattle(); battle=false; }
	}

	/**
	 * Shows the cards a Player has in his/her possession
	 * @return Vector Returns the cards in a vector
	 */
	public List getCurrentCards() {
		return game.getCurrentPlayer().getCards();
	}

	/**
	 * Checks whether a Player has armies in a country
	 * @param color The color of the country
	 * @return int Returns the number of armies
	 */
	public int hasArmiesInt(int color) {
		return game.getCountryInt(color).getArmies();
	}

	/**
	 * Checks whether a Player can attack a country
	 * @param a The name of the country attacking
	 * @param d The name of the country defending
	 * @return boolean Returns true if the player owns the country, else returns false
	 *
	//  * @deprecated

	public boolean canAttack(String a, String d) {

		if ( ((Country)game.getCountry(a)).isNeighbours( (Country)game.getCountry(d) ) ) { return true; }
		else { return false; }

	}
	 */

	/**
	 * checks whether a Player can attach a country
	 * @param nCountryFrom	The name of the country attacking
	 * @param nCountryTo	The name of the country defending
	 * @return boolean Returns true if the player can attack the other one, false if not
	 */
	public boolean canAttack(int nCountryFrom, int nCountryTo)
	{
		if (game.getCountryInt( nCountryFrom).isNeighbours( game.getCountryInt( nCountryTo))) {
			return true;
		}
		return false;
	}//public boolean canAttack(int nCountryFrom, int nCountryTo)



	/**
	 * Checks whether a Player owns a country
	 * @param name The name of the country
	 * @return boolean Returns true if the player owns the country, else returns false
	 */
	public boolean isOwnedCurrentPlayerInt(int name) {

		// not thread safe, so this can cause problems, but this method is used in display to thats ok


		if ( (game!=null && game.getCurrentPlayer()!=null && game.getCountryInt( name )!=null) &&

			( ((Country)game.getCountryInt( name )).getOwner() == null || ((Country)game.getCountryInt( name )).getOwner() == game.getCurrentPlayer() )

		) { return true; }
		else { return false; }
	}

	/**
	 * Get the current mission of the game, depending on the game mode
	 * @return String Returns the current mission
	 */
	public String getHumanPlayerMission() {
            switch (game.getGameMode()) {
                case RiskGame.MODE_DOMINATION:
                    return resb.getString( "core.mission.conquerworld");
                case RiskGame.MODE_CAPITAL:
                    return resb.getString( "core.mission.capturecapitals");
                case RiskGame.MODE_SECRET_MISSION:
                    Player human = getSingleLocalHumanPlayer();
                    if (human != null) {
                        return human.getMission().getDiscription();
                    }
                    // else it MUST be our turn (or game over)
                    return game.getCurrentPlayer().getMission().getDiscription();
                default:
                    return resb.getString( "core.mission.error.cantshow");
            }
	}

	/**
	 * Get the colours of the players in the game
	 * @return Color[] Return the colour of the game players
	 */
	public int[] getPlayerColors() {

                RiskGame g = game;

                // sometimes this method can get called if we close a game half way through a paint
                if (g==null) return new int[0];

		if ( g.getState() == RiskGame.STATE_DEFEND_YOURSELF ) {
                    Country defender = g.getDefender();
                    if (defender!=null) {
			return new int[] { defender.getOwner().getColor() };
                    }
		}

		List Players = g.getPlayers();
		boolean setup = g.getSetupDone();

		int num=0;
		int start=0;

		for (int c=0; c< Players.size() ; c++) {

			if ( ((Player)Players.get(c)).getNoTerritoriesOwned() > 0 || !setup ) { num++; }
			if ( ((Player)Players.get(c)) == g.getCurrentPlayer() ) { start=c; }
		}

		int[] playerColors = new int[num];

		int current=0;

		for (int c=start; c< Players.size() ; c++) {

			if ( ((Player)Players.get(c)).getNoTerritoriesOwned() > 0 || !setup ) { playerColors[ current ] = ((Player)Players.get(c)).getColor() ; current++; }
			if ( current==num ) { break; }
			if ( c==Players.size()-1 ) { c=-1; }
		}

		return playerColors;
	}

	/**
	 * Get the colour of the current player
	 * @return Color Return the colour of the current player in the game
	 */
	public int getCurrentPlayerColor() {

		if (game != null && game.getState() != RiskGame.STATE_NEW_GAME) {
			return ((Player)game.getCurrentPlayer()).getColor();
		}
		else {
			return 0;
		}
	}

	/**
	 * Get the colour of the current player
	 * @param n the Country number identifier
	 * @return Color Return the colour of a player that owns a country
	 */
	public int getColorOfOwner(int n) {
		return ((Player)((Country)game.getCountryInt(n)).getOwner()).getColor();
	}

	/**
	 * Checks whether a set of cards can be traded in for extra armies
	 * @param c1 the name of the first card
	 * @param c2 the name of the second card
	 * @param c3 the name of the third card
	 * @return boolean Return true if the card can be traded, else return false
	 */
	public boolean canTrade(String c1, String c2, String c3) {

		if (game.getState() == RiskGame.STATE_TRADE_CARDS ) {

			Card[] cards = game.getCards(c1,c2,c3);

                        if (cards[0] == null || cards[1] == null || cards[2] == null) {
                            return false;
                        }

			return game.checkTrade(cards[0], cards[1], cards[2]);
		}
		return false;
	}

	/**
	 * Get the state of the cards
	 * @return int Return the newCardState
	 */
	public int getNewCardState() {
		return game.getNewCardState();
	}

	/**
	 * Get the present game
	 * @return RiskGame Return the current game
	 */
	public RiskGame getGame() {
		return game;
	}

        public boolean getLocalGame() {
            return unlimitedLocalMode;
        }

	/**
	 * Get the name of the country from the game
	 * @param color The (unique) country identifier
	 * @return String Return Country name if it is there, else return empty speech-marks otherwise
	 */
	public String getCountryName(int color) {
		Country t = game.getCountryInt(color);
		if (t==null) {
			return "";
		} else {
			return t.getName();
		}
	}

	public Player getCountryCapital(int color) {
	    Country t = game.getCountryInt(color);
	    List<Player> players = game.getPlayers();
	    for (Player player: players) {
	        if (player.getCapital() == t) {
	            return player;
	        }
	    }
	    return null;
	}

	/**
	 * returns the country display name
	 * @param nCountryId	The ID of the country
	 * @return	The country's display name

	public String getCountryDisplayName(int nCountryId)
	{
		Country country = game.getCountryInt( nCountryId);
		if (country == null) {
			return "";
		} else {
			return country.getName(); // Display
		}
	}//public String getCountryDisplayName(int nCountryId)
	 */


	/**
	 * Checks whether the current Player has autoEndGo on
	 * @return boolean Return autoEndGo
	 */
	public boolean getAutoEndGo() {

		if (game != null && game.getCurrentPlayer()!=null) {

			return game.getCurrentPlayer().getAutoEndGo();
		}
		else {
			// this should never happen, but can come up with bad timing problems
			return false;
		}
	}

	/**
	 * Checks whether the current Player has autoDefend on
	 * @return boolean Return autoDefend
	 */
	public boolean getAutoDefend() {

		if (game !=null && game.getCurrentPlayer()!=null) {

			return game.getCurrentPlayer().getAutoDefend();
		}
		else {
			// this should never happen, but can come up with bad timing problems
			return false;
		}
	}

        public String getMyAddress() {
            return myAddress;
        }

        public Player getSingleLocalHumanPlayer() {
            RiskGame game = getGame();
            if (game == null || game.getState() == RiskGame.STATE_NEW_GAME || game.getState() == RiskGame.STATE_GAME_OVER) {
                return null;
            }
            List<Player> players = game.getPlayers();
            String myAddress = getMyAddress();
            Player human1 = null, human2 = null;
            boolean tooMany1 = false, tooMany2 = false;
            for (Player player : players) {
                if (player.isAlive()) {
                    if (player.getType() == Player.PLAYER_HUMAN) {
                        if (human1 == null) {
                            human1 = player;
                        } else {
                            tooMany1 = true;
                        }
                    }
                    if (myAddress.equals(player.getAddress())) {
                        if (human2 == null) {
                            human2 = player;
                        } else {
                            tooMany2 = true;
                        }
                    }
                }
            }
            if (human1 != null && !tooMany1) {
                return human1;
            }
            if (human2 != null && !tooMany2) {
                return human2;
            }
            return null;
        }

	public void showMessageDialog(String a) {
		controller.showMessageDialog(a);
	}

        private void closeGame() {

            boolean createNewAddress = false;

            // shutdown the network connection for this game
            if ( onlinePlayClient != null ) {
                onlinePlayClient.closeGame();
                onlinePlayClient = null;

                // in case lobby had set us some other address we reset it
                createNewAddress = true;
            }

            // if there are more commands for this game from the network we should clear them
            if (!inbox.isEmpty()) {
                logger.log(Level.INFO, "clearing commands " + inbox);
                inbox.clear();
            }

            // shutdown the GUI for this game
            if (game != null) {
                // does not work from here
                closeBattle();
                controller.closeGame();
                ai.closeGame();
                game = null;
            }

            // we want to do this last as this may take a long time
            if (createNewAddress) {
                myAddress = RiskUtil.createRandomUniqueAddress();
            }
        }

	public void newMemoryGame(RiskGame g) {
                closeGame();
                game = g;
                controller.newGame(true);
                controller.showCardsFile( "loaded from memory" , (game.getNoMissions()!=0) );

                // we dont do this here as it wont work
                //controller.showMapPic(game);
                // the preview pic is instead set directly by the MapEditor calling on the SetupPanel
                /** @see net.yura.domination.ui.swinggui.SwingGUIPanel#showMapImage(javax.swing.Icon) */

                unlimitedLocalMode = true;
	}

        public void setOnlinePlay(OnlineRisk online) {
            onlinePlayClient = online;
            unlimitedLocalMode = onlinePlayClient==null;
        }

        /**
         * If we are opening a new game from outside the Risk command parser
         * e.g. from a network game, or some other source
         */
        public void setGame(RiskGame b) {
                if (game!=null) {
                    closeBattle();
                    controller.closeGame();
                    ai.closeGame();
                }
		inbox.clear();
		game = b;
                controller.startGame(unlimitedLocalMode);// need to always call this as there may be a new map
                getInput();
        }

        private void renamePlayer(String name,String newName, String newAddress,int newType) {

                if ("".equals(name) || "".equals(newName) || "".equals(newAddress) || newType==-1) {
                    throw new IllegalArgumentException("bad rename "+name+" "+newName+" "+newAddress+" "+newType);
                }

                // get all the players and make all with the ip of the leaver become nutral
                List players = game.getPlayers();
                Player leaver=null,newNamePlayer=null;
                for (int c=0; c< players.size(); c++) {
                    Player player = (Player)players.get(c);
                    if (player.getName().equals(newName)) {
                        newNamePlayer = player;
                    }
                    if (player.getName().equals(name)) {
                        leaver=player;
                    }
                }

                if (leaver==null) {
                    throw new IllegalArgumentException("can not find player with name \""+name+"\"");
                }
                if (newNamePlayer!=null && !name.equals(newName)) {
                    throw new IllegalArgumentException("can not rename \""+name+"\". someone with new name \""+newName+"\" is already in this game");
                }

                // AI will never have players addr for lobby game
                leaver.rename( newName );
                leaver.setType( newType );
                leaver.setAddress( newAddress );

                if (onlinePlayClient!=null) {
                    onlinePlayClient.playerRenamed(name,newName, newAddress,newType);
                }
        }

        public void setAddress(String address) {
            myAddress = address;
        }

	public Player findEmptySpot() {
            if (game!=null) {
                List players = game.getPlayers();
                for (int c=0; c< players.size() ; c++) {
                    Player player = (Player)players.get(c);
                    if ( player.getType() == Player.PLAYER_AI_CRAP && player.isAlive()) {
                        return player;
                    }
                }
            }
            return null;
	}
}
