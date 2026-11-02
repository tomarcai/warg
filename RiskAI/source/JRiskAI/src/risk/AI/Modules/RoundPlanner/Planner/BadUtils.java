package risk.AI.Modules.RoundPlanner.Planner;

import net.yura.domination.engine.core.Country;

/**
 * very bad utils, do NOT use!!!!
 */
@Deprecated
public class BadUtils {

    @Deprecated
    static void setArmies(Country country, int newArmies) {
        int oldArmies = country.getArmies();
        if (newArmies > oldArmies) {
            country.addArmies(newArmies - oldArmies);
        }
        else {
            country.removeArmies(oldArmies - newArmies);
        }
    }
}
