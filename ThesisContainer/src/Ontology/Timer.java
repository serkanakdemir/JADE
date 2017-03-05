package Ontology;

import jade.content.Predicate;

public class Timer implements Predicate {
	private Long time = 0L;

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

}
