package akka;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.*;
import model.Alarm;

// #BC Whole class
// This is equivalent to SystemBehavior of Mining project.
public class EmsRouterBehavior extends AbstractBehavior<EmsRouterBehavior.Command> {	
	public interface Command extends Serializable {}
	int poolSize = 100;
	/* It has a Router and pool of Actors, which are defined by EmsWorkerBehavior.
	 */
	private PoolRouter<EmsWorkerBehavior.Command> workerPoolRouter;
	private ActorRef<EmsWorkerBehavior.Command> workers;
	
	private EmsRouterBehavior(ActorContext<EmsRouterBehavior.Command> context) {
		super(context);
		getContext().getLog().debug("Router created with poolSize of " + poolSize);
		workerPoolRouter = Routers.pool(poolSize,
				Behaviors.supervise(EmsWorkerBehavior.create()).onFailure(SupervisorStrategy.restart()));
		workers = getContext().spawn(workerPoolRouter, "managerPool");
	}

	public static Behavior<EmsRouterBehavior.Command> create() {
		return Behaviors.setup(EmsRouterBehavior::new);
	}

	@Override
	public Receive<EmsRouterBehavior.Command> createReceive() {
		//return createReceiveFP();
		return createReceiveOO();
	}

	/******** OO style Message handlers ********/
	public Receive<Command> createReceiveOO() {
		return newReceiveBuilder()
				.onMessage(ProcessAlarmEvent.class, this::onProcessAlarmEvent)
				.build();
	}
	private Behavior<Command> onProcessAlarmEvent (
			ProcessAlarmEvent command) {
		//TODO: FIFO ordering, Rogue node elimination, alarm throttling, etc
		
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		//getContext().getLog().debug("Alarm: " + command.getAlarm() + 	" @ " +ts );

		/* Assuming that this class does not process the response, we do not
		 * define a message ProcessAlarmEventResult. And, we return EmsManagerBehavior 
		 * to the EmsWorkerBehavior, so that EmsWorkerBehavior directly calls EmsManagerBehavior.
		 * EmsManagerBehavior is the caller of this class.
		 */
		workers.tell(new EmsWorkerBehavior.ProcessAlarmEvent(command.getAlarm(), command.getController()));
		//workers.tell(new EmsWorkerBehavior.ProcessAlarmEvent(command.getAlarm(), getContext().getSelf()));
		
		return this;
	}

	/******** Message or command definition ********/
	public static class ProcessAlarmEvent implements Command {
		private static final long serialVersionUID = 1L;
		private Alarm alarm;
		private ActorRef<EmsManagerBehavior.Command> controller;

		public ProcessAlarmEvent (Alarm alarm, ActorRef<EmsManagerBehavior.Command> controller) {
			this.alarm = alarm;
			this.controller = controller;
		}

		public Alarm getAlarm() {
			return alarm;
		}
		
		public ActorRef<EmsManagerBehavior.Command> getController() {
			return controller;
		}
	}
}
