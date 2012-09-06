package org.hypergraphdb.type;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.hypergraphdb.HGConfiguration;
import org.hypergraphdb.HGException;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.util.HGUtils;

/**
 * <p>
 * This class encapsulates startup configuration parameters for the HyperGraphDB
 * type system. An instance of this class is provided in the top-level
 * {@link HGConfiguration}
 * </p>
 *
 * @author Borislav Iordanov
 *
 */
public class HGTypeConfiguration
{
	private HGTypeSchema<?> defaultSchema = null;
	private String defaultSchemaClass = null;

	private Map<String, HGTypeSchema<?>> schemas = new LinkedHashMap<String, HGTypeSchema<?>>(); //using Linked map to get default first

	public HGTypeConfiguration()
	{
	}

	public Collection<HGTypeSchema<?>> getSchemas()
	{
		return schemas.values();
	}

	/**
	 * <p>Return the instance responsible for creating HyperGraphDB type from Java classes.</p>
	 */
	public <T extends HGTypeSchema<?>> T getDefaultSchema(HyperGraph graph)
	{
		return getDefaultSchema(graph.getConfig());
	}

	/**
	 * <p>Return the instance responsible for creating HyperGraphDB type from Java classes.</p>
	 */
	@SuppressWarnings("unchecked")
	public <T extends HGTypeSchema<?>> T getDefaultSchema(HGConfiguration config)
	{
		if (defaultSchema == null)
		{
			if (defaultSchemaClass == null)  //lazily set to allow default to always be first in "list"
			{
				setDefaultSchema(new JavaTypeSchema());
			}
			else //lazily set to allow setting from different class loaders
			{
				try {
					Class<?> cl = HGUtils.loadClass(config, defaultSchemaClass);
					Constructor<?> ctor = cl.getDeclaredConstructor();
					ctor.setAccessible(true);
					setDefaultSchema((HGTypeSchema<?>)ctor.newInstance());
				}
				catch (Exception e) {
					throw new HGException(e);
				}
			}
		}

		return (T)defaultSchema;
	}

	/**
	 * <p>Specify the instance responsible for creating HyperGraphDB type from Java classes.</p>
	 */
	public void setDefaultSchema(HGTypeSchema<?> typeSchema)
	{
		this.defaultSchema = typeSchema;
		schemas.put(typeSchema.getName(), typeSchema);
	}

	public void setDefaultSchemaClass(String defaultSchemaClass) {
		this.defaultSchemaClass = defaultSchemaClass;
	}

	public void addSchema(HGTypeSchema<?>...schemas)
	{
		for (HGTypeSchema<?> s : schemas)
			this.schemas.put(s.getName(), s);
	}

	public void addAdditionalScheme(String scheme, HGTypeSchema<?>...schemas)
	{
		for (HGTypeSchema<?> s : schemas)
			this.schemas.put(scheme, s);
	}

	public <T extends HGTypeSchema<?>> T getSchema(HyperGraph graph, String name)
	{
		return getSchema(graph.getConfig(), name);
	}

	@SuppressWarnings("unchecked")
	public <T extends HGTypeSchema<?>> T getSchema(HGConfiguration config, String name)
	{
		T schema = (T)schemas.get(name);

		if (schema == null && name.equals(JavaTypeSchema.SCHEME_NAME))
		{
			if (defaultSchema == null)
			{
				defaultSchema = getDefaultSchema(config);
			}

			if (defaultSchema.getName().equals(JavaTypeSchema.SCHEME_NAME))
			{
				schema = (T)defaultSchema;
			}
			else
			{
				schema = (T)new JavaTypeSchema();
				addSchema(schema);
			}
		}

		return schema;
	}
}