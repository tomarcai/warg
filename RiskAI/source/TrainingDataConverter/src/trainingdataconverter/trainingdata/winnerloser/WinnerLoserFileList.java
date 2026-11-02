package trainingdataconverter.trainingdata.winnerloser;

import java.io.EOFException;
import java.io.File;
import trainingdataconverter.trainingdata.io.XMLInput;

public class WinnerLoserFileList {
    private WinnerLoserGameList gameList = new WinnerLoserGameList();

    public WinnerLoserFileList() {
    }

    public void add(File file) {
        String playerReference = "";
        String winningPlayer = "";
        String gameFile = "";
        String filename = file.getPath();
        XMLInput input = new XMLInput(filename);

        try {
            label40:
            while(!input.endReached()) {
                input.readLine();
                if (input.contains("<module_run>") || input.contains("<attack_plan>") || input.contains("<defense_plan>")) {
                    input.readLine();
                    if (input.contains("<player_reference>")) {
                        input.readLine();
                        playerReference = input.getText();

                        do {
                            input.readLine();
                        } while(!input.contains("</player_reference>"));
                    }

                    input.readLine();
                    if (input.contains("<game_file>")) {
                        input.readLine();
                        gameFile = (new File(filename)).getParent() + "/../../" + input.getText();
                        winningPlayer = this.findGameWinner(gameFile);

                        while(true) {
                            input.readLine();
                            if (input.contains("</game_file>")) {
                                break label40;
                            }
                        }
                    }
                }
            }
        } catch (EOFException var8) {
        }

        boolean playerWon = playerReference.equals(winningPlayer);
        this.gameList.add(gameFile, new WinnerLoserFile(file, playerWon));
    }

    public File[] getList() {
        return this.gameList.getWinnerLoserList();
    }

    private String findGameWinner(String filename) {
        String result = "";
        //int numberOfPlayers = false;
        XMLInput input = new XMLInput(filename);

        try {
            input.readLine();

            for(; !input.endReached(); input.readLine()) {
                if (input.contains("<game>")) {
                    do {
                        input.readLine();
                        if (input.contains("<winner>")) {
                            input.readLine();
                            result = input.getText();

                            do {
                                input.readLine();
                            } while(!input.contains("</winner>"));
                        }
                    } while(!input.contains("</game>"));
                }
            }
        } catch (EOFException var6) {
        }

        return result;
    }
}
