/*
 * This file is part of the HyperGraphDB source distribution. This is copyrighted
 * software. For permitted uses, licensing options and redistribution, please see
 * the LicensingInformation file at the root level of the distribution.
 *
 * Copyright (c) 2005-2010 Kobrix Software, Inc.  All rights reserved.
 */
package org.hypergraphdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.atom.HGSubsumes;
import org.hypergraphdb.atom.HGTypeStructuralInfo;
import org.hypergraphdb.event.HGLoadPredefinedTypeEvent;
import org.hypergraphdb.handle.HGLiveHandle;
import org.hypergraphdb.storage.BAtoHandle;
import org.hypergraphdb.storage.BAtoString;
import org.hypergraphdb.transaction.HGTransaction;
import org.hypergraphdb.transaction.TxCacheMap;
import org.hypergraphdb.type.HGAtomType;
import org.hypergraphdb.type.HGTypeConfiguration;
import org.hypergraphdb.type.HGTypeSchema;
import org.hypergraphdb.type.JavaTypeMapper;
import org.hypergraphdb.type.JavaTypeSchema;
import org.hypergraphdb.type.LinkType;
import org.hypergraphdb.type.NullType;
import org.hypergraphdb.type.SubsumesType;
import org.hypergraphdb.type.Top;
import org.hypergraphdb.util.HGUtils;

/**
 * <p>
 * The <code>HGTypeSystem</code> manages atom type information for a given
 * {@link HyperGraph} database. Each atom stored in the database has a type
 * that carries some semantic information about the atom as well as implementing
 * the low-level storage representation of the atom's value. Types can be dynamically
 * added to and removed from the database - they themselves are atoms. Such atom types
 * are distinguished by the fact that their runtime instances implement the
 * {@link HGAtomType} interface. Every
 * database can have its own user-definable type system bootstrapped with a
 * set of predefined types. The type of all predefined types is called top. When
 * the instances of a given type are themselves types (i.e. implement the
 * {@link HGAtomType} interface, they are called type constructors. Such type
 * constructors are used to manage dynamically added types, for example for
 * application specific Java classes.
 *
 * <p>
 * This class is also responsible for creating and managing mappings from Java classes to
 * HyperGraphDB types. The runtime instance of each atom is of some Java class and
 * there is one and only one HyperGraphDB type associated with that class. Predefined
 * types and the mechanism by which Java class are mapped to HyperGraphDB type go hand in
 * hand and they are both part of the global database configuration in the form of a
 * {@link HGTypeConfiguration} instance.
 * </p>
 *
 * <p>
 * You can obtain the type of a given atom or the type associated with a given Java class by
 * calling one of the <code>getAtomType</code> methods. To obtain the handle of a type (instead
 * of the actual {@link HGAtomType} instance), call one of the <code>getTypeHandle</code> methods.
 * </p>
 *
 * <h3>Class loading</h3>
 *
 * Since this class does a lot of introspection when mapping newly seen Java classes to
 * HyperGraphDB types, it is worth noting how classes are looked up. The mechanism is rather
 * simple: the <code>ClassLoader</code> set via the <code>setClassLoader</code> is
 * tried if provided at all, otherwise the current thread class loader is tried, and finally
 * <code>this.getClass().getClassLoader()</code> is tried.
 *
 * <h3>Aliases</h3>
 *
 * <p>
 * An alias can be defined for a commonly used type. An alias is simply a name
 * that is associated with a type. The type instance can then be retrieved by
 * using the alias. A type may have more than one alias. Use the
 * <code>addAlias</code>, <code>removeAlias</code>, <code>getType(String)</code>
 * and <code>getHandle</code> methods for working with aliases.
 * </p>
 *
 * @author Borislav Iordanov
 */
public class HGTypeSystem
{
	private static final int MAX_URI_TO_TYPE = 2000;
	private static final String TYPE_ALIASES_DB_NAME = "hg_typesystem_type_alias";
	private static final String JAVA_PREDEFINED_TYPES_DB_NAME = "hg_typesystem_javapredefined_types";
	private static final String URI2HG_TYPES_DB_NAME = "hg_typesystem_uri2hg_types";

	public static final HGAtomType top = Top.getInstance();

	private HyperGraph graph = null;
	private HGTypeConfiguration config = null;

	public TxCacheMap<URI, HGHandle> uriToAtomType = null;

	// Useful for the many methods that work on Java Class(es) for backward compatibility.
	private JavaTypeSchema javaSchema = null;
//	private HGBidirectionalIndex<String, HGPersistentHandle> classToTypeDB = null;

	// per transaction map used during type construction to avoid
	// circularity in recursive types
	public static final Map<URI, HGHandle> emptyMap = new HashMap<URI, HGHandle>();

	public Map<URI, HGHandle> getLocalIdMap()
	{
		HGTransaction tx = graph.getTransactionManager().getContext().getCurrent();
		if (tx == null)
			return emptyMap;
		Map<URI, HGHandle> m = tx.getAttribute(HGTypeSystem.class.getName() + ".idmap");
		if (m == null)
		{
			m = new HashMap<URI, HGHandle>();
			tx.setAttribute(HGTypeSystem.class.getName() + ".idmap", m);
		}
		return m;
	}

	private HGBidirectionalIndex<String,  HGPersistentHandle> aliases = null;
	private HGBidirectionalIndex<String,  HGPersistentHandle> urisDB = null;
	private HGIndex<HGPersistentHandle, String> predefinedTypesDB = null;
	private HGLiveHandle topHandle;
	private HGLiveHandle nullTypeHandle;

	private HGBidirectionalIndex<String, HGPersistentHandle> getUriDB()
	{
		if (urisDB == null)
		{
			urisDB = graph.getStore().getBidirectionalIndex(URI2HG_TYPES_DB_NAME,
																						 BAtoString.getInstance(),
																						 BAtoHandle.getInstance(graph.getHandleFactory()),
																						 null,
																						 true);
		}
		return urisDB;
	}

	private HGBidirectionalIndex<String, HGPersistentHandle> getAliases()
	{
		if (aliases == null)
		{
			aliases = graph.getStore().getBidirectionalIndex(TYPE_ALIASES_DB_NAME,
																				 BAtoString.getInstance(),
																				 BAtoHandle.getInstance(graph.getHandleFactory()),
																				 null,
																				 true);
		}
		return aliases;
	}

	private HGIndex<HGPersistentHandle, String> getPredefinedTypesDB()
	{
		if (predefinedTypesDB == null)
		{
			predefinedTypesDB = graph.getStore().getIndex(JAVA_PREDEFINED_TYPES_DB_NAME,
																			 BAtoHandle.getInstance(graph.getHandleFactory()),
																			 BAtoString.getInstance(),
																			 null,
																			 true);
		}
		return predefinedTypesDB;
	}

	void addPrimitiveTypeToStore(HGPersistentHandle handle)
	{
		HGPersistentHandle [] layout = new HGPersistentHandle[]
			{
				topHandle.getPersistent(),
				graph.getHandleFactory().nullHandle()
			};
		graph.getStore().store(handle, layout);
		graph.indexByType.addEntry(topHandle.getPersistent(), handle);
	}

	//AP:Added public
	public void bootstrap(HGTypeConfiguration typeConfiguration)
	{
		config = typeConfiguration;
		javaSchema = config.getSchema(graph, JavaTypeSchema.SCHEME_NAME);

		uriToAtomType = new TxCacheMap<URI, HGHandle>(
				graph.getTransactionManager(), UriToTypeCache.class, null);

		top.setHyperGraph(graph);
		topHandle = graph.cache.atomRead(graph.getHandleFactory().topTypeHandle(),
																		 top,
																		 new HGAtomAttrib());
		graph.cache.freeze(topHandle);

		HGAtomType linkType = new LinkType();
		linkType.setHyperGraph(graph);
		HGLiveHandle linkHandle = graph.cache.atomRead(graph.getHandleFactory().linkTypeHandle(),
																									 linkType,
																									 new HGAtomAttrib());
		graph.cache.freeze(linkHandle);

		HGAtomType subsumesType = new SubsumesType();
		subsumesType.setHyperGraph(graph);
		HGLiveHandle subsumesHandle = graph.cache.atomRead(graph.getHandleFactory().subsumesTypeHandle(),
																											 subsumesType,
																											 new HGAtomAttrib());
		graph.cache.freeze(subsumesHandle);

		HGAtomType nullType = new NullType();
		nullType.setHyperGraph(graph);
		nullTypeHandle = graph.cache.atomRead(graph.getHandleFactory().nullTypeHandle(),
																					nullType,
																					new HGAtomAttrib());
		graph.cache.freeze(nullTypeHandle);

		//
		// If we are actually creating a new database, populate with primitive types.
		//
		if (graph.getStore().getLink(topHandle.getPersistent()) == null)
		{
			addPrimitiveTypeToStore(topHandle.getPersistent());
			addPrimitiveTypeToStore(linkHandle.getPersistent());
			addPrimitiveTypeToStore(subsumesHandle.getPersistent());
		}

		javaSchema.initialize(graph, config);  //1st initialize the java schema
		Collection<HGTypeSchema<?>> schemas = new ArrayList<HGTypeSchema<?>>(config.getSchemas());  //to avoid concurrent mod when adding additional scheme

		for (HGTypeSchema<?> schema : schemas)  //then do all the others
		{
			if (!schema.getName().equals(JavaTypeSchema.SCHEME_NAME))
				schema.initialize(graph, config);
		}
	}

