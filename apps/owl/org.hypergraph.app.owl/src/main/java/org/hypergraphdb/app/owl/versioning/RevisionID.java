package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGPersistentHandle;

/**
 * A RevisionID is unique in the context of one repository.
 * It is meaningfully ordered in the context of one repository.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public interface RevisionID extends Comparable<RevisionID>{

	HGPersistentHandle getOntologyID();
	
	int getRevision();	
}
