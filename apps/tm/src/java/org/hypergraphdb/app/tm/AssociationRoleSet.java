package org.hypergraphdb.app.tm;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.util.HGUtils;
import org.tmapi.core.AssociationRole;

/**
 * 
 * <p>
 * This exposes the target set of an association's roles as a Java <code>Set</code>
 * as required by the TMAPI.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
class AssociationRoleSet implements Set<HGAssociationRole>
{
	HyperGraph graph;
	HGHandle [] targetSet;

	AssociationRoleSet(HyperGraph graph, HGHandle targetSet[])
	{	
		this.graph  = graph;
		this.targetSet = targetSet;
	
	}

	HGAssociationRole get(int i)
	{
		HGAssociationRole r = (HGAssociationRole)graph.get(targetSet[i]);
		r.graph = graph;
		return r;
	}
	
	public boolean add(HGAssociationRole o)
	{
		throw new UnsupportedOperationException();
	}

	public boolean addAll(Collection<? extends HGAssociationRole> c)
	{
		throw new UnsupportedOperationException();	
	}

	public void clear()
	{
		throw new UnsupportedOperationException();	
	}

	public boolean contains(Object o)
	{
		for (HGHandle h : targetSet)
			if (HGUtils.eq(o, graph.get(h)))
				return true;
		return false;
	}

	public boolean containsAll(Collection<?> c)
	{
		for (Object x : c)
			if (!contains(x))
				return false; 
		return true;
	}

	public boolean isEmpty()
	{
		return targetSet.length == 0;
	}

	public Iterator<HGAssociationRole> iterator()
	{
		return new Iterator<HGAssociationRole>()
		{
			int i = 0;
			public void remove() { throw new UnsupportedOperationException(); }
			public boolean hasNext() { return i < targetSet.length; }
			public HGAssociationRole next()  { return get(i++); } 		
		};
	}

	public boolean remove(Object o)
	{
		throw new UnsupportedOperationException();	
	}

	public boolean removeAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();	
	}

	public boolean retainAll(Collection<?> c)
	{
		throw new UnsupportedOperationException();	}

	public int size()
	{
		return targetSet.length;
	}

	public Object[] toArray()
	{
		Object [] result = new Object[targetSet.length];
		for (int i = 0; i < targetSet.length; i++)
			result[i] = get(i);
		return result;
	}

	public <T> T[] toArray(T[] a)
	{
        if (a.length < targetSet.length)
            a = (T[])java.lang.reflect.Array.
            		newInstance(a.getClass().getComponentType(), targetSet.length);        
		for (int i = 0; i < targetSet.length; i++)
			a[i] = (T)get(i);
		return a;
	}	
}