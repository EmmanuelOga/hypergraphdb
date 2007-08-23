package org.hypergraphdb.app.tm;

import java.util.Set;

import org.hypergraphdb.HGException;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.annotation.HGIgnore;
import org.tmapi.core.Association;
import org.tmapi.core.AssociationRole;
import org.tmapi.core.TMAPIException;
import org.tmapi.core.Topic;
import org.tmapi.core.TopicInUseException;

public class HGAssociation extends HGScopedObject implements Association, HGLink
{
	private HGHandle [] targetSet = HyperGraph.EMTPY_HANDLE_SET;

	public HGAssociation()
	{
	}
	
	public HGAssociation(HGHandle [] targetSet)
	{
		this.targetSet = targetSet;		 
	}
	
	public AssociationRole createAssociationRole(Topic player, Topic type)
	{
		HGAssociationRole result = new HGAssociationRole(
				new HGHandle[] {graph.getHandle(player), graph.getHandle(type)});
		result.graph = graph;
		return result;
	}

	public Set<HGAssociationRole> getAssociationRoles()
	{
		return new AssociationRoleSet(graph, targetSet);
	}

	public Topic getReifier()
	{
		return (Topic)graph.get(U.getReifierOf(graph, graph.getHandle(this)));
	}
	
	public void setReifier(Topic topic)
	{
		U.setReifierOf(graph, graph.getHandle(this), graph.getHandle(topic));
	}
	
	@HGIgnore
	public Topic getType()
	{
		return (Topic)graph.get(U.getTypeOf(graph, graph.getHandle(this)));
	}

	@HGIgnore
	public void setType(Topic type)
	{
		U.setTypeOf(graph, graph.getHandle(this), graph.getHandle(type));
	}

	public int getArity()
	{
		return targetSet.length;
	}

	public HGHandle getTargetAt(int i)
	{
		return targetSet[i];
	}

	public void notifyTargetHandleUpdate(int i, HGHandle handle)
	{
		targetSet[i] = handle;
	}	
	
    public void notifyTargetRemoved(int i)
    {
    	HGHandle [] newOutgoing = new HGHandle[targetSet.length - 1];
    	System.arraycopy(targetSet, 0, newOutgoing, 0, i);
    	System.arraycopy(targetSet, i + 1, newOutgoing, i, targetSet.length - i -1);
    	targetSet = newOutgoing;
    }
    
	void setTargetSet(HGHandle [] newTargetSet)
	{
		this.targetSet = newTargetSet;
	}
	
	public void remove() throws TopicInUseException
	{
		HGHandle thisH = graph.getHandle(this);
		setType(null);
		for (HGAssociationRole role : getAssociationRoles())
			try { role.remove(); } catch (TMAPIException ex) {throw new HGException(ex); }
		HGHandle reifier = U.getReifierOf(graph, thisH);
		if (reifier != null)
		{
			U.setReifierOf(graph, thisH, null);
			((Topic)graph.get(reifier)).remove();
		}
		graph.remove(thisH);
	}	
}