package org.hypergraphdb.peer.jxta;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.id.IDFactory;
import net.jxta.membership.Authenticator;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.hypergraphdb.query.HGAtomPredicate;
import org.hypergraphdb.util.Pair;

/**
 * @author Cipri Costa
 *
 * <p>
 * Handles problems related to the JXTA network : intialize, stop, discovery, publishing, etc
 * </p>
 */
public class DefaultJXTANetwork implements JXTANetwork{
	private static int RDV_WAIT_TIMEOUT = 3000;
	
	private static NetworkManager peerManager = null;
	private static PeerGroup netPeerGroup = null;
	private static PeerGroup hgdbGroup = null;
	
	private LinkedList<Advertisement> ownAdvs = new LinkedList<Advertisement>();
	private Map<Advertisement, HGAtomPredicate> peerAdvs = Collections.synchronizedMap(new HashMap<Advertisement, HGAtomPredicate>());
	private Set<PipeID> ownPipes = new HashSet<PipeID>();
	
	public boolean init(JXTAPeerConfiguration config)
	{
		
	    try {
	    	System.out.println("Initializing instance " + config.getPeerName() + " ...");   	
	    	URI configURI = new File(new File(".jxta"), config.getPeerName()).toURI();
	    	
	    	System.out.println("Using config file: " + configURI.toString());
	    	peerManager = new NetworkManager(NetworkManager.ConfigMode.ADHOC, config.getPeerName(), configURI);
	    	
	    	dumpNetworkConfig(peerManager.getConfigurator());
	    	
	    	peerManager.startNetwork();
	    	
	    	//wait for rendezvous if needed
	    	if (config.getNeedsRdvConn())
	    	{
	    		boolean rdvFound = false;
	    		while (!rdvFound)
	    		{
		    		System.out.println("start waiting for rendezvous...");
		    		rdvFound = peerManager.waitForRendezvousConnection(3000);
	    			
		    		if (rdvFound) System.out.println("Rendevous found!");
		    		else System.out.println("No rendevous found...");
	    		}
	    	}

	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    
	    if (peerManager != null){
	    	
	    	netPeerGroup = peerManager.getNetPeerGroup();
	    	try
			{
				joinCustomGroup(config);
			} catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
		
		System.out.println("Finished initializing");

		return (netPeerGroup != null);
	}
	


	private void joinCustomGroup(JXTAPeerConfiguration config) throws Exception
	{
		System.out.println("Joining group " + config.getPeerGroupName());
		PeerGroupID groupId = IDFactory.newPeerGroupID(netPeerGroup.getPeerGroupID(), config.getPeerGroupName().getBytes());
				
		//try to find it, if not, publish it
		Enumeration<Advertisement> advs;
		
		advs = netPeerGroup.getDiscoveryService().getLocalAdvertisements(DiscoveryService.GROUP, "GID", groupId.toString());
		if (advs != null)
		{
			if (advs.hasMoreElements())
			{
				Advertisement adv = advs.nextElement();
				if (adv != null)
				{
					System.out.println("Found local group advertisement... ");
					hgdbGroup = netPeerGroup.newGroup(adv);				
				}
			}
		}
		
		if (hgdbGroup == null)
		{
			System.out.println("Can not find local group advertisements. Creating a new group ... ");

			ModuleImplAdvertisement implAdv = netPeerGroup.getAllPurposePeerGroupImplAdvertisement();
			hgdbGroup = netPeerGroup.newGroup(groupId, implAdv, config.getPeerGroupName(), ""); 

			System.out.println("publishing group...");
			
			PeerGroupAdvertisement adv = hgdbGroup.getPeerGroupAdvertisement();
			netPeerGroup.getDiscoveryService().remotePublish(adv);
			
			System.out.println("Group published successfully.");
		}
		
		System.out.println("Joining group...");
		AuthenticationCredential cred = new AuthenticationCredential(hgdbGroup, null, null);
		Authenticator auth = hgdbGroup.getMembershipService().apply(cred);
	
		if (auth != null)
		{
			if (auth.isReadyForJoin())
			{
				hgdbGroup.getMembershipService().join(auth);
				System.out.println("group joined");
			}
		}
	}

	public void start()
	{
    	new Thread(new AdvPublisher()).start();
    	new Thread(new AdvSubscriber()).start();
	}
	
	public PeerGroup getPeerGroup()
	{
		return netPeerGroup;
	}
	
	public void publishAdv(Advertisement adv)
	{
		synchronized(this)
		{
			ownAdvs.addLast(adv);
		}
		netPeerGroup.getDiscoveryService().remotePublish(adv);
	}
	public Advertisement getPipeAdv()
	{
		return ownAdvs.getFirst();
	}
	public Set<Advertisement> getAdvertisements()
	{
		return peerAdvs.keySet();
	}
	private class AdvPublisher implements Runnable
	{
		public void run()
		{
	        DiscoveryService discoveryService = hgdbGroup.getDiscoveryService();

	        long lifetime = 5 * 1000;//DiscoveryService.DEFAULT_LIFETIME;
	        long expiration = 5 * 1000;//DiscoveryService.DEFAULT_EXPIRATION;
	        long waittime = 5 * 1000;//DiscoveryService.DEFAULT_EXPIRATION;
	        
	        try {
	            while (true) {
	            	for(Advertisement adv : ownAdvs)
	            	{
	            		//System.out.println("publishing: " + adv.getID().toString());
	            		
	            		discoveryService.publish(adv, lifetime, expiration);
	            		discoveryService.remotePublish(adv, expiration);
	            	}
	                try {
	                    Thread.sleep(waittime);
	                } catch (Exception e) {
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
	}
	
	private class AdvSubscriber implements Runnable, DiscoveryListener
	{

		public void run()
		{
	        long waittime = 1000L;
	        DiscoveryService discoveryService = hgdbGroup.getDiscoveryService();

	        try {
	            // Add ourselves as a DiscoveryListener for DiscoveryResponse events
	        	discoveryService.addDiscoveryListener(this);
	        	
	        	while (true)
	        	{
	        		//System.out.println("Getting remote advertisements");
		        	discoveryService.getRemoteAdvertisements(null,  DiscoveryService.ADV,  null, null, 100, null);

	                try {
	                    Thread.sleep(waittime);
	                } catch (Exception e) {
	                    // ignored
	                }

		        	
	        	}
	        } catch (Exception e) {
	            e.printStackTrace();
	        }

		}

		public void discoveryEvent(DiscoveryEvent ev)
		{
	        DiscoveryResponseMsg res = ev.getResponse();

	        // let's get the responding peer's advertisement
	        String peerName = ev.getSource().toString();
	        	        
/*	        System.out.println(" [  Got a Discovery Response [" + res.getResponseCount() + " elements]  from peer : " + peerName + "  ]");
	        PeerAdvertisement peerAdv = res.getPeerAdvertisement();
*/	        
	        //not interested in selfs advertisements
	        Advertisement adv;
	        Enumeration<Advertisement> advs = res.getAdvertisements();

	        if (advs != null) {
	            while (advs.hasMoreElements()) {
	                adv = (Advertisement) advs.nextElement();
	                if (adv instanceof PipeAdvertisement)
	                {
	                	if (!peerAdvs.containsKey(adv))
	                	{
	                		PipeID pipeId = (PipeID) ((PipeAdvertisement)adv).getPipeID();
	                		if (!ownPipes.contains(pipeId))
	                		{
		                		peerAdvs.put(adv, null);
		                		System.out.println("New Pipe from " + peerName + " (" + pipeId + ")");	                			
	                		}
	                		
	                	}
	                }
	            }
	        }
		}
		
	}
	
	private void dumpNetworkConfig(NetworkConfigurator configurator)
	{
		System.out.println("Configuration: ");
		System.out.println("	PeerID = " + configurator.getPeerID().toString());
		System.out.println("	Name = " + configurator.getName());
		
		//System.out.println(configurator.getPlatformConfig().toString());
		
	}

	public void addOwnPipe(PipeID pipeId)
	{
		ownPipes.add(pipeId);
	}

	public HGAtomPredicate getAtomInterests(Object peerId)
	{
		return peerAdvs.get(peerId);
	}

	public void setAtomInterests(Object peerId, HGAtomPredicate interest)
	{
		System.out.println("Peer " + ((PipeAdvertisement)peerId).getName() + " is interested in " + interest);
		peerAdvs.put((Advertisement)peerId, interest);
	}
}
