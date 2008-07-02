package org.hypergraphdb.peer;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hypergraphdb.query.And;
import org.hypergraphdb.query.AnyAtomCondition;
import org.hypergraphdb.query.ArityCondition;
import org.hypergraphdb.query.HGAtomPredicate;
import org.hypergraphdb.query.HGQueryCondition;
import org.hypergraphdb.type.BonesOfBeans;

/**
 * 
 * <p>
 * Utility methods to be used in constructing nested structures for complex
 * message representations. This class consists entirely of static methods
 * and is designed to be imported with <code>import org.hypergraphdb.peer.Structs.*</code>.
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class Structs
{	
	/**
	 * <p>Return primitives, lists and maps <code>as-is</code>, transform collections
	 * to lists and beans to structs (i.e. String->Value maps). Special case: 
	 * instances of either <code>HGQueryCondition</code> or <code>HGAtomPredicate</code>
	 * or passed to <code>hgQueryOrPredicate</code> to create an appropriate
	 * representation.</p>
	 */
	public static Object svalue(Object x)
	{
		if (x instanceof HGQueryCondition || x instanceof HGAtomPredicate)
			return hgQueryOrPredicate(x);
		else if (x == null || 
			x instanceof Number || 
			x instanceof Boolean || 
			x instanceof String ||
			x instanceof Map ||
			x instanceof List)
			return x;
		else if (x instanceof Collection)
		{
			ArrayList<Object> l = new ArrayList<Object>();
			l.addAll((Collection<?>)x);
			return l;
		}
		else
			return struct(x);
	}
	
	/**
	 * <p>Use reflection to create a map of the bean properties of the argument.</p>
	 */
	public static Map<String, Object> struct(Object bean)
	{
		if (bean == null)
			return null;
		Map<String, Object> m = new HashMap<String, Object>();
		for (PropertyDescriptor desc : BonesOfBeans.getAllPropertyDescriptors(bean.getClass()).values())
		{
			if (desc.getReadMethod() != null && desc.getWriteMethod() != null)
				m.put(desc.getName(), svalue(BonesOfBeans.getProperty(bean, desc)));
		}
		return m;
	}	
	
	/**
	 * <p>Create a record-like structure of name value pairs as a regular Java
	 * <code>Map<String, Object</code>. The method takes a variable number of arguments
	 * where each argument at an even position must be a name with the argument following
	 * it its value.</p>
	 * 
	 * <p>For example: <code>struct("personName", "Adriano Celentano", "age", 245)</code>.</p>
	 */
	public static Map<String, Object> struct(Object...args)
	{		
		if (args == null)
			return null;
		Map<String, Object> m = new HashMap<String, Object>();		
		if (args.length % 2 != 0)
			throw new IllegalArgumentException("The arguments array to struct must be of even size: a flattened list of name/value pairs");
		for (int i = 0; i < args.length; i+=2)
		{
			if (! (args[i] instanceof String) )
				throw new IllegalArgumentException("An argument at the even position " + i + " is not a string.");
			m.put((String)args[i], svalue(args[i+1]));
		}
		return m;
	}
	
	/**
	 * <p>Create a Java list out of a list of arguments.</p>
	 */
	public static List<Object> list(Object...args)
	{
		List<Object> l = new ArrayList<Object>();
		if (args == null)
			return l;
		else for (Object x : args)
			l.add(x);
		return l;
	}
	
	private static Map<Class<?>, String> hgClassNames = new HashMap<Class<?>, String>();
	static
	{
		hgClassNames.put(And.class, "and");
		hgClassNames.put(AnyAtomCondition.class, "any");
		hgClassNames.put(ArityCondition.class, "arity");
		// etc....		
	}
	
	public static List<Object> hgQueryOrPredicate(Object x)
	{
		if (x == null)
			return null;
		String name = hgClassNames.get(x.getClass());
		if (name == null)
			throw new IllegalArgumentException(
				"Unknown HyperGraph query condition or atom predicate type '" + x.getClass().getName() + "'");
		return list(name, svalue(x));
	}
	
	public static List<Object> hgQuery(HGQueryCondition condition)
	{
		return hgQueryOrPredicate(condition);
	}
	
	public static List<Object> hgPredicate(HGAtomPredicate predicate)
	{
		return hgQueryOrPredicate(predicate);
	}
	
	public static Map<String, Object> merge(Map<String, Object> m1, Map<String, Object> m2)
	{
		Map<String, Object> m = new HashMap<String, Object>();
		if (m1 != null)
			m.putAll(m1);
		if (m2 != null)
			m.putAll(m2);
		return m;
	}
	
	public List<Object> append(List<Object> l1, List<Object> l2)
	{
		List<Object> l = new ArrayList<Object>();
		if (l1 != null)
			l.addAll(l1);
		if (l2 != null)
			l.addAll(l2);
		return l;
	}
}
