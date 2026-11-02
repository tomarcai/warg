package risk.AI.Data_Structures;

import java.util.*;

public class C_TimingList {
	
	private Vector<Long> timingList = new Vector();
	
	/** Creates a new instance of C_Timing */
	public C_TimingList() {
	}
	
	public void addTime(long time) {
		timingList.add(time);
	}
	
	public long getAverage() {
		long sum = 0;
		for (int i = 0; i < timingList.size(); i++) {
			sum += timingList.get(i).longValue();
		}
		if (timingList.size() > 0) {
			return sum/timingList.size();		
		} else {
			return -1;
		}
	}
	
	public long getMin() {
		if (timingList.size() > 0) {
			long min = Long.MAX_VALUE;
			for (int i = 0; i < timingList.size(); i++) {
				if (timingList.get(i).longValue() < min) {
					min = timingList.get(i).longValue();
				}
			}
			return min;		
		} else {
			return -1;
		}
	}
	
	public long getMax() {
		if (timingList.size() > 0) {
			long max = -1;
			for (int i = 0; i < timingList.size(); i++) {
				if (timingList.get(i).longValue() > max) {
					max = timingList.get(i).longValue();
				}
			}
			return max;	
		} else {
			return -1;
		}
	}
	
	public String toString() {
		String str = "";
		for (int i = 0; i < timingList.size(); i++) {
			str += timingList.get(i).longValue();
			if (i < timingList.size()-1) {
				str += ",";
			}
		}
		return str;
	}
	
	public String getFullString() {
		return "Average="+getAverage()+", Min="+getMin() + ", Max="+getMax() + " ("+toString()+")";
	}
	
	public Vector<Long> getTimingList() {
		return this.timingList;
	}
	
}
