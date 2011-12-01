package org.hypergraphdb.app.owl.type;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.app.owl.model.OWLAnnotationPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLClassHGDB;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeHGDB;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.hypergraphdb.type.HGCompositeType;
import org.hypergraphdb.type.HGProjection;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedObject;

/**
 * OWLNamedObjectType.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 3, 2011
 * 
 * 2011.11.30 Optimizing this class (keep IRI typehandle) changed Load time of County ontology (1MB) from FunctionalSyntaxFile from 4m45sec to 1m45sec.
 * 2011.12.01 Added default constructor, HG could not instantiate type.
 */
public class OWLNamedObjectType extends HGAtomTypeBase implements HGCompositeType {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends OWLNamedObject>> OWL_NAMED_OBJECT_TYPES_HGDB = Arrays.<Class<? extends OWLNamedObject>>asList(
			OWLAnnotationPropertyHGDB.class,
			OWLClassHGDB.class,
			OWLDatatypeHGDB.class,
			OWLNamedIndividualHGDB.class,
			OWLDataPropertyHGDB.class,
			OWLObjectPropertyHGDB.class);
		
	public static final String DIM_IRI = "IRI";
	//public static final String DIM_URI = "URI";
	public static final List<String> DIMENSIONS = Collections.unmodifiableList(Arrays.asList(
			DIM_IRI));
	public Class<? extends OWLNamedObject> type;
	
	public OWLNamedObjectType() {
	}
	
//2011.12.01 replaced with def construtor	public OWLNamedObjectType(Class<? extends OWLNamedObject> type) {
//		this.type = type;
//	}

	/**
	 * @return the type
	 */
	public Class<? extends OWLNamedObject> getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Class<? extends OWLNamedObject> type) {
		this.type = type;
	}

	public Object make(HGPersistentHandle handle, LazyRef<HGHandle[]> targetSet,
			IncidenceSetRef incidenceSet) {
		HGHandle[] layout = graph.getStore().getLink(handle);
		IRI iri = graph.get(layout[0]);
		Constructor<? extends OWLNamedObject> constructor = null;
		if (iri == null)
			throw new NullPointerException("IRI missing for OwlNamedObject at " + handle);
		//
		//using reflection
		if (OWL_NAMED_OBJECT_TYPES_HGDB.contains(type)) {
			try {
				constructor = type.getConstructor(IRI.class);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}			
		} else {
			throw new IllegalStateException("Could not create object. OWLNamedObject subclass not recognized:" + type);			
		}
		try {
			return constructor.newInstance(iri);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
//		if (type.equals(OWLAnnotationPropertyHGDB.class)) {
//			return new OWLAnnotationPropertyHGDB(iri);
//		} else if (type.equals(OWLClassHGDB.class)) {
//			return new OWLClassHGDB(iri);
//		} else if (type.equals(OWLDatatypeHGDB.class)) {
//			return new OWLDatatypeHGDB(iri);
//		} else if (type.equals(OWLNamedIndividualHGDB.class)) {
//			return new OWLNamedIndividualHGDB(iri);
//		} else if (type.equals(OWLDataPropertyHGDB.class)) {
//			return new OWLDataPropertyHGDB(iri);
//		} else if (type.equals(OWLObjectPropertyHGDB.class)) {
//			return new OWLObjectPropertyHGDB(iri);
//		} else {
//			throw new IllegalStateException("Could not create object. OWLNamedObject subclass not recognized:" + type);
//		}
	}

	public void release(HGPersistentHandle handle) {
		graph.getStore().removeLink(handle);
	}

	public HGPersistentHandle store(Object instance) {
		OWLNamedObject oni = (OWLNamedObject) instance;
		// if (oid.isAnonymous())
		// return graph.getHandleFactory().anyHandle();
		HGHandle irihandle = hg.assertAtom(graph, oni.getIRI());
		return graph.getStore().store(new HGPersistentHandle[] { irihandle.getPersistent() });
	}

	//
	// HGCompositeType Interface
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.type.HGCompositeType#getDimensionNames()
	 */
	@Override
	public Iterator<String> getDimensionNames() {
		return DIMENSIONS.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.type.HGCompositeType#getProjection(java.lang.String)
	 */
	@Override
	public HGProjection getProjection(String dimensionName) {
		if (DIM_IRI.equals(dimensionName))
			return projection;
		else
			throw new IllegalArgumentException();
	}
	
	//2011.11.30 OPTIMIZATION (Projection was created each time before, led to loading class each time.)
	HGProjection projection = new HGProjection() {

		@Override
		public int[] getLayoutPath() {
			return null;
		}

		@Override
		public String getName() {
			return DIM_IRI;
		}

		//2011.11.30 OPTIMIZATION hilpold
		//Based on profiling results that showed wasted time with classloading.
		
		HGHandle typeHandle = null;
		
		@Override
		public HGHandle getType() {
			if (typeHandle == null || !graph.isLoaded(typeHandle)) {
			 typeHandle = graph.getTypeSystem().getTypeHandle(IRI.class);
			 System.out.print("|");
			}
			return typeHandle;
			//OLD return graph.getTypeSystem().getTypeHandle(IRI.class);
		}

		@Override
		public void inject(Object atomValue, Object value) {
		}

		@Override
		public Object project(Object atomValue) {
			return ((OWLNamedObject)atomValue).getIRI();
		}
	};		


}
