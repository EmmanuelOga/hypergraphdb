package org.hypergraphdb.peer.protocol;

import org.hypergraphdb.peer.serializer.HGSerializer;

public interface SerializerMapper
{
	HGSerializer accept(Class<?> clazz);
	HGSerializer getSerializer();
}
