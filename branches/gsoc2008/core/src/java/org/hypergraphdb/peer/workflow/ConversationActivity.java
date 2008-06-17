package org.hypergraphdb.peer.workflow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.servicemix.beanflow.AbstractActivity;
import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.peer.protocol.Performative;
import org.hypergraphdb.util.Pair;


public abstract class ConversationActivity<StateType> extends AbstractActivity
{
	private UUID conversationId;
	private PeerInterface peerInterface;
	
	private AtomicReference<StateType> state = new AtomicReference<StateType>();

	private HashMap<Pair<Performative, StateType>, Pair<Method, StateType>> steps = new HashMap<Pair<Performative,StateType>, Pair<Method,StateType>>();
	
	public ConversationActivity(PeerInterface peerInterface)
	{
		this.peerInterface = peerInterface;
		conversationId = UUID.randomUUID();
	}
	public ConversationActivity(PeerInterface peerInterface, UUID conversationId)
	{
		this.peerInterface = peerInterface;
		this.conversationId = conversationId;
	}
	
	public abstract void init();
	
	protected void setState(StateType newState)
	{
		state.set(newState);
	}
	
	protected void registerReceiveHook(Performative performative, String function, StateType startState, StateType runState)
	{
		try
		{
			Method method = this.getClass().getMethod(function, Message.class);
			
			steps.put(new Pair<Performative, StateType>(performative, startState), new Pair<Method, StateType>(method, runState));
		} catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//put in queue
	}
	
	public void handleMessage(Message msg)
	{
		System.out.println("ConversationActivity: received " + msg.toString() + "while in " + state.get().toString());
		
		//try to go directly to the state
		Pair<Performative, StateType> key = new Pair<Performative, StateType>(msg.getPerformative(), state.get());
		
		if (steps.containsKey(key))
		{
			Pair<Method, StateType> value = steps.get(key);
			
			if (state.compareAndSet(key.getSecond(), value.getSecond()))
			{
				try
				{
					StateType newState = (StateType)value.getFirst().invoke(this, msg);
					state.set(newState);
				} catch (IllegalArgumentException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//TODO else enqueue
		}
		
		//put in queue ... 
		//call right function ... 
	}
	public UUID getConversationId()
	{
		return conversationId;
	}
	public void setConversationId(UUID conversationId)
	{
		this.conversationId = conversationId;
	}
	public PeerInterface getPeerInterface()
	{
		return peerInterface;
	}
	public void setPeerInterface(PeerInterface peerInterface)
	{
		this.peerInterface = peerInterface;
	}
	
	
}
