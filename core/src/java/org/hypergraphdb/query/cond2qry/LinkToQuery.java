/* 
 * This file is part of the HyperGraphDB source distribution. This is copyrighted 
 * software. For permitted uses, licensing options and redistribution, please see  
 * the LicensingInformation file at the root level of the distribution.  
 * 
 * Copyright (c) 2005-2010 Kobrix Software, Inc.  All rights reserved. 
 */
package org.hypergraphdb.query.cond2qry;

import java.util.ArrayList;
import java.util.Iterator;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.HGQueryCondition;
import org.hypergraphdb.query.IncidentCondition;
import org.hypergraphdb.query.LinkCondition;
import org.hypergraphdb.query.impl.IntersectionQuery;
//import org.hypergraphdb.query.impl.SortedIntersectionResult;
import org.hypergraphdb.query.impl.ZigZagIntersectionResult;
import org.hypergraphdb.util.Ref;

public class LinkToQuery implements ConditionToQuery
{

	public QueryMetaData getMetaData(HyperGraph graph, HGQueryCondition c)
	{
		QueryMetaData qmd;
		if (((LinkCondition)c).targets().size() == 0)
		{
			qmd = QueryMetaData.EMPTY.clone(c);
		}
		else
		{
			qmd = QueryMetaData.ORDERED.clone(c);
		}
		qmd.predicateCost = 0.5;
		return qmd;
	}

	public HGQuery<?> getQuery(final HyperGraph graph, final HGQueryCondition c)
	{
		final LinkCondition lc = (LinkCondition)c;
		ArrayList<HGQuery<?>> L = new ArrayList<HGQuery<?>>();
		for (Ref<HGHandle> t : lc.targets())
			L.add(ToQueryMap.toQuery(graph, new IncidentCondition(t)));
		if (L.isEmpty())
			return HGQuery.NOP;
		else if (L.size() == 1)
			return L.get(0);
		else
		{
			Iterator<HGQuery<?>> i = L.iterator();
			IntersectionQuery result = new IntersectionQuery(i.next(), 
															 i.next(), 
															 new ZigZagIntersectionResult<HGHandle>());
			while (i.hasNext())
				result = new IntersectionQuery(i.next(), 
											   result,
											   new ZigZagIntersectionResult<HGHandle>());
			return result;
		}
	}
}