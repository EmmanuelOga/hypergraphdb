package org.hypergraphdb.app.tm;

import java.util.Set;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.atom.HGRel;
import org.tmapi.core.DuplicateSourceLocatorException;
import org.tmapi.core.Locator;
import org.tmapi.core.TMAPIException;
import org.tmapi.core.TopicMap;
import org.tmapi.core.TopicMapObject;

public abstract class HGTopicMapObjectBase implements TopicMapObject, HGGraphHolder
{
	HyperGraph graph;
	
	public void setHyperGraph(HyperGraph graph)
	{
		this.graph = graph;
	}
	
	public void addSourceLocator(Locator l) throws DuplicateSourceLocatorException
	{
		HGHandle lh = U.findLocatorHandle(graph, l);
		if (lh == null)
			lh = graph.add(l);
		// check if this locator is locating something else
		Object x = U.getOneRelated(graph,HGTM.hSourceLocator, lh, null); 
		if (x != null && x != this)
			throw new DuplicateSourceLocatorException(this, 
													  this, 
													  l, 
													  "Attempt to add an existing source locator.");
		else
			graph.add(new HGRel(HGTM.SourceLocator, new HGHandle[] { lh, graph.getHandle(this)} ),
					HGTM.hSourceLocator);
	}

	public String getObjectId()
	{		
		HGHandle handle = graph.getHandle(this);
		return handle == null ? null : graph.getPersistentHandle(handle).toString();
	}

	public Set getSourceLocators()
	{
		return U.getRelatedObjects(graph, HGTM.hSourceLocator, null, graph.getHandle(this));
	}

	public TopicMap getTopicMap()
	{
		return U.getTopicMapOf(this);
	}

	public void removeSourceLocator(Locator l)
	{
		HGHandle lh = U.findLocatorHandle(graph, l);
		if (lh == null)
			return;
		HGHandle rel = hg.findOne(graph, 
		          hg.and(hg.type(HGTM.hSourceLocator), 
		        		 hg.orderedLink(lh, graph.getHandle(this))));
		if (rel != null)
			graph.remove(rel);
		// If this locator is not used in anything else, we may remove it.
		if (graph.getIncidenceSet(lh).length == 0)
			graph.remove(lh);
	}
}