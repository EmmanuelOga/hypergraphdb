package org.hypergraphdb.app.owl.versioning;

import org.hypergraphdb.HGPersistentHandle;

/**
 * A RevisionID is unique in the context of one repository.
 * It is meaningfully ordered in the context of one repository.
 * 
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 13, 2012
 */
public class RevisionID implements Comparable<RevisionID> {

	/**
	 * Represents the first revision.
	 * Before this revision no changes were recorded.
	 * e.g. after import, the empty ontology after creation.
	 */
	public static final int REVISION_FIRST = 0;

	public static final int REVISION_INCREMENT = 1;

	/**
	 * Persistent handle to the OWLOntology that this Revision represents.
	 */
	private HGPersistentHandle ontologyUUID;
	
	private int revision;
	
	public RevisionID() {
		//do nothing
		revision = REVISION_FIRST;
	}
	
	public RevisionID(HGPersistentHandle ontologyUUID, int revision) {
		this.ontologyUUID = ontologyUUID;
		this.revision = revision;
	}
	
	public HGPersistentHandle getOntologyUUID() {
		return ontologyUUID;
	}
	
	public void setOntologyUUID(HGPersistentHandle ontologyID) {
		this.ontologyUUID = ontologyID;
	}
		
	public int getRevision() {
		return revision;	
	}
	
	public void setRevision(int revision) {
		this.revision = revision;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(RevisionID o) {
		if (o == this) {
			return 0;
		} else {
			int ontoComp = ontologyUUID.compareTo(o.getOntologyUUID());
			if (ontoComp != 0) {
				return ontoComp;
			} else {
				return (revision < o.getRevision() ? -1 : (revision == o.getRevision() ? 0 : 1));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return ontologyUUID.hashCode() + 13 * revision; 
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) {
		    return true;
		}
		if (anObject instanceof RevisionID) {
			RevisionID other = (RevisionID) anObject;
			return compareTo(other) == 0;
		} else {
			return false;
		}
	}	
}