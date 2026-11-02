package net.yura.domination.engine.translation;

import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import net.yura.domination.engine.RiskUtil;

/**
 * Translation services for maps:
 * Countries and continents
 *
 * @author Christian Weiske <cweiske@cweiske.de>
 */
public class MapTranslator {

	private ResourceBundle MapResb = null;
	private ResourceBundle CardsResb = null;

	/**
	 * sets the currently used map
	 * Tries to find a matching properties file + resource bundle
	 *
	 * @param strFile	The map file absolute path
	 */
	public void setMap(String strFile) {

                if (strFile==null) {
                    MapResb = null;
                    return;
                }
            
		//remove the extension
		String strName = strFile.substring( 0, strFile.lastIndexOf( '.'));

		//now get the locale and try to the strName + "_" + 2-letter-code
		strFile = strName + "_" + TranslationBundle.getBundle().getLocale().getLanguage() + ".properties";

		try {
                                //file exists, use it!
				MapResb = new PropertyResourceBundle( RiskUtil.openMapStream(strFile) );
		}
		catch (Exception ioe) {
			try {
				MapResb = RiskUtil.getResourceBundle(MapTranslator.class, "DefaultMaps", TranslationBundle.getBundle().getLocale());
			}
                        catch (MissingResourceException e) {
				//ok, we don't have one
				MapResb = null;
			}
		}
	}

	/**
	 * returns the translation for the given string if any
	 * If there is no translation, the string is simply returned
	 */
	public String getTranslatedMapName(String strOriginal) {
		if (MapResb == null) {
			return strOriginal;
		}

		String strReturn;
		try {
			strReturn = MapResb.getString(strOriginal);
		}
                catch (MissingResourceException e) {
			//no translation for the string
			strReturn = strOriginal;
		}
		return strReturn;
	}

	/**
	 * sets the currently used map
	 * Tries to find a matching properties file + resource bundle
	 *
	 * @param INstrFile	The map file absolute path
	 */
	public void setCards(String INstrFile) {

		//remove the extension
		String strName = INstrFile.substring( 0, INstrFile.lastIndexOf( '.'));

		//now get the locale and try to the strName + "_" + 2-letter-code
		String strFile = strName + "_" + TranslationBundle.getBundle().getLocale().getLanguage() + ".properties";

		try {
                        // if file exists, use it!
			CardsResb = new PropertyResourceBundle(RiskUtil.openMapStream(strFile));
		}
                catch (Exception ioe) {
		    if (INstrFile.equals("risk.cards")) { // only use this with the default cards file
			//load default cards translation bundle
			try {
				CardsResb = RiskUtil.getResourceBundle(MapTranslator.class, "DefaultCards", TranslationBundle.getBundle().getLocale());
			}
                        catch (MissingResourceException e) {
				CardsResb = null;
			}
		    }
                    else {
                        //ok, we don't have one
                        CardsResb = null;
                    }
		}
	}

	/**
	 * returns the translation for the given missionId if any
	 * If there is no translation, null is returned
	 */
	public String getTranslatedMissionName(String riskMissionId) {
		if (CardsResb == null) {
			return null;
		}

		try {
			return CardsResb.getString(riskMissionId);
		}
                catch (MissingResourceException e) {
			//no translation for the string
			return null;
		}
	}
}
