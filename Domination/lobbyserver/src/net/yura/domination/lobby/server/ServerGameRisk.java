package net.yura.domination.lobby.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskIO;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.lobby.model.Game;
import net.yura.lobby.server.LobbySession;
import net.yura.lobby.server.TurnBasedGame;
import net.yura.mobile.util.Url;

public class ServerGameRisk extends TurnBasedGame {

        static final GameSettings settings;

	ServerRisk myrisk;

        static {
            // we get 'user.dir' here so that we can override this for junit tests
            final File gameDir = new File(System.getProperty("user.dir"), RiskUtil.GAME_NAME);
            final File mapsDir = new File(gameDir, "maps");

            RiskUtil.streamOpener = new RiskIO() {
                public InputStream openStream(String name) throws IOException {
                    return new FileInputStream(new File(gameDir, name));
                }
                public InputStream openMapStream(String name) throws IOException {
                    return new FileInputStream(new File(mapsDir, name));
                }
                public ResourceBundle getResourceBundle(Class c, String n, Locale l) {
                    // TODO this should be different for different clients connected to this server
                    return ResourceBundle.getBundle(c.getPackage().getName()+"."+n, l );
                }
                public void openURL(URL url) throws Exception {

                }
                public void openDocs(String doc) throws Exception {

                }
                public void saveGameFile(String name, RiskGame obj) throws Exception {

                }
                public InputStream loadGameFile(String file) throws Exception {
                    return null;
                }
                public void getMap(String filename, Observer observer) {
                    observer.update(null, RiskUtil.ERROR);
                }
                public java.io.OutputStream saveMapFile(String fileName) throws Exception {
                    return RiskUtil.getOutputStream(mapsDir , fileName);
                }
                public void renameMapFile(String oldName, String newName) {
                    RiskUtil.rename(new File(mapsDir, oldName), new File(mapsDir, newName));
                }
                public boolean deleteMapFile(String mapName) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };

            settings = new GameSettings(mapsDir);
            try {
                MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
                mbs.registerMBean(settings, new ObjectName("net.yura.domination:type=GameSettings") );
            }
            catch (Exception ex) {
                RiskUtil.printStackTrace(ex);
            }
        }

	public ServerGameRisk() {
	}
        
        // officially released supported clients

        /**
         * pre Italian rule change version 24
         * new Italian rules version 25
         */
        private final String APP_IOS = "iOSDomination";
        /**
         * pre Italian rule change version 106
         * new Italian rules version 107
         */
        private final String APP_ANDROID = "AndroidDomination";
        /**
         * pre Italian rule change version 1.3.5
         * new Italian rules version 1.3.6
         */
        private final String APP_FLASH = "FlashDomination";
        /**
         * pre Italian rule change version 1.3.5
         * new Italian rules version 1.3.6
         */
        private final String APP_SWING = "SwingDomination";

        @Override
        public boolean isSupportedClient(LobbySession session) {
            String appNameVersion = session.getClientVersion();
            int space = appNameVersion.indexOf(' '); // this space is added by Lobby server so should always be there
            String appName = appNameVersion.substring(0, space);
            String appVersion = appNameVersion.substring(space + 1, appNameVersion.length());

            try {
                if (APP_ANDROID.equals(appName)) {
                    int androidVersion = Integer.parseInt(appVersion);
                    if (androidVersion < settings.getMinAndroidVersion()) {
                        return false;
                    }
                }

                String[] options = startGameOptions.split("\\n");
                String startGameCommand = options[4];

                if (startGameCommand.contains(Risk.STARTGAME_OPTION_CARD_ITALIAN_LIKE_SET)) {
                    if (APP_IOS.equals(appName)) {
                        int iosVersion = Integer.parseInt(appVersion);
                        if (iosVersion < 25) {
                            return false;
                        }
                    }
                    else if (APP_ANDROID.equals(appName)) {
                        int iosVersion = Integer.parseInt(appVersion);
                        if (iosVersion < 107) {
                            return false;
                        }
                    }
                    else if (APP_FLASH.equals(appName) || APP_SWING.equals(appName)) {
                        int[] versions  = Arrays.stream(appVersion.split(Pattern.quote("."))).mapToInt(Integer::parseInt).toArray();
                        if (compare(versions, new int[] {1,3,6}) < 0) {
                            return false;
                        }
                    }
                }
            }
            catch (NumberFormatException ex) {
                Logger.getLogger(ServerGameRisk.class.getName()).warning("strange version " + appNameVersion);
            }

            return true;
        }

