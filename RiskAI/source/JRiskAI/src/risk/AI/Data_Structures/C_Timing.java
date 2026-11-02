package risk.AI.Data_Structures;

public class C_Timing {
	
	private long time = -10;
	private long startTime = -10;
	
	/** Creates a new instance of C_Timing */
	public C_Timing() {
	}
	
	public void startTimer() {
		time = -1;
		//startTime = System.currentTimeMillis();
		startTime = System.nanoTime();
	}
	
	public void endTimer() {
		if (startTime != -10) {
			if (startTime != -1) {
				//time = System.currentTimeMillis() - startTime;
				time = System.nanoTime() - startTime;
				startTime = -1;
			} else {
				throw new UnsupportedOperationException("The timer has already been stopped!");
			}
		} else {
			throw new UnsupportedOperationException("The timer has not been run!");
		}
	}
	
	public long getTimer() {
		if (time != -10) {
			if (time != -1) {
				return time/1000; // in microsec.
			} else {
				throw new UnsupportedOperationException("endTimer() should have been called first!");
			}
		} else {
			throw new UnsupportedOperationException("The timer has not been run!");
		}
	}
	
	public long getTimer_inNanoSec() {
		if (time != -1) {
			return time;
		} else {
			throw new UnsupportedOperationException("startTimer() and endTimer() should have been called first!");
		}
	}
	
	public boolean isStopped() {
		return (startTime == -1);
	}
	
}
