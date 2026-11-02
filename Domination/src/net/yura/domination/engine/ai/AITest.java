// Yura Mamyrin

package net.yura.domination.engine.ai;

import java.util.ArrayList;
import java.util.List;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;

/**
 * AI that attacks as much as it can all the time
 * @author Yura Mamyrin
 */
public class AITest extends AISubmissive {

    public int getType() {
        return 5;
    }

    public String getCommand() {
        return "test";
    }

    public static class Attack {
	public final Country source;
	public final Country destination;

	public Attack(Country s, Country d){
	    source=s;
	    destination=d;
	}
	public String toString(){
	    if (source == null || destination == null) { return ""; }
	    return "attack " + source.getColor() + " " + destination.getColor();
	}
    }

    public String getPlaceArmies() {
		if ( game.NoEmptyCountries()==false ) {
		    return "autoplace";
		}
		else {
		    List t = player.getTerritoriesOwned();
		    String name=null;
			name = findAttackableTerritory(player);
			if ( name == null ) {
			return "placearmies " + ((Country)t.get(0)).getColor() +" "+player.getExtraArmies()  ;
		    }

		    if (game.getSetupDone() ) {
			return "placearmies " + name +" "+player.getExtraArmies() ;
		    }

		    return "placearmies " + name +" 1";

		}
    }

    public String getAttack() {
	//Vector t = player.getTerritoriesOwned();
	List outputs = new ArrayList();
	Attack move;

	/*  // Extract method: findAttackableNeighbors()
	Vector n;
	for (int a=0; a< t.size() ; a++) {
	    if ( ((Country)t.elementAt(a)).getArmies() > 1 ) {
		n = ((Country)t.elementAt(a)).getNeighbours();
		for (int b=0; b< n.size() ; b++) {
		    if ( ((Country)n.elementAt(b)).getOwner() != player ) {
			outputs.add( "attack " + ((Country)t.elementAt(a)).getColor() + " " + ((Country)n.elementAt(b)).getColor() );
		    }
		}
	    }
	}  */
	outputs = findAttackableNeighbors(player, 0);
	if (outputs.size() > 0) {
		move = (Attack) outputs.get( (int)Math.round(Math.random() * (outputs.size()-1) ) );
		//System.out.println(player.getName() + ": "+ move.toString());    //TESTING
		return move.toString();
		//return (String)outputs.elementAt( (int)Math.round(Math.random() * (outputs.size()-1) ) );
	}
	return "endattack";
    }

    public String getRoll() {
	    int n=((Country)game.getAttacker()).getArmies() - 1;
	    if (n > 3) {
		    return "roll "+3;
	    }
	    return "roll "+n;
    }

    /******************
     * Helper Methods *
     ******************/

    /**
     * (moved here from AICrap/AISubmissive)
     * Attempts to find the first territory that can be used to attack from
     * @param p player object
     * @return Sring name is a move to attack from any space they can (that has less than 500 armies)
     * else returns null
     */
    public static String findAttackableTerritory(Player p) {
    	List countries = p.getTerritoriesOwned();

    	for (int i = 0; i < countries.size(); i++) {
    		List neighbors = ((Country)countries.get(i)).getNeighbours();
    		for (int j = 0; j < neighbors.size(); j++) {
    			if (((Country)neighbors.get(j)).getOwner() != p) {
                                // never launch an attack from own capital
    				if (p.getCapital() == null || (p.getCapital() != null && ((Country)countries.get(i)).getColor() != p.getCapital().getColor())) {
    					return String.valueOf(((Country)countries.get(i)).getColor());
                                }
    			}
    		}
    	}

    	return null;
    }

    /************
     * @name findAttackableNeighbors
     * @param player the player who's TerritoriesOwned we will use to attack
     * @param ratio - threshold of attack to defence armies to filter out
     * @return a Vector of possible attacks for a given list of territories
     * 	where the ratio of source/target armies is above ratio
     **************/
    public static List findAttackableNeighbors(Player player, double ratio) {
        
        List t = player.getTerritoriesOwned();
	List output = new ArrayList();
	List n;
    	Country source,target;
	if (ratio<0) { ratio = 0;}
	for (int a = 0; a < t.size(); a++) {
	    source=(Country)t.get(a);
	    if ( source.getOwner() == player && source.getArmies() > 1 ) {
		n = source.getNeighbours();
		for (int b = 0; b < n.size(); b++) {
		    target=(Country)n.get(b);
		    if ( target.getOwner() != player &&
			( ((double)source.getArmies()) / ((double)target.getArmies()) > ratio)
		      	) {     // simplify logic
			//output.add( "attack " + source.getColor() + " " + target.getColor() );
			output.add(new Attack(source,target));
		    }
		}
	    }
	}
	return output;
    }
}
