package org.hypergraphdb.peer.protocol;

/**
 * @author ciprian.costa
 *
 * Interface use to hide the actual type of message that is used.
 */
public interface MessageFactory
{
	Message createMessage();
}
