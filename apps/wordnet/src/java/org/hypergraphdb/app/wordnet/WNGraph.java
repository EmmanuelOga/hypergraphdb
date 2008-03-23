package org.hypergraphdb.app.wordnet;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.wordnet.data.*;
import org.hypergraphdb.algorithms.*;

/**
 * 
 * <p>
 * A collection of utility methods for applying graph traversals and
 * algorithms onto the HyperGraph representation of WordNet.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class WNGraph
{
	private HyperGraph graph;
	
	public WNGraph(HyperGraph graph)
	{
		this.graph = graph;
	}
	
	/**
	 * <p>Return the underlying HyperGraph instance.</p>
	 */
	public HyperGraph getGraph() 
	{
		return graph;
	}
	
	/**
	 * <p>Get an atom adjancy generator for a WordNet semantic relationship.</p>
	 * 
	 * @param type One of the <code>SemanticLink</code> sub-classes.
	 * @param leftToRight <code>true</code> if the second targets of the relationships
	 * should be traversed and <code>false</code> if the first targets should be
	 * traversed.  
	 */
	public HGALGenerator relatedGenerator(Class<? extends SemanticLink> type, boolean leftToRight)
	{
		return new DefaultALGenerator(graph, hg.type(type), null, leftToRight, !leftToRight, false);
	}
	
	public HGTraversal relatedDepthFirst(HGHandle start, Class<? extends SemanticLink> type, boolean leftToRight)
	{
		return new HGDepthFirstTraversal(start, relatedGenerator(type, leftToRight));
	}
	
	public HGTraversal relatedBreadthFirst(HGHandle start, Class<? extends SemanticLink> type, boolean leftToRight)
	{
		return new HGBreadthFirstTraversal(start, relatedGenerator(type, leftToRight));
	}	
}