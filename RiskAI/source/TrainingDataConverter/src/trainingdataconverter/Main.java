package trainingdataconverter;

import java.io.IOException;
import trainingdataconverter.trainingdata.IG_ContinentData;
import trainingdataconverter.trainingdata.IG_MissionData;
import trainingdataconverter.trainingdata.IG_NextMoveData;
import trainingdataconverter.trainingdata.IG_WinningData;
import trainingdataconverter.trainingdata.InitialPlacementData;
import trainingdataconverter.trainingdata.MasterPrioritizerData;
import trainingdataconverter.trainingdata.RP_AP_PriorityData;
import trainingdataconverter.trainingdata.RP_De_CostData;
import trainingdataconverter.trainingdata.RP_De_PriorityData;
import trainingdataconverter.trainingdata.RP_ScoreAttackPlanData;
import trainingdataconverter.trainingdata.RP_ScoreMergedPlanData;
import trainingdataconverter.trainingdata.TrainingData;

public class Main {
    public Main() {
    }

    public static void main(String[] args) {
        String aiDataLoadDirectory;
        String convertType;
        String modulename;
        if (args.length == 3) {
            modulename = args[0];
            convertType = args[1];
            aiDataLoadDirectory = args[2];
        } else {
            System.out.println("Usage: java -jar TrainingDataConverter.jar \"modulename\" \"technique\" \"ai-data-directory\"");
            System.out.println("");
            System.out.println("  modulename: The module data to convert. ");
            System.out.println("    -Possibilities: \"ig_continent\", ");
            System.out.println("    \t            \"ig_winning\", ");
            System.out.println("    \t            \"ig_mission\", ");
            System.out.println("    \t            \"ig_nextmove\", ");
            System.out.println("    \t            \"masterprioritizer\", ");
            System.out.println("    \t            \"initialplacement\", ");
            System.out.println("    \t            \"rp_ap_priority\", ");
            System.out.println("    \t            \"rp_scoreattackplan\", ");
            System.out.println("    \t            \"rp_scoreattackplan\", ");
            System.out.println("    \t            \"rp_de_cost\" or ");
            System.out.println("    \t            \"rp_de_priority\".");
            System.out.println("  technique: The AI technique to convert to. ");
            System.out.println("    -Possibilities: \"nn\", \"dt\", \"nb\" or \"bn\" (dt and nb does the same)");
            System.out.println("  ai-data-dir: The directory location where the directory \"ai-data\" is located. ");
            System.out.println("    -Example: \"./\"");
            aiDataLoadDirectory = "";
            convertType = "";
            modulename = "";
            System.exit(0);
        }

        if (!aiDataLoadDirectory.endsWith("/")) {
            aiDataLoadDirectory = aiDataLoadDirectory + "/";
        }

        String aiDataSaveDirectory = aiDataLoadDirectory + "ai-data/training_examples_converted/";
        aiDataLoadDirectory = aiDataLoadDirectory + "ai-data/training_examples/modules/";
        TrainingData data = null;
        if (modulename.equals("ig_continent")) {
            aiDataLoadDirectory = aiDataLoadDirectory + modulename;
            data = new IG_ContinentData();
        } else if (modulename.equals("ig_winning")) {
            aiDataLoadDirectory = aiDataLoadDirectory + modulename;
            data = new IG_WinningData();
        } else if (modulename.equals("ig_mission")) {
            aiDataLoadDirectory = aiDataLoadDirectory + modulename;
            data = new IG_MissionData();
        } else if (modulename.equals("ig_nextmove")) {
            aiDataLoadDirectory = aiDataLoadDirectory + "master_prioritizer";
            data = new IG_NextMoveData();
        } else if (modulename.equals("master_prioritizer")) {
            aiDataLoadDirectory = aiDataLoadDirectory + modulename;
            data = new MasterPrioritizerData();
        } else if (modulename.equals("initialplacement")) {
            aiDataLoadDirectory = aiDataLoadDirectory + modulename;
            data = new InitialPlacementData();
        } else if (modulename.equals("rp_ap_priority")) {
            aiDataLoadDirectory = aiDataLoadDirectory + "attack_plan";
            data = new RP_AP_PriorityData();
        } else if (modulename.equals("rp_scoreattackplan")) {
            aiDataLoadDirectory = aiDataLoadDirectory + "attack_plan";
            data = new RP_ScoreAttackPlanData();
        } else if (modulename.equals("rp_scoreattackplan")) {
            aiDataLoadDirectory = aiDataLoadDirectory + "rp_placearmies";
            data = new RP_ScoreMergedPlanData();
        } else if (modulename.equals("rp_de_cost")) {
            aiDataLoadDirectory = aiDataLoadDirectory + "defense_plan";
            data = new RP_De_CostData();
        } else if (modulename.equals("rp_de_priority")) {
            aiDataLoadDirectory = aiDataLoadDirectory + "defense_plan";
            data = new RP_De_PriorityData();
        }

        if (data != null) {
            try {
                ((TrainingData)data).convert(convertType, modulename, aiDataLoadDirectory, aiDataSaveDirectory);
            } catch (IOException var7) {
                System.err.println(var7);
            }
        } else {
            System.out.println("Error: Syntax error in modulename (first argument).");
        }

    }
}
