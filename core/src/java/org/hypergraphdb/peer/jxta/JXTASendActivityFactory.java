package org.hypergraphdb.peer.jxta;

import net.jxta.document.Advertisement;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.protocol.PipeAdvertisement;

import org.apache.servicemix.beanflow.Activity;
import org.hypergraphdb.peer.workflow.ActivityFactory;

public class JXTASendActivityFactory implements ActivityFactory
{
	private PeerGroup peerGroup;
	private Advertisement pipeAdv;
	
	public JXTASendActivityFactory(PeerGroup peerGroup, PipeAdvertisement pipeAdv)
	{
		this.peerGroup = peerGroup;
		this.pipeAdv = pipeAdv;
	}
	
	@Override
	public Activity createActivity()
	{
		return new JXTASendActivity(peerGroup, pipeAdv);
	}

}
