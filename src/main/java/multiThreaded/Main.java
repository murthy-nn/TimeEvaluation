package multiThreaded;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import model.Alarm;

/* Summary: 
 * Main method generates alarms and give them to the Executors.
 * Executors based threadPool to process the alarms.
 * Worker thread prints the alarms in the basic version. Will perform advanced
 * operation such as filtering, throttling, etc.
 * CheckForResults will close the Executors once all the alarms are processed.
 * 
 * How does CheckForResults work?
 * Main let CheckForResults know how many alarms will be generated.
 * Worker notifies CheckForResults after processing an alarm.
 * CheckForResults in turn check whether all the alarms are processed.
 * Once all alarm processed, CheckForResults generates the time report.
 */
public class Main {
	public static void main(String[] args) {
		int alarmsToBeProcessed = 1000; // Alarms to be generated and processed

		//Create a threadpool of Workers
		ExecutorService es = Executors.newFixedThreadPool(10, new MyThreadFactory());
		
		//Timestamp of processing each alarm
		List <Timestamp> timeStamps =  new ArrayList<Timestamp>(); 
		
		//Elapsed time for processing each alarm.
		List <Integer> elapsedTimeList =  new ArrayList<Integer>();  

		//Create a Result-Checking thread
		Thread resultsThread = new Thread
				(new CheckForResults(alarmsToBeProcessed, timeStamps, elapsedTimeList), "resultsThread");
		resultsThread.start();
		
		for (int i=1; i<=alarmsToBeProcessed; i++) {
			Alarm alarm = new Alarm ("Alarm-"+i, "1.2.3.4", i, "10am");
			es.execute(new Worker(alarm, timeStamps, elapsedTimeList));
		}
		
		try {
			resultsThread.join();
			System.out.println("ES is shutdown when resultsThread returns back");
			es.shutdownNow();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

/*
 * Class introduced to name the Threadpool.
 */
class MyThreadFactory implements ThreadFactory {
	public Thread newThread(Runnable r) {
		return new Thread(r, "AlarmThreadPool");
	}
}