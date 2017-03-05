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
import jade.content.schema.ConceptSchema;
import jade.content.schema.ObjectSchema;
import jade.content.schema.PredicateSchema;
import jade.content.schema.PrimitiveSchema;
import jade.content.schema.TermSchema;

public class ContainerInformationOntology extends Ontology {

	public static final String NAME = "container-info-ontology";

	// VOCABULARY
	// Concepts
	public static final String CONTAINER_INFO = "CONTAINER_INFO";
	public static final String CONTAINER_INFO_WRAPPER = "CONTAINER_INFO_WRAPPER";
	public static final String CONTAINER_INFO_LIST = "CONTAINER_INFO_LIST";

	// Actions
	public static final String SHOW = "SHOW";
	public static final String CONTAINER_INFO_ITEM = "containerlist";
	public static final String CONTAINER_INFO_WRAPPER_ITEM = "containerInformations";
	public static final String CONTAINER_INFO_WRAPPER_FILE_CHANGE_INFO = "filechangemode";

	public static final String CONTAINER_NAME = "containername";
	public static final String CONTAINER_PATH = "containerpath";

	private static Ontology theInstance = new ContainerInformationOntology();

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
	private ContainerInformationOntology() {
		// __CLDC_UNSUPPORTED__BEGIN
		super(NAME, BasicOntology.getInstance());

		try {
			PrimitiveSchema stringSchema = (PrimitiveSchema) getSchema(BasicOntology.STRING);

			add(new ConceptSchema(CONTAINER_INFO_LIST), ContainerInformations.class);
			add(new ConceptSchema(CONTAINER_INFO), ContainerInformation.class);
			add(new PredicateSchema(CONTAINER_INFO_WRAPPER), ContainerInformationsWrapper.class);

			ConceptSchema cs = (ConceptSchema) getSchema(CONTAINER_INFO_LIST);
			cs.add(CONTAINER_INFO_ITEM, (ConceptSchema) getSchema(CONTAINER_INFO), 1, ObjectSchema.UNLIMITED);

			cs = (ConceptSchema) getSchema(CONTAINER_INFO);
			cs.add(CONTAINER_NAME, (TermSchema) getSchema(BasicOntology.STRING));
			cs.add(CONTAINER_PATH, (TermSchema) getSchema(BasicOntology.STRING));

			PredicateSchema ps = (PredicateSchema) getSchema(CONTAINER_INFO_WRAPPER);
			ps.add(CONTAINER_INFO_WRAPPER_ITEM, getSchema(CONTAINER_INFO_LIST));
			ps.add(CONTAINER_INFO_WRAPPER_FILE_CHANGE_INFO, stringSchema, ObjectSchema.MANDATORY);
		} catch (OntologyException oe) {
			oe.printStackTrace();
		}
	}
}
