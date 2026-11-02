import java.util.*;

class ArmyCost {
	
	static Random r = new Random();
	
	final static int num_armies = 20;
	final static int iterations = 100000;
	
	public static void main(String args[]) {
		double sum = 0;
		for (int j = 0; j < 20; j++) {
			double real_cost = 0.8534144*num_armies - 0.2213413*(1 - Math.pow(-0.525359,(double)num_armies));
			float average = 0;
			for (int i = 0; i < iterations; i++) {
				int armies = num_armies;
				int lost = 0;
				while (armies > 0) {
					int winner;
					if (armies == 1) {
						winner = getWinner(3,1);
						if (winner == 1) {
							armies--;
						} else {
							lost++;
						}
					} else {
						winner = getWinner(3,2);
						if (winner == 1) {
							armies = armies - 2;
						} else {
							if (winner == 0) {
								armies = armies - 1;
								lost++;
							} else {
								lost = lost + 2;
							}
						}
					}
					
				}
				average = average + lost;
			}
			float calc_cost = average/iterations;
			sum = sum + Math.abs((calc_cost-real_cost));
			System.out.println(Math.abs(calc_cost-real_cost));
		}
		System.out.println("Average: "+(sum/20));
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
		if (de_dice2 != 0) {
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