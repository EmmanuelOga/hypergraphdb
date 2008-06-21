package org.hypergraphdb.peer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Cipri Costa
 *
 * Implementors can filter peers that match  a give description. The result of the filter can be 
 * used as a target in any subclass of the <code>PeerRelatedActivity</code> class.
 */
public abstract class PeerFilter 
{
	protected Object targetDescription;
	
	private List<Object> targets = new ArrayList<Object>();

	public PeerFilter()
	{
	}
		
	public abstract void filterTargets();

	public Iterator<Object> iterator()
	{
		return targets.iterator();
	}

	protected void matchFound(Object target)
	{
		targets.add(target);
	}
	
	
	public Object getTargetDescription()
	{
		return targetDescription;
	}

	public void setTargetDescription(Object targetDescription)
	{
		this.targetDescription = targetDescription;
	}

}
