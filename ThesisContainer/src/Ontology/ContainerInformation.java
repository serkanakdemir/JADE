package Ontology;

import jade.content.Concept;

public class ContainerInformation implements Concept {
	private String containername;
	private String containerpath;

	public String getContainername() {
		return containername;
	}

	public void setContainername(String containername) {
		this.containername = containername;
	}

	public String getContainerpath() {
		return containerpath;
	}

	public void setContainerpath(String containerpath) {
		this.containerpath = containerpath;
	}

}