	/**
	 * <p>
	 * Use this method to load a set of primitive types in bulk, from a text descriptor
	 * resource (loaded using this class's class loader).
	 * </p>
	 * <p>
	 * The resource is expected to be in the following format: 1 type per line where
	 * each line consists of two or more columns separated by the space character.
	 * The first columns should be a canonical string representation of a UUID. The second
	 * column should be the class name of the class implementing the type. The (optional)
	 * subsequent columns should list the names of the classes that this type "covers".
	 * The following is an example where the first line simply adds a predefined type
	 * without any corresponding covered Java classes, and the second shows a type
	 * that covers only one class:
	 * </p>
	 * <p>
	 * <pre><code>
	 * db733325-19d5-11db-8b55-23bc8177d6ec org.hypergraphdb.type.NullType
	 * 2ec10476-d964-11db-a08c-eb6f4c8f155a org.hypergraphdb.type.AtomRefType org.hypergraphdb.atom.HGAtomRef
	 * </code></pre>
	 * </p>
	 * @param resource
	 */
	public void storePrimitiveTypes(String resource)
	{
		InputStream resourceIn = getClass().getResourceAsStream(resource);
		if (resourceIn == null)
			throw new HGException("Fatal error: could not load primitive types from " +
														resource + ", this resource could not be found!");
		BufferedReader reader = new BufferedReader(new InputStreamReader(resourceIn));
		try
		{
			for (String line = reader.readLine(); line != null; line = reader.readLine())
			{
				line = line.trim();
				if (line.length() == 0)
					continue;

				if (line.startsWith("#"))
					continue;

				StringTokenizer tok = new StringTokenizer(line, " ");
				if (tok.countTokens() < 2)
					throw new HGException("Fatal error: could not load primitive types from " +
																resource + ", the line " + line + " is ill formed.");
				String pHandleStr = tok.nextToken();
				String typeClassName = tok.nextToken();
				Class<?> typeClass = Class.forName(typeClassName);
				HGAtomType type = (HGAtomType)typeClass.newInstance();
				type.setHyperGraph(graph);
				HGPersistentHandle pHandle = graph.getHandleFactory().makeHandle(pHandleStr);

				if (tok.hasMoreTokens())
				{
					while (tok.hasMoreTokens())
					{
						String valueClassName = tok.nextToken();
						Class<?> valueClass = Class.forName(valueClassName);
						addPredefinedType(pHandle, type, valueClass);
					}
				}
				else
					addPredefinedType(pHandle, type, (URI)null);
			}
		}
		catch (IOException ex)
		{
			throw new HGException("Fatal error: could not load primitive types from " +
														resource + " due to an IO exception!", ex);
		}
		catch (ClassNotFoundException ex)
		{
			throw new HGException("Fatal error: could not load primitive types from " +
														resource + " due to a missing class from the classpath: " + ex.getMessage(), ex);
		}
		catch (Throwable t)
		{
			throw new HGException("Fatal error: could not load primitive types from " + resource, t);
		}
		finally
		{
			try {resourceIn.close();}	catch (Throwable t) {}
		}
	}

	HGLiveHandle loadPredefinedType(HGPersistentHandle pHandle)
	{
		graph.getEventManager().dispatch(graph, new HGLoadPredefinedTypeEvent(pHandle));
		String classname = getPredefinedTypesDB().findFirst(pHandle);
		if (classname == null)
		{
			throw new HGException("Unable to load predefined type with handle " +
					pHandle +
					" please review the documentation about predefined types and how to hook them with the HyperGraph type system.");
		}

		try
		{
			Class<?> clazz = loadClass(classname);
			HGAtomType type = (HGAtomType)clazz.newInstance();
			type.setHyperGraph(graph);
			return (HGLiveHandle)addPredefinedType(pHandle, type, (URI)null);
		}
		catch (Throwable ex)
		{
			throw new HGException("Could not create predefined type instance with " +
														classname + " for type " + pHandle + ": " + ex.toString(), ex);
		}
	}

	public HGTypeSchema<?> getSchema()
	{
		// TODO: what about contextual schemas? thread-bound? transaction-bound?
		return config.getDefaultSchema(graph);
	}

	public void defineTypeAtom(final HGHandle typeHandle, final URI typeUri)
	{
		final HGTypeSchema<?> schema = config.getSchema(graph, typeUri.getScheme());

		if (graph.getTransactionManager().getContext().getCurrent() != null)
		{
			schema.defineType(typeUri, typeHandle);
			uriToAtomType.put(typeUri, typeHandle);
			getUriDB().addEntry(typeUri.toString(), typeHandle.getPersistent());
		}
		else
			graph.getTransactionManager().transact(new Callable<HGHandle>() {
				public HGHandle call()
				{
					schema.defineType(typeUri, typeHandle);
					uriToAtomType.put(typeUri, typeHandle);
					getUriDB().addEntry(typeUri.toString(), typeHandle.getPersistent());
					return null; // ignored
				}
			});
	}

	/**
	 * <p>
	 * Create a HyperGraph type for the specified Java class and store the type
	 * under the passed in <code>handle</code>.
	 * </p>
	 *
	 * @param handle
	 * @param clazz
	 * @deprecated Please use {@link #defineTypeAtom(HGHandle, URI)} instead.
	 */
	public void defineTypeAtom(final HGPersistentHandle handle, final Class<?> clazz)
	{
		defineTypeAtom(handle, javaSchema.toTypeURI(clazz));
	}

	/**
	 * <p>
	 * Declare that a given type is a sub-type of another type. It is not necessary to
	 * call this method for sub-typing relationships that are automatically inferred
	 * from a Java class hierarchy. However, custom types and type constructors can use
	 * this method for sub-type bookkeeping, which is important for querying and indexing.
	 * A sub-type is represented by a {@link HGSubsumes} link.
	 * </p>
	 * <p>
	 * In addition, the {@link HGIndexManager}
	 * must be informed about a sub-typing relationships in order to maintain indices
	 * appropriately.
	 * </p>
	 *
	 * @param superType The parent type.
	 * @param subType The child type.
	 */
	public void assertSubtype(HGHandle superType, HGHandle subType)
	{
		graph.add(new HGSubsumes(superType, subType));
		graph.getIndexManager().registerSubtype(superType, subType);
	}

