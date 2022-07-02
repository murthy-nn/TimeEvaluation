package akka;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.tailf.jnc.Device;
import com.tailf.jnc.DeviceUser;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;
import model.Alarm;

public class EmsWorkerBehavior extends AbstractBehavior<EmsWorkerBehavior.Command> {
	/* NTS, no member variable is defined for any of the Behavior . Thats how Actor
	 * do not maintain any state. Rather, state is passed through Command/messages.
	 */
	public interface Command extends Serializable {}

	private EmsWorkerBehavior(ActorContext<Command> context) {
		super(context);
	}

	public static Behavior<Command> create() {
		return Behaviors.setup(EmsWorkerBehavior::new);
	}

	@Override
	public Receive<Command> createReceive() {
		//return createReceiveFP();
		return createReceiveOO();
	}
	
	/******** FP style Message handlers ********/
	public Receive<Command> createReceiveFP() {
		return newReceiveBuilder()
				.onMessage(ProcessAlarmEvent.class, message -> {
					//TODO: write into DB, notify others, etc
					Timestamp ts = new Timestamp(System.currentTimeMillis());
					//System.out.println( "Alarm: " + message.getAlarm() + 	" @ " +ts );
					getContext().getLog().debug("Alarm: " + message.getAlarm() + 	" @ " +ts );
					//No need to return the caller
					//message.getController().tell(new SystemBehavior.HashResultCommand(hashResult));
					return Behaviors.same();
					//return Behaviors.stopped();//ico failure
				})
				.build();
	}
	

	/******** OO style Message handlers ********/
	public Receive<Command> createReceiveOO() {
		return newReceiveBuilder()
				.onMessage(ProcessAlarmEvent.class, this::onProcessAlarmEvent)
				.build();
	}
	
	private Behavior<Command> onProcessAlarmEvent (
			ProcessAlarmEvent command) {
		//TODO: write into DB, notify others, etc
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//With following line commented out, Akka takes less time than the multi-threaded application
		//getContext().getLog().debug("Processed " + command.getAlarm() + 	" @ " + ts );

		if (command.getAlarm().getDescription().equals("Alarm-1") ) {
			getContext().getLog().debug("Processed the first alarm @ " + ts );
		}
		
		/* TODO: alarmsToBeProcessed to be manually updated here and in
		 * EmsManagerBehavior 
		 */
		if (command.getAlarm().getDescription().equals("Alarm-100000") ) {
			getContext().getLog().debug("Processed the last alarm @ " + ts );
		}
		command.getController().tell(
				new EmsManagerBehavior.AlarmProcessedEvent (ts));
				//new EmsManagerBehavior.AlarmProcessedEvent (LocalDateTime.now()));

		return this;
	}
	
	/* This is the last entity in the e2e chain and does not define any response
	 * command/message to process and hence, no onProcessAlarmEventResult 
	 * is defined.
	 */
	
	/******** Message or command definition ********/
	public static class ProcessAlarmEvent implements Command {
		private static final long serialVersionUID = 1L;
		private Alarm alarm;
		private ActorRef<EmsManagerBehavior.Command> controller;
		public ProcessAlarmEvent(Alarm alarm, ActorRef<EmsManagerBehavior.Command> controller) {
			super();
			this.alarm = alarm;
			this.controller = controller;
		}
		public static long getSerialversionuid() {
			return serialVersionUID;
		}
		public Alarm getAlarm() {
			return alarm;
		}
		public ActorRef<EmsManagerBehavior.Command> getController() {
			return controller;
		}
	}
}