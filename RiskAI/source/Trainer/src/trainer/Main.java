package trainer;

import trainer.trainers.*;

public class Main {
	
	/** Creates a new instance of Main */
	public Main() {
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		String aiDataLoadDirectory, aiType, modulename, filename;
		if (args.length >= 3) {
			modulename = args[0];
			aiType = args[1];
			aiDataLoadDirectory = args[2];
		} else{
			System.out.println("Usage: java -jar Trainer.jar \"modulename\" \"technique\" \"ai-data-directory\"");
			System.out.println("");
			System.out.println("  modulename: The module to train. ");
			System.out.println("    -Possibilities: \"ig_continent\", ");
			System.out.println("    	            \"ig_winning\", ");
			System.out.println("    	            \"ig_mission\", ");
			System.out.println("    	            \"ig_nextmove\", ");
			System.out.println("    	            \"masterprioritizer\", ");
			System.out.println("    	            \"initialplacement\", ");
			System.out.println("    	            \"rp_ap_priority\", ");
			System.out.println("    	            \"rp_scoreattackplan\", ");
			System.out.println("    	            \"rp_scoreattackplan\", ");
			System.out.println("    	            \"rp_de_cost\" or ");
			System.out.println("    	            \"rp_de_priority\".");
			System.out.println("  technique: The AI technique to convert to. ");
			System.out.println("    -Possibilities: \"nn\", \"dt\", \"nb\" or \"nn_wo\"");
			System.out.println("  ai-data-dir: The directory location where the directory \"ai-data\" is located. ");
			System.out.println("    -Example: \"./\"");
			aiDataLoadDirectory = "";
			aiType = "";
			modulename = "";
			System.exit(0);
			modulename = "";
			aiType = "";
			aiDataLoadDirectory = "";
		}
		if (!aiDataLoadDirectory.endsWith("/")) {
			aiDataLoadDirectory += "/";
		}
		aiDataLoadDirectory += "ai-data/training_examples_converted/" + modulename + "/";
		if (args.length >= 4) {
			filename = args[3]; // Filename to train from
		} else {
			filename = ""; // If no filename is given, training will be made using all files in the directory
		}
		if (aiType.equals("nn") || aiType.equals("all")) {
			NNTrainer trainer = new NNTrainer();
			trainer.train(aiDataLoadDirectory, filename);
		}
		if (aiType.equals("nn_wo") || aiType.equals("all")) {
			NNWOTrainer trainer = new NNWOTrainer();
			trainer.train(aiDataLoadDirectory, filename);
		}
		if (aiType.equals("dt") || aiType.equals("all")) {
			WekaTrainer trainer = new WekaTrainer("dt");
			trainer.train(aiDataLoadDirectory, filename);
			/*
			// Null-value test
			String arff = "";
			arff += "@relation weather.symbolic\n";
			arff += "\n";
			arff += "@attribute outlook {sunny, overcast, rainy}\n";
			arff += "@attribute temperature {hot, mild, cool}\n";
			arff += "@attribute humidity {high, normal}\n";
			arff += "@attribute windy {TRUE, FALSE}\n";
			arff += "@attribute play {yes, no}\n";
			arff += "\n";
			arff += "@data\n";
			arff += "overcast,cool,high,TRUE,no\n"; // null = -1
			//arff += "overcast,hot,high,TRUE,no\n"; // yes = 0
			//arff += "overcast,mild,high,TRUE,no\n"; // no = 1
			System.out.println("Classification: " + trainer.query(arff.toString()));
			*/
		}
		if (aiType.equals("nb") || aiType.equals("all")) {
			WekaTrainer trainer = new WekaTrainer("nb");
			trainer.train(aiDataLoadDirectory, filename);
		}
	}
	
}
