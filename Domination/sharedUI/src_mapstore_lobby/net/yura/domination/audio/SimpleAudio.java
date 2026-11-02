package net.yura.domination.audio;

import net.yura.mobile.gui.Application;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

public class SimpleAudio implements AudioSystem, ThreadFactory, PlayerListener {

    private static final Logger LOGGER = Logger.getLogger(SimpleAudio.class.getName());

    private Map<String, Player> currentMusicPlayers = new HashMap(); // filename -> player (ONLY for music)
    private Map<Player, String> playerToFilename = new WeakHashMap(); // player -> filename (ALL sounds)

    private int consecutiveStartErrors;
    private boolean fatalAudioSystemError;

    /**
     * we need a single thread for starting and stopping music
     * otherwise if one thread starts it and another thread stops it
     * the stop may never happen as it may never find the player.
     *
     * We need to create our own instance so we have access to the BlockingQueue
     */
    //private final Executor singleThread = Executors.newSingleThreadExecutor(this);
    private final ThreadPoolExecutor musicThread = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), this);

    private final ThreadPoolExecutor soundThread = new ThreadPoolExecutor(1, 1,
            0L,TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(), this);

    @Override
    public Thread newThread(Runnable r) {
        Thread th = Executors.defaultThreadFactory().newThread(r);
        th.setName("SimpleAudioStartStopThread");
        return th;
    }

    private Player getPlayer(String fileName) throws IOException, MediaException {

        // TODO we need some sort of cache, as each time java seems to load it from scratch
        // i guess it makes sense as the file or url destination may have changed from last time
        // so we need to keep the last X audio samples used

        //Player player = Manager.createPlayer(RiskUtil.openStream(fileName), "audio/basic");
        Player player = Manager.createPlayer("file:///android_asset/" + fileName);

        // we sometimes get very strange errors on android
        // we want to track if this is for specific files so we keep the names here
        playerToFilename.put(player, fileName);

        return player;
    }

    /**
     * we add a listener ONLY when this is NOT a looping sound, so we can clean up when it finishes.
     */
    @Override
    public void playerUpdate(Player player, String s, Object o) {
        if (PlayerListener.END_OF_MEDIA.equals(s)) {
            player.close();
        }
        else if (PlayerListener.ERROR.equals(s)) {
            Level level = Level.WARNING;
            // on android we get a loads of "100 0" (MediaPlayer.MEDIA_ERROR_SERVER_DIED) errors, we want to just ignore them all
            if (Application.getPlatform() == Application.PLATFORM_ANDROID && o instanceof String && ((String)o).matches("^100 -?\\d+$")) {
                level = Level.INFO;
            }
            LOGGER.log(level, "player error " + playerToFilename.get(player) + " " + player + " " + o);
        }
    }

    public void play(final String fileName) {
        if (fatalAudioSystemError) return;

        purge(soundThread.getQueue(), fileName);

        // on very very old computers even playing a short sound clip is very slow
        soundThread.execute(new Runnable() {
            @Override
            public void run() {
                Player player = null;
                try {
                    player = getPlayer(fileName);
                    player.addPlayerListener(SimpleAudio.this);
                    player.start(); // can throw oom
                    consecutiveStartErrors = 0;
                }
                catch (Exception ex) {
                    startError(fileName, player, ex);
                }
                catch (Error oom) { // OutOfMemoryError and NoClassDefFoundError
                    startError(fileName, player, oom);
                }
            }

            @Override
            public String toString() {
                return fileName;
            }
        });
    }

    /**
     * Only 1 music file with this name can play at a time
     */
    @Override
    public void start(final String fileName) {
        if (fatalAudioSystemError) return;

        purge(musicThread.getQueue(), fileName);

        musicThread.execute(new Runnable() {
            @Override
            public void run() {
                Player player = null;
                try {
                    player = getPlayer(fileName);
                    player.setLoopCount(-1);
                    currentMusicPlayers.put(fileName, player);
                    player.start(); // can throw oom
                    consecutiveStartErrors = 0;
                }
                catch (Exception ex) {
                    startError(fileName, player, ex);
                }
                catch (Error oom) { // OutOfMemoryError and NoClassDefFoundError
                    startError(fileName, player, oom);
                }
            }
            @Override
            public String toString() {
                return fileName;
            }
        });
    }
    
    private void startError(String fileName, Player player, Throwable ex) {
        consecutiveStartErrors++;
        if (ex instanceof Error) { // OutOfMemoryError and NoClassDefFoundError
            fatalAudioSystemError = true;
        }
        if (consecutiveStartErrors > 10) {
            // we can get some very random errors, maybe the computer has no sound card
            // java.lang.IllegalArgumentException: No line matching interface Clip supporting format PCM_SIGNED unknown sample rate, 16 bit, stereo, 4 bytes/frame, big-endian is supported. 
            fatalAudioSystemError = true;
        }
        // sometimes we try everything and we just cant play a sound
        // java.lang.IllegalArgumentException: Mixer not supported: null
        // on android: java.io.IOException: Prepare failed.: status=0x64 (MediaPlayer.MEDIA_ERROR_SERVER_DIED)
        LOGGER.log(Level.INFO, "unable to play " + fileName, ex);
        try {
            currentMusicPlayers.remove(fileName);
            if (player != null) {
                player.close();
            }
        }
        catch (Throwable th) {}
    }

    @Override
    public void stop(final String audioFile) {
        try {
            musicThread.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Player player = currentMusicPlayers.remove(audioFile);
                        if (player != null) {
                            // this is not needed as we only start and stop from a single thread
                            //if (player.getState() != Player.STARTED) {
                            //    LOGGER.log(Level.INFO, "player not started yet, will stop with listener: " + audioFile);
                            //    player.addPlayerListener(SimpleAudio.this);
                            //}
                            player.stop();
                            player.close();
                        }
                        else {
                            // this really should never happen
                            LOGGER.log(Level.INFO, "unable to stop, not found: " + audioFile);
                        }
                    }
                    catch (Exception ex) {
                        LOGGER.log(Level.WARNING, "unable to stop " + audioFile, ex);
                    }
                }
            });
        }
        // in case the app is run without any libs, and is unable to find the classes needed to play sound
        catch (Error error) {}
    }

    private void purge(final BlockingQueue<Runnable> q, String name) {
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if (name.equals(r.toString())){
                    it.remove();
                }
            }
        } catch (ConcurrentModificationException fallThrough) {
            // Take slow path if we encounter interference during traversal.
            // Make copy for traversal and call remove for cancelled entries.
            // The slow path is more likely to be O(N*N).
            for (Object r : q.toArray())
                if (name.equals(r.toString())) {
                    q.remove(r);
                }
        }
    }

    /**
     * @see Executors.FinalizableDelegatedExecutorService#finalize()
     */
    protected void finalize() {
        // TODO this will be removed from newer java
        musicThread.shutdown();
        soundThread.shutdown();
    }
}
