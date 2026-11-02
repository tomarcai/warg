// Yura Mamyrin, Group D

package net.yura.domination.guishared;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.filechooser.FileFilter;
import net.yura.domination.engine.JavaCompatUtil;
import net.yura.domination.engine.translation.TranslationBundle;

/**
 * <p> Risk File Filter </p>
 * @author Yura Mamyrin
 */
public class RiskFileFilter extends FileFilter implements FilenameFilter {

	public final static String RISK_MAP_FILES		="map";
	public final static String RISK_CARDS_FILES		="cards";
	public final static String RISK_SAVE_FILES		="save";

        /**
         * a user created list of commands for the parser
         */
	public final static String RISK_SCRIPT_FILES		="risk";

        /**
         * list of engine mutation commands
         */
	public final static String RISK_LOG_FILES		="log";
	//public final static String RISK_PROPERTIES_FILES	="properties";

	private final String[] extensions;

	public RiskFileFilter(String... ext) {
		extensions = ext;
	}

	public boolean accept(File f) {

		if (f.isDirectory()) {
			return true;
		}

		String ext = getExtension(f);

		if (ext != null) {
                    for (String e : extensions) {
                        if (ext.equals(e)) {
                            return true;
                        }
                    }
		}

		return false;
	}

	/**
	 * returns the description for a file extension
	 */
	public String getDescription() {
		java.util.ResourceBundle resb = TranslationBundle.getBundle();

		String name;

                if (extensions[0].equals(RISK_MAP_FILES)) {
                        name = resb.getString("riskfilefilter.map");
                }
                else if (extensions[0].equals(RISK_CARDS_FILES)) {
                        name = resb.getString("riskfilefilter.cards");
                }
                else if (extensions[0].equals(RISK_SAVE_FILES)) {
                        name = resb.getString("riskfilefilter.save");
                }
                else if (extensions[0].equals(RISK_SCRIPT_FILES)) {
                        name = resb.getString("riskfilefilter.script");
                }
                else if (extensions[0].equals(RISK_LOG_FILES)) {
                        name = resb.getString("riskfilefilter.log") ;
                }
                else {
                        name = resb.getString("riskfilefilter.files");
                }

                List types = new ArrayList();
                for (String e : extensions) {
                    types.add("*." + e);
                }
		return name + " (" + JavaCompatUtil.join(types, ", ") + ")";
	}

	/**
	* Get the extension of a file.
	*/
	public static String getExtension(File f) {
		String ext = null;
		String s = f.getName();
		int i = s.lastIndexOf('.');

		if (i > 0 &&  i < s.length() - 1) {
			ext = s.substring(i+1).toLowerCase();
		}
		return ext;
	}

	public boolean accept(File dir, String name) {
		return accept(new File(dir, name));
	}
}
