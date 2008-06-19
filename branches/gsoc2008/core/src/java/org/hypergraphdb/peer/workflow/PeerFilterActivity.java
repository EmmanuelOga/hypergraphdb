package org.hypergraphdb.peer.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import net.jxta.document.Advertisement;

import org.apache.servicemix.beanflow.AbstractActivity;
import org.apache.servicemix.beanflow.Activity;
import org.apache.servicemix.beanflow.ActivityHelper;
import org.hypergraphdb.peer.protocol.Message;

public abstract class PeerFilterActivity extends AbstractActivity
{
	protected Object targetDescription;
	private ActivityFactory activityFactory;
	private Message msg;
	
	private List<Activity> activities = new ArrayList<Activity>();

	public PeerFilterActivity()
	{
	}
		
	@Override
	public void run()
	{
		filterTargets();
		
		//wait until at least one stops
		
//		stop();		
	}

	protected void matchFound(Object target)
	{
		PeerRelatedActivity newActivity = (PeerRelatedActivity)activityFactory.createActivity();
		newActivity.setMessage(msg);
		newActivity.setTarget(target);
		activities.add(newActivity);
		
		ActivityHelper.start(newActivity);
	}
	
	@Override
	public void startWithTimeout(Timer arg0, long arg1)
	{
		// TODO Auto-generated method stub
		
	}
	
	protected abstract void filterTargets();

	public Object getTargetDescription()
	{
		return targetDescription;
	}

	public void setTargetDescription(Object targetDescription)
	{
		this.targetDescription = targetDescription;
	}

	public ActivityFactory getActivityFactory()
	{
		return activityFactory;
	}

	public void setActivityFactory(ActivityFactory activityFactory)
	{
		this.activityFactory = activityFactory;
	}

	public Message getMessage()
	{
		return msg;
	}

	public void setMessage(Message msg)
	{
		this.msg = msg;
	}
	
}
