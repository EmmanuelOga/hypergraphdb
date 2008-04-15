package org.hypergraphdb.app.tm;

import java.util.ArrayList;
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
	Set<HGTopic> types = null;
	Set<HGOccurrence> occurrences = null;
	Set<HGTopicName> names = null;
	Set<HGAssociationRole> roles = null;
	
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
		if (getTypes().contains(type))
			return;
		types.add((HGTopic)type);
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
				graph.add(new HGRel(HGTM.TypeOf, new HGHandle[] {graph.getHandle(type), resultHandle}), 
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
			getOccurrences().add(result);
			graph.getTransactionManager().commit();
		}
		catch (RuntimeException ex)
		{
			graph.getTransactionManager().abort();
			throw ex;
		}
	}
	
	public HGOccurrence createOccurrence(String value, Topic type, Collection scope)
	{
		HGOccurrence result = new HGOccurrence();
		result.setValue(value);
		addOccurrence(result, type, scope);
		return result;
	}

	public HGOccurrence createOccurrence(Locator resource, Topic type, Collection scope)
	{
		HGOccurrence result = new HGOccurrence();
		result.setResource(resource);
		addOccurrence(result, type, scope);
		return result;
	}
	
	public HGTopicName createTopicName(String value)
	{
		return createTopicName(value, (Collection)null);
	}

	public HGTopicName createTopicName(String value, Collection scope) throws MergeException
	{
		return createTopicName(value, null, scope);
	}

	public HGTopicName createTopicName(String value, Topic...scope)
	{
		ArrayList<Topic> A = new ArrayList<Topic>();
		if (scope != null)
			for (Topic t : scope) if (t != null) A.add(t);
		return createTopicName(value, null, A);
	}
	
	public HGTopicName createTopicName(String value, Topic type, Collection scope) 
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
			getTopicNames().add(result);
			graph.getTransactionManager().commit();
			return result;
		}
		catch (RuntimeException ex)
		{
			graph.getTransactionManager().abort();
			throw ex;
		}
	}

	public Set<HGOccurrence> getOccurrences()
	{
		if (occurrences == null)
			occurrences = U.getRelatedObjects(graph, HGTM.hOccurrence, null, graph.getHandle(this));
		return occurrences;
	}

	public Set<HGTopic> getReified()
	{
		return U.getRelatedObjects(graph, HGTM.hReifierOf, graph.getHandle(this), null);
	}

	public Set<HGAssociationRole> getRolesPlayed()
	{
		if (roles != null)
			return roles;
		Set<HGAssociationRole> result = new HashSet<HGAssociationRole>(); 
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
			return roles = result;
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

	public Set<HGTopicName> getTopicNames()
	{
		if (names == null)
			names = U.getRelatedObjects(graph, HGTM.hNameOf, null, graph.getHandle(this)); 
		return names;
	}

	public HGTopicName getDefaultName()
	{
		for (HGTopicName name : getTopicNames())
			if (name.getScope().isEmpty())
				return name;
		return null;
	}
	
	public HGTopic getType()
	{
		getTypes();
		if (types.size() == 0)
			return null;
		else
			return types.iterator().next();
	}
	
	public Set<HGTopic> getTypes()
	{
		if (types == null)
			types = U.getRelatedObjects(graph, HGTM.hTypeOf, null, graph.getHandle(this));
		return types;
	}
	
	/**
	 * <p>Return a set of all topics that are have <code>this</code> topic as a type.</p>
	 */
	public Set<HGTopic> getInstances()
	{
		return U.getRelatedObjects(graph, HGTM.hTypeOf, graph.getHandle(this), null);
	}

	/**
	 * <p>
	 * Return all topic map elements that are scoped by this topic.
	 * </p>
	 */
	public Set<HGTopicMapObjectBase> getScoped()
	{
		return U.getRelatedObjects(graph, HGTM.hScopeOf, null, graph.getHandle(this));		
	}
	
	public Set<HGOccurrence> getOccurrencesByType(Topic type)
	{
		Set<HGOccurrence> result = new HashSet<HGOccurrence>();
		for (HGOccurrence occ : getOccurrences())
		{
			if (occ.getType() == type)
				result.add(occ);
		}
		return result;
	}

	/**
	 * 
	 * <p>
	 * Get a single occurrence with the specified type.
	 * </p>
	 *
	 * @param type The type of the occurrence.
	 * @return
	 */
	public HGOccurrence getOccurrence(Topic type)
	{
		Set<HGOccurrence> result = getOccurrencesByType(type);
		if (result.size() > 0)
			return result.iterator().next();
		else
			return null;
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
		{
			graph.remove(rel);
			if (types != null)
				types.remove(type);
		}
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
			U.dettachFromMap(graph, thisH);
			Set toRemove = new HashSet();
			
			toRemove.addAll(getTopicNames());
			for (TopicName n : (Collection<TopicName>)toRemove)
				n.remove();			
			toRemove.clear();
			toRemove.addAll(getOccurrences());
			for (Occurrence o : (Collection<Occurrence>)toRemove)
				o.remove();
			Set reified = U.getRelatedObjects(graph, HGTM.hReifierOf, graph.getHandle(this), null);
			for (Object x : reified)
				U.setReifierOf(graph, graph.getHandle(x), null);			
			toRemove.clear();
			toRemove.addAll(getSourceLocators());			
			for (Locator l : (Collection<Locator>)toRemove)
				removeSourceLocator(l);
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
	 * Remove the topic and all its associations, and all things in its scope,
	 * anything that could prevent it from being removed. Note note this will remove
	 * all topics that have this one listed as their type and/or listed as their scope.
	 * In fact, calling this method is equivalent to calling <code>forceRemove(true, true)</code>
	 * </p>
	 * 
	 * @throws TMAPIException
	 */
	public void forceRemove() throws TMAPIException
	{
		forceRemove(true, true);
	}
	
	/**
	 * <p>
	 * Remove the topic and all its associations. The topic will be dissociated from
	 * scoping an object or typing another topic. In addition, if topics that are instances 
	 * of this topic should be removed, pass <code>true</code> in the <code>removeInstances</code>
	 * parameter. Similarly, whether object having this topic as (part of) their scope should
	 * be removed is controlled by the <code>removeScoped</code> parameter.
	 * </p>
	 * 
	 * @param removeInstances Whether to remove topics having this topic in their types list.
	 * @param removeScoped Whether to remove topic map objects having this topic in their scope. 
	 * @throws TMAPIException
	 */	
	public void forceRemove(boolean removeInstances, boolean removeScoped) throws TMAPIException
	{
		// From associations
		Collection<HGAssociationRole> roles = new ArrayList<HGAssociationRole>(getRolesPlayed().size()); 
		roles.addAll(getRolesPlayed());
		for (HGAssociationRole role : roles)
			role.remove();		
		 
		if (removeInstances)
			for (HGTopic t : getInstances())
				t.remove();
		else
			U.removeRelations(graph, HGTM.hTypeOf, graph.getHandle(this), null);
		
		if (removeScoped)
			for (HGTopicMapObjectBase x : getScoped())
				x.remove();
		else
			U.removeRelations(graph, HGTM.hScopeOf, null, graph.getHandle(this));
		
		remove();
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
	
	public String toString()
	{
		Set<HGTopicName> names = this.getTopicNames();
		for (HGTopicName n : names)
			if (n.getScope().size() == 0)
				return n.getValue();
		Set locators = this.getSourceLocators();
		if (locators.size() > 0)
		{ 
			String first = locators.iterator().next().toString();
			int idx = first.indexOf('#');
			if (idx > -1)
				return first.substring(idx + 1);
			else
				return first;
		}
		else
			return "topic";
	}
}