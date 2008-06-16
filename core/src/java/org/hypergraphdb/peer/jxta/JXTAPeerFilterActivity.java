package org.hypergraphdb.peer.jxta;

import java.util.Set;

import net.jxta.document.Advertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.hypergraphdb.peer.workflow.PeerFilterActivity;

public class JXTAPeerFilterActivity extends PeerFilterActivity
{
	private Set<Advertisement> advs;
	public JXTAPeerFilterActivity(Set<Advertisement> advs)
	{
		this.advs = advs;
	}

	@Override
	protected void filterTargets()
	{
		synchronized (advs)
		{
			for(Advertisement adv : advs)
			{
				if (shouldSend(adv))
				{
					matchFound(adv);
				}
			}
		}
		
		// TODO Auto-generated method stub
		
	}
	
	private boolean shouldSend(Advertisement adv)
	{
		//for the time being ... something very simple
		if ((targetDescription != null) && (adv instanceof PipeAdvertisement))
		{
			return targetDescription.toString().equals(((PipeAdvertisement)adv).getName());
		}
		
		return true;
	}

}
