package org.hypergraphdb.app.wordnet.data;
import org.hypergraphdb.HGHandle;import org.hypergraphdb.HGPlainLink;
/** * A <code>Pointer</code> encodes a lexical or semantic relationship between  * WordNet entities.  A lexical relationship holds between Words; a semantic  * relationship holds between Synsets.  Relationships are <it>directional</it>:   * the two roles of a relationship are the <it>source</it> and <it>target</it>. */public class Pointer extends HGPlainLink{    //strong: sourceIndex is zero-based here, but in data.* files	//it's starting from 1	private int sourceIndex;	private int targetIndex;
	public Pointer() 	{
		super();
	}
	public Pointer(HGHandle[] outgoingSet) 	{
		super(outgoingSet);
	}
	public int getSourceIndex() 	{
		return sourceIndex;
	}
	public int getTargetIndex() 	{
		return targetIndex;
	}
	public void setSourceIndex(int sourceIndex) 	{
		this.sourceIndex = sourceIndex;
	}
	public void setTargetIndex(int targetIndex) 	{
		this.targetIndex = targetIndex;
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