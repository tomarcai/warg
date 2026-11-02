package trainingdataconverter.trainingdata.gamedata;

import java.util.Vector;

public class PastCollection {
    private Vector<TerritoryAttribute[]> boards = new Vector();
    private Vector<String> actions = new Vector();

    public PastCollection() {
    }

    public void addBoardState(TerritoryAttribute[] boardState) {
        this.boards.add(boardState);
    }

    public void addAction(String action) {
        this.actions.add(action);
    }

    public int size() {
        return this.boards.size();
    }

    public void clear() {
        this.boards.clear();
        this.actions.clear();
    }

    public TerritoryAttribute[] getBoardState(int index) {
        return (TerritoryAttribute[])this.boards.get(index);
    }

    public String getAction(int index) {
        return (String)this.actions.get(index);
    }
}
