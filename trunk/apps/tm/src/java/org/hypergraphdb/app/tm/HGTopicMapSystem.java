package org.hypergraphdb.app.tm;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.tmapi.core.FeatureNotRecognizedException;
import org.tmapi.core.Locator;
import org.tmapi.core.TMAPIRuntimeException;
import org.tmapi.core.TopicMap;
import org.tmapi.core.TopicMapExistsException;
import org.tmapi.core.TopicMapSystem;
import org.hypergraphdb.*;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.query.AtomProjectionCondition;

public final class HGTopicMapSystem implements TopicMapSystem
{
	private HyperGraph graph;
	private boolean closeHGOnExit = false;
	
	public HGTopicMapSystem(HyperGraph hg)
	{
		this.graph = hg;
	}
	
	public HyperGraph getGraph()
	{
		return graph;
	}
	
	public void close()
	{
		if (closeHGOnExit)
			graph.close();
	}

	public List<TopicMap> getTopicMaps()
	{
		return hg.<TopicMap>findAll(graph, hg.apply(hg.deref(graph), hg.type(HGTopicMap.class)));
	}
	
	public TopicMap createTopicMap(String uri) throws TopicMapExistsException
	{
		Locator l = U.findLocator(graph, uri);
		if (l == null)
		{
			l = U.makeLocator(uri);
		}
		HGTopicMap result = new HGTopicMap();
		result.setBaseLocator(l);
		result.graph = graph;
		graph.add(result);
		return result;
	}

	public TopicMap createTopicMap(String baseLocatorReference, String baseLocatorNotation)
			throws TopicMapExistsException
	{
		if (!baseLocatorNotation.equals("URI"))
			throw new TMAPIRuntimeException("Unsupported locator notation '" + baseLocatorNotation + "'"); 
		return createTopicMap(baseLocatorReference);
	}

	public Set getBaseLocators()
	{
		HashSet<Object> result = new HashSet<Object>();
		for (HGHandle h : hg.<HGHandle>findAll(graph, new AtomProjectionCondition("baseLocator", hg.type(HGTopicMap.class))))
		{
			result.add(graph.get(h));
		}
		return result;
	}

	public boolean getFeature(String featureName) throws FeatureNotRecognizedException
	{
		return false;
	}

	public String getProperty(String propertyName)
	{
		return null;
	}

	public TopicMap getTopicMap(String baseLocatorReference)
	{
		Locator l = U.findLocator(graph, baseLocatorReference);
		return l == null ? null : getTopicMap(l);
	}

	public TopicMap getTopicMap(Locator baseLocator)
	{
		HGHandle h = hg.findOne(graph, hg.and(hg.type(HGTopicMap.class), 
											  hg.eq("baseLocator", baseLocator)));
		if (h == null)
			return null;
		else
		{
			HGTopicMap map = (HGTopicMap)graph.get(h);
			map.system = this;
			return map;
		}
	}

	public TopicMap getTopicMap(String baseLocatorReference, String baseLocatorNotation)
	{
		if (!baseLocatorNotation.equals("URI"))
			throw new TMAPIRuntimeException("Unsupported locator notation '" + baseLocatorNotation + "'");
		else
			return getTopicMap(baseLocatorReference);
	}
	
	public void load(String uri)
	{
//		TMXMLUtils.load(uri, this);
	}
}