
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class StartClass {

	public static void main(String[] args) throws StaleProxyException {
		Runtime runtime = Runtime.instance();
		Profile profile = new ProfileImpl();
		// profile.setParameter(Profile.GUI, Boolean.TRUE.toString());
		// profile.setParameter(Profile.MAIN_PORT, String.valueOf(8085));
		profile.setParameter(Profile.MAIN_HOST, "localhost");

		AgentContainer agentContainer = runtime.createAgentContainer(profile);
		if (agentContainer == null) {
			System.out.println("null");
		} else {
			System.out.println("oh bee");
		}

		Object[] arguments = new Object[1];
		arguments[0] = "CONTAINER_1";
		AgentController seller = agentContainer.createNewAgent("xxx", "Agents.GuiAgent", arguments);
		seller.start();
	}

}
