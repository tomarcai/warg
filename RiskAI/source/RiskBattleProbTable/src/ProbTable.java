import java.util.*;
import java.io.*;

class ProbTable {
	
	static Random r = new Random();
		
	static int max_at_armies = 100;
	static int max_de_armies = 100;
		
	static float[][] table = new float[max_at_armies][max_de_armies];
	
	static int iterations = 100000;
	
	public static void main(String args[]) {
		for (int at_armies = 1; at_armies <= max_at_armies; at_armies++) {
			for (int de_armies = 1; de_armies <= max_de_armies; de_armies++) {
				if ((de_armies > 1) && (table[at_armies-1][de_armies-2] < 0.005f)) {
					table[at_armies-1][de_armies-1] = 0.0f;
				} else {
					int attacker_won = 0;
					int defender_won = 0;
					for (int i = 0; i < iterations; i++) {
						int at_armies_left = at_armies;				
						int de_armies_left = de_armies;
						while ((at_armies_left > 0) && (de_armies_left > 0)) {
							if (at_armies_left > 2) {
								if (de_armies_left > 1) {
									switch (getWinner(3,2)) {
										case -1: at_armies_left = at_armies_left -2; break;
										case 0: at_armies_left--; de_armies_left--; break;
										case 1: de_armies_left = de_armies_left -2; break;
									}
								} else {
									switch (getWinner(3,1)) {
										case -1: at_armies_left--; break;
										case 0: System.out.println("OOPS! 1"); break;
										case 1: de_armies_left--; break;
									}
								}
							} else {
								if (at_armies_left > 1) {
									if (de_armies_left > 1) {
										switch (getWinner(2,2)) {
											case -1: at_armies_left = at_armies_left -2; break;
											case 0: at_armies_left--; de_armies_left--; break;
											case 1: de_armies_left = de_armies_left -2; break;
										}
									} else {
										switch (getWinner(2,1)) {
											case -1: at_armies_left--; break;
											case 0: System.out.println("OOPS! 2"); break;
											case 1: de_armies_left--; break;
										}
									}
								} else {
									if (de_armies_left > 1) {
										switch (getWinner(1,2)) {
											case -1: at_armies_left--; break;
											case 0: System.out.println("OOPS! 3"); break;
											case 1: de_armies_left--; break;
										}
									} else {
										switch (getWinner(1,1)) {
											case -1: at_armies_left--; break;
											case 0: System.out.println("OOPS! 4"); break;
											case 1: de_armies_left--; break;
										}
									}
								}
							}
						}
						if ((de_armies_left < 1) && (at_armies_left < 1)) { 
							System.out.println("Oops!");
						}
						if (de_armies_left < 1) { 
							attacker_won++;
						} else {
							defender_won++;
						}
					}
					float prob = ((float)attacker_won)/((float)iterations);
					table[at_armies-1][de_armies-1] = prob;
					System.out.println("Attacking with "+at_armies+" armies against "+de_armies+" armies: "+prob+" prob attacker wins.");
				}
			}
		}
		saveTable();
	}
	
	static void saveTable() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("BattleOutcomeTable.txt"));
			for (int i = 0; i < max_at_armies; i++) {
				String line = "";
				for (int j = 0; j < max_de_armies; j++) {
					line += table[i][j] + ",";
				}
				out.write(line);
				out.newLine();
			}
			out.close();
		} catch(FileNotFoundException e) {}
		catch(IOException e) {}
	}
	
	// 1 attacker won, 0 both lost, -1 defender win.
	static int getWinner(int attackerDice, int defenderDice) {
		int defenderLose = 0;
		int attackerLose = 0;
		int at_dice1 = 0;
		int at_dice2 = 0;
		int at_dice3 = 0;
		int de_dice1 = 0;
		int de_dice2 = 0;
		switch (attackerDice) {
			case 1: at_dice1 = r.nextInt(6)+1; break;
			case 2: at_dice1 = r.nextInt(6)+1; at_dice2 = r.nextInt(6)+1; break;
			case 3: at_dice1 = r.nextInt(6)+1; at_dice2 = r.nextInt(6)+1; at_dice3 = r.nextInt(6)+1; break;
		}
		switch (defenderDice) {
			case 1: de_dice1 = r.nextInt(6)+1; break;
			case 2: de_dice1 = r.nextInt(6)+1; de_dice2 = r.nextInt(6)+1; break;
		}
		if (de_dice2 > de_dice1) {
			int t = de_dice1;
			de_dice1 = de_dice2;
			de_dice2 = t;
		}
		if (at_dice3 > at_dice2) {
			int t = at_dice2;
			at_dice2 = at_dice3;
			at_dice3 = t;
		}
		if (at_dice2 > at_dice1) {
			int t = at_dice1;
			at_dice1 = at_dice2;
			at_dice2 = t;
		}
		if (at_dice3 > at_dice2) {
			int t = at_dice2;
			at_dice2 = at_dice3;
			at_dice3 = t;
		}
		if (at_dice1 > de_dice1) {
			defenderLose++;
		} else {
			attackerLose++;
		}
		if ((de_dice2 != 0) && (at_dice2 != 0)) {
			if (at_dice2 > de_dice2) {
				defenderLose++;
			} else {
				attackerLose++;
			}
		}
		if (defenderLose == attackerLose) { return 0; }
		if (defenderLose < attackerLose) { return -1; } else { return 1; }
	}
}
