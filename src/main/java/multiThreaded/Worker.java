package multiThreaded;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.Alarm;

public class Worker implements Runnable {
	Alarm alarm;
	List<Timestamp> timeStamps;
	private List<Integer> elapsedTimeList;

	public Worker (Alarm alarm, List<Timestamp> timeStamps, List<Integer> elapsedTimeList) {
		this.alarm = alarm;	
		this.timeStamps = timeStamps;
		this.elapsedTimeList = elapsedTimeList;
	}

	@Override
	public void run() {
		Long startTime = System.currentTimeMillis();
		
		synchronized (timeStamps) {
			/* Apart from printing the alarm here, much more work 
			 * would be done here in production setup.
			 */
			System.out.println( alarm);
			timeStamps.add(new Timestamp(System.currentTimeMillis()));
			timeStamps.notifyAll();
		}
		
		Long endTime = System.currentTimeMillis();

		synchronized (elapsedTimeList) {
			elapsedTimeList.add((int) (endTime-startTime));
			elapsedTimeList.notifyAll();
		}
	}
}