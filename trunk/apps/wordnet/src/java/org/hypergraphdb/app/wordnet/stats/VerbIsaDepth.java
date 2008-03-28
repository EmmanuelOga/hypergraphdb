package org.hypergraphdb.app.wordnet.stats;

import org.hypergraphdb.app.wordnet.WNGraph;
import org.hypergraphdb.app.wordnet.data.KindOf;

public class VerbIsaDepth extends DepthStat
{
	@Override
	protected Long doCalculation()
	{
		WNGraph wngraph = new WNGraph(graph);
		return super.doCalculation(wngraph.getVerbIsaRoot(), 
								   wngraph.relatedGenerator(KindOf.class, false));

	}
}