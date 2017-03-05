package PathFinder;

import Ontology.ContainerInformation;
import Ontology.ContainerInformationsWrapper;
import common.BaseAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class PathFinderAgent extends BaseAgent {

	/**
	 * bu Agent her container'ın hangi path üzerinde çalıştığı bilgisini sağlar.
	 * Böylece agent'lar çalıştıkları container' için dinlemeleri gereken
	 * dizinleri bilirler
	 */
	private volatile ContainerInformationsWrapper wrapper;
	private static final long serialVersionUID = 1L;

	@Override
	protected void setup() {
		baseSetup("containers-information");
		Object[] args = getArguments();
		wrapper = new ContainerInformationsWrapper((String) args[0]);
		fillContainerManuallyForNow();
		addBehaviour(new ContainerAddListener());
		addBehaviour(new ContainerRemoveListener());
		addBehaviour(new ContainerQueryListener());
	}

	private void fillContainerManuallyForNow() {
		ContainerInformation containerInformation1 = new ContainerInformation();
		containerInformation1.setContainername("CONTAINER_1");
		containerInformation1.setContainerpath("C:/Users/Serkan/Desktop/AgentContainers/AC1");

		ContainerInformation containerInformation2 = new ContainerInformation();
		containerInformation2.setContainername("CONTAINER_2");
		containerInformation2.setContainerpath("C:/Users/Serkan/Desktop/AgentContainers/AC2");

		ContainerInformation containerInformation3 = new ContainerInformation();
		containerInformation3.setContainername("CONTAINER_3");
		containerInformation3.setContainerpath("C:/Users/Serkan/Desktop/AgentContainers/AC3");

		wrapper.getContainerInformations().getContainerlist().add(containerInformation1);
		wrapper.getContainerInformations().getContainerlist().add(containerInformation2);
		wrapper.getContainerInformations().getContainerlist().add(containerInformation3);

	}

	private class ContainerAddListener extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633464210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchConversationId("add-container");
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
					ContainerInformation containerInformation = (ContainerInformation) getContentManager()
							.extractContent(msg);
					wrapper.getContainerInformations().getContainerlist().add(containerInformation);
					myAgent.send(reply);
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}

	private class ContainerRemoveListener extends CyclicBehaviour {

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchConversationId("remove-container");
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					ACLMessage reply = msg.createReply();
					ContainerInformation containerInformation = (ContainerInformation) getContentManager()
							.extractContent(msg);
					reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

					for (int i = 0; i < wrapper.getContainerInformations().getContainerlist().size(); i++) {
						ContainerInformation container = (ContainerInformation) wrapper.getContainerInformations()
								.getContainerlist().get(i);
						if (container.getContainername().equals(containerInformation.getContainername())) {
							wrapper.getContainerInformations().getContainerlist().remove(container);
							break;
						}
					}
					myAgent.send(reply);
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private class ContainerQueryListener extends CyclicBehaviour {

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("query-container"),
						MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					getContentManager().fillContent(reply, wrapper);
					myAgent.send(reply);
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println("ContainerQueryListener" + e);
			}
		}
	}
}
