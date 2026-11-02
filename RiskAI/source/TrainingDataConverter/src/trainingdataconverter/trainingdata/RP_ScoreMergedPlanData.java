package trainingdataconverter.trainingdata;

import java.io.File;
import trainingdataconverter.trainingdata.gamedata.MergedPlanElement;
import trainingdataconverter.trainingdata.gamedata.MergedPlanList;
import trainingdataconverter.trainingdata.gamedata.ScoreAttribute;
import trainingdataconverter.trainingdata.io.ARFFOutput;
import trainingdataconverter.trainingdata.io.NNTDOutput;

public class RP_ScoreMergedPlanData extends TrainingData {
    public RP_ScoreMergedPlanData() {
    }

    public static String makeARFFRelationName() {
        return "RP_ScoreMergedPlan";
    }

    private void writeARFFHeader(ARFFOutput output) {
        MergedPlanElement plan = this.gameDataArray[0].getMergedPlanList().get(0);
        plan.getEstimatedCost().useHalfIntervals(true);
        plan.getPriority().useHalfIntervals(true);
        plan.getScore().useHalfIntervals(true);
        output.writeRelationName(makeARFFRelationName());
        output.writeLine("");
        output.writeHeader(plan.getEstimatedCost());
        output.writeHeader(plan.getPriority());
        output.writeHeader(plan.getIsDefensePlan());
        output.writeHeader(plan.getScore());
        output.writeLine("");
        output.writeLine("@data");
    }

    private void writeARFFDataLine(ARFFOutput output, MergedPlanElement plan, String targetValue) {
        plan.getEstimatedCost().useHalfIntervals(true);
        plan.getPriority().useHalfIntervals(true);
        plan.getScore().useHalfIntervals(true);
        output.writeData(plan.getEstimatedCost(), false);
        output.writeData(plan.getPriority(), false);
        output.writeData(plan.getIsDefensePlan(), false);
        output.writeLine(targetValue);
    }

    private void writeARFFData(ARFFOutput output) {
        String[] targetValues = ScoreAttribute.getARFFAttributeStates().getStringArray();

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length && this.gameDataArray[dataIndex] != null; ++dataIndex) {
            GameData data = this.gameDataArray[dataIndex];
            int winnerIndex = data.getWinningPlayer();
            int playerReference = data.getPlayerReference();
            MergedPlanList planList = data.getMergedPlanList();

            for(int planIndex = 0; planIndex < planList.size(); ++planIndex) {
                MergedPlanElement plan = planList.get(planIndex);
                if (playerReference != winnerIndex) {
                    String loserValue = plan.getScore().getARFFValue();

                    for(int valueIndex = 0; valueIndex < targetValues.length; ++valueIndex) {
                        String value = targetValues[valueIndex];
                        if (!value.equals(loserValue)) {
                            this.writeARFFDataLine(output, plan, value);
                        }
                    }
                } else {
                    for(int i = 0; i < targetValues.length - 1; ++i) {
                        this.writeARFFDataLine(output, plan, plan.getScore().getARFFValue());
                    }
                }
            }
        }

    }

    public String makeARFFString() {
        ARFFOutput output = new ARFFOutput();
        this.writeARFFHeader(output);
        MergedPlanElement plan = this.gameDataArray[0].getMergedPlanList().get(0);
        this.writeARFFDataLine(output, plan, plan.getScore().getARFFValue());
        return output.getString();
    }

    public void saveARFFToDirectory(String directoryName, boolean initFiles) {
        if (this.gameDataArray == null) {
            throw new RuntimeException("Can not write ARFF when gameDataArray is null");
        } else {
            this.createDirectory(directoryName);
            String filename = directoryName + makeARFFRelationName() + ".arff";
            ARFFOutput output = new ARFFOutput();
            if (initFiles) {
                this.writeARFFHeader(output);
            }

            this.writeARFFData(output);
            output.saveToFile(filename, initFiles);
            debug("Converted and saved to directory", (new File(directoryName)).toString());
        }
    }

    public void saveNNTDToDirectory(String directoryName, boolean initFiles) {
        this.createDirectory(directoryName);
        String filename = directoryName + "data.nntd";
        NNTDOutput output = new NNTDOutput();
        int inputNodes = 3;
        int outputNodes = 1;
        if (initFiles) {
            output.writeHeader(inputNodes, 4, outputNodes);
            output.writeLine("");
            output.writeLine("@data");
        }

        for(int dataIndex = 0; dataIndex < this.gameDataArray.length; ++dataIndex) {
            for(int i = 0; i < this.gameDataArray[dataIndex].getMergedPlanList().size(); ++i) {
                String inputData = this.gameDataArray[dataIndex].getMergedPlanList().get(i).getEstimatedCost().getNNValue() + "," + this.gameDataArray[dataIndex].getMergedPlanList().get(i).getPriority().getNNValue() + "," + this.gameDataArray[dataIndex].getMergedPlanList().get(i).getIsDefensePlan().getNNValue();
                String targetData = this.gameDataArray[dataIndex].getMergedPlanList().get(i).getScore().getNNValue();
                if (this.gameDataArray[dataIndex].getWinningPlayer() == this.gameDataArray[dataIndex].getPlayerReference()) {
                    output.writeData(true, inputData, targetData, inputNodes, outputNodes);
                } else {
                    output.writeData(false, inputData, targetData, inputNodes, outputNodes);
                }
            }
        }

        output.saveToFile(filename, initFiles);
        debug("Converted and saved to directory", (new File(directoryName)).toString());
    }
}
