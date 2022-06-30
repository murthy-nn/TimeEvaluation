package multiThreaded;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckForResults implements Runnable {
	List <Timestamp> timeStamps;  
	int alarmsToBeProcessed = 0;
	Long startTime = System.currentTimeMillis();
	Timestamp startTime1 = new Timestamp(System.currentTimeMillis());
	private List<Integer> elapsedTimeList;

	public CheckForResults(int alarmsToBeProcessed, List <Timestamp> timeStamps, List<Integer> elapsedTimeList) {
		this.timeStamps = timeStamps;
		this.alarmsToBeProcessed = alarmsToBeProcessed;
		this.elapsedTimeList = elapsedTimeList;
	}

	@Override
	public void run() {
		while  (true) {
			synchronized (timeStamps) {
				if (timeStamps.size() != alarmsToBeProcessed) {
					System.out.println("Not all alarms processed");
					try {
						timeStamps.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					System.out.println("\nAll alarms processed");
					Long endTime = System.currentTimeMillis();
					System.out.println("Multithreaded: " + (endTime - startTime) + 
							" ms taken to process " + alarmsToBeProcessed + " alarms");
					
					Collections.sort(timeStamps);
					System.out.println(
							startTime1 + " -- startTime recorded by CheckForResults\n" + 
							new Timestamp(System.currentTimeMillis()) + " -- endTime recorded by CheckForResults\n" + 
							timeStamps.get(alarmsToBeProcessed-1) + " -- Latest timeStamps reported by worker ");
					
					System.out.println("Min time taken by a runnable for mining " + elapsedTimeList.get(0));
					System.out.println("Max time taken by a runnable for mining " + 
							elapsedTimeList.get(elapsedTimeList.size()-1) + "\n");
					
					break;
				}
			}
		}
	}
}