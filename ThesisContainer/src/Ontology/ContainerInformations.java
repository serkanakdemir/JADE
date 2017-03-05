package Ontology;

import jade.content.Concept;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

public class ContainerInformations implements Concept {
	private List containerlist = new ArrayList();

	public List getContainerlist() {
		return containerlist;
	}

	public void setContainerlist(List containerlist) {
		this.containerlist = containerlist;
	}

}
