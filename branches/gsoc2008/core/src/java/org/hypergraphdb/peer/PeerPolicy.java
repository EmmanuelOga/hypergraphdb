package org.hypergraphdb.peer;

public interface PeerPolicy
{
	boolean shouldStore(Object atom);
}
