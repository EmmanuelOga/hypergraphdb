package org.hypergraphdb.peer.workflow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import net.jxta.document.Advertisement;

import org.apache.servicemix.beanflow.AbstractActivity;
import org.apache.servicemix.beanflow.Activity;
import org.apache.servicemix.beanflow.ActivityHelper;
import org.hypergraphdb.peer.protocol.Message;

public abstract class PeerFilter 
{
	protected Object targetDescription;
	private ActivityFactory activityFactory;
	private Message msg;
	
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
/*		PeerRelatedActivity newActivity = (PeerRelatedActivity)activityFactory.createActivity();
		newActivity.setMessage(msg);
		newActivity.setTarget(target);
		activities.add(newActivity);
		
		ActivityHelper.start(newActivity);
*/	}
	
	
	public Object getTargetDescription()
	{
		return targetDescription;
	}

	public void setTargetDescription(Object targetDescription)
	{
		this.targetDescription = targetDescription;
	}

}
