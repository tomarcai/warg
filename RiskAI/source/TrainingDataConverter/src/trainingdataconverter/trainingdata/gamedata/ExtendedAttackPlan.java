package trainingdataconverter.trainingdata.gamedata;

public class ExtendedAttackPlan {
    private CostAttribute minimumCost;
    private CostAttribute estimatedCost;
    private PriorityAttribute priority;
    private ScoreAttribute score;
    private TerritoryAttribute[] attackPlan;

    public ExtendedAttackPlan(int minimumCost, int estimatedCost, float priority, float score, TerritoryAttribute[] attackPlan) {
        this.minimumCost = new CostAttribute("Minimum", minimumCost);
        this.estimatedCost = new CostAttribute("Estimated", estimatedCost);
        this.priority = new PriorityAttribute(priority);
        this.score = new ScoreAttribute(score);
        this.attackPlan = attackPlan;
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

    public TerritoryAttribute[] getAttackPlan() {
        return this.attackPlan;
    }
}
