package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;

/**
 * 
 * <p>
 * A semantic link that represents and adverb (first target) that was
 * derived from an adjective (second target). 
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class DerivedFrom extends SemanticLink
{
	public DerivedFrom() {
		super();
	}

	public DerivedFrom(HGHandle[] outgoingSet) {
		super(outgoingSet);
	}
}
