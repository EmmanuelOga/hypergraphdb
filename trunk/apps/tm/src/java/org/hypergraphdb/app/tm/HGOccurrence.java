package org.hypergraphdb.app.tm;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.annotation.AtomReference;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.atom.HGRel;
import org.tmapi.core.Locator;
import org.tmapi.core.Occurrence;
import org.tmapi.core.TMAPIException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicInUseException;

public class HGOccurrence extends HGScopedObject implements Occurrence
{
	@AtomReference("symbolic")
	private Locator dataType;
	private String value;
	HGHandle topic;
	
	public HGOccurrence()
	{
	}
	
	@HGIgnore
	public Topic getReifier()
	{
		return (Topic)graph.get(U.getReifierOf(graph, graph.getHandle(this)));
	}
	
	@HGIgnore
	public void setReifier(Topic topic)
	{
		U.setReifierOf(graph, graph.getHandle(this), graph.getHandle(topic));
	}
	
	public Locator getResource()
	{
		return getDataType();
	}

	public Topic getTopic()
	{
		if (topic == null)
		{
			HGHandle rel = hg.findOne(graph, hg.and(hg.type(HGTM.hOccurrence), 
													hg.link(graph.getHandle(this))));
			topic = ((HGRel)graph.get(rel)).getTargetAt(1);
		}
		return (Topic)graph.get(topic);
	}

	@HGIgnore
	public Topic getType()
	{
		HGHandle type = U.getTypeOf(graph, graph.getHandle(this));
		return type != null ? (Topic)graph.get(type) : null;		
	}

	public String getValue()
	{
		return value;
	}

	public void setResource(Locator resource)
	{
		setDataType(resource);;
	}

	@HGIgnore
	public void setType(Topic newType)
	{
		U.setTypeOf(graph, graph.getHandle(this), graph.getHandle(newType));	
	}

	public void setValue(String value)
	{
		this.value = value;
	}
	
	public Locator getDataType()
	{
		return dataType;
	}
	
	public void setDataType(Locator dataType)
	{
		this.dataType = dataType;
	}	
	
	public void remove() throws TopicInUseException
	{
		HGHandle thisH = graph.getHandle(this);
		HGHandle reifier = U.getReifierOf(graph, thisH);
		if (reifier != null)
		{
			U.setReifierOf(graph, thisH, null);
			((Topic)graph.get(reifier)).remove();
		}				
		setType(null);
		graph.remove(thisH, false);
	}	
}