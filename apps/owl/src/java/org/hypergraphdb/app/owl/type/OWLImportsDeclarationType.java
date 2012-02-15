package org.hypergraphdb.app.owl.type;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.IncidenceSetRef;
import org.hypergraphdb.LazyRef;
import org.hypergraphdb.type.HGAtomTypeBase;
import org.hypergraphdb.type.HGCompositeType;
import org.hypergraphdb.type.HGProjection;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;

import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * OWLImportsDeclarationType.
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 26, 2011
 */
public class OWLImportsDeclarationType extends HGAtomTypeBase implements HGCompositeType {
	public static final String DIM_IRI = "IRI";
	public static final String DIM_URI = "URI";
	public static final List<String> DIMENSIONS = Collections.unmodifiableList(Arrays.asList(DIM_IRI, DIM_URI));
	
	public Object make(HGPersistentHandle handle,
					   LazyRef<HGHandle[]> targetSet, 
					   IncidenceSetRef incidenceSet)
	{
		if (handle.equals(graph.getHandleFactory().anyHandle()))
			return new OWLImportsDeclarationImpl(null); // anonymous
		HGHandle [] layout = graph.getStore().getLink(handle);
		IRI iri = graph.get(layout[0]);
		if (iri == null)
			throw new NullPointerException("IRI missing for OWLImportsDeclaration at " + handle);
		return new OWLImportsDeclarationImpl(iri);
	} 

	public void release(HGPersistentHandle handle)
	{
		graph.getStore().removeLink(handle);
	}

	public HGPersistentHandle store(Object instance)
	{
		OWLImportsDeclaration oid = (OWLImportsDeclaration)instance;
//		if (oid.isAnonymous())
//			return graph.getHandleFactory().anyHandle();
		HGHandle irihandle = hg.assertAtom(graph, oid.getIRI());
		return graph.getStore().store(new HGPersistentHandle[]{irihandle.getPersistent()}); 
	}

	//
	// HGCompositeType Interface
	//
	
	/* (non-Javadoc)
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
			return new HGProjection() {

				@Override
				public int[] getLayoutPath() {
					return null;
				}

				@Override
				public String getName() {
					return DIM_IRI;
				}

				@Override
				public HGHandle getType() {
					return graph.getTypeSystem().getTypeHandle(IRI.class);
				}

				@Override
				public void inject(Object atomValue, Object value) {
				}

				@Override
				public Object project(Object atomValue) {
					return ((OWLImportsDeclaration) atomValue).getIRI();
				}

			};
		else if (DIM_URI.equals(dimensionName))
			return new HGProjection() {

				@Override
				public int[] getLayoutPath() {
					return null;
				}

				@Override
				public String getName() {
					return DIM_URI;
				}

				@Override
				public HGHandle getType() {
					return graph.getTypeSystem().getTypeHandle(URI.class);
				}

				@Override
				public void inject(Object atomValue, Object value) {
				}

				@Override
				public Object project(Object atomValue) {
					return ((OWLImportsDeclaration) atomValue).getURI();
				}

			};
		else
			throw new IllegalArgumentException();
	}
}
