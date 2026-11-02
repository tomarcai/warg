package risk.AI.Data_Structures.Module_Output;

import net.yura.domination.engine.core.Country;

public class O_RP_FortifyTransfer {
	
	// content[] is a size 3 array of integers. content[0] is the "moveFrom" country-integer, content[1] is the "moveTo" country-integer and content[2] is the number of armies.
	Country fromCountry;
	Country toCountry;
	int armies;
	
	public Country getFromCountry() {
	    return fromCountry;
	}	
	
	public Country getToCountry() {
	    return toCountry;
	}	
	
	public int getNumOfArmies() {
	    return armies;
	}	
	
	public void setNumOfArmies(int armies) {
	    this.armies = armies;
	}
	
	public void setFromCountry(Country fromCountry) {
	    this.fromCountry = fromCountry;
	}
	
	public void setToCountry(Country toCountry) {
	    this.toCountry = toCountry;
	}
	
}