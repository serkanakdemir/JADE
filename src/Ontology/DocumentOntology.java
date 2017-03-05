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

public class DocumentOntology extends Ontology {

	public static final String NAME = "document-ontology";

	// VOCABULARY
	// Concepts
	public static final String REMOTEDOCUMENTPATH = "REMOTEDOCUMENTPATH";
	public static final String LOCALDOCUMENTPATH = "LOCALDOCUMENTPATH";
	public static final String DOCUMENTNAME = "DOCUMENTNAME";
	public static final String DOCUMENTTYPE = "DOCUMENTTYPE";
	public static final String LOCALVERSION = "LOCALVERSION";
	public static final String REMOTEVERSION = "REMOTEVERSION";
	public static final String RESPONSE = "RESPONSE";
	public static final String BYTEARRAY = "BYTEARRAY";

	// Actions
	public static final String SHOW = "SHOW";
	public static final String ADD = "ADD";
	public static final String UPDATE = "UPDATE";
	public static final String DELETE = "DELETE";
	// Predicates
	public static final String DOCUMENT = "DOCUMENT";

	private static Ontology theInstance = new DocumentOntology();

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
	private DocumentOntology() {
		// __CLDC_UNSUPPORTED__BEGIN
		super(NAME, BasicOntology.getInstance());

		try {
			PrimitiveSchema stringSchema = (PrimitiveSchema) getSchema(BasicOntology.STRING);
			PrimitiveSchema floatSchema = (PrimitiveSchema) getSchema(BasicOntology.FLOAT);
			PrimitiveSchema byteSchema = (PrimitiveSchema) getSchema(BasicOntology.BYTE_SEQUENCE);

			add(new PredicateSchema(DOCUMENT), Document.class);
			PredicateSchema documentSchema = (PredicateSchema) getSchema(DOCUMENT);
			documentSchema.add(DOCUMENTNAME, stringSchema, ObjectSchema.MANDATORY);
			documentSchema.add(DOCUMENTTYPE, stringSchema, ObjectSchema.MANDATORY);
			documentSchema.add(LOCALDOCUMENTPATH, stringSchema, ObjectSchema.MANDATORY);
			documentSchema.add(REMOTEDOCUMENTPATH, stringSchema, ObjectSchema.MANDATORY);
			documentSchema.add(REMOTEVERSION, floatSchema, ObjectSchema.OPTIONAL);
			documentSchema.add(LOCALVERSION, floatSchema, ObjectSchema.OPTIONAL);
			documentSchema.add(RESPONSE, stringSchema, ObjectSchema.OPTIONAL);
			documentSchema.add(BYTEARRAY, byteSchema, ObjectSchema.OPTIONAL);

		} catch (OntologyException oe) {
			oe.printStackTrace();
		}
	}
}
