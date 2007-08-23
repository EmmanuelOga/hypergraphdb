package org.hypergraphdb.app.tm;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGLink;
import org.hypergraphdb.annotation.HGIgnore;
import org.tmapi.core.Association;
import org.tmapi.core.AssociationRole;
import org.tmapi.core.TMAPIException;
import org.tmapi.core.Topic;

/**
 * 
 * <p>
 * Implements a topic map association role. An association role is implemented as an 
 * ordered link between three items (in that order): the role player, the role type and
 * the parent association. 
 * </p>
 * 
 * @author Borislav Iordanov
 *
 */
public class HGAssociationRole extends HGTopicMapObjectBase implements AssociationRole, HGLink
{
	private HGHandle [] targetSet = new HGHandle[3];
	
	public HGAssociationRole()
	{
	}
	
	public HGAssociationRole(HGHandle [] targetSet)
	{
		this.targetSet = targetSet;
	}
		
	public Association getAssociation()
	{
		return (Association)graph.get(targetSet[2]);
	}

	@HGIgnore
	public Topic getPlayer()
	{
		return (Topic)graph.get(targetSet[0]);
	}

	@HGIgnore
	public Topic getReifier()
	{
		return null;
	}

	@HGIgnore
	public Topic getType()
	{
		return (Topic)graph.get(targetSet[1]);
	}

	public void setPlayer(Topic player)
	{
		targetSet[0] = graph.getHandle(player);
	}

	public void setType(Topic type)
	{
		targetSet[1] = graph.getHandle(type);
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
	
	public void remove() throws TMAPIException
	{
		HGHandle thisH = graph.getHandle(this);
		HGHandle reifier = U.getReifierOf(graph, thisH);
		if (reifier != null)
		{
			U.setReifierOf(graph, thisH, null);
			((Topic)graph.get(reifier)).remove();
		}		
		graph.remove(thisH);
	}
	
    public void notifyTargetRemoved(int i)
    {
    	throw new IllegalArgumentException("Illegal attempt to remove a HGAssociationRole target.");
    }	
}