package org.hypergraphdb.type;

import java.net.URI;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HyperGraph;

public interface HGTypeSchema<TypeDescriptor>
{
		/**
		 * <p>
		 * Return the name of this <code>HGTypeSchema</code>. The name of a type
		 * schema uniquely identifies it.
		 * </p>
		 */
		String getName();

		/**
		 * <p>
		 * Return true if the schema handles the scheme in the argument, false otherwise.
		 * </p>
		 */
		boolean handles(String scheme);

		/**
		 * <p>
		 * Initialize the schema with the {@link HyperGraph} instance to which it is bound.
		 * A given schema runtime instance is only bound to one database instance.
		 * </p>
		 *
		 * @param graph
		 */
		void initialize(HyperGraph graph, HGTypeConfiguration typeConfiguration);

		/**
		 * <p>
		 * Construct a new HyperGraphDB type from the specified type identifier<code>URI</code>.
		 * It is the responsibility of the schema implementation to find the correct
		 * <code>TypeDescriptor</code> for that identifier. The schema may return an existing
		 * HyperGraph type that corresponds to the identifier is it finds one already
		 * in the database. However, types, like other atoms, are not guaranteed to be
		 * immutable. Therefore, a schema does not "own" a HyperGraph type and the latter
		 * may be modified outside of its control.
		 * </p>
		 *
		 * @param typeId The identifier of the type. The identifier must be valid
		 * within this schema. In particular the schema part of the URI must match
		 * this schema's name.
		 * @return The {@link HGHandle} of a newly created or existing HyperGraph type
		 * that corresponds to the passed in type identifier.
		 */
		void defineType(URI typeId, HGHandle typeHandle);

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
		HGHandle findType(URI typeId);

		/**
		 * <p>
		 * The type schema may wrap a given HyperGraphDB type into a different
		 * runtime instance. This is useful when atoms, and therefore in all likelihood
		 * their types as well, have different runtime representations depending on
		 * the current type schema in effect. Every time a type is loaded by the type
		 * system, the <code>toRuntimeType</code> of the current type schema is called
		 * to give it a change to provide a different representation.
		 * </p>
		 *
		 * @param typeHandle The {@link HGHandle} of the type.
		 * @param typeInstance The type instance constructed by default from the
		 * type system.
		 * @return A (possibly) different runtime representation of the type.
		 */
		HGAtomType toRuntimeType(HGHandle typeHandle, HGAtomType typeInstance, URI uri);

		/**
		 * <p>
		 * If a given type is wrapped under a different runtime representation by
		 * the {@link #toRuntimeType(HGHandle, HGAtomType)} method, this method
		 * retrieves the underlying default representation as constructed by the
		 * type system.
		 * </p>
		 * @param typeHandle The {@link HGHandle} of the type.
		 * @param typeInstance A modified runtime representation of the type.
		 * @return
		 */
		HGAtomType fromRuntimeType(HGHandle typeHandle, HGAtomType typeInstance);

		/**
		 * <p>Return the location of the type configuration file. This file can be either
		 * a classpath resource or a file on disk or
		 */
		public String getPredefinedTypes();

		/**
		 * <p>
		 * Specify the type configuration file to use when bootstrapping the type system. This file
		 * must contain the list of predefined types needed for the normal functioning of a database
		 * instance. Each line in this text file is a space separated list of (1) the persistent handle
		 * of the type (2) The Java class implementing the {@link HGAtomType} interface and optionally
		 * (3) one or more Java classes to which the type implementation is associated.
		 * </p>
		 *
		 * @param typeConfiguration The location of the type configuration file. First, an attempt
		 * is made to load this location is a classpath resource. Then as a local file. Finally as
		 * a remote URL-based resource.
		 */
		public void setPredefinedTypes(String predefinedTypes);

		URI toTypeURI(Object object);

		URI toTypeURI(Class<?> javaClass);
}