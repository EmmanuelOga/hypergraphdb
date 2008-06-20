package org.hypergraphdb.peer.workflow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.hypergraphdb.peer.PeerInterface;
import org.hypergraphdb.peer.protocol.Message;
import org.hypergraphdb.util.Pair;

public abstract class TaskActivity<StateType> extends AbstractActivity<StateType> implements ActivityStateListener
{
	private UUID taskId;
	private PeerInterface peerInterface;
	
	private HashMap<UUID, Conversation<?>> conversations = new HashMap<UUID, Conversation<?>>();
	private HashMap<StateType, LinkedBlockingQueue<AbstractActivity<?>>> activityQueues = new HashMap<StateType, LinkedBlockingQueue<AbstractActivity<?>>>();
	private HashMap<Pair<StateType, Object>, Pair<StateType, Method>> transitions = new HashMap<Pair<StateType,Object>, Pair<StateType,Method>>();
	
	private HashSet<Object> conversationStates = new HashSet<Object>(); 

	public TaskActivity(PeerInterface peerInterface)
	{
		this(peerInterface, UUID.randomUUID());
	}
	
	public TaskActivity(PeerInterface peerInterface, UUID taskId)
	{
		this.peerInterface = peerInterface;
		this.taskId = taskId;
				
		peerInterface.registerTask(taskId, this);
	}

	public void stateChanged(Object newState, AbstractActivity<?> activity)
	{
		System.out.println("TaskActivity: stateChanged to " + newState + " while in " + getState() );
		
		StateType currentState = getState();
		Pair<StateType, Method> dest = transitions.get(new Pair<StateType, Object>(currentState, newState));
		if (dest != null)
		{
			if (compareAndSetState(currentState, dest.getFirst()))
			{
				try
				{
					StateType targetState = (StateType) dest.getSecond().invoke(this, activity);
					setState(targetState);
				
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
			}else{
				//TODO add to queue
			}
		}
			
		//add activity to according queue
	}

	protected abstract void startTask();
	
	protected void doRun()
	{
		startTask();
		

		//get queue for current state
		LinkedBlockingQueue<AbstractActivity<?>> queue = activityQueues.get(getState());

		if (queue != null)
			{
			//wait for input on that
			AbstractActivity<?> activity = null;
			try
			{
				activity = queue.take();
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (activity != null)
			{
				//get step info
				Pair<StateType, Method> dest = transitions.get(new Pair<StateType, Object>(getState(), activity.getState()));
				
				if (compareAndSetState(getState(), dest.getFirst()))
				{
					try
					{
						StateType newState = (StateType)(dest.getSecond().invoke(this, activity));
						setState(newState);
						
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
			}
		}
		//handle it until done
	}

	protected void registerConversation(Conversation<?> conversation, UUID conversationId)
	{

		if (conversation != null)
		{
			//add handlers
			for(Object conversationState : conversationStates)
			{
				conversation.setStateListener(conversationState, this);
			}
			conversations.put(conversationId, conversation);
		}
		
	}
	public void handleMessage(Message msg)
	{
		System.out.println("TaskActivity: handleMessage");
		Conversation<?> conversation = conversations.get(msg.getConversationId());
		if (conversation == null)
		{
			conversation = createNewConversation(msg);
			registerConversation(conversation, msg.getConversationId());
		}
		conversation.handleIncomingMessage(msg);
	}

	
	protected Conversation<?> createNewConversation(Message msg)
	{
		return null;
	}

	protected void registerConversationHandler(StateType fromState, Object conversationState, String functionName, StateType toState)
	{
		//create a new input queue
		if (!activityQueues.containsKey(fromState))
		{
			LinkedBlockingQueue<AbstractActivity<?>> queue = new LinkedBlockingQueue<AbstractActivity<?>>();
			activityQueues.put(fromState, queue);
		}
		
		//add transition
		try
		{
			Method method = this.getClass().getMethod(functionName, AbstractActivity.class);
			
			transitions.put(new Pair<StateType, Object>(fromState, conversationState), new Pair<StateType, Method>(toState, method));
		} catch (SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//remember conversation state
		conversationStates.add(conversationState);
	}

	
	protected Message getReply(Message msg)
	{
		Message reply = new Message(msg.getAction(), msg.getConversationId());
		reply.setTaskId(msg.getTaskId());
		
		return reply;
	}
	
	
	public UUID getTaskId()
	{
		return taskId;
	}

	public void setTaskId(UUID taskId)
	{
		this.taskId = taskId;
	}
	
	public PeerInterface getPeerInterface()
	{
		return peerInterface;
	}
	public void setPeerInterface(PeerInterface peerInterface)
	{
		this.peerInterface = peerInterface;
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
/*	
	
	public void handleMessage(Message msg)
	{
		System.out.println("TaskActivity: received " + msg.toString() + "while in " + getState().toString());
	
		//see if part of an existing conversation
		Conversation<?> conversation = conversations.get(msg.getConversationId());
		if (conversation == null)
		{
			conversation = makeNewConversation(msg);
			//add listners for states we are interested in
			
			//create a new conversation
		}
		
		conversation.handleMessage(msg);
		
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	private HashMap<Pair<Performative, StateType>, Pair<Method, StateType>> steps = new HashMap<Pair<Performative,StateType>, Pair<Method,StateType>>();
	
	
	public abstract void init();
		
	protected void startConversation()
	{
		peerInterface.registerReceiveHook(conversationId, this);
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
	

	

	public UUID getConversationId()
	{
		return conversationId;
	}
	public void setConversationId(UUID conversationId)
	{
		this.conversationId = conversationId;
	}
*/
	
	
}
