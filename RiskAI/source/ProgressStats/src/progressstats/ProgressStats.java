package progressstats;

public class ProgressStats {
	
	public static String getText(int currentItemIndex, int numberOfItems) {
		String percent = String.valueOf(Math.round(100 * ((float)currentItemIndex + 1)/ numberOfItems));
		if ((percent.equals("100")) && (currentItemIndex != numberOfItems - 1)) {
			percent = "99";
		}
		return "[" + percent + "% done, " + (numberOfItems - 1 - currentItemIndex) + " left]";
	}
}