        /**
         * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
         */
        public static int compare(int[] versions1, int[] versions2) {
            for (int c = 0; c < versions1.length; c++) {
                if (versions1[c] < versions2[c]) {
                    return -1;
                }
                else if (versions1[c] > versions2[c]) {
                    return 1;
                }
            }
            return 0;
        }

        private void createGame() {
            if (myrisk == null) {
                myrisk = new ServerRisk(this);
                myrisk.setName("Domination-Game-" + getId());

                // POP UP DEBUG WINDOW
                //Increment1Frame gui = new Increment1Frame( myrisk );
                //RiskGUI gui = new RiskGUI( myrisk );
                //gui.setVisible(true);
            }
        }

        @Override
        public void setGameDetails(Game game) {
            super.setGameDetails(game);

            // check game is valid at earliest possible time, and throw error if not
            String[] options = startGameOptions.split("\\n");
            int aieasy = Integer.parseInt(options[1]);
            int aiaverage = Integer.parseInt(options[0]);
            int aihard = Integer.parseInt(options[2]);
            if ((game.getMaxPlayers() + aieasy + aiaverage + aihard) > RiskGame.MAX_PLAYERS) {
                throw new IllegalArgumentException("invalid number of players. humans:" + game.getMaxPlayers() + " + AI:"+aieasy + "," + aiaverage + "," + aihard + " > MAX_PLAYERS:" + RiskGame.MAX_PLAYERS);
            }
        }

        @Override
	public void startGame(String[] players) {
                createGame();
		//System.out.println("\tNEW GAME STARTING FOR RISK: "+gameid);

		//myguid = gameid;

		myrisk.makeNewGame();


		String[] options = startGameOptions.split("\\n");

		int aieasy = Integer.parseInt(options[1]);
		int aiaverage = Integer.parseInt(options[0]);
		int aihard = Integer.parseInt(options[2]);

		if ((players.length + aieasy + aiaverage + aihard) > RiskGame.MAX_PLAYERS) {
                    throw new IllegalArgumentException("player number missmatch for startgame: humans:" + Arrays.asList(players) + " AI:"+aieasy + "," + aiaverage + "," + aihard);
                }

		myrisk.addSetupCommandToInbox(options[3]); // set the map file to use

                List<String> playerCommands = new ArrayList();
                
		List<String> colorString = new ArrayList<String>();
		colorString.add( myrisk.getRiskConfig("default.player1.color") );
		colorString.add( myrisk.getRiskConfig("default.player2.color") );
		colorString.add( myrisk.getRiskConfig("default.player3.color") );
		colorString.add( myrisk.getRiskConfig("default.player4.color") );
		colorString.add( myrisk.getRiskConfig("default.player5.color") );
		colorString.add( myrisk.getRiskConfig("default.player6.color") );
		Iterator<String> it = colorString.iterator();

                // sort them so if player bob was green last time, they are again
		Arrays.sort(players);
		for (int c = 0; c < players.length; c++) {
			it.hasNext();
			String color = it.next();
			String playerid = "player"+(c+1);
			playerCommands.add(playerid + " newplayer human "+color + " " + players[c]);
		}
                
                // BeginnerBot, RookieBot, AmateurBot and ProBot.
                // Normal Medium Average Standard

		for (int c = 0; c < aieasy; c++) {
			it.hasNext();
			String color = it.next();
			playerCommands.add(myrisk.getAddress() + " newplayer ai easy " + color + " EasyBot" + (c+1));
		}
                for (int c = 0; c < aiaverage; c++) {
			it.hasNext();
			String color = it.next();
			playerCommands.add(myrisk.getAddress() + " newplayer ai average " + color + " AverageBot" + (c+1));
		}
		for (int c = 0; c < aihard; c++) {
			it.hasNext();
			String color = it.next();
			playerCommands.add(myrisk.getAddress() + " newplayer ai hard " + color + " HardBot" + (c+1));
		}

                // shuffle so the starting order is not the same each game
                // otherwise there will be a much higher chance (5 out of 6) person A goes before person B
                // we also want to mix up the order the bots take
                Collections.shuffle(playerCommands);
                
                for (String playerCommand : playerCommands) {
                    myrisk.addSetupAddressCommandToInbox(playerCommand);
                }
                
		myrisk.addSetupCommandToInbox(options[4]); // start the game

		// HACK: only return when the game is setup
		while(!myrisk.getWaiting()) {
			try { Thread.sleep(100); }
			catch(InterruptedException e){}
		}

                myrisk.setPaued(false);

                if (myrisk.getGame().getState() == RiskGame.STATE_NEW_GAME) {
                    // give better error if player tries to start a mission game when map does not support it
                    if (myrisk.getGame().getGameMode() == RiskGame.MODE_SECRET_MISSION && myrisk.getGame().getMissions().size() < myrisk.getGame().getPlayers().size() ) {
                        throw new IllegalStateException("map does not support missions");
                    }
                    // this can happen if the user creates a mission game on a map that does not support missions
                    throw new IllegalStateException("game failed to start");
                }
	}

