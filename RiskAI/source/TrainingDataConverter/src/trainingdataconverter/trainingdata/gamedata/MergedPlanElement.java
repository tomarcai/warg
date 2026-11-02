package trainingdataconverter.trainingdata.gamedata;

public class MergedPlanElement {
    private CostAttribute minimumCost;
    private CostAttribute estimatedCost;
    private PriorityAttribute priority;
    private ScoreAttribute score;
    private AttackerArmiesLeftAttribute attackerArmiesLeft;
    private IsDefensePlanAttribute isDefensePlan;
    private MergedPlanTerritoryAttribute territory;

    public MergedPlanElement(String territory, int minimumCost, int estimatedCost, float priority, float score, int attackerArmiesLeft, boolean isDefensePlan) {
        this.territory = new MergedPlanTerritoryAttribute(territory);
        this.minimumCost = new CostAttribute("Minimum", minimumCost);
        this.estimatedCost = new CostAttribute("Estimated", estimatedCost);
        this.priority = new PriorityAttribute(priority);
        this.score = new ScoreAttribute(score);
        this.attackerArmiesLeft = new AttackerArmiesLeftAttribute(attackerArmiesLeft);
        this.isDefensePlan = new IsDefensePlanAttribute(isDefensePlan);
    }

    public CostAttribute getMinimumCost() {
        return this.minimumCost;
    }

    public CostAttribute getEstimatedCost() {
        return this.estimatedCost;
    }

    public PriorityAttribute getPriority() {
        return this.priority;
    }

    public ScoreAttribute getScore() {
        return this.score;
    }

    public MergedPlanTerritoryAttribute getTerritory() {
        return this.territory;
    }

    public AttackerArmiesLeftAttribute getAttackerArmiesLeft() {
        return this.attackerArmiesLeft;
    }

    public IsDefensePlanAttribute getIsDefensePlan() {
        return this.isDefensePlan;
    }
}
