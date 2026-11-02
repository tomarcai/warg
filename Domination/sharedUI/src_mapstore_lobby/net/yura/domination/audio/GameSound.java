package net.yura.domination.audio;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskAdapter;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.core.RiskGame;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GameSound extends RiskAdapter {
    
    private static final Logger LOGGER = Logger.getLogger(GameSound.class.getName());

    public final static GameSound INSTANCE = new GameSound();

    public static final String MUSIC_MENU = "music_menu";
    public static final String MUSIC_SETUP = "music_setup";
    public static final String MUSIC_LOBBY = "music_lobby";

    public static final String MUSIC_MY_TURN = "music_my_turn";
    public static final String MUSIC_OTHER_TURN = "music_other_turn";
    public static final String MUSIC_BATTLE = "music_battle";

    public static final String MUSIC_VICTORY = "music_victory";
    public static final String MUSIC_DEFEAT = "music_defeat";

    public static final String MENU_BUTTON = "menu_button";
    public static final String BUTTON = "button";
    public static final String BACK_BUTTON = "back_button";
    public static final String BUTTON_START_GAME = "button_start_game";

    public static final String CARDS_RECEIVE = "cards_receive";
    public static final String CARDS_TRADE = "cards_trade";

    public static final String PLACE_ARMY = "place_army";
    public static final String PLACE_ARMIES = "place_armies";

    public static final String ATTACK = "attack";
    public static final String BATTLE_RETREAT = "battle_retreat";

    public static final String DICE_ROLL = "dice_roll";
    public static final String DICE_WIN = "dice_win";
    public static final String DICE_LOSE = "dice_lose";
    public static final String DICE_DRAW = "dice_draw";
    
    public static final String BATTLE_WIN = "battle_win";
    public static final String BATTLE_DEFEAT = "battle_defeat";

    public static final String BATTLE_DEFENSE_WIN = "battle_defense_win";
    public static final String BATTLE_DEFENSE_DEFEAT = "battle_defense_defeat";

    public static final String MOVE_ARMIES = "move_armies";
    public static final String MOVE_TACTICAL = "move_tactical";

    public static final String LOBBY_START = "lobby_start";
    public static final String LOBBY_JOIN = "lobby_join";
    public static final String LOBBY_LEAVE = "lobby_leave";
    public static final String LOBBY_PLAY = "lobby_play";
    public static final String LOBBY_WATCH = "lobby_watch";
    public static final String LOBBY_SET_NICK = "lobby_set_nick";

//    public static final String[] ALL_SOUNDS = new String[]{
//            MENU_BUTTON, BUTTON, BACK_BUTTON, BUTTON_START_GAME, CARDS_RECEIVE, CARDS_TRADE, PLACE_ARMY, PLACE_ARMIES,
//            ATTACK, BATTLE_RETREAT, DICE_ROLL, DICE_WIN, DICE_LOSE, DICE_DRAW, BATTLE_WIN, BATTLE_DEFEAT, BATTLE_DEFENSE_WIN,
//            MOVE_ARMIES, MOVE_TACTICAL, LOBBY_START, LOBBY_JOIN, LOBBY_LEAVE, LOBBY_PLAY, LOBBY_WATCH, LOBBY_SET_NICK };

    private boolean soundEnabled = true;
    private boolean musicEnabled = true;

    private AudioSystem audioSystem;
    private Map<String, String> currentTheme; // ID -> filename
    private String currentMusicId;

    public void setAudioSystem(Risk risk, AudioSystem audio) {
        audioSystem = audio;
        risk.addRiskListener(this);
        currentMusicId = MUSIC_MENU;
    }

    public void load(String theme) throws Exception {

        try {
            if (currentMusicId != null) {
                stopPlayingLoopedSound();
            }
            
            final String folder = "sound";

            InputStream in = RiskUtil.openStream(folder + "/" + theme + ".xml");

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

            currentTheme = new HashMap();
            parser.parse(in, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if ("sound".equals(qName)) {
                        String key = attributes.getValue("name");
                        String filename = attributes.getValue("file");

                        // TODO this check is wrong on android/ios, also if 'user.dir' has been changed
                        //if (!new File("sound", filename).exists()) {
                        //    System.out.println("[WARNING!!!!!] File not found: " + filename);
                        //}

                        currentTheme.put(key, folder + "/" + filename);
                    }
                }
            });
        }
        finally {
            if (currentMusicId != null) {
                playCurrentLoopedSound();
            }
        }
    }
    
    public void setSoundEnabled(boolean on) {
        soundEnabled = on;
    }
    public void setMusicEnabled(boolean on) {
        boolean musicOld = musicEnabled;
        musicEnabled = on;
        if (currentMusicId != null) {
            if (musicOld && !musicEnabled) {
                stopPlayingLoopedSound();
            }
            else if (!musicOld && musicEnabled) {
                playCurrentLoopedSound();
            }
        }
    }

    public void playSound(String audioId) {
        
        LOGGER.fine("Playing sound with id: " + audioId);
        
        if (soundEnabled && audioSystem != null && currentTheme != null) {
            String audioFile = currentTheme.get(audioId);
            if (audioFile != null) {
                audioSystem.play(audioFile);
            }
        }
    }

    public void playMusic(String audioId) {
        currentMusicId = audioId;
        playCurrentLoopedSound();
    }

    public void stopMusic() {
        if (musicEnabled) {
            stopPlayingLoopedSound();
        }
        currentMusicId = null;
    }

    private void playCurrentLoopedSound() {
        LOGGER.fine("Playing music with id: " + currentMusicId);
        if (musicEnabled && audioSystem != null && currentTheme != null) {
            String audioFile = currentTheme.get(currentMusicId);
            if (audioFile != null) {
                audioSystem.start(audioFile);
            }
        }
    }

    private void stopPlayingLoopedSound() {
        LOGGER.fine("Stopping music with id: " + currentMusicId);
        if (audioSystem != null && currentTheme != null) {
            String audioFile = currentTheme.get(currentMusicId);
            if (audioFile != null) {
                audioSystem.stop(audioFile);
            }
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    
    private void switchToMusic(String id) {
        if (!id.equals(currentMusicId)) {
            if (currentMusicId != null) {
                stopMusic();
            }
            playMusic(id);
        }
    }
    
    
    boolean lobbyOpen;
    boolean gameWon;
    boolean gameLost;
    
    public void setLobbyOpen(boolean open) {
        lobbyOpen = open;
        switchToMusic(getOutOfGameMusic());
    }
    public void setGameWon(boolean won) {
        gameWon = won;
        switchToMusic(getMyTurnGameMusic());
    }
    public void setGameLost(boolean lost) {
        gameLost = lost;
        switchToMusic(getOtherTurnGameMusic());
    }

    private String getOutOfGameMusic() {
        return lobbyOpen ? MUSIC_LOBBY : MUSIC_MENU;
    }

    private String getMyTurnGameMusic() {
        return gameWon ? MUSIC_VICTORY : MUSIC_MY_TURN;
    }

    private String getOtherTurnGameMusic() {
        return gameLost ? MUSIC_DEFEAT : MUSIC_OTHER_TURN;
    }

    // ============ RiskAdapter methods =============

    public void needInput(int s) {
        if (s > RiskGame.STATE_NEW_GAME && s != RiskGame.STATE_ROLLING && s != RiskGame.STATE_DEFEND_YOURSELF && s != RiskGame.STATE_GAME_OVER) {
            switchToMusic( getMyTurnGameMusic() );
        }
    }
    public void noInput() {
        switchToMusic( getOtherTurnGameMusic() );
    }

    public void newGame(boolean t) {
        switchToMusic(MUSIC_SETUP);
    }
    public void startGame(boolean localGame) {
        playSound(BUTTON_START_GAME);
        switchToMusic( getOtherTurnGameMusic() );
    }

    public void gameOver(boolean bln) {
        if (bln) {
            setGameWon(true);
        }
        else {
            setGameLost(true);
        }
    }
    
    public void closeGame() {
        gameWon = false;
        gameLost = false;
        // TODO maybe play sound for closing game
        switchToMusic( getOutOfGameMusic() );
    }

    public void openBattle(int c1num, int c2num) {
        switchToMusic(MUSIC_BATTLE);
    }
    public void closeBattle() {
        switchToMusic( getOtherTurnGameMusic() );
    }

    /**
     * result == -1: attacker lost
     * result == 0: nobody won
     * result == 1: attacker won
     * result == 2: attacker won and player eliminated
     */
    public void showDiceResults(int[] atti, int[] defi, boolean weAreAttacker, int result) {

        // work out what sound to play
        int out = 0;
        int fights = Math.min(atti.length, defi.length);
        for (int c = 0; c < fights; c++) {
            out = out + (atti[0] > defi[0] ? 1 : -1);
        }

        if (!weAreAttacker) {
            // we must be the defender
            out = -out;
            result = -result;
        }

        if (result >= 1) {
            if (weAreAttacker) {
                playSound(BATTLE_WIN);
            }
            else {
                playSound(BATTLE_DEFENSE_WIN);
            }

            if (result == 2) {
                // TODO maybe play some player eliminated sound
            }
        }
        else if (result <= -1) {
            if (weAreAttacker) {
                playSound(BATTLE_DEFEAT);
            }
            else {
                playSound(BATTLE_DEFENSE_DEFEAT);
            }
                
            if (result == -2) {
                setGameLost(true);
            }
        }
        else if (out == 0) {
            playSound(DICE_DRAW);
        }
        else if (out > 0) {
            playSound(DICE_WIN);
        }
        else {
            playSound(DICE_LOSE);
        }
    }

    public void playerGotCard() {
        playSound(CARDS_RECEIVE);
    }

/*
    public void sendMessage(String output, boolean a, boolean b) {}
    public void setGameStatus(String state) {}
    public void showMapPic(RiskGame p) {}
    public void showCardsFile(String c, boolean m) {}
    public void serverState(boolean s) {}
    public void addPlayer(int type, String name, int color, String ip) {}
    public void delPlayer(String name) {}
    public void setNODAttacker(int n) {}
    public void setNODDefender(int n) {}
    public void sendDebug(String a) {}
    public void showMessageDialog(String a) {}
*/
}
