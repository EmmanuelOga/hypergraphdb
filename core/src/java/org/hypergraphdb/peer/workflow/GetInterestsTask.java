package org.hypergraphdb.peer.workflow;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import org.hypergraphdb.peer.HGDBOntology;
import org.hypergraphdb.peer.PeerFilter;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.PeerRelatedActivity;
import org.hypergraphdb.peer.PeerRelatedActivityFactory;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.query.HGAtomPredicate;

/**
 * @author ciprian.costa
 * The task is used both to get the interests of all peers and to responde 
 * to publish interest messages.
 *
 */
public class GetInterestsTask extends TaskActivity<GetInterestsTask.State>
{
	protected enum State {Started, Done}
	
	private Message msg = null;
	//TODO: temporary
	private AtomicInteger count = new AtomicInteger();
	
	public GetInterestsTask(PeerInterface peerInterface)
	{
		super(peerInterface, State.Started, State.Done);
	}

	public GetInterestsTask(PeerInterface peerInterface, Message msg)
	{
		super(peerInterface, msg.getTaskId(), State.Started, State.Done);
		
		this.msg = msg;
	}

	protected void startTask()
	{
		if (msg != null)
		{
			//task was created because someone else is publishing
			getPeerInterface().getPeerNetwork().setAtomInterests(msg.getReplyTo(), (HGAtomPredicate)msg.getContent());
			setState(State.Done);
		}else{
			//task is intended for retrieving information from other peers
			PeerRelatedActivityFactory activityFactory = getPeerInterface().newSendActivityFactory();

			PeerFilter peerFilter = getPeerInterface().newFilterActivity(null);

			peerFilter.filterTargets();
			Iterator<Object> it = peerFilter.iterator();
			count.set(1);
			while (it.hasNext())
			{
				count.incrementAndGet();
				Object target = it.next();
				sendMessage(activityFactory, target);
			}
			if (count.decrementAndGet() == 0) 
			{
				setState(State.Done);
			}
		}	
	};

	private void sendMessage(PeerRelatedActivityFactory activityFactory, Object target)
	{
		Message msg = getPeerInterface().getMessageFactory().createMessage();
		msg.setPerformative(Performative.Request);
		msg.setAction(HGDBOntology.ATOM_INTEREST);
		msg.setTaskId(getTaskId());
		
		PeerRelatedActivity activity = (PeerRelatedActivity)activityFactory.createActivity();
		activity.setTarget(target);
		activity.setMessage(msg);
		
		getPeerInterface().execute(activity);
		
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.peer.workflow.TaskActivity#handleMessage(org.hypergraphdb.peer.protocol.Message)
	 * 
	 * TODO: for now just overriding it, need to change when more complex conversations are implemented.
	 */
	public void handleMessage(Message msg)
	{
		getPeerInterface().getPeerNetwork().setAtomInterests(msg.getReplyTo(), (HGAtomPredicate)msg.getContent());

		if (count.decrementAndGet() == 0)
		{
			setState(State.Done);
		}
	}
	
	public static class GetInterestsFactory implements TaskFactory
	{
		public GetInterestsFactory()
		{
		}
		public TaskActivity<?> newTask(PeerInterface peerInterface, Message msg)
		{
			return new GetInterestsTask(peerInterface, msg);
		}
		
	}

}
