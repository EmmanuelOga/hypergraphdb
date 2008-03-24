package org.hypergraphdb.app.wordnet;

import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleFactory;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.app.wordnet.data.*;
import org.hypergraphdb.algorithms.*;
import org.hypergraphdb.query.HGAtomPredicate;

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
	 * <p>
	 * Return a generator for the <code>Isa</code> semantic relationships. The generator
	 * will examine atoms that are related to the current one through <code>Isa</code> or
	 * one of its derived relationship types (InstanceOf, KindOf).
	 * </p>
	 * 
	 * @param returnChildren Whether to include more specific (i.e. hyponym) concepts
	 * than the current one being examined.
	 * @param returnParents Whether to include more general (i.e. hypernym) concepts
	 * than the current one being examined.
	 * @return The adjency generator.
	 */
	public HGALGenerator isaRelatedGenerator(boolean returnChildren, boolean returnParents)
	{
		final HGAtomPredicate isaPred = hg.type(Isa.class);
		final HGAtomPredicate kindOfPred = hg.type(KindOf.class);
		final HGAtomPredicate instanceOfPred = hg.type(InstanceOf.class);
		final HGAtomPredicate pred = new HGAtomPredicate()
		{
			public boolean satisfies(HyperGraph graph, HGHandle handle)
			{
				return kindOfPred.satisfies(graph, handle) ||
					   instanceOfPred.satisfies(graph, handle) ||
					   isaPred.satisfies(graph, handle);
			}
		};
		return new DefaultALGenerator(graph, pred, null, returnChildren, returnParents, false);
	}
	
	/**
	 * <p>Get an atom adjency generator for a WordNet semantic relationship.</p>
	 * 
	 * @param type One of the <code>SemanticLink</code> sub-classes.
	 * @param leftToRight <code>true</code> if the second targets of the relationships
	 * should be traversed and <code>false</code> if the first targets should be
	 * traversed.  
	 */
	public HGALGenerator relatedGenerator(Class<? extends SemanticLink> type, boolean leftToRight)
	{
		return new DefaultALGenerator(graph, hg.type(type), null, !leftToRight, leftToRight, false);
	}
	
	public HGTraversal relatedDepthFirst(HGHandle start, Class<? extends SemanticLink> type, boolean leftToRight)
	{
		return new HGDepthFirstTraversal(start, relatedGenerator(type, leftToRight));
	}
	
	public HGTraversal relatedBreadthFirst(HGHandle start, Class<? extends SemanticLink> type, boolean leftToRight)
	{
		return new HGBreadthFirstTraversal(start, relatedGenerator(type, leftToRight));
	}
	
	public List<HGHandle> findRelated(HGHandle synset, Class<? extends SemanticLink> type, boolean leftToRight)
	{
		return hg.findAll(graph, hg.apply(hg.targetAt(graph, leftToRight ? 1 : 0),
				hg.and(hg.type(type), 
					   hg.incident(synset), 
					   hg.orderedLink(leftToRight ? synset : HGHandleFactory.anyHandle,
							   	      leftToRight ? HGHandleFactory.anyHandle : synset))
				));
	}
}