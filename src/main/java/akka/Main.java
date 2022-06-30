package akka;

import java.time.Duration;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;

/* 
 * Summary: Akka based. 
 * No AddNode. Not connected to JNC.
 * EmsManagerBehavior generates  alarms.
 * EMSWorker prints the alarms. 
 * Total time taken is tracked.
 */

/* Details: In order to find out the total time taken to process all the alarms,
 * the-last-stop-class EmsWorkerBehavior returns back to EmsManagerBehavior.
 * 
 * In order to save number of calls, EmsWorkerBehavior DIRECTLY returns back to 
 * EmsManagerBehavior avoiding the intermediate classes such as RouterBehavior.
 * 
 * TODO: The EmsManagerBehavior::onProcessAlarmResponse may take
 * time to process all the responses sent by EmsWorkerBehavior. In order to 
 * find out the EXACT time taken by EmsWorkerBehavior to process all the alarms,
 * timestamp of processing alarm is sent with each 
 * EmsWorkerBehavior::onProcessAlarmResponse. We can check the oldest 
 * out of these timestamps and compare with the EmsManagerBehavior::startTime 
 * to find the exact time taken to process all the alarms.
 */
public class Main {
	public static void main(String[] args) {
		ActorSystem<akka.EmsManagerBehavior.Command> actorSystem = 
				ActorSystem.create(EmsManagerBehavior.create(), "AlarmProcessor");

		AskPattern.ask(actorSystem,
				me -> new EmsManagerBehavior.ProcessAlarmEvent(),
				Duration.ofSeconds(30),
				actorSystem.scheduler());
	}
}