// Yura Mamyrin, Group D

package net.yura.domination.engine.core;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

/**
 * <p> Risk Player </p>
 * @author Yura Mamyrin
 */
public class Player implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * does not received reinforcements
     */
    public final static int PLAYER_NEUTRAL    = -1;
    public final static int PLAYER_HUMAN      = 0;

    /**
     * does receive reinforcements, but never attacks
     * @see net.yura.domination.engine.ai.AISubmissive
     */
    public final static int PLAYER_AI_CRAP    = 3;

    public final static int PLAYER_AI_EASY    = 1;
    public final static int PLAYER_AI_AVERAGE = 4;
    public final static int PLAYER_AI_HARD    = 2;

    private String name;

    /**
        playerType (come with the game):
       -1 - neutral
        0 - human
        1 - AI (Easy)
        2 - AI (Hard)
        3 - AI (Crap)
        4 - AI (Average)
        5 - AI (Test) {@link net.yura.domination.engine.ai.AITest}

        need to be downloaded separately
        6 - AI (Old Hard AI) {@link net.yura.domination.engine.ai.old.AIHardOld}
        7 - AI framework custom
        8 - AI framework best
        9 - AI framework scripted
     */
    private int type;
    
    private int color;
    private int extraArmies;
    private final Vector cardsOwned;
    private final Vector territoriesOwned;
    private final Vector playersEliminated;
    private Country capital;
    private Mission mission;
    private String address;

    private final Vector Statistics;
    protected Statistic currentStatistic;

    private boolean autoendgo;
    private boolean autodefend;
    // private int attackUntill;

    /**
     * Creates a new Player
     * @param t The type of the player (human or AI)
     * @param n The name of the player
     * @param c The color of the player
     * @param a THe address of the player on the current computer
     */
    public Player(int t, String n, int c, String a) {
	type = t;
	name = n;
	color = c;
        territoriesOwned = new Vector();
        cardsOwned = new Vector();
	playersEliminated = new Vector();
        extraArmies = 0;
	address=a;

	autoendgo=true;

	// well everyone wants this really, i hope
	autodefend=true;

	Statistics = new Vector();
	currentStatistic = new Statistic();
	Statistics.add(currentStatistic);
    }

    public void rename(String na) {
	name = na;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String toString() {
	return name;
    }

    public void nextTurn() {
	currentStatistic = new Statistic();
	Statistics.add(currentStatistic);
    }

    /**
     * If there is no value for a statistic for a given turn of the game then {@link Double#NaN} will be used.
     */
    public double[] getStatistics(StatType type) {
	double[] statistics = new double[ Statistics.size() ];
	for (int c=0; c< statistics.length ; c++) {
	    statistics[c] = ((Statistic)Statistics.elementAt(c)).get(type);
	}
	return statistics;
    }

    public List<Statistic> getStatistics() {
	return Statistics;
    }

    public int getNoArmies() {
	int n = 0;

        // this can get changed from the game thread and read from the UI thread at the same time
        synchronized(territoriesOwned) {
            for (int c = territoriesOwned.size() - 1; c >= 0; c--) {
                // add new armies for the Continents Owned
                n = n + ((Country)territoriesOwned.get(c)).getArmies();
            }
        }

	return n;
    }

    /**
     * Gets the address of the player on the current computer
     * @return String The name of the player
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets the name of the player
     * @return String The name of the player
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the color of the player
     * @return Color Returns the color of the player
     */
    public int getColor() {
	return color;
    }

    /**
     * Gets the number of extra armies that the player has
     * @return int Returns the number of extra armies that the player has
     */
    public int getExtraArmies() {
	return extraArmies;
    }

    /**
     * Adds the number of armies to the player's extra armies
     * @param n The number of armies you want to add
     */
    public void addArmies(int n) {
	extraArmies = extraArmies + n;
    }

    /**
     * Minus the number of armies from the player's extra armies
     * @param n The number of armies you want to minus
     */
    public void loseExtraArmy(int n) {
        extraArmies = extraArmies - n;
    }

    /**
     * Gains a card
     * @param card The card you have gained
     */
    public void giveCard(Card card) {
        cardsOwned.add(card);
    }

    /**
     * Gets the cards that the player owns
     * @return Vector Returns the vector of all the cards the player owns
     */
    public List getCards() {
        return cardsOwned;
    }

    /**
     * Gets the captical that the player chose
     * @return Country Returns the player's capital country
     */
    public Country getCapital() {
	return capital;
    }

    /**
     * Sets the player's capital country
     * @param c The new capital country
     */
    public void setCapital(Country c) {
	capital=c;
    }

    /**
     * Gets the player's mission
     * @return Mission Returns player's mission
     */
    public Mission getMission() {
	return mission;
    }

    /**
     * Sets the player's mission
     * @param m The new mission
     */
    public void setMission(Mission m) {
	mission=m;
    }

    /**
     * Takes the player's first card
     * @return Card The card that is taken
     */
    public Card takeCard() {
	Card c=(Card)cardsOwned.firstElement();
	cardsOwned.removeElementAt(0);
        return c;
    }

    /**
     * Trading in the cards, adds 2 extra armies to the first country that it owns on the cards and removing the cards from the player
     * @param card1 First card
     * @param card2 Second card
     * @param card3 Third card
     */
    public int tradeInCards(Card card1, Card card2, Card card3, int numberOfArmiesForFirstCountry) {

        boolean country1Owned = territoriesOwned.contains(card1.getCountry());
        boolean country2Owned = territoriesOwned.contains(card2.getCountry());
        boolean country3Owned = territoriesOwned.contains(card3.getCountry());

        if (numberOfArmiesForFirstCountry != 0) {
            // check if you should get extra armies on the territoriesOwned
            if (country1Owned) {
                card1.getCountry().addArmies(numberOfArmiesForFirstCountry);
                currentStatistic.addReinforcements(numberOfArmiesForFirstCountry);
            }
            else if (country2Owned) {
                card2.getCountry().addArmies(numberOfArmiesForFirstCountry);
                currentStatistic.addReinforcements(numberOfArmiesForFirstCountry);
            }
            else if (country3Owned) {
                card3.getCountry().addArmies(numberOfArmiesForFirstCountry);
                currentStatistic.addReinforcements(numberOfArmiesForFirstCountry);
            }
        }

	if (!cardsOwned.remove(card1) | !cardsOwned.remove(card2) | !cardsOwned.remove(card3)) {
            throw new IllegalArgumentException("player does not have card");
        }
	cardsOwned.trimToSize();

        return (country1Owned ? 1 : 0) + (country2Owned ? 1 : 0) + (country3Owned ? 1 : 0);
    }


    /**
     * Gets the countries that the player owns
     * @return Vector Returns a vector of all the countries the player owns
     */
    public List getTerritoriesOwned() {
        return territoriesOwned;
    }

    /**
     * Gets the number of countries that the player owns
     * @return int Returns the number of countries owned
     */
    public int getNoTerritoriesOwned() {
        return territoriesOwned.size();
    }

    public void newCountry(Country newCountry) {
        territoriesOwned.addElement(newCountry);
    }

    /**
     * Removes a country from the player
     * @param lessCountry The country you want to remove
     */
    public void lostCountry(Country lessCountry){
	territoriesOwned.remove(lessCountry);
	territoriesOwned.trimToSize();
    }

    /**
     * Eliminates a player
     * @param p The player to be eliminated
     */
    public void addPlayersEliminated(Player p) {
	playersEliminated.add(p);
    }

    /**
     * Gets all the players that are eliminated
     * @return Vector Returns all the eliminated players
     */
    public List getPlayersEliminated() {
	return playersEliminated;
    }

    /**
     * Gets the type of player
     * @return int Returns the type of player
     */
    public int getType() {
	return type;
    }

    /**
     * Sets the type of player
     * @param t The type of player
     */
    public void setType(int t) {
	type =t;
    }

    /**
     * Sets the address of the current player
     * @param a String The address of the computer
     */
    public void setAddress(String a) {
	address =a;
    }

    public void setAutoEndGo(boolean b) {
	autoendgo=b;
    }

    public boolean getAutoEndGo() {
	return autoendgo;
    }

    public void setAutoDefend(boolean b) {
	autodefend=b;
    }

    public boolean getAutoDefend() {
	return autodefend;
    }

    public boolean isAlive() {
        return getExtraArmies() > 0 || getNoTerritoriesOwned() > 0;
    }
}
