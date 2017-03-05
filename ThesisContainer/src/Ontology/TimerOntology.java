/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
*****************************************************************/

package Ontology;

import jade.content.onto.BasicOntology;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;

public class TimerOntology extends Ontology {

	public static final String NAME = "timer-ontology";

	// VOCABULARY
	// Concepts
	public static final String TIME_CONCEPT = "time";

	// Actions
	public static final String GET_TIME = "GET_TIME";

	// Predicates
	public static final String TIME_PREDICATE = "TIME_PREDICATE";

	private static Ontology theInstance = new TimerOntology();

	/**
	 * This method grants access to the unique instance of the ontology.
	 * 
	 * @return An <code>Ontology</code> object, containing the concepts of the
	 *         ontology.
	 */
	public static Ontology getInstance() {
		return theInstance;
	}

	/**
	 * Constructor
	 */
	private TimerOntology() {
		// __CLDC_UNSUPPORTED__BEGIN
		super(NAME, BasicOntology.getInstance());

		try {
			PrimitiveSchema floatSchema = (PrimitiveSchema) getSchema(BasicOntology.INTEGER);
			add(new PredicateSchema(TIME_PREDICATE), Timer.class);
			PredicateSchema timeSchema = (PredicateSchema) getSchema(TIME_PREDICATE);
			timeSchema.add(TIME_CONCEPT, floatSchema, ObjectSchema.MANDATORY);

		} catch (OntologyException oe) {
			oe.printStackTrace();
		}
	}
}
