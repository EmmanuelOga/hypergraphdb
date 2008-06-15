package org.hypergraphdb.peer.jxta;

import net.jxta.peergroup.PeerGroup;

import org.apache.servicemix.beanflow.Activity;
import org.hypergraphdb.peer.workflow.ActivityFactory;

public class JXTASendActivityFactory implements ActivityFactory
{
	private PeerGroup peerGroup;
	
	public JXTASendActivityFactory(PeerGroup peerGroup)
	{
		this.peerGroup = peerGroup;
	}
	@Override
	public Activity createActivity()
	{
		return new JXTASendActivity(peerGroup);
	}

}
