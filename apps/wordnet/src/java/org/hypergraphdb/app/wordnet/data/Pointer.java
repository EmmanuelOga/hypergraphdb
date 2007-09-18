package org.hypergraphdb.app.wordnet.data;
import org.hypergraphdb.HGHandle;import org.hypergraphdb.HGPlainLink;
/** * A <code>Pointer</code> encodes a lexical or semantic relationship between  * WordNet entities.  A lexical relationship holds between Words; a semantic  * relationship holds between Synsets.  Relationships are <it>directional</it>:   * the two roles of a relationship are the <it>source</it> and <it>target</it>. */public class Pointer extends HGPlainLink{	public Pointer() 	{
		super();
	}
	public Pointer(HGHandle[] outgoingSet) 	{
		super(outgoingSet);
	}
	public String toString()
	{
		return this.getClass().getName();
	}
	public void setTargets(HGHandle [] _outgoingSet)
	{
		outgoingSet = _outgoingSet; 
	}
}