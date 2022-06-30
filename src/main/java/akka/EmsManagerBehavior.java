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
	private int alarmProcessed = 0;
	private int alarmsToBeProcessed = 100; // Alarms to be generated and processed
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
				.onMessage(ProcessAlarmEvent.class, this::onProcessAlarmEvent)
				.onMessage(ProcessAlarmResponse.class, this::onProcessAlarmResponse)
				.build();
	}
	private Behavior<Command> onProcessAlarmEvent (
			ProcessAlarmEvent command) {
		//TODO: FIFO ordering, Rogue node elimination, alarm throttling, etc
		
		Behavior<EmsRouterBehavior.Command> routerBehavior =
				Behaviors.supervise(EmsRouterBehavior.create()).onFailure(SupervisorStrategy.resume());
		ActorRef<EmsRouterBehavior.Command> router = getContext().spawn(routerBehavior, "router");
		
		for (int i=1; i<=alarmsToBeProcessed; i++) {
			Alarm alarm = new Alarm ("Alarm-"+i, "1.2.3.4", i, "10am");

			getContext().getLog().debug("Alarm: " + alarm + 	" @ " +
					new Timestamp(System.currentTimeMillis()));

			getContext().watch(router);
			router.tell(new EmsRouterBehavior.ProcessAlarmEvent(alarm, getContext().getSelf()));
		}

		return this;
	}

	private Behavior<Command> onProcessAlarmResponse (
			ProcessAlarmResponse command){
		getContext().getLog().debug("# of Alarm processed " + alarmProcessed );
		alarmProcessed ++;
		
		if (alarmProcessed == alarmsToBeProcessed) {
			Long endTime = System.currentTimeMillis();
			getContext().getLog().info("Akka:  " + (endTime - startTime) + 
					" ms taken to process " + alarmsToBeProcessed + " alarms");
			getContext().getSystem().terminate();
		}
		return this;
	}
	
	/******** Message or command definition ********/
	public static class ProcessAlarmEvent implements Command {
		private static final long serialVersionUID = 1L;
		public ProcessAlarmEvent() {
			super();
		}
	}

	public static class ProcessAlarmResponse implements Command {
		private static final long serialVersionUID = 1L;
		private LocalDateTime  localDateTime ;
		public ProcessAlarmResponse(LocalDateTime localDateTime) {
			super();
		}
		public LocalDateTime getLocalDateTime() {
			return localDateTime;
		}
		public void setLocalDateTime(LocalDateTime localDateTime) {
			this.localDateTime = localDateTime;
		}
	}
}