package org.hypergraphdb.peer.workflow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractActivity<StateType> implements Runnable
{
	private AtomicReference<StateType> state = new AtomicReference<StateType>();
	private HashMap<StateType, ArrayList<ActivityStateListener>> stateListeners = new HashMap<StateType, ArrayList<ActivityStateListener>>(); 

	public AbstractActivity()
	{
		
	}
	
	protected abstract void doRun();
	
	public void run()
	{
		doRun();
	}
	
	protected boolean compareAndSetState(StateType oldState, StateType newState)
	{
		return state.compareAndSet(oldState, newState);
	}
	
	protected StateType getState()
	{
		return state.get();
	}
	protected void setState(StateType newValue)
	{
		state.set(newValue);
	}
	
	public void setStateListener(Object state, ActivityStateListener listener)
	{
		ArrayList<ActivityStateListener> list = stateListeners.get(state);
		if (list == null)
		{
			list = new ArrayList<ActivityStateListener>();
			stateListeners.put((StateType)state, list);
		}
		
		list.add(listener);
	}

	protected void stateChanged()
	{
		StateType newState = state.get();
		ArrayList<ActivityStateListener> list =  stateListeners.get(newState);
		if (list != null)
		{
			for(ActivityStateListener listener : list)
			{
				listener.stateChanged(newState, this);
			}
		}
	}
}
