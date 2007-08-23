package org.hypergraphdb.app.wordnet.data;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPlainLink;

public class SynsetLink extends HGPlainLink {
	/** The text (definition, usage examples) associated with the synset. */
	private String gloss;

	public SynsetLink() {
	}

	public SynsetLink(HGHandle[] targets) {
		super(targets);
	}

	public SynsetLink(String gloss, HGHandle[] targets) {
		super(targets);
		this.gloss = gloss;
	}

	public String getGloss() {
		return gloss;
	}
	
	public void setGloss(String g) {
		gloss = g;
	}
	
	public void setTargets(HGHandle [] targets)
	{
		outgoingSet = targets;
	}
	
	public boolean equals(Object object) {
		if (object == null || !(object instanceof SynsetLink))
			return false;
		SynsetLink link = (SynsetLink) object;
		
		if (link.getArity() != this.getArity())
			return false;
		if(link.getGloss() != null && !link.getGloss().equals(this.getGloss()))
			return false;
		for (int i = 0; i < this.getArity(); i++)
			if (!link.getTargetAt(i).equals(this.getTargetAt(i)))
				return false;
		return true;
	}
	
	public int hashCode() {
		return getGloss().hashCode();
	}

}
