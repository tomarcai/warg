/*
 * T_BoardIntegrity.java
 *
 * Created on 7. marts 2006, 17:57
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package risk.AI.Data_Structures;

import net.yura.domination.engine.ColorUtil;
import net.yura.domination.engine.core.Player;

public class T_BoardIntegrity {
	
	private int[] currentBoard = null;
	
	public void recordBoard(T_Board board) {
		currentBoard = calcBoardValue(board);
	}
	
	public void checkBoardIntegrity(String class_name, String method_name, T_Board board) {
		if (!isBoardTheSame(calcBoardValue(board),currentBoard)) {
			boardIntegrityFailed(class_name,method_name);
		}
	}
	
	public boolean checkBoardIntegrity_returnResult(T_Board board) {
		return isBoardTheSame(calcBoardValue(board),currentBoard);
	}
	
	/**
	 * Used to ensure board integrity.
	 */
	private boolean isBoardTheSame(int[] newBoardValue, int[] currentBoardValue) {
		if (currentBoardValue != null) {
			for (int i = 0; i < 84; i++) {
				if (newBoardValue[i] != currentBoardValue[i]) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}
	
	/**
	 * Used to ensure board integrity.
	 */
	private int[] calcBoardValue(T_Board board) {
		int[] newBoardValue = new int[84];
		for (int i = 0; i < board.getCountryCount(); i++) {
			newBoardValue[i*2] = ownerToInt(board.getCountryByInt(i).getOwner());
			newBoardValue[i*2+1] = board.getCountryByInt(i).getArmies();
		}
		return newBoardValue;
	}	
	
	/**
	 * Used to ensure board integrity.
	 */
	private int ownerToInt(Player player) {
		if (player == null) return -1;

		int c = player.getColor();
		switch (c) {
			case ColorUtil.BLACK: return 0;
			case ColorUtil.BLUE: return 1;
			case ColorUtil.CYAN: return 2;
			case ColorUtil.DARK_GRAY: return 3;
			case ColorUtil.GRAY: return 4;
			case ColorUtil.GREEN: return 5;
			case ColorUtil.LIGHT_GRAY: return 6;
			case ColorUtil.MAGENTA: return 7;
			case ColorUtil.ORANGE: return 8;
			case ColorUtil.PINK: return 9;
			case ColorUtil.RED: return 10;
			case ColorUtil.WHITE: return 11;
			case ColorUtil.YELLOW: return 12;
			default: return -1;
		}
	}		

	private void boardIntegrityFailed(String class_name, String method_name) {
		System.out.println(class_name+"."+method_name+": Board integrity failed!");
	}
}
