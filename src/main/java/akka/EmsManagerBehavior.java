package akka;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;
import model.Alarm;

/*
 * This class generates alarm in bulk and feed into RouterBehavior.
 * 
 * TODO; In production setup, Protocol stack's callback will replace this class.
 */
public class EmsManagerBehavior extends AbstractBehavior<EmsManagerBehavior.Command> {
	public interface Command extends Serializable {}
	private int totalAlarmProcessed = 0;
	private int alarmsToBeProcessed = 100000; // Alarms to be generated and processed
	Long startTime = 0L;

	private EmsManagerBehavior(ActorContext<EmsManagerBehavior.Command> context) {
		super(context);
		startTime = System.currentTimeMillis();
	}

	public static Behavior<EmsManagerBehavior.Command> create() {
		return Behaviors.setup(EmsManagerBehavior::new);
	}

	@Override
	public Receive<EmsManagerBehavior.Command> createReceive() {
		return createReceiveOO();
	}

	/******** OO style Message handlers ********/
	public Receive<Command> createReceiveOO() {
		return newReceiveBuilder()
				.onMessage(GenerateAlarmEvent.class, this::onGenerateAlarmEvent)
				.onMessage(AlarmProcessedEvent.class, this::onAlarmProcessed)
				.build();
	}
	private Behavior<Command> onGenerateAlarmEvent (
			GenerateAlarmEvent command) {
		Timestamp ts = new Timestamp(System.currentTimeMillis());

		Behavior<EmsRouterBehavior.Command> routerBehavior =
				Behaviors.supervise(EmsRouterBehavior.create()).onFailure(SupervisorStrategy.resume());
		ActorRef<EmsRouterBehavior.Command> router = getContext().spawn(routerBehavior, "router");
		
		for (int i=1; i<=alarmsToBeProcessed; i++) {
			Alarm alarm = new Alarm ("Alarm-"+i, "1.2.3.4", i, "10am");
			if (i ==1 ) {
				getContext().getLog().debug("Generated the first alarm @ " + ts );
			}
			if (i ==alarmsToBeProcessed ) {
				getContext().getLog().debug("Generated the last alarm @ " + ts );
			}
			
			//With following line commented out, Akka takes less time than the multi-threaded application
			//getContext().getLog().debug("Generated " + alarm + 	" @ " + new Timestamp(System.currentTimeMillis()));

			//getContext().watch(router);//Not required as per Lightbend discussion
			router.tell(new EmsRouterBehavior.ProcessAlarmEvent(alarm, getContext().getSelf()));
		}

		return this;
	}

	private Behavior<Command> onAlarmProcessed (
			AlarmProcessedEvent command){
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		
		//With following line commented out, Akka takes less time than the multi-threaded application
		//getContext().getLog().debug("# of Alarm processed " + totalAlarmProcessed );		
		
		if (totalAlarmProcessed == 0) {
			getContext().getLog().debug("Notified by the worker that FIRST alarm is processed @ " + ts);
			//getContext().getLog().debug("Notified by the worker that FIRST alarm is processed " + command.getTimeStamp());
		}
		
		totalAlarmProcessed ++;

		if (totalAlarmProcessed == alarmsToBeProcessed) {
			getContext().getLog().debug("Notified by the worker that LAST alarm is processed @ " + ts);
			Long endTime = System.currentTimeMillis();
			getContext().getLog().debug("Akka:  " + (endTime - startTime) + 
					" ms taken to process " + alarmsToBeProcessed + " alarms");
			getContext().getSystem().terminate();
		}
		return this;
	}
	
	/******** Message or command definition ********/
	public static class GenerateAlarmEvent implements Command {
		private static final long serialVersionUID = 1L;
		public GenerateAlarmEvent() {
			super();
		}
	}

	public static class AlarmProcessedEvent implements Command {
		private static final long serialVersionUID = 1L;
		private Timestamp  ts ;
		public AlarmProcessedEvent(Timestamp ts) {
			super();
			this.ts = ts;
		}
		public Timestamp getTimeStamp() {
			return ts;
		}
		public void setTimeStamp(Timestamp ts) {
			this.ts = ts;
		}
	}
}