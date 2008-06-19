package org.hypergraphdb.peer.workflow;

import java.util.concurrent.atomic.AtomicReference;

public class AbstractActivity<StateType>
{
	private AtomicReference<StateType> state = new AtomicReference<StateType>();

}
