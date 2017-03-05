package Synchronization;

import java.util.Calendar;

import Ontology.Timer;
import common.BaseAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TimerAgent extends BaseAgent {

	/**
	 * Serkan Akdemir This Agent is created as file version manager All files
	 * must be versioned by the value created which is produced by TimerAgent
	 */
	private static final long serialVersionUID = 356456541L;

	@Override
	protected void setup() {
		baseSetup("TimerAgent");
		addBehaviour(new TimeRequestListener());
	}

	private class TimeRequestListener extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633444210004640L;
		Object lock = new Object();

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("get-current-time"),
						MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					// only one Agent can get version at the same time!
					synchronized (lock) {
						ACLMessage reply = msg.createReply();
						Timer timer = new Timer();
						timer.setTime(Calendar.getInstance().getTimeInMillis());
						getContentManager().fillContent(reply, timer);
						reply.setPerformative(ACLMessage.INFORM);
						myAgent.send(reply);
					}
				} else {
					block();
				}

			} catch (Exception e) {
				System.out.println(e);
				e.printStackTrace();
			}
		}
	}

}
