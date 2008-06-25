package org.hypergraphdb.peer.protocol;

import java.util.Collection;

/**
 * @author ciprian.costa
 *
 * Allows manipulation of properties (get/set for base types, collections and generic objects).
 * All properties are identified by a strign key that can not be null.
 * 
 */
public interface Document
{
	Object get(String key);
	Object get(String key, int index);
	boolean getBoolean(String key);
	boolean getBoolean(String key, int index);
	double getDouble(String key);
	double getDouble(String key, int index);
	int getInt(String key);
	int getInt(String key, int index);
	long getLong(String key);
	long getLong(String key, int index);
	String getString(String key);
	String getString(String key, int index);

	void put(String key, Object value);
	void put(String key, boolean value);
	void put(String key, double value);
	void put(String key, int value);
	void put(String key, long value);
	void put(String key, String value);
	
	void put(String key, Collection<?> col);
	int length(String key);
	
	void remove(String key);
	
}