	/**
	 * <p>Construct the <code>HGtypeSystem</code> associated with a hypergraph.</p>
	 *
	 * @param graph The <code>HyperGraph</code> which the type system is bound.
	 */
	public HGTypeSystem(HyperGraph graph)
	{
		this.graph = graph;
		//
		// Initialize databases to avoid having to synchronize later.
		//
		getAliases();
		getUriDB();
		getPredefinedTypesDB();
//		this.javaTypes = new JavaTypeFactory();
//		javaTypes.setHyperGraph(graph);
	}

	/**
	 * <p>Return the <code>HyperGraph</code> on which this type
	 * system operates.
	 * </p>
	 */
	public HyperGraph getHyperGraph()
	{
		return graph;
	}

	/**
	 * <p>Return the <code>JavaTypeFactory</code> which is responsible for mapping
	 * Java class to HyperGraph types.</p>
	 * @deprecated
	 */
	public JavaTypeMapper getJavaTypeFactory()
	{
		return javaSchema.getJavaTypeFactory();
	}

	/**
	 * <p>Return the type of the special Java value <code>null</code>.</p>
	 */
	public HGHandle getNullType()
	{
		return nullTypeHandle;
	}

	/**
	 * <p>Return the type of all predefined types.</p>
	 */
	public HGHandle getTop()
	{
		return topHandle;
	}

	/**
	 * <p>
	 * Return the HyperGraphDB type handle corresponding to the given Java class if
	 * a type for this class was previously defined. Return <code>null</code> otherwise.
	 * </p>
	 */
	public HGHandle getTypeHandleIfDefined(URI uri)
	{
		return getTypeHandleIfDefined(uri, false);
	}

	/**
	 * <p>
	 * Return the HyperGraphDB type handle corresponding to the given Java class if a type for this class was
	 * previously defined. Return <code>null</code> otherwise.
	 * </p>
	 */
	public HGHandle getTypeHandleIfDefined(URI uri, boolean localOnly)
	{
//  	System.out.println("GTHID for: " + uri);
		HGHandle typeHandle = findType(uri);
		if (!localOnly && typeHandle == null)
		{
			HGTypeSchema<?> schema = config.getSchema(graph, uri.getScheme());
			if (schema == null)
				throw new HGException("Couldn't find schema for type " + uri);

			typeHandle = schema.findType(uri);
		}

//  	System.out.println("GTHID for: " + uri + " returning handle:" + typeHandle);
		return typeHandle;
	}

	/**
	 * <p>Return the {@link HGHandle} of an existing HyperGraph type that corresponds
	 * to the specified type identifier according to this schema.</p>
	 *
	 * @param typeId The identifier of the type. The identifier must be valid
	 * within this schema. In particular the schema part of the URI must match
	 * this schema's name.
	 * @return The {@link HGHandle} of the HyperGraph type if it exists or <code>null</code>
	 * if it doesn't.
	 */
	public HGHandle findType(URI typeId)
	{
		Map<URI, HGHandle> m = getLocalIdMap();
		HGHandle typeHandle = m.get(typeId);
		if (typeHandle == null)
			typeHandle = uriToAtomType.get(typeId);
		if (typeHandle == null)
			typeHandle = getHandleForIdentifier(typeId);
		return typeHandle;
	}

	public void removeType(URI typeId)
	{
		uriToAtomType.remove(typeId);
	}

	public HGHandle getHandleForIdentifier(URI typeId)
	{
		return getUriDB().findFirst(typeId.toString());
	}

	public Set<URI> getIdentifiersForHandle(HGHandle typeHandle)
	{
		HGSearchResult<String> rs = getUriDB().findByValue(typeHandle.getPersistent());
		HashSet<URI> S = new HashSet<URI>();
		try
		{
			while (rs.hasNext())
				try { S.add(new URI(rs.next())); }
				catch (Exception ex) { throw new HGException(ex); }
			return S;
		}
		finally
		{
			rs.close();
		}
	}

	/**
	 * <p>
	 * Load a class with the given name the way the <code>HGTypeSystem</code> would try
	 * to load it. First try a user-defined class loader with this instance, then try
	 * the Thread content class loader, finally try <code>this.getClass().getClassLoader()</code>
	 * which is usually the system class loader.
	 * </p>
	 *
	 * @param classname
	 * @return
	 */
	public Class<?> loadClass(String classname)
	{
		try
		{
			return HGUtils.loadClass(getHyperGraph(), classname);
		}
		catch (Exception t)
		{
			throw new HGException("Could not load class " + classname, t);
		}
	}

	/**
	 * <p>HyperGraph internal method to handle the loading of a type. A type can be
	 * loaded in one of two ways: either through the <code>getType(HGHandle)</code>
	 * of this class or directly by calling <code>HyperGraph.get</code>. In both cases,
	 * the type system should be explicitly made aware that a new type has been loaded
	 * and be given the possibility to decorate the HGAtomType instance....
	 * </p>
	 *
	 * @param type
	 */
	HGAtomType toRuntimeInstance(HGPersistentHandle handle, HGAtomType type)
	{
		Set<URI> uris = graph.getTypeSystem().getIdentifiersForHandle(handle);

		for (URI u : uris)  //normally only 1
		{
			for (HGTypeSchema<?> schema : config.getSchemas())  //scan is ordered with default first
			{
				if (schema.handles(u.getScheme()))
				{
					return schema.toRuntimeType(handle, type, u);
				}
			}
		}

		return null;
	}

	/**
	 * <p>Specify an application specific predefined type, possibly overriding a default
	 * HyperGraph basic type. This method allows you to add base level types when the primitive types
	 * and type constructors provided with HyperGraph are not sufficient. For instance, one may
	 * replace the handling of simple data types such as strings and booleans, or the management
	 * of certain structured data such as a particular Java class etc.</p>
	 *
	 * <p>
	 * Any <code>HGAtomType</code> that does not have a proper representation in the HyperGraph storage,
	 * should be added at application startup time through this method. While generally it will,
	 * such a top-level type does not need to correspond to a Java type. If there's no corresponding
	 * Java type, the <code>clazz</code> parameter in a call to this method should be <code>null</code>.
	 * </p>
	 *
	 * <p>
	 * Note that a HyperGraph type may map to more than one corresponding Java class. Thus, multiple
	 * calls with the same <code>type</code> parameter, but different <code>clazz</code> parameters
	 * can be made to create a many-to-one relationship between Java type and HyperGraph types.
	 * </p>
	 *
	 * <p>
	 * There is one special case in the mapping of HyperGraph types to Java types: the handling of Java
	 * primitive arrays. From HyperGraph's storage perspective, all arrays are generally recorded in the same
	 * way regardless of the type of their elements (each element is stored through its own type).
	 * Therefore, a single HyperGraph array type would be able to handle all Java primitive arrays.
	 * Of course, it is possible to have specific implementations for a particular <code>T[]</code>
	 * Java types (for instance, an optimized <code>boolean[]</code>). But there is a special generic
	 * handling of all Java built-in arrays that is specified as the HyperGraph type of the
	 * <code>Object[]</code> class. That is, to specify the <code>HGAtomType</code>
	 * that should be used for Java primitive array storage, use the class of <code>Object[]</code>
	 * as the third parameter of this method. For example:
	 * </p>
	 *
	 * <p><code>
	 * typeSystem.addPredefinedType(persistentHandle, type, Class.forName("[Ljava.lang.Object;"));
	 * </code></p>
	 *
	 * @param handle The persistent handle of this type.
	 * @param type The run-time instance of the type.
	 * @param clazz The Java class to which this type corresponds. All atoms that are instances
	 * of this Java class will be handled through this type. This parameter may be null if the
	 * type should not be mapped to a Java class.
	 * @return A run-time handle for the newly added type.
	 */
	public HGHandle addPredefinedType(final HGPersistentHandle handle, final HGAtomType type, final URI typeId)
	{
		if (graph.getTransactionManager().getContext().getCurrent() != null)
			return addPredefinedTypeTransaction(handle, type, typeId);
		else
			return graph.getTransactionManager().transact(new Callable<HGHandle>()
				{ public HGHandle call() { return addPredefinedTypeTransaction(handle, type, typeId); } });
	}

	/**
	 * @deprecated Use {@link #addPredefinedType(HGPersistentHandle, HGAtomType, URI) instead;
	 */
	public HGHandle addPredefinedType(final HGPersistentHandle handle, final HGAtomType type, final Class<?> clazz)
	{
		return addPredefinedType(handle, type, javaSchema.toTypeURI(clazz));
	}

	private HGHandle addPredefinedTypeTransaction(HGPersistentHandle handle, HGAtomType type, final URI typeId)
	{
		type.setHyperGraph(graph);

		//
		// Make sure the type is in storage...
		//
		if (graph.getStore().getLink(handle) == null)
		{
			addPrimitiveTypeToStore(handle);
			graph.add(new HGSubsumes(getTop(), handle));
			try
			{
				//
				// If the type class has a default constructor, we add it to the list
				// automatically instantiable predefined types. Otherwise, we can't
				// instantiate it, and it is somebody else's business to do so.
				//
				if (type.getClass().getConstructor(new Class[0]) != null)
					getPredefinedTypesDB().addEntry(handle, type.getClass().getName());
			}
			catch (NoSuchMethodException e) {
				/* TODO Log this some day when we have logging. */
				//hilpold 2011.10.11
				System.out.println("No default constructor for: " + type.getClass());
			}
		}

		HGLiveHandle typeHandle = graph.cache.get(handle);
		if (typeHandle == null)
			typeHandle = graph.cache.atomRead(handle, type, new HGAtomAttrib());
		else
			typeHandle = graph.cache.atomRefresh(typeHandle, type, false);
		graph.cache.freeze(typeHandle);

		if (typeId != null)
		{
			if (getUriDB().findFirst(typeId.toString()) != null)
				getUriDB().removeAllEntries(typeId.toString());
			uriToAtomType.put(typeId, handle);
			getUriDB().addEntry(typeId.toString(), handle);
		}
		return typeHandle;
	}

	/**
	 * <p>
	 * Return the Java class that corresponds to the given HyperGraphDB type handle. The
	 * result is the class of the run-time instances constructed with the type identified
	 * by <code>typeHandle</code>.
	 * </p>
	 *
	 * @param typeHandle The <code>HGHandle</code> identifying the type whose runtime Java
	 * class is required.
	 * @return The Java class corresponding to <code>typeHandle</code> or <code>null</code>
	 * if there's no such correspondence.
	 */
	public Class<?> getClassForType(HGHandle typeHandle)
	{
		Set<URI> ids = getIdentifiersForHandle(typeHandle);
		for (URI u : ids)
			if (u.getScheme().equals(javaSchema.getName()))
				return javaSchema.getTypeDescriptor(u);
		return null;
	}

