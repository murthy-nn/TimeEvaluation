package multiThreaded;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckForResults implements Runnable {
	int alarmsToBeProcessed = 0;
	
	List <Timestamp> timeStamps;  //Shared memory
	private List<Integer> elapsedTimeList;//Shared memory

	Long startTime = System.currentTimeMillis();
	Timestamp startTime1 = new Timestamp(System.currentTimeMillis());

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
					
					System.out.println("Min time taken for processing an alarm " + elapsedTimeList.get(0));
					System.out.println("Max time taken for processing an alarm " + 
							elapsedTimeList.get(elapsedTimeList.size()-1) + "\n");
					
					break;
				}
			}
		}
	}
}