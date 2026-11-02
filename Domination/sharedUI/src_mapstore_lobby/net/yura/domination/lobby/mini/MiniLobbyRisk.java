package net.yura.domination.lobby.mini;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Observable;
import java.util.Observer;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import net.yura.domination.LoadingManager;
import net.yura.domination.audio.GameSound;
import net.yura.domination.engine.OnlineRisk;
import net.yura.domination.engine.OnlineUtil;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.mapstore.Map;
import net.yura.domination.mapstore.MapPreview;
import net.yura.domination.mapstore.MapUpdateService;
import net.yura.lobby.mini.MiniLobbyClient;
import net.yura.lobby.mini.MiniLobbyGame;
import net.yura.lobby.model.Game;
import net.yura.lobby.model.GameType;
import net.yura.mobile.gui.Icon;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.components.Button;

/**
 * @author Yura Mamyrin
 * @see net.yura.domination.lobby.client.ClientGameRisk
 */
public abstract class MiniLobbyRisk implements MiniLobbyGame,OnlineRisk {

    private static final Logger logger = Logger.getLogger( MiniLobbyRisk.class.getName() );

    private Risk myrisk;
    protected MiniLobbyClient lobby;

    public MiniLobbyRisk(Risk risk) {
        myrisk = risk;

        GameSound.INSTANCE.setLobbyOpen(true);
    }

    public void addLobbyGameMoveListener(MiniLobbyClient lgl) {
        lobby = lgl;
        Button chatButton = lobby.getChatButton();
        chatButton.setText("");
        chatButton.setIcon(new Icon("/discord.png"));
    }
    
    public void openChat() {
        Application.openURL("http://domination.sourceforge.net/chat.shtml");
    }

    public boolean isMyGameType(GameType gametype) {
        return RiskUtil.GAME_NAME.equals(gametype.getName());
    }

    @Override
    public void prepareAndOpenGame(final Game game) {
        LoadingManager.showLoadingScreen(true);

        final String mapUID = OnlineUtil.getMapNameFromLobbyStartGameOption(game.getOptions());

        // TODO check if we are already in the process of downloading this map

        // check if we have this map already & if we need to do a update for the map
        if (MapPreview.haveLocalMap(mapUID) && !MapUpdateService.getInstance().contains(mapUID)) {
            lobby.mycom.openGame(game.getId());
        }
        else {
            net.yura.domination.mapstore.GetMap.getMap(mapUID, new Observer() {
                @Override
                public void update(Observable observable, Object data) {
                    if (data == RiskUtil.SUCCESS) {
                        lobby.mycom.openGame(game.getId());
                    }
                    else {
                        LoadingManager.showLoadingScreen(false);
                        lobby.error("map download failed for: " + mapUID);
                    }
                }
            });
        }
    }

    private boolean openGame;

    /**
     * @see net.yura.domination.lobby.client.ClientGameRisk#gameObject(java.lang.Object)
     */
    public void objectForGame(Object message) throws IOException, ClassNotFoundException {
        try {
            if (message instanceof byte[]) {
                ByteArrayInputStream in = new ByteArrayInputStream( (byte[])message );
                ObjectInputStream oin = new ObjectInputStream(in);
                Object object = oin.readObject();

                if (object instanceof RiskGame) {
                    RiskGame thegame = (RiskGame) object;
                    Player player = thegame.getPlayer(lobby.whoAmI());
                    String address = player == null ? "_watch_" : player.getAddress();
                    myrisk.setOnlinePlay(this);
                    myrisk.setAddress(address);
                    myrisk.setGame(thegame);
                    openGame = true;
                }
                else {
                    logger.info("unknown object " + object);
                }
            }
            else {
                throw new RuntimeException("unknown object "+message);
            }
        }
        finally {
            LoadingManager.showLoadingScreen(false);
        }
    }

    /**
     * @see net.yura.domination.lobby.client.ClientGameRisk#gameString(java.lang.String)
     */
    public void stringForGame(String message) {
        if (openGame) {
            myrisk.parserFromNetwork(message);
        }
        else {
            logger.info("GAME NOT OPEN SO IGNORING: "+message);
        }
    }

    public void disconnected() {
        LoadingManager.showLoadingScreen(false);
        myrisk.disconnected();
    }

    public void userLogin(String username) {
        GameSound.INSTANCE.playSound(GameSound.LOBBY_SET_NICK);
    }
    public void loginGoogle() {
    }
    public void gameStarted(int id) {
        logger.info("gameStarted: " + id);
    }

    @Override
    public void playerRenamed(String oldName, String newName) {
        logger.info("player renamed: " + oldName + " -> " + newName);
    }

    @Override
    public void playerAdded(String name) {
        logger.info("player added: " + name);
    }

    @Override
    public void playerRemoved(String name) {
        logger.info("player removed: " + name);
    }

    public void gameActionPerformed(int action) {
        String sound = null;
        switch (action) {
            case Game.STATE_CAN_JOIN: sound = GameSound.LOBBY_JOIN; break;
            case Game.STATE_CAN_LEAVE: sound = GameSound.LOBBY_LEAVE; break;
            case Game.STATE_CAN_WATCH: sound = GameSound.LOBBY_WATCH; break;
            case Game.STATE_CAN_PLAY: sound = GameSound.LOBBY_PLAY; break;
        }
        if (sound != null) {
            GameSound.INSTANCE.playSound(sound);
        }
    }


    WeakHashMap mapping = new WeakHashMap();
    MapPreviewClient mapPreviewClient = new MapPreviewClient() {
        public void publishMap(Map map) {
            mapMetaData(map);
        }
        public void publishImg(String mapUID) {
            lobby.getRoot().repaint();
        }
    };
    
    protected void mapMetaData(Map map) {
        // we do not care about the map metadata
    }

    public Icon getIconForGame(Game game) {
        String mapUID = OnlineUtil.getMapNameFromLobbyStartGameOption(game.getOptions());
        mapping.put(game, mapUID); // keep a strong ref to the mapUID as long as we have a strong ref to the game

        return mapPreviewClient.getIconForMap(mapUID);
    }

    public void lobbyShutdown() {
        mapPreviewClient.shutdown();

        GameSound.INSTANCE.setLobbyOpen(false);
    }

    public String getGameDescription(Game game) {
        return OnlineUtil.getGameDescriptionFromLobbyStartGameOption( game.getOptions() );
    }



    // WMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMW
    // WMWMWMWMWMWMWMWMWMWMWMWMWMW OnlineRisk MWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMW
    // WMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMWMW

    public void sendUserCommand(String messagefromgui) {
        lobby.sendGameMessage(messagefromgui);
    }

    public void sendGameCommand(String gameCommand) {
	// this happens for game commands on my go
        logger.info("ignore GameCommand " + gameCommand);
    }

    public void closeGame() {
        openGame = false;
        lobby.closeGame();
    }

    public void playerRenamed(String oldName, String newName, String newAddress, int newType) {
        if (oldName.equals(lobby.whoAmI())) {
            myrisk.setAddress("_watch_");
        }
        if (newName.equals(lobby.whoAmI())) {
            myrisk.setAddress(newAddress);
        }
    }
}
