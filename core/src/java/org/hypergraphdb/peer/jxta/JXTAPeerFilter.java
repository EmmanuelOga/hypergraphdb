package org.hypergraphdb.peer.jxta;

import java.util.Set;

import net.jxta.document.Advertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.hypergraphdb.peer.PeerFilter;
import org.hypergraphdb.util.Pair;

/**
 * @author Cipri Costa
 *
 * Object that finds all the known peers that match with some description. For the time being,
 * all it does is check the published name in the pipe advertisement and compare it with the 
 * description that is assumed to be a string.
 */
public class JXTAPeerFilter extends PeerFilter
{
	private Set<Pair<Advertisement, Advertisement>> advs;
	public JXTAPeerFilter(Set<Pair<Advertisement, Advertisement>> advs)
	{
		this.advs = advs;
	}

	@Override
	public void filterTargets()
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
