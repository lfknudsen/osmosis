// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.xml.v0_6.impl;

import org.openstreetmap.osmosis.core.OsmosisRuntimeException;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;


/**
 * Parses the xml representation of a relation member type into an entity type
 * object.
 * 
 * @author Brett Henderson
 */
public class MemberTypeParser {
	/**
	 * Parses the database representation of a relation member type into an
	 * entity type object.
	 * 
	 * @param memberType
	 *            The database value of member type.
	 * @return A strongly typed entity type.
	 */
	public EntityType parse(String memberType) {
		return switch (memberType) {
			case "node" 	-> EntityType.Node;
			case "way" 		-> EntityType.Way;
			case "relation" -> EntityType.Relation;
			default -> throw new OsmosisRuntimeException("The member type " + memberType + " is not recognised.");
		};
	}

	public EntityType parse(char memberType) {
		return switch (memberType) {
			case 'n' -> EntityType.Node;
			case 'w' -> EntityType.Way;
			case 'r' -> EntityType.Relation;
			default -> throw new OsmosisRuntimeException("The member type " + memberType + " is not recognised.");
		};
	}
}
