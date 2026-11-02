package trainingdataconverter.trainingdata.gamedata;

public class TerritoryAttribute extends GameDataAttribute {
    private static String[] arffStatesFull = new String[]{"-68.0;-43.0", "-43.0;-30.0", "-30.0;-20.0", "-20.0;-18.0", "-17.0;-16.0", "-15.0;-15.0", "-14.0;-14.0", "-13.0;-13.0", "-12.0;-12.0", "-11.0;-11.0", "-10.0;-10.0", "-9.0;-9.0", "-8.0;-8.0", "-7.0;-7.0", "-6.0;-6.0", "-5.0;-5.0", "-4.0;-4.0", "-3.0;-3.0", "-2.0;-2.0", "-1.0;-1.0", "1.0;1.0", "2.0;2.0", "3.0;3.0", "4.0;4.0", "5.0;5.0", "6.0;6.0", "7.0;7.0", "8.0;8.0", "9.0;9.0", "10.0;10.0", "11.0;11.0", "12.0;12.0", "13.0;13.0", "14.0;14.0", "15.0;15.0", "16.0;17.0", "18.0;20.0", "20.0;30.0", "30.0;43.0", "43.0;68.0"};
    private static String[] arffStatesHalf = new String[]{"-68.0;-50.0", "-49.0;-26.0", "-25.0;-16.0", "-15.0;-11.0", "-10.0;-7.0", "-7.0;-5.0", "-4.0;-4.0", "-3.0;-3.0", "-2.0;-2.0", "-1.0;-1.0", "1.0;1.0", "2.0;2.0", "3.0;3.0", "4.0;4.0", "5.0;7.0", "7.0;10.0", "11.0;15.0", "16.0;25.0", "26.0;49.0", "50.0;68.0"};
    private static AttributeStates arffAttributeStates;
    private String owner;
    private int armies;
    private String currentPlayer = null;

    public TerritoryAttribute(String name, String owner, int armies) {
        this.name = name;
        if (armies < 0) {
            throw new UnsupportedOperationException("Number of armies must at least be 0! (armies = " + armies + ")");
        } else {
            this.owner = owner;
            this.armies = armies;
            this.setValue(String.valueOf(armies));
        }
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getCurrentPlayer() {
        return this.currentPlayer;
    }

    public void useHalfIntervals(boolean useHalfIntervals) {
        if (useHalfIntervals) {
            arffAttributeStates = new AttributeStates(arffStatesHalf);
        } else {
            arffAttributeStates = new AttributeStates(arffStatesFull);
        }

    }

    public String getARFFValue() {
        if (this.currentPlayer == null) {
            throw new RuntimeException("setCurrentPlayer() must be called before a territory value can be retrieved");
        } else {
            boolean occupied = this.owner.equals(this.currentPlayer);
            int value;
            if (this.owner.equals(this.currentPlayer)) {
                value = this.armies;
            } else {
                value = -this.armies;
            }

            return arffAttributeStates.getState((float)value);
        }
    }

    public String getNNValue() {
        if (this.currentPlayer == null) {
            throw new RuntimeException("setCurrentPlayer() must be called before a territory value can be retrieved");
        } else {
            int multiplier = -1;
            if (this.owner.equals(this.currentPlayer)) {
                multiplier = 1;
            }

            if (this.armies > 50) {
                this.armies = 50;
            }

            return String.valueOf((float)(multiplier * this.armies) / 50.0F);
        }
    }

    public String getARFFHeader() {
        return arffAttributeStates.getARFFHeader(this.name);
    }

    public String getOwner() {
        return this.owner;
    }

    public String getName() {
        return this.name;
    }

    public int getArmies() {
        return this.armies;
    }

    static {
        arffAttributeStates = new AttributeStates(arffStatesFull);
    }
}