	/**
	 * <p>
	 * Specifically map a HyperGraphDB {@link HGAtomType}, already stored as an
	 * atom with handle <code>typeHandle</code> to the Java class <code>clazz</code>.
	 * Any previous type association with <code>clazz</code> will be removed.
	 * </p>
	 * @param typeHandle can't be null
	 * @param clazz can't be null
	 */
	public void setTypeForClass(final HGHandle typeHandle, final Class<?> clazz)
	{
		setTypeForURI(typeHandle, JavaTypeSchema.classToURI(clazz));
	}

	/**
	 * <p>
	 * Specifically map a HyperGraphDB {@link HGAtomType}, already stored as an
	 * atom with handle <code>typeHandle</code> to its uri.
	 * Any previous type association with <code>uri</code> will be removed.
	 * </p>
	 * @param typeHandle can't be null
	 * @param uri can't be null
	 */
	public void setTypeForURI(final HGHandle typeHandle, final URI uri)
	{
		graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>()
		{
			public HGHandle call()
			{
				removeType(uri);
				getUriDB().removeAllEntries(uri.toString());
				uriToAtomType.put(uri, typeHandle);
				getUriDB().addEntry(uri.toString(), typeHandle.getPersistent());
				return null;
			}
		});
	}

	public void addAliasURI(final URI typeId, final HGHandle typeHandle)
	{
		uriToAtomType.put(typeId, typeHandle);
		getUriDB().addEntry(typeId.toString(), typeHandle.getPersistent());
	}

	/**
	 * <p>
	 * Return the Java classname that corresponds to the given HyperGraphDB type handle. The
	 * result is the name of the class class of the run-time instances constructed with
	 * the type identified by <code>typeHandle</code>.
	 * </p>
	 *
	 * @param typeHandle The <code>HGHandle</code> identifying the type whose runtime Java
	 * class is required.
	 * @return The Java class name corresponding to <code>typeHandle</code> or <code>null</code>
	 * if there's no such correspondence.
	 */
	public String getClassNameForType(HGHandle typeHandle)
	{
		Class<?> cl = null;
		URI u = getURIForType(typeHandle, javaSchema.getName());
		if (u != null)
			cl = javaSchema.getTypeDescriptor(u);
		return cl == null ? null : cl.getName();
	}

	/**
	 * <p>
	 * Return the URI that corresponds to the given HyperGraphDB type handle and scheme specified.
	 * </p>
	 *
	 * @param typeHandle The <code>HGHandle</code> identifying the type whose runtime Java
	 * class is required.
	 * @param scheme The scheme identifying the schema that manages this type.
	 * @return The URI corresponding to <code>typeHandle</code> or <code>null</code>
	 * if there's no such correspondence.
	 */
	public URI getURIForType(HGHandle typeHandle, String scheme)
	{
		Set<URI> uris = getIdentifiersForHandle(typeHandle);
		for (URI u : uris)
			if (u.getScheme().equals(scheme))
			{
				return u;
			}
		return null;
	}

	/**
	 * <p>Return the <code>HGAtomType</code> by its <code>HGHandle</code>.</p>
	 *
	 * @param handle The handle of the atom type itself. Note that to retrieve the type
	 * of an atom, you must use the <code>getAtomType(Object)</code> method.
	 */
	public HGAtomType getType(HGHandle handle)
	{
		return (HGAtomType)graph.get(handle);
	}

	/**
	 * <p>Return the <code>HGAtomType</code> corresponding to the given alias.</p>
	 *
	 * @param alias The alias.
	 * @return The type instance or <code>null</code> if this alias has not
	 * been defined.
	 */
	public HGAtomType getType(String alias)
	{
		HGHandle handle = getTypeHandle(alias);
		if (handle != null)
			return getType(handle);
		else
			return null;
	}

	/**
	 * <p>
	 * Return the <code>HGAtomType</code> corresponding to the passed in
	 * Java class. This is equivalent to <code>(HGAtomType)HyperGraph.get(getTypeHandle(clazz))</code>.
	 * </p>
	 *
	 */
	@SuppressWarnings("unchecked")
		public <T extends HGAtomType> T getAtomType(Class<?> clazz)
	{
		return (T)graph.get(getTypeHandle(clazz));
	}

	/**
	 * <p>
	 * Return the default <code>HyperGraph</code> type of the given atom object. Note
	 * the <em>default</em> here means that the type returned is the one that would
	 * be automatically assigned to instances of the concrete type of <code>object</code>.
	 * That is, calling this method is equivalent to calling <code>getAtomType(object.getClass())</code>.
	 * If <code>object</code> is the run-time instance of an actual HyperGraph atom that
	 * was explicitly assigned a type, the latter may be different than the default type.
	 * </p>
	 */
	public HGAtomType getAtomType(Object object)
	{
		return getType(getTypeHandle(object));
	}

	/**
	 * <p>Return the type instance of a given atom.</p>
	 *
	 * @param handle The atom whose type is desired.
	 * @return The type of the atom.
	 */
	public HGAtomType getAtomType(HGHandle handle)
	{
		return (HGAtomType)getTypeHandle(handle);
	}

	/**
	 * <p>Return <code>true</code> if there is a HyperGraph type corresponding to the given
	 * class and <code>false</code> otherwise.</p>
	 */
	public boolean hasType(Class<?> clazz)
	{
		return getTypeHandleIfDefined(clazz) != null;
	}

	/**
	 * Return the handle of the type associated with the specified class. The name of
	 * the class is first converted to a type URI. See {@link #getHandleForIdentifier(URI)}.
	 */
	public HGHandle getTypeHandleIfDefined(Class<?> clazz)
	{
		return findType(javaSchema.toTypeURI(clazz));
	}

	/**
	 * <p>Return the <code>HGHandle</code> of the HyperGraph type representing a given
	 * Java class. If no type has been associated yet with that particular <code>Class</code>, a
	 * new one will be created using the currently active <code>JavaTypeFactory</code>.</p>
	 *
	 * @param clazz The <code>Class</code> instance of the Java class. Cannot be <code>null</code>
	 * @return The <code>HGHandle</code> for that class. If the Java class hasn't been previously
	 * mapped to a HyperGraph atom type, a new HyperGraph type will be created and the new handle
	 * will be returned.
	 */
	public HGHandle getTypeHandle(final Class<?> clazz)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>()
		{
			public HGHandle call()
			{
				HGHandle h = getTypeHandleIfDefined(clazz);
				return h != null ? h : createNewType(javaSchema.toTypeURI(clazz));
			}
		});
	}

	public HGHandle getTypeHandle(final URI typeIdentifier)
	{
		return graph.getTransactionManager().ensureTransaction(new Callable<HGHandle>()
		{
			public HGHandle call()
			{
				HGHandle h = getTypeHandleIfDefined(typeIdentifier);
				return h != null ? h : createNewType(typeIdentifier);
			}
		});
	}

	private HGHandle createNewType(final URI typeIdentifier)
	{
		HGPersistentHandle typeHandle = graph.getHandleFactory().makeHandle();
		defineTypeAtom(typeHandle, typeIdentifier);
		return typeHandle;
	}

	/**
	 * <p>Return the handle of the type corresponding to the given alias.</p>
	 *
	 * @param alias The alias.
	 * @return The type handle or <code>null</code> if this alias has not
	 * been defined.
	 */
	public HGHandle getTypeHandle(String alias)
	{
		if (alias == null)
			return null;
		else
		{
			HGPersistentHandle handle = getAliases().findFirst(alias);
			if (handle != null)
				return graph.refreshHandle(handle);
			else
				return null;
		}
	}

	/**
	 * <p>
	 * Return the (handle of the) type of the given atom.
	 * </p>
	 *
	 * @param atomHandle The handle of the atom whose type is desired.
	 * @deprecated Please call {@link #HyperGraph.getType(HGHandle)} instead. This method
	 * <strong>will</strong> be removed in future versions.
	 */
	public HGHandle getTypeHandle(HGHandle atomHandle)
	{
		HGPersistentHandle [] layout = graph.getStore().getLink(graph.getPersistentHandle(atomHandle));
		if (layout == null || layout.length == 0)
			throw new HGException("Could not retrieve atom with handle " +
														graph.getPersistentHandle(atomHandle) + " from the HyperGraph store.");
		HGHandle live = graph.cache.get(layout[0]);
		return live == null ? layout[0] : live;
	}

	/**
	 * <p>
	 * Return the HyperGraph type handle of the given Java object.
	 * </p>
	 *
	 * <p>
	 * This method will first try to find the HyperGraph <code>HGHandle</code> of the object
	 * and retrieve the type based on that handle. If not, it will retrieve the default
	 * HyperGraph type of the concrete Java class of the object (i.e. of x.getClass()).
	 * </p>
	 *
	 * @param x The object whose HyperGraph type is desired. Cannot be <code>null</code>.
	 * @return The <code>HGHandle</code> of the HyperGraph type for that object.
	 */
	public HGHandle getTypeHandle(Object x)
	{
		if (x == null)
			throw new NullPointerException(
							"HGTypeSystem.getAtomType(Object) invoked with a null object -- and 'null' has no type.");
		HGHandle atom = graph.getHandle(x);
		if (atom != null)
			return getTypeHandle(atom);
		else
		{
			URI xUri = null;
			for (HGTypeSchema<?> schema : config.getSchemas())  //scan is ordered with default first
			{
				xUri = schema.toTypeURI(x);
				if (xUri != null)
					break;
			}

			return getTypeHandle(xUri);
		}
	}

	/**
	 * <p>
	 * Add a new alias for a given type.
	 * </p>
	 *
	 * @param typeHandle The <code>HGPersistentHandle</code> of the type. Note
	 * that the method doesn't check whether this is in fact a type handle.
	 * @param alias A non-null alias name. If this name is already used to alias
	 * another type, an exception is thrown.
	 */
	public void addAlias(final HGHandle typeHandle, final String alias)
	{
		if (graph.getTransactionManager().getContext().getCurrent() != null)
			addAliasTransaction(typeHandle, alias);
		else
			graph.getTransactionManager().transact(new Callable<Object>()
				{ public Object call() { addAliasTransaction(typeHandle, alias); return null; } });
	}

	private void addAliasTransaction(HGHandle typeHandle, String alias)
	{
		HGBidirectionalIndex<String, HGPersistentHandle> aliases = getAliases();
		synchronized (aliases)
		{
			HGPersistentHandle handle = aliases.findFirst(alias);
			if (handle == null)
				aliases.addEntry(alias, graph.getPersistentHandle(typeHandle));
			else
				throw new HGException("Alias '" + alias + "' already defined.");
		}
	}

	/**
	 * <p>
	 * Retrieve all the aliases of a given type.
	 * </p>
	 *
	 * @param typeHandle The handle of the type whose aliases are desired.
	 * @return A regular <code>HGSearchResult</code> containing the aliases. Make
	 * sure to close the result set as all other result sets in HyperGraphDB.
	 */
	public Set<String> findAliases(HGHandle typeHandle)
	{
		Set<String> result =  new HashSet<String>();
		HGSearchResult<String> rs = getAliases().findByValue(graph.getPersistentHandle(typeHandle));
		try
		{
			while (rs.hasNext())
				result.add(rs.next());
			return result;
		}
		finally
		{
			HGUtils.closeNoException(rs);
		}
	}

	/**
	 * <p>Return any metadata associated with the given type or <code>null</code>.</p>
	 */
	public HGTypeStructuralInfo getTypeMetaData(HGHandle typeHandle)
	{
		HGTypeStructuralInfo typeStruct = hg.getOne(graph, hg.and(hg.type(HGTypeStructuralInfo.class),
												hg.eq("typeHandle", typeHandle)));
		return typeStruct;
	}

	/**
	 * <p>
	 * Remove a type alias. If the alias hasn't been previously
	 * defined, nothing is done.
	 * </p>
	 *
	 * @param alias The alias to remove. Cannot by <code>null</code>.
	 * @throws NullPointerException if <code>alias</code> is null.
	 */
	public void removeAlias(final String alias)
	{
		if (graph.getTransactionManager().getContext().getCurrent() != null)
			removeAliasTransaction(alias);
		else
			graph.getTransactionManager().transact(new Callable<Object>()
				{ public Object call() { removeAliasTransaction(alias); return null; } });
	}

	private void removeAliasTransaction(String alias)
	{
		HGBidirectionalIndex<String, HGPersistentHandle> aliases = getAliases();
		synchronized (aliases)
		{
			HGPersistentHandle handle = aliases.findFirst(alias);
			if (handle != null)
				aliases.removeEntry(alias, handle);
		}
	}

	/**
	 * <p>Permanently delete the type referred to by the passed in persistent handle.</p>
	 *
	 * <p>Should be called only by HyperGraph when removing an atom that is also a type.</p>
	 *
	 * @param typeHandle
	 * @param type the type instance
	 */
	void remove(final HGPersistentHandle typeHandle, final HGAtomType type)
	{
		//
		// Remove subsumes relationships.
		//
		List<HGHandle> subsumesLinks = hg.findAll(graph, hg.and(hg.incident(typeHandle), hg.type(HGSubsumes.class)));
		for (HGHandle h : subsumesLinks)
			graph.remove(h);

		//
		// Remove HGTypeStructuralInfo attached to this type.
		//
		HGHandle typeStruct = hg.findOne(graph, hg.and(hg.type(HGTypeStructuralInfo.class), hg.eq("typeHandle", typeHandle)));
		if (typeStruct != null)
			graph.remove(typeStruct);

		//
		// Remove all aliases
		//
		HGBidirectionalIndex<String, HGPersistentHandle> aliases = getAliases();
		HGSearchResult<String> rs = aliases.findByValue(typeHandle);
		try
		{
			while (rs.hasNext())
			{
				// TODO: maybe a problem here if we are removing while iterating...
				aliases.removeEntry(rs.next(), typeHandle);
			}
		}
		finally
		{
			rs.close();
		}

		//
		// Remove from HG type <-> Java class mappings
		//
		try
		{
			HGBidirectionalIndex<String, HGPersistentHandle> idx = getUriDB();
			Set<URI> uris = getIdentifiersForHandle(typeHandle);
			for (URI u : uris)
			{
				idx.removeEntry(u.toString(), typeHandle);
//				HGTypeSchema<?> schema = config.getSchema(u.getScheme());
//				if (schema != null)
//					schema.removeType(u);
				removeType(u);
			}
		}
		catch (Throwable t)
		{
			throw new HGException(t);
		}
		finally
		{
			if (rs != null) try { rs.close(); } catch (Throwable _) { }
		}
	}

	private class UriToTypeCache extends LinkedHashMap<String, HGHandle> {
		static final long serialVersionUID = -1;

		public UriToTypeCache() {
			super(1000, 0.75f, true);
		}

		@Override
		protected boolean removeEldestEntry(final Map.Entry<String, HGHandle> eldest) {
			if (size() > MAX_URI_TO_TYPE) {
				if (eldest.getValue() instanceof HGLiveHandle) {
					final HGLiveHandle h = (HGLiveHandle)eldest.getValue();

					if (h.getRef() == null) {
						return true; // if it has been evicted from the atom cache, removed it from here too
					}
					else if (graph.getCache().isFrozen(h)) {
						return get(eldest.getKey()) == null; // this will return false and put the element on top of the
						// list
					}
					else {
						return false; // simply return false, but don't remove since it's still in the cache
					}
				}
				else {
					final HGLiveHandle h = graph.getCache().get((HGPersistentHandle)eldest.getValue());

					if (h != null) {
						eldest.setValue(h);
						return false;
					}
					else {
						return true;
					}
				}
			}
			else {
				return false;
			}
		}
	}

}