        @Override
	public void loadGame(String[] lobbyPlayers, byte[] gameData) {
            createGame();
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(gameData);
                ObjectInputStream oin = new ObjectInputStream(in);
                RiskGame riskGame = (RiskGame)oin.readObject();

                String address=myrisk.getAddress();
                List<Player> players = riskGame.getPlayers();
                for (Player player:players) {
                    if (player.getType()!=Player.PLAYER_HUMAN) {
                        player.setAddress(address);
                    }
                }
                myrisk.setGame(riskGame);
                myrisk.setPaued(false);
            }
            catch (Exception ex) {
                throw new RuntimeException(ex);
            }
	}

        @Override
        public byte[] saveGameState() {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                myrisk.getGame().saveGame(bout); // TODO this is prob not very thread safe!!
                return bout.toByteArray();
            }
            catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        @Override
	public void destroyGame() {
		myrisk.setKillFlag();
	}

        @Override
	public void clientHasJoined(String username) {
                sendObjectToClient(saveGameState(), username);
	}

        /**
         * @return May return null, all methods calling this method must handle null!
         */
        private String getPlayerId(String username) {
            RiskGame game = myrisk.getGame();
            List<Player> players = game.getPlayers();
            for (Player player:players) {
                if (player.getType()==Player.PLAYER_HUMAN && player.getName().equals(username)) {
                    return player.getAddress();
                }
            }
            return null;
        }

	// get message from the user
        @Override
	public void stringFromPlayer(String username, String message) {
		//System.out.print("\tGOTFROMCLIENT "+username+":"+message+"\n");
		String address = getPlayerId(username);
                Player player = myrisk.getGame().getCurrentPlayer();
		//if (game.getCurrentPlayer()!=null) { System.out.print( "\t"+game.getCurrentPlayer().getAddress()+" "+address ); }
		// game not started OR game IS started and it is there go
		if (message.trim().equals("closegame")) {
			throw new RuntimeException("closegame not allowed to be sent to core: "+username);
		}
                if (message.trim().equals("undo")) {
			throw new RuntimeException("undo not allowed to be sent to core: "+username);
		}
		if (player==null) {
                        throw new RuntimeException("currentPlayer is null");
		}

                // ignore messages from defenders as old version still send them (android <= 67 & desktop <= 1.2.2)
                // first we need to make sure we ignore rolls that are sent at the correct time, like when it is our turn defending
                Country defending = myrisk.getGame().getDefender();
                Player defender = defending == null ? null : defending.getOwner();
                if (defender != null && defender.getAutoDefend() && defender.getAddress().equals(address) && message.trim().startsWith("roll")) {
                    // this command may arrive in our turn or just after (as the autodefend on the server has already happened)
                    System.out.println("DEFENDING ROLL IGNORED \"" + username + "\" [" + address + " " + message + "] myturn=" + player.getAddress().equals(address)+ " state=" + myrisk.getGame().getState());
                    return;
                }
                // also we may get the defending roll at any point after, such as during moving when defender country is not owned by us any more
                if (!player.getAddress().equals(address) && (myrisk.getGame().getState() == RiskGame.STATE_BATTLE_WON || myrisk.getGame().getState() == RiskGame.STATE_ATTACKING) && message.trim().startsWith("roll")) {
                    System.out.println("DEFENDING ROLL IGNORED \"" + username + "\" [" + address + " " + message + "] myturn=FALSE state=" + myrisk.getGame().getState());
                    return;
                }
                // end ignore defend hack for (android <= 67 & desktop <= 1.2.2)

                if (!player.getAddress().equals(address)) {
                    throw new RuntimeException("got command but it is not our go: username="+username+" address="+address+" message=\""+message+"\" current player: "+player.getName() + " state: " + myrisk.getGame().getState());                        
                }

		myrisk.addPlayerCommandToInbox(address, message);
	}


        @Override
	public void doBasicGo(String username) {
		String playerid = getPlayerId(username);
		// this check is already done
		//if (myrisk.getGame().getCurrentPlayer().getAddress().equals(playerid)) {
			myrisk.addPlayerCommandToInbox(playerid+"-doBasicGo", myrisk.getBasicPassiveGo() );
		//}
		// else something is going very wrong!!!!
		// such as cheating
	}

        @Override
	public boolean playerResigns(String username, boolean gameTriggered) {
		boolean gameRemoved = false;

		String playerid = getPlayerId(username);

		//String currentAddress = myrisk.getGame().getCurrentPlayer().getAddress();

		if (playerid != null) {
                        List<Player> players = (List<Player>)myrisk.getGame().getPlayers();
			//myrisk.renamePlayer(username,newName,myrisk.getAddress(),Player.PLAYER_AI_CRAP);

                        String newName = username + (gameTriggered ? "-AutoResigned" : "-Resigned");
                        Player thePlayer=null,oldPlayer=null;
                        int aliveHumans=0,aliveAIs=0;
                        for (Player player:players) {
                            String name = player.getName();
                            if (name.equals(username)) {
                                thePlayer = player;
                            }
                            else if (name.equals(newName)) {
                                oldPlayer = player;
                            }
                            // check they are alive
                            else if (player.isAlive()) {
                                if (player.getType()==Player.PLAYER_HUMAN) {
                                    aliveHumans++;
                                }
                                else if (player.getType()!=Player.PLAYER_AI_CRAP) {
                                    aliveAIs++;
                                }
                            }
                        }

                        if(thePlayer == null) { // this should never happen
                            throw new IllegalArgumentException("can not find player " + username);
                        }
                        if (oldPlayer != null) { // this should never happen
                            throw new IllegalArgumentException("player with name already in game " + newName);
                        }

                        // only rename the resigned player if they are alive and game not over.
                        // If they are dead, or if game over in domination mode or ai has won, no point.
                        // if game over and mode is mission or capital, the winner may choose to continue the game, so resign the player.
                        if (thePlayer.isAlive() && !(myrisk.getGame().getState() == RiskGame.STATE_GAME_OVER && (myrisk.getGame().getGameMode() == RiskGame.MODE_DOMINATION || myrisk.getWinner().getType() != Player.PLAYER_HUMAN))) {

                                sendRename(username, newName, myrisk.getAddress(), Player.PLAYER_AI_EASY);
                        }

                        if (aliveHumans == 0) {
                            gameRemoved = gameFinished(whoHasMostPoints());

                            // We have no humans and no ais, (we only have resigned human, crap/submissive ais)
                            // so we must pause the game or it will get stuck in a loop of placing armies and
                            // nothing else actually happening, and will never finish.

                            // We need this check as if someone is still watching the game, the game does not get
                            // destroyed and will take up more and more space in memory, as round after round goes
                            // by and nothing actually happens

                            // TODO in LobbyClientGUI it is possible to re-join a game at this stage, and the game
                            // will still be paused so it will not actually do anything, then again, if the TurnBasedGame.finished
                            // flag is set, the game will get destroyed when all human players are not watching it anyway
                            if (aliveAIs == 0) {
                                myrisk.setPaued(true);
                            }
                        }
		}
                else {
                    throw new RuntimeException("can not resign player "+username+" they are not in this game");
                }

		// already handled by the turn based game
		// ----
		// if they have resigned on there own go then do a go for them
		//if (currentAddress.equals(playerid)) {
		//	myrisk.parser( AIPlayer.getOutput(myrisk.getGame(),AIPlayer.aicrap) );
		//}

                return gameRemoved;
	}

        private void sendRename(String oldName, String newName, String newAddress, int newType) {
            HashMap map = new HashMap();
            map.put("oldName", oldName);
            map.put("newName", newName);
            map.put("newAddress", newAddress);
            map.put("newType", newType);
            myrisk.addPlayerCommandToInbox("RENAME", Url.toQueryString(JavaCompatUtil.asHashtable(map)));
        }

        @Override
        public void playerJoins(String newuser) {
            Player player = myrisk.findEmptySpot();
            if (player==null) {
                throw new RuntimeException("no AI CRAP found in game");
            }
            String playerId = "player"+( myrisk.getGame().getPlayers().indexOf(player) +1);
            String oldName = player.getName();
            sendRename(oldName, newuser, playerId, Player.PLAYER_HUMAN);
        }

        @Override
	public void renamePlayer(String oldUser, String newUser) {
            String playerId = getPlayerId(oldUser);
            if (playerId == null) {
                throw new IllegalArgumentException(oldUser + " not found in game player: " + myrisk.getGame().getPlayers());
            }
            sendRename(oldUser, newUser, playerId, Player.PLAYER_HUMAN);
	}

        /**
         * This is called each time we want input from ANYEONE, including humans and AI
         * it also may get called more times then it should, e.g. during auto defend
         * @see #doBasicGo(java.lang.String)
         */
	public void getInputFromSomeone() {
            Player player = myrisk.getGame().getCurrentPlayer();
            // Player should never be null, but better not to crash here
            String username = (player != null && player.getType() == Player.PLAYER_HUMAN && myrisk.getGame().getState() != RiskGame.STATE_GAME_OVER) ? player.getName() : null;

            // we tell TurnBasedGame who we expect a command from
            // if this person does not give us a command, TurnBasedGame will call doBasicGo()
            // when called with null (for AI) it cancels the timeout timer
            getInputFromClient(username);
	}

        private String whoHasMostPoints() {
		RiskGame game = myrisk.getGame();
		if ( game.checkPlayerWon() ) {
			return game.getCurrentPlayer().getName();
		}
                String name="???";
                int best=-1;
                List<Player> players = game.getPlayers();
                for (int c=0;c<players.size();c++) {
                        Player player = players.get(c);
                        // player.getType() == Player.PLAYER_HUMAN &&
                        // if all resign then no humans left
                        if ( player.getNoTerritoriesOwned()>best) {
                                name = player.getName();
                                best = player.getNoTerritoriesOwned();
                        }
                }
                return name;
	}
}
