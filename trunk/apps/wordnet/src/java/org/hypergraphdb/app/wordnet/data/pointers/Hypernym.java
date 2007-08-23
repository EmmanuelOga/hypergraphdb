package org.hypergraphdb.app.wordnet.data.pointers;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.app.wordnet.data.Pointer;
public class Hypernym extends Pointer{	public Hypernym() 	{		super();
	}
	public Hypernym(HGHandle[] outgoingSet) 	{
		super(outgoingSet);
	}
}