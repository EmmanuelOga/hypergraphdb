package org.hypergraphdb.peer.jxta;

import java.util.Set;

import net.jxta.document.Advertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.hypergraphdb.peer.workflow.PeerFilterActivity;
import org.hypergraphdb.util.Pair;

public class JXTAPeerFilterActivity extends PeerFilterActivity
{
	private Set<Pair<Advertisement, Advertisement>> advs;
	public JXTAPeerFilterActivity(Set<Pair<Advertisement, Advertisement>> advs)
	{
		this.advs = advs;
	}

	@Override
	protected void filterTargets()
	{
		synchronized (advs)
		{
			for(Pair<Advertisement, Advertisement> advPair : advs)
			{
				if (shouldSend(advPair.getFirst(), advPair.getSecond()))
				{
					matchFound(advPair);
				}
			}
		}
		
		// TODO Auto-generated method stub
		
	}
	
	private boolean shouldSend(Advertisement peerAdv, Advertisement pipeAdv)
	{
		//for the time being ... something very simple
		if ((targetDescription != null) && (pipeAdv instanceof PipeAdvertisement))
		{
			return targetDescription.toString().equals(((PipeAdvertisement)pipeAdv).getName());
		}
		
		return false;
	}

}
