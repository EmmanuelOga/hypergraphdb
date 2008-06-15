package org.hypergraphdb.peer.workflow;

import java.util.Timer;

import org.apache.servicemix.beanflow.AbstractActivity;
import org.apache.servicemix.beanflow.Activity;
import org.apache.servicemix.beanflow.ActivityHelper;

public class RememberClientActivity extends AbstractActivity
{
	private ActivityFactory sendActivityFactory;
	private Activity receiveActivity;
	private PeerFilterActivity peerFilter;
	
	public RememberClientActivity(PeerFilterActivity peerFilter, ActivityFactory sendActivityFactory)
	{
		this.peerFilter = peerFilter;
		this.sendActivityFactory = sendActivityFactory;
	}

	@Override
	public void run()
	{
		peerFilter.setActivityFactory(sendActivityFactory);
		//ActivityHelper.start(receiveActivity);

		ActivityHelper.start(peerFilter);
		
	}

	@Override
	public void startWithTimeout(Timer arg0, long arg1)
	{
		// TODO Auto-generated method stub
		
	}

	
}
