package common;

import java.util.Iterator;

import Ontology.ContainerInformation;
import Ontology.ContainerInformationOntology;
import Ontology.ContainerInformationsWrapper;
import Ontology.DocumentOntology;
import Ontology.Timer;
import Ontology.TimerOntology;
import jade.content.ContentElement;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

public class BaseAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected String containerPath = null;
	protected String fileChangeMode = null;
	protected Long globalTime = null;
	protected List agentMoveContainerList = new ArrayList();
	protected List broadcastContainerList = new ArrayList();

	protected volatile boolean containerPathAsked = false;
	protected volatile boolean containerListAsked = false;

	protected void baseSetup(String type) {
		handleContentProperties();
		if (!type.equals("containers-information")) {
			callToGetContainerPath();
		}
		register(type);
	}

	protected void handleContentProperties() {
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
		getContentManager().registerOntology(DocumentOntology.getInstance());
		getContentManager().registerOntology(TimerOntology.getInstance());
		getContentManager().registerOntology(ContainerInformationOntology.getInstance());
		getContentManager().registerOntology(JADEManagementOntology.getInstance());
	}

	private void register(String type) {
		try {
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription sd = new ServiceDescription();
			sd.setType(type);
			sd.setName("JADE-" + type);
			sd.addOntologies(DocumentOntology.NAME);
			sd.addOntologies(TimerOntology.NAME);
			sd.addOntologies(ContainerInformationOntology.NAME);
			sd.addOntologies(JADEManagementOntology.NAME);
			// sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
			sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL0);
			dfd.addServices(sd);
			sd.setOwnership(getContainerController().getContainerName());
			DFService.register(this, dfd);
		} catch (Exception fe) {
			fe.printStackTrace();
		}

	}

	protected void callToGetContainerPath() {
		try {
			if (!containerPathAsked) {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("containers-information");
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(this, template);
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				cfp.setConversationId("query-container");
				cfp.setOntology(ContainerInformationOntology.NAME);
				cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);

				if (result.length > 0) {
					containerPathAsked = true;
					for (int i = 0; i < result.length; ++i) {
						cfp.addReceiver(result[i].getName());
					}
					addBehaviour(new ContinerPathListener());
					send(cfp);
				}
			} else {
				System.out.println("containerPath is ALREADY asked for" + getName());
			}
		} catch (Exception e) {
			System.out.println("callToGetContainerPath" + e);
		}
	}

	public void setContainerPath(String containerPath) {
		this.containerPath = containerPath;
	}

	public String getFileChangeMode() {
		return fileChangeMode;
	}

	public void setFileChangeMode(String fileChangeMode) {
		this.fileChangeMode = fileChangeMode;
	}

	private class ContinerPathListener extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633444210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("query-container"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage clientMessage = myAgent.receive(mt);
				if (clientMessage != null) {
					containerPathAsked = false;
					ContainerInformationsWrapper wrapper = (ContainerInformationsWrapper) getContentManager()
							.extractContent(clientMessage);
					fileChangeMode = wrapper.getFilechangemode();
					for (int i = 0; i < wrapper.getContainerInformations().getContainerlist().size(); i++) {
						ContainerInformation information = (ContainerInformation) wrapper.getContainerInformations()
								.getContainerlist().get(i);
						if (getContainerController().getContainerName().equals(information.getContainername())) {
							containerPath = information.getContainerpath();
							System.out.println("containerPath: " + containerPath + "for agent : " + getName());
							return;
						}
					}
					System.out.println("aaaaaaa containerPath: " + containerPath + "for agent : " + getName());
					removeBehaviour(this);
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println("ContinerPathListener : " + e);
			}
		}
	}

	protected void addTimeBehaviour() {
		addBehaviour(new TimeRefresher(this, 10000));
		addBehaviour(new TimeListener());
	}

	protected class TimeRefresher extends TickerBehaviour {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7957746469484416493L;

		public TimeRefresher(Agent a, long period) {
			super(a, period);
		}

		@Override
		protected void onTick() {
			askTime();
		}
	}

	protected void askTime() {
		ACLMessage callTimer = new ACLMessage(ACLMessage.REQUEST);

		// findTimerAgent
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("TimerAgent");
		template.addServices(sd);

		DFAgentDescription[] result;
		try {
			result = DFService.search(this, template);
			callTimer.setConversationId("get-current-time");
			callTimer.setOntology(TimerOntology.NAME);
			callTimer.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
			if (result != null && result.length > 0) {
				for (int i = 0; i < result.length; ++i) {
					callTimer.addReceiver(result[i].getName());
				}
				send(callTimer);
			}
		} catch (FIPAException e) {
			System.out.println("askTime" + e);
		}
	}

	public Long getGlobalTime() {
		return globalTime;
	}

	public void setGlobalTime(Long globalTime) {
		this.globalTime = globalTime;
	}

	private class TimeListener extends CyclicBehaviour {

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("get-current-time"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					Timer timer = (Timer) getContentManager().extractContent(msg);
					globalTime = timer.getTime();
					// System.out.println("globalTime : " + globalTime);
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println("TimeListener : " + e);
			}
		}

	}

	protected void askContainerList() {
		try {
			if (!containerListAsked) {
				System.out.println("containerList is asked");
				containerListAsked = true;
				QueryPlatformLocationsAction query = new QueryPlatformLocationsAction();
				Action action = new Action(getAID(), query);

				ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
				message.addReceiver(getAMS());
				message.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
				message.setOntology(JADEManagementOntology.getInstance().getName());
				getContentManager().fillContent(message, action);
				addBehaviour(new ListenContainerListBehaviour());
				send(message);
			} else {
				System.out.println("containerList is ALREADY asked");
			}
		} catch (Exception e) {
			System.out.println("askContainerList" + e);
		}
	}

	class ListenContainerListBehaviour extends CyclicBehaviour {

		@Override
		public void action() {
			ACLMessage receivedMessage = myAgent.receive(MessageTemplate.MatchSender(myAgent.getAMS()));
			if (receivedMessage != null) {

				containerListAsked = false;
				ContentElement content = null;
				agentMoveContainerList.clear();
				broadcastContainerList.clear();
				try {
					content = myAgent.getContentManager().extractContent(receivedMessage);
				} catch (CodecException | OntologyException e) {
					System.out.println("ListenContainerListBehaviour" + e);
				}

				Result result = (Result) content;
				List listOfPlatforms = (List) result.getValue();
				// use it
				Iterator iter = listOfPlatforms.iterator();
				while (iter.hasNext()) {
					ContainerID next = (ContainerID) iter.next();
					agentMoveContainerList.add(next);
					broadcastContainerList.add(next);
				}
				System.out.println("ContainerListsize has arrived to us: " + listOfPlatforms.size());
				goToNextContainer(this);
			} else {
				block();
			}
		}

	}

	protected void sleepForAWhile() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void goToNextContainer(Behaviour behaviour) {
		// TODO Auto-generated method stub
		// it will be overrided if needed
	}

	public List getAgentMoveContainerList() {
		return agentMoveContainerList;
	}

	public void setAgentMoveContainerList(List agentMoveContainerList) {
		this.agentMoveContainerList = agentMoveContainerList;
	}

	public List getBroadcastContainerList() {
		return broadcastContainerList;
	}

	public void setBroadcastContainerList(List broadcastContainerList) {
		this.broadcastContainerList = broadcastContainerList;
	}

}
