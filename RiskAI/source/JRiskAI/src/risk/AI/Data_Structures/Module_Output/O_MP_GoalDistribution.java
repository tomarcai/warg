package risk.AI.Data_Structures.Module_Output;

import risk.AI.Data_Structures.*;

public class O_MP_GoalDistribution {
	
	// Goal Distribution:
	// 00-05: Conquer and defend continent.
	// 06-11: Conquer continent.
	// 12-17: Defend continent.
	// 18-23: Obstruct continent.
	// 24-29: Obstruct and defend obstruction in continent.
	// 30-35: Attack player.
	// 36   : 18 territories w/ 2 on each.
	// 37   : 24 territories w/ 1 on each.
	private float[] dist = new float[38];
	
	/**
	 * Sets the full distribution for all 38 goals.
	 */
	public void setFullDistribution(float[] dist) {
		this.dist = dist;
	}
	
	/**
	 * Gets the full distribution for all 38 goals.
	 */
	public float[] getFullDistribution() {
		return dist;
	}
	
	/**
	 * Conquer and defend continent
	 */
	public void setDistribution_CD(float[] continent_dist) {
		for (int i = 0; i < 6; i++) {
			dist[i] = continent_dist[i];
		}
	}
	
	/**
	 * Conquer continent
	 */
	public void setDistribution_C(float[] continent_dist) {
		for (int i = 0; i < 6; i++) {
			dist[i+6] = continent_dist[i];
		}
	}
	
	/**
	 * Defend continent
	 */
	public void setDistribution_D(float[] continent_dist) {
		for (int i = 0; i < 6; i++) {
			dist[i+12] = continent_dist[i];
		}
	}
	
	/**
	 * Obstruct continent
	 */
	public void setDistribution_O(float[] continent_dist) {
		for (int i = 0; i < 6; i++) {
			dist[i+18] = continent_dist[i];
		}
	}
	
	/**
	 * Obstruct and defend obstruction in continent
	 */
	public void setDistribution_OD(float[] continent_dist) {
		for (int i = 0; i < 6; i++) {
			dist[i+24] = continent_dist[i];
		}
	}
	
	/**
	 * Attack player
	 */
	public void setDistribution_A(float[] player_dist) {
		for (int i = 0; i < 6; i++) {
			dist[i+30] = player_dist[i];
		}
	}
	
	/**
	 * 18 territories w/ 2 on each
	 */
	public void setDistribution_18(float dist) {
		this.dist[36] = dist;
	}
	
	/**
	 * 24 territories w/ 1 on each
	 */
	public void setDistribution_24(float dist) {
		this.dist[37] = dist;
	}
	
	/**
	 * Conquer and defend continent
	 */
	public float[] getDistribution_CD() {
		float[] tmp = new float[6];
		for (int i = 0; i < 6; i++) {
			tmp[i] = dist[i];
		}
		return tmp;
	}
	
	/**
	 * Conquer continent
	 */
	public float[] getDistribution_C() {
		float[] tmp = new float[6];
		for (int i = 0; i < 6; i++) {
			tmp[i] = dist[i+6];
		}
		return tmp;
	}
	
	/**
	 * Defend continent
	 */
	public float[] getDistribution_D() {
		float[] tmp = new float[6];
		for (int i = 0; i < 6; i++) {
			tmp[i] = dist[i+12];
		}
		return tmp;
	}
	
	/**
	 * Obstruct continent
	 */
	public float[] getDistribution_O() {
		float[] tmp = new float[6];
		for (int i = 0; i < 6; i++) {
			tmp[i] = dist[i+18];
		}
		return tmp;
	}
	
	/**
	 * Obstruct and defend obstruction in continent
	 */
	public float[] getDistribution_OD() {
		float[] tmp = new float[6];
		for (int i = 0; i < 6; i++) {
			tmp[i] = dist[i+24];
		}
		return tmp;
	}
	
	/**
	 * Attack player
	 */
	public float[] getDistribution_A() {
		float[] tmp = new float[6];
		for (int i = 0; i < 6; i++) {
			tmp[i] = dist[i+30];
		}
		return tmp;
	}
	
	/**
	 * 18 territories w/ 2 on each
	 */
	public float getDistribution_18() {
		return dist[36];
	}
	
	/**
	 * 24 territories w/ 1 on each
	 */
	public float getDistribution_24() {
		return dist[37];
	}
}