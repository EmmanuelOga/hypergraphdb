package org.hypergraphdb.app.tm;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.atom.HGRel;
import org.hypergraphdb.util.HGUtils;
import org.tmapi.core.AssociationRole;
import org.tmapi.core.Locator;
import org.tmapi.core.MergeException;
import org.tmapi.core.ModelConstraintException;
import org.tmapi.core.Occurrence;
import org.tmapi.core.TMAPIException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicInUseException;
import org.tmapi.core.TopicName;

public class HGTopic extends HGTopicMapObjectBase implements Topic
{	
	public HGTopic()
	{
	}
	
	public void addSubjectIdentifier(Locator subjectIdentifier) throws MergeException
	{
		HGHandle locHandle = graph.getHandle(subjectIdentifier);
		graph.add(new HGRel(HGTM.SubjectIdentifier, new HGHandle[] { locHandle, graph.getHandle(this)}), 
				  HGTM.hSubjectIdentifier);		
	}

	public void addSubjectLocator(Locator subjectLocator) throws MergeException, ModelConstraintException
	{
		HGHandle locHandle = graph.getHandle(subjectLocator);
		graph.add(new HGRel(HGTM.SubjectLocator, new HGHandle[] { locHandle, graph.getHandle(this)} ),
				  HGTM.hSubjectLocator);
	}

	public void addType(Topic type)
	{
		HGHandle tHandle = graph.getHandle(type);
		graph.add(new HGRel(HGTM.TypeOf, new HGHandle[] { tHandle, graph.getHandle(this)} ),
				  HGTM.hTypeOf);		
	}
	
	private void addOccurrence(HGOccurrence result, Topic type, Collection scope)
	{
		graph.getTransactionManager().beginTransaction();
		try
		{			
			result.graph = graph;
			HGHandle resultHandle = graph.add(result);
			result.topic = graph.getHandle(this);
			if (type != null)
				graph.add(new HGRel(HGTM.TypeOf, new HGHandle[] {resultHandle, graph.getHandle(type)}), 
						  HGTM.hTypeOf);
			if (scope != null)
			{
				for (Iterator i = scope.iterator(); i.hasNext(); )
				{
					Topic t = (Topic)i.next();
					graph.add(new HGRel(HGTM.ScopeOf, 
										new HGHandle [] {resultHandle, graph.getHandle(t)}), 
							  HGTM.hScopeOf);
				}
			}
			graph.add(new HGRel(HGTM.Occurence, new HGHandle[] {resultHandle, graph.getHandle(this)}), 
					  HGTM.hOccurrence);
			graph.getTransactionManager().commit();
		}
		catch (RuntimeException ex)
		{
			graph.getTransactionManager().abort();
			throw ex;
		}
	}
	
	public Occurrence createOccurrence(String value, Topic type, Collection scope)
	{
		HGOccurrence result = new HGOccurrence();
		result.setValue(value);
		addOccurrence(result, type, scope);
		return result;
	}

	public Occurrence createOccurrence(Locator resource, Topic type, Collection scope)
	{
		HGOccurrence result = new HGOccurrence();
		result.setResource(resource);
		addOccurrence(result, type, scope);
		return result;
	}

	public TopicName createTopicName(String value, Collection scope) throws MergeException
	{
		return createTopicName(value, null, scope);
	}

	public TopicName createTopicName(String value, Topic type, Collection scope) 
		throws UnsupportedOperationException, MergeException
	{
		graph.getTransactionManager().beginTransaction();
		try
		{		
			HGTopicName result = new HGTopicName();
			result.value = value;
			result.graph = graph;
			HGHandle resultHandle = graph.add(result);
			if (type != null)
				U.setTypeOf(graph, resultHandle, graph.getHandle(type));
			if (scope != null)
			{
				for (Iterator i = scope.iterator(); i.hasNext(); )
				{
					Topic t = (Topic)i.next();
					graph.add(new HGRel(HGTM.ScopeOf, 
										new HGHandle [] {resultHandle, graph.getHandle(t)}), 
							  HGTM.hScopeOf);
				}
			}
			graph.add(new HGRel(HGTM.NameOf, new HGHandle[] {resultHandle, graph.getHandle(this)}), 
					  HGTM.hNameOf);
			graph.getTransactionManager().commit();
			return result;
		}
		catch (RuntimeException ex)
		{
			graph.getTransactionManager().abort();
			throw ex;
		}
	}

