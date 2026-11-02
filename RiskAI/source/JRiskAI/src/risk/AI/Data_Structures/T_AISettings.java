package risk.AI.Data_Structures;

import java.io.*;
import java.util.logging.Logger;

/**
 * Stores those of the AI's variables that can be tweaked
 */
public class T_AISettings {
	
	/** The MP goal "18 with 2 on each" is considered important if above this value. In an attack plan, at least two armies will be left behind if this is the case. */
	public float importanceThreshold_18w2 = 0.8f;
	/** The threshold for when the AI will attack a territory - commonly */
	public float attackProbabilityOfWinning = 0.35f;
	/** The threshold for when the AI will attack a territory - if it is the last territory in a continent */
	public float attackProbabilityOfWinning_lastTerritory = 0.25f;
	/** The threshold for when the AI will attack a territory - if the AI is close to winning */
	public float attackProbabilityOfWinning_closeToWinning = 0.05f;
	/** The threshold for when the AI will attack a territory - if the AI attacks with the sole purpose of getting a Risk card */
	public float attackProbabilityOfWinning_riskCard = 0.40f;
	/** The threshold for when the AI thinks it is going to win in this round */
	public float attackProbabilityOfWinning_threshold = 0.8f;
	/** How much must an attack plan exceed it's estimated cost, before it is revised */
	public float attackEstimatedCostExceed = 0.20f;
	/** An attack plan is discarded if its priority is lower than this */
	public float discardAttackPlanPriority = 0.01f;
	/** The number of rounds an IG such as "How close to winning" should predict its estimates */
	public int roundsToPredict = 5;
	/** IG "How close to winning" uses this factor to take other player's armies into account when computing the estimate for "Kill Player" missions. The number of armies that neither belong the player we are looking at, nor the target player, is multiplied with this factor and is then thought to assist the target player. */
	public float otherArmiesFactor = 0.2f;
	/** Should the framework save training examples or not. */
	public boolean saveTrainingExample = false;
	/** Should the framework save a simple winner file. */
	public boolean saveWinnerFile = false;
	/** Should the framework save game statistics. */
	public boolean saveFullGameStats = false;
	/** The directory where the trained AI models for the learning AI techniques are stored */
	public String trainedAIModelsDirectory = "ai-data/trained_models/";
	/***************************************************************/
	/*                                                             */
	/* IMPORTANT: When adding new variables, increase this number: */
	/*                                                             */
	private final static int NUMBER_OF_SETTINGS = 14;
	/*                                                             */
	/*                                                             */
	/***************************************************************/
	private int numberOfSettingsLoaded;
	
	/**
	 * Creates a new instance of T_AISettings and loads the settings from a file
	 * @param filename The settings file
	 */
	public T_AISettings(String filename) {
		loadFromFile(filename);
	}
	
	/**
	 * Loads the settings from a file
	 * @param filename The settings file
	 */
	public void loadFromFile(String filename) {
		numberOfSettingsLoaded = 0;
		try {
			FileReader reader = new FileReader(filename);
			BufferedReader in = new BufferedReader(reader);
			String line, varName;
			line = in.readLine();
			while (line != null) {
				varName = line.substring(0, line.indexOf("="));
				if (varName.equals("importanceThreshold_18w2")) {
					importanceThreshold_18w2 = readFloat(line, varName);
				} else if (varName.equals("attackProbabilityOfWinning")) {
					attackProbabilityOfWinning = readFloat(line, varName);
				} else if (varName.equals("attackProbabilityOfWinning_lastTerritory")) {
					attackProbabilityOfWinning_lastTerritory = readFloat(line, varName);
				} else if (varName.equals("attackProbabilityOfWinning_closeToWinning")) {
					attackProbabilityOfWinning_closeToWinning = readFloat(line, varName);
				} else if (varName.equals("attackProbabilityOfWinning_threshold")) {
					attackProbabilityOfWinning_threshold = readFloat(line, varName);
				} else if (varName.equals("attackProbabilityOfWinning_riskCard")) {
					attackProbabilityOfWinning_riskCard = readFloat(line, varName);
				} else if (varName.equals("attackEstimatedCostExceed")) {
					attackEstimatedCostExceed = readFloat(line, varName);
				} else if (varName.equals("discardAttackPlanPriority")) {
					discardAttackPlanPriority = readFloat(line, varName);
				} else if (varName.equals("roundsToPredict")) {
					roundsToPredict = readInt(line, varName);
				} else if (varName.equals("otherArmiesFactor")) {
					otherArmiesFactor = readFloat(line, varName);
				} else if (varName.equals("saveTrainingExample")) {
					saveTrainingExample = readBoolean(line, varName);
				} else if (varName.equals("saveWinnerFile")) {
					saveWinnerFile = readBoolean(line, varName);
				} else if (varName.equals("saveFullGameStats")) {
					saveFullGameStats = readBoolean(line, varName);
				} else if (varName.equals("trainedAIModelsDirectory")) {
					trainedAIModelsDirectory = readString(line, varName);
				}
				line = in.readLine();
			}
			in.close();
			reader.close();
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			//ex.printStackTrace();
			Logger.getLogger(T_AISettings.class.getName()).info("unable to load: " + filename);
		}
		// Save settings file if some of the settings were missing in the file
		if (numberOfSettingsLoaded != NUMBER_OF_SETTINGS) {
			this.saveToFile(filename);
		}
	}
	
	private String readString(String line, String varName) {
		numberOfSettingsLoaded++;
		return line.substring(varName.length() + 1);
	}
	
	private float readFloat(String line, String varName) {
		numberOfSettingsLoaded++;
		return Float.parseFloat(readString(line, varName));
	}
	
	private boolean readBoolean(String line, String varName) {
		numberOfSettingsLoaded++;
		return Boolean.parseBoolean(readString(line, varName));
	}
	
	private int readInt(String line, String varName) {
		numberOfSettingsLoaded++;
		return Integer.parseInt(readString(line, varName));
	}
	
	/**
	 * Saves the settings to a file
	 * @param filename The settings file
	 */
	public void saveToFile(String filename) {
		try {
			FileWriter writer = new FileWriter(filename);
			BufferedWriter out = new BufferedWriter(writer);
			writeLine(out, "importanceThreshold_18w2=" + importanceThreshold_18w2);
			writeLine(out, "attackProbabilityOfWinning=" + attackProbabilityOfWinning);
			writeLine(out, "attackProbabilityOfWinning_lastTerritory=" + attackProbabilityOfWinning_lastTerritory);
			writeLine(out, "attackProbabilityOfWinning_closeToWinning=" + attackProbabilityOfWinning_closeToWinning);
			writeLine(out, "attackProbabilityOfWinning_threshold=" + attackProbabilityOfWinning_threshold);
			writeLine(out, "attackProbabilityOfWinning_riskCard=" + attackProbabilityOfWinning_riskCard);
			writeLine(out, "attackEstimatedCostExceed=" + attackEstimatedCostExceed);
			writeLine(out, "discardAttackPlanPriority=" + discardAttackPlanPriority);
			writeLine(out, "roundsToPredict=" + roundsToPredict);
			writeLine(out, "otherArmiesFactor=" + otherArmiesFactor);
			writeLine(out, "saveTrainingExample=" + saveTrainingExample);
			writeLine(out, "saveWinnerFile=" + saveWinnerFile);
			writeLine(out, "saveFullGameStats=" + saveFullGameStats);
			writeLine(out, "trainedAIModelsDirectory=" + trainedAIModelsDirectory);
			out.close();
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeLine(BufferedWriter out, String line) {
		try {
			out.write(line);
			out.newLine();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
}
