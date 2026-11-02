/*
 * T_BattleOutcomeProbTable.java
 *
 * Created on 9. februar 2006, 15:32
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.io.*;
import java.util.*;

/**
 *
 * @author Pelle Coltau
 */
public class T_BattleOutcomeProbTable {
	
	private float[][] table;
	
	/** Creates a new instance of T_BattleOutcomeProbTable */
	public T_BattleOutcomeProbTable() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("BattleOutcomeTable.txt")); 
			Vector v_table = new Vector();
			String line = in.readLine();
			while (line != null) {
				v_table.add(new Vector());
				String[] str = line.split(",");
				for (int i = 0; i < str.length; i++) {
					((Vector)v_table.lastElement()).add(Float.parseFloat(str[i]));
				}
				line = in.readLine();
			}
			table = new float[v_table.size()][((Vector)v_table.firstElement()).size()];
			for (int i = 0; i < v_table.size(); i++) {
				for (int j = 0; j < ((Vector)v_table.get(i)).size(); j++) {
					table[i][j] = ((Float)((Vector)v_table.get(i)).get(j)).floatValue();
				}
			}
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public float getBattleProbability(int attackingArmies, int defendingArmies) {
		return table[Math.min(attackingArmies,100)-1][Math.min(defendingArmies,100)-1];
	}
	
}
