package net.yura.domination.engine;

/**
 * @author Yura Mamyrin
 */
public interface OnlineRisk {

    /**
     * Send command from the actual user to everyone on the network
     * This is ONLY called when I am the current player (myAddress == currentlyPlayer.address)
     */
    public void sendUserCommand(String command);

    /**
     * send a game command (e.g. CARD, DICE, etc) out to everyone
     * This is ONLY called when I am the current player (myAddress == currentlyPlayer.address)
     * also used for auto-defend roll command, that happens on the lobby server, or when address match
     */
    public void sendGameCommand(String command);

    /**
     * tells the multiplayer client that the game has been closed
     */
    public void closeGame();

    /**
     * do any extra things such as update address when a player is renamed
     */
    public void playerRenamed(String oldName, String newName, String newAddress, int newType);
}