	public Set<Occurrence> getOccurrences()
	{
		return U.getRelatedObjects(graph, HGTM.hOccurrence, null, graph.getHandle(this));
	}

	public Set getReified()
	{
		return U.getRelatedObjects(graph, HGTM.hReifierOf, graph.getHandle(this), null);
	}

	public Set<AssociationRole> getRolesPlayed()
	{
		Set<AssociationRole> result = new HashSet<AssociationRole>(); 
		HGSearchResult<HGAssociationRole> rs = null;
		try
		{
			HGHandle thisH = graph.getHandle(this);
			rs = graph.find(hg.apply(hg.deref(graph), 
									 hg.and(hg.type(HGAssociationRole.class), 
											 		hg.incident(thisH))));			
			while (rs.hasNext())
			{
				HGAssociationRole role = rs.next();
				if (thisH.equals(role.getTargetAt(0)))
					result.add(role);					
			}
			return result;
		}
		finally
		{
			HGUtils.closeNoException(rs);
		}
	}

	public Set<Locator> getSubjectIdentifiers()
	{
		return U.getRelatedObjects(graph, HGTM.hSubjectIdentifier, null, graph.getHandle(this));
	}

	public Set<Locator> getSubjectLocators()
	{
		return U.getRelatedObjects(graph, HGTM.hSubjectLocator, null, graph.getHandle(this));
	}

	public Set<TopicName> getTopicNames()
	{
		return U.getRelatedObjects(graph, HGTM.hNameOf, null, graph.getHandle(this));
	}

	public Set<Topic> getTypes()
	{
		return U.getRelatedObjects(graph, HGTM.hTypeOf, null, graph.getHandle(this));
	}

	public void mergeIn(Topic other) throws MergeException
	{
	}

	public void removeSubjectIdentifier(Locator subjectIdentifier)
	{
		HGHandle locHandle = graph.getHandle(subjectIdentifier);
		HGHandle rel = hg.findOne(graph, 
						          hg.and(hg.type(HGTM.hSubjectIdentifier), 
						        		 hg.orderedLink(locHandle, graph.getHandle(this))));
		if (rel != null)
			graph.remove(rel);
	}

	public void removeSubjectLocator(Locator subjectLocator)
	{
		HGHandle locHandle = graph.getHandle(subjectLocator);
		HGHandle rel = hg.findOne(graph, 
						          hg.and(hg.type(HGTM.hSubjectLocator), 
						        		 hg.orderedLink(locHandle, graph.getHandle(this))));
		if (rel != null)
			graph.remove(rel);		
	}

	public void removeType(Topic type)
	{
		HGHandle typeHandle = graph.getHandle(type);
		HGHandle rel = hg.findOne(graph, 
						          hg.and(hg.type(HGTM.hTypeOf), 
						        		 hg.orderedLink(typeHandle, graph.getHandle(this))));
		if (rel != null)
			graph.remove(rel);		
	}

	public void remove() throws TopicInUseException
	{
		HGHandle thisH = graph.getHandle(this);
		if (!canRemove())
			throw new TopicInUseException("Topic '" + 
					thisH + 
					"' is either a type of something, or a scope defining topic, or a role player in a role.");
		try
		{
			for (TopicName n : getTopicNames())
				n.remove();
			for (Occurrence o : getOccurrences())
				o.remove();
			Set reified = U.getRelatedObjects(graph, HGTM.hReifierOf, graph.getHandle(this), null);
			for (Object x : reified)
				U.setReifierOf(graph, graph.getHandle(x), null);
			for (Locator l : getSubjectIdentifiers())
				removeSubjectIdentifier(l);
			for (Locator l : getSubjectLocators())
				removeSubjectLocator(l);
		}
		catch (TMAPIException ex)
		{
			throw new HGException(ex);
		}
		graph.remove(thisH, false);
	}
	
	/**
	 * <p>
	 * Return <code>true</code> if this topic is not currently in use and
	 * thus can be safely removed. When this method returns <code>false</code>,
	 * a call to remove will throw a <code>TopicInUseException</code>.
	 * </p>
	 */
	public boolean canRemove()
	{
		HGHandle thisH = graph.getHandle(this);		
		return U.getRelatedObjects(graph, HGTM.hTypeOf, thisH, null).isEmpty() &&
			   U.getRelatedObjects(graph, HGTM.hScopeOf, null, thisH).isEmpty() &&			   
			   getRolesPlayed().isEmpty();
	}
}