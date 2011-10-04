package org.hypergraphdb.app.owl;

import static org.semanticweb.owlapi.model.AxiomType.AXIOM_TYPES;
import static org.semanticweb.owlapi.util.CollectionFactory.createSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.hypergraphdb.HGGraphHolder;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGHandleHolder;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.app.owl.core.AbstractInternalsHGDB;
import org.hypergraphdb.app.owl.model.OWLAnnotationPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLClassHGDB;
import org.hypergraphdb.app.owl.model.OWLDataPropertyHGDB;
import org.hypergraphdb.app.owl.model.OWLDatatypeHGDB;
import org.hypergraphdb.app.owl.model.OWLDeclarationAxiomHGDB;
import org.hypergraphdb.app.owl.model.OWLNamedIndividualHGDB;
import org.hypergraphdb.app.owl.model.OWLObjectPropertyHGDB;
import org.hypergraphdb.app.owl.type.link.ImportDeclarationLink;
import org.hypergraphdb.handle.HGLiveHandle;
import org.hypergraphdb.query.SubgraphMemberCondition;
import org.hypergraphdb.transaction.HGTransactionConfig;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

/**
 * HGDBOntologyInternalsImpl.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBOntologyInternalsImpl extends AbstractInternalsHGDB implements HGGraphHolder, HGHandleHolder{
	   static {
		   //TODO Disable force assertions before release.
	        boolean assertsEnabled = false;
	        assert assertsEnabled = true; //force assertions.
	        if (!assertsEnabled) {
	            throw new RuntimeException("We need Asserts to be enabled. Use: java -ea:org.hypergraphdb.app.owl...");
	        }
	    } 
	protected HGHandle handle;
	protected HyperGraph graph;
	protected HGDBOntologyImpl ontology; 
	protected HGHandle ontoHandle; 

	// hilpold protected Set<OWLImportsDeclaration> importsDeclarations;
	protected Set<OWLAnnotation> ontologyAnnotations; // recursive??
	protected Map<AxiomType<?>, Set<OWLAxiom>> axiomsByType;
	protected Map<OWLAxiom, Set<OWLAxiom>> logicalAxiom2AnnotatedAxiomMap;
	protected Set<OWLClassAxiom> generalClassAxioms;
	protected Set<OWLSubPropertyChainOfAxiom> propertyChainSubPropertyAxioms;
	//protected Map<OWLClass, Set<OWLAxiom>> owlClassReferences;
	//protected Map<OWLObjectProperty, Set<OWLAxiom>> owlObjectPropertyReferences;
	//protected Map<OWLDataProperty, Set<OWLAxiom>> owlDataPropertyReferences;
	//protected Map<OWLNamedIndividual, Set<OWLAxiom>> owlIndividualReferences;
	protected Map<OWLAnonymousIndividual, Set<OWLAxiom>> owlAnonymousIndividualReferences;
	//protected Map<OWLDatatype, Set<OWLAxiom>> owlDatatypeReferences;
	//protected Map<OWLAnnotationProperty, Set<OWLAxiom>> owlAnnotationPropertyReferences;

	// hilpold 2011.09.27 eliminating protected Map<OWLEntity,
	// Set<OWLDeclarationAxiom>> declarationsByEntity;

	public HGDBOntologyInternalsImpl() {
		initMaps();
	}

	protected void initMaps() {
		// hilpold this.importsDeclarations = createSet();
		this.ontologyAnnotations = createSet();
		this.axiomsByType = createMap();
		this.logicalAxiom2AnnotatedAxiomMap = createMap();
		this.generalClassAxioms = createSet();
		this.propertyChainSubPropertyAxioms = createSet();
		//this.owlClassReferences = createMap();
		//this.owlObjectPropertyReferences = createMap();
		//this.owlDataPropertyReferences = createMap();
		//this.owlIndividualReferences = createMap();
		this.owlAnonymousIndividualReferences = createMap();
		//this.owlDatatypeReferences = createMap();
		//this.owlAnnotationPropertyReferences = createMap();
		// this.declarationsByEntity = createMap();
	}

	// BORIS
	// public Set<OWLAxiom> getAxiomsByType(AxiomType t)
	// {
	// HGHandle hgType = axiomTypeToHGType.get(t);
	// HashSet<OWLAxiom> S= new HashSet();
	// S.addAll(hg.getAll(graph, hg.type(hgType)));
	// return S;
	// }

	/**
	 * Entity of OWLDeclarationAxiom declared?
	 * iff we have Entity with incidenceset count > 0.
	 * We should remove such entities.
	 * hilpold
	 */
	public boolean isDeclared(OWLDeclarationAxiom ax) {
		HGHandle entityHandle = graph.getHandle(ax.getEntity());
		if (entityHandle != null) {
			return graph.getIncidenceSet(entityHandle).size() > 0;
		} else { 
			return (graph.getHandle(ax) != null);
		}
		//old return declarationsByEntity.containsKey(ax.getEntity());
	}

	public boolean isEmpty() {
		for (Set<OWLAxiom> axiomSet : axiomsByType.values()) {
			if (!axiomSet.isEmpty()) {
				return false;
			}
		}
		return ontologyAnnotations.isEmpty();
	}

	public Set<OWLDatatypeDefinitionAxiom> getDatatypeDefinitions(OWLDatatype datatype) {
		Set<OWLDatatypeDefinitionAxiom> result = createSet();
		Set<OWLDatatypeDefinitionAxiom> axioms = getAxiomsInternal(AxiomType.DATATYPE_DEFINITION);
		for (OWLDatatypeDefinitionAxiom ax : axioms) {
			if (ax.getDatatype().equals(datatype)) {
				result.add(ax);
			}
		}
		return result;
	}

	public Set<OWLSubAnnotationPropertyOfAxiom> getSubAnnotationPropertyOfAxioms(
			OWLAnnotationProperty subProperty) {
		Set<OWLSubAnnotationPropertyOfAxiom> result = createSet();
		for (OWLSubAnnotationPropertyOfAxiom ax : getAxiomsInternal(AxiomType.SUB_ANNOTATION_PROPERTY_OF)) {
			if (ax.getSubProperty().equals(subProperty)) {
				result.add(ax);
			}
		}
		return result;
	}

	public Set<OWLAnnotationPropertyDomainAxiom> getAnnotationPropertyDomainAxioms(
			OWLAnnotationProperty property) {
		Set<OWLAnnotationPropertyDomainAxiom> result = createSet();
		for (OWLAnnotationPropertyDomainAxiom ax : getAxiomsInternal(AxiomType.ANNOTATION_PROPERTY_DOMAIN)) {
			if (ax.getProperty().equals(property)) {
				result.add(ax);
			}
		}
		return result;
	}

	public Set<OWLAnnotationPropertyRangeAxiom> getAnnotationPropertyRangeAxioms(
			OWLAnnotationProperty property) {
		Set<OWLAnnotationPropertyRangeAxiom> result = createSet();
		for (OWLAnnotationPropertyRangeAxiom ax : getAxiomsInternal(AxiomType.ANNOTATION_PROPERTY_RANGE)) {
			if (ax.getProperty().equals(property)) {
				result.add(ax);
			}
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <T extends OWLAxiom> Set<T> getAxiomsInternal(AxiomType<T> axiomType) {
		return (Set<T>) getAxioms(axiomType, axiomsByType, false);
	}

	public Set<OWLAxiom> getReferencingAxioms(OWLAnonymousIndividual individual) {
		return getReturnSet(getAxioms(individual, owlAnonymousIndividualReferences, false));
	}

	public Set<OWLAxiom> getReferencingAxioms(final OWLEntity owlEntity) {
		//TODO use static type map instead of OWLENTITY.class
		//TODO create method to HGHandle getFindEntity(OWLEntity owlEntity);
		//TODO shall we ensure that entity is in ontology?
		//TODO this get;s called with null by one or more protege views!!
		if (owlEntity == null) {
			System.out.println("BAD ? getReferencingAx(null) called");
			return Collections.emptySet();
		}
		String className = owlEntity.getClass().getCanonicalName();
		if (className.startsWith("uk.ac")) { 
			System.out.println("BAD ! OWLENTITY TYPE IS : " + owlEntity.getClass().getSimpleName());
			System.out.println("BAD ! Object IS : " + owlEntity);
			System.out.println("BAD ! IRI IS : " + owlEntity.getIRI());
			return Collections.emptySet();
		}
		List<OWLAxiom> axioms;
		axioms = graph.getTransactionManager().transact(new Callable<List<OWLAxiom>>() {
			public List<OWLAxiom> call() {
				List<OWLAxiom> l;
				HGHandle owlEntityHandle = graph.getHandle(owlEntity);
				if (owlEntityHandle == null) {
					// TODO might not find what we need, because owlEntity.getClass must match our 
					// *HGDB implementation types.
					owlEntityHandle = hg.findOne(graph,
							hg.and(hg.type(owlEntity.getClass()), hg.eq("IRI", owlEntity.getIRI())));
				}
				if (owlEntityHandle == null) {
					l = Collections.emptyList();
				} else {
					l = hg.getAll(graph,
							hg.and(hg.typePlus(OWLAxiom.class), hg.incident(owlEntityHandle)));
				}
				return l;
			}
		}, HGTransactionConfig.READONLY);
//		if (owlEntity instanceof OWLClass) {
//			axioms = getAxioms(owlEntity.asOWLClass(), owlClassReferences, false);
//		} else if (owlEntity instanceof OWLObjectProperty) {
//			axioms = getAxioms(owlEntity.asOWLObjectProperty(), owlObjectPropertyReferences, false);
//		} else if (owlEntity instanceof OWLDataProperty) {
//			axioms = getAxioms(owlEntity.asOWLDataProperty(), owlDataPropertyReferences, false);
//		} else if (owlEntity instanceof OWLNamedIndividual) {
//			axioms = getAxioms(owlEntity.asOWLNamedIndividual(), owlIndividualReferences, false);
//		} else if (owlEntity instanceof OWLDatatype) {
//			axioms = getAxioms(owlEntity.asOWLDatatype(), owlDatatypeReferences, false);
//		} else if (owlEntity instanceof OWLAnnotationProperty) {
//			axioms = getAxioms(owlEntity.asOWLAnnotationProperty(),
//					owlAnnotationPropertyReferences, false);
//		} else {
//			axioms = Collections.emptySet();
//		}
		return getReturnSet(axioms);
	}
	
	//hilpold
	public Set<OWLDeclarationAxiom> getDeclarationAxioms(OWLEntity entity) {
		//is entity in graph, fail if not?
		final HGHandle entityHandle = graph.getHandle(entity);
		List<OWLDeclarationAxiom> l;
		//All links of type OWLDeclarationAxiom in the incidence set of OWLEntity.
		l = graph.getTransactionManager().transact(new Callable <List<OWLDeclarationAxiom>>() {
			public List<OWLDeclarationAxiom> call() {
				return hg.getAll(graph,
						  hg.and(hg.type(OWLDeclarationAxiomHGDB.class), hg.incident(entityHandle))
						);
			}}, HGTransactionConfig.READONLY);		
		return getReturnSet(l);
	}


	//	l = hg.getAll(graph,
	//	  hg.and(
	//	    hg.apply(hg.targetAt(graph, 0) 
	//		 ,hg.and(hg.orderedLink(hg.anyHandle(), entityHandle), hg.type(AxiomToEntityLink.class))
	//	    ) 
	//	  )
	//	);
	// return getReturnSet(getAxioms(entity, declarationsByEntity, false));

	public Set<OWLImportsDeclaration> getImportsDeclarations() {
		// get link by name and link(handle)
		List<OWLImportsDeclaration> l;
		l = graph.getTransactionManager().transact(new Callable <List<OWLImportsDeclaration>>() {
			public List<OWLImportsDeclaration> call() {
				return hg.getAll(graph, hg.apply(hg.targetAt(graph, 1), hg.and(hg
						.type(ImportDeclarationLink.class), hg.orderedLink(ontoHandle,
						hg.anyHandle()), new SubgraphMemberCondition(ontoHandle))));
			}
		}, HGTransactionConfig.READONLY);
		
		// List<IRI> imports = hg.getAll(getHyperGraph(),
		// hg.apply(hg.targetAt(getHyperGraph(), 1),
		// hg.and(hg.eq("importsLink"), hg.orderedLink(
		// this.getHyperGraph().getHandle(this), hg.anyHandle()))));
		// result.addAll(imports);
		return new HashSet<OWLImportsDeclaration>(l);
	}

	boolean containsImportDeclaration(OWLImportsDeclaration importDeclaration) {
		return getImportsDeclarations().contains(importDeclaration);
	}

	public boolean addImportsDeclaration(final OWLImportsDeclaration importDeclaration) {
		boolean success = false;
		ontology.printGraphStats("Before AddImp");
		success = graph.getTransactionManager().transact(new Callable<Boolean>() {
			public Boolean call() {
				if (containsImportDeclaration(importDeclaration))
					return false;
				else {
					//DBG try this
					//graph.getCache().remove(graph.getCache().get(importDeclaration));
					//HGHandle importDeclarationHandle = graph.getHandle(importDeclaration);
					// ------------------------------------------------------------------------------------------------------------------
					//BIG PROBLEM: if we call graph.add(atom) without the type. looking up the type will actually fail.
					//For some reason, on a protege redo, even after remove, HG sees a persistentlink for a removed object and fails on loading a type for it.
					// might be a HG bug; or can I remove an object from cache? manually? I tried and didn't work.
//					HGHandle typeHandle = graph.getTypeSystem().getTypeHandle(importDeclaration.getClass());
//					HGHandle importDeclarationHandle = graph.add(importDeclaration, typeHandle);
					HGHandle importDeclarationHandle = graph.add(importDeclaration);
					//}					
					ImportDeclarationLink link = new ImportDeclarationLink(ontoHandle,
							importDeclarationHandle);
					HGHandle linkHandle = graph.add(link);
					// hilpold this.importsDeclarations.add(importDeclaration);
					ontology.add(importDeclarationHandle);
					ontology.add(linkHandle);
					//So graph.getTransactionManager().commit();
					//assert
					return true;
				}
			}
		});
		ontology.printGraphStats("After  AddImp");
		assert(ontology.findOne(hg.eq(importDeclaration)) != null);
		assert(!ontology.findAll(hg.type(ImportDeclarationLink.class)).isEmpty());
		
		return success;
	}

	/**
	 * Removes both, the importDeclaration and the link connecting it to
	 * internals from hypergraph.
	 */
	public boolean removeImportsDeclaration(final OWLImportsDeclaration importDeclaration) {
		ontology.printGraphStats("Before RemImp");
		boolean success = graph.getTransactionManager().transact(new Callable<Boolean>() {
			public Boolean call() {
				boolean success;
				HGHandle importDeclarationHandle;
				HGHandle link;
				//graph.getTransactionManager().beginTransaction();
				if (!containsImportDeclaration(importDeclaration)) {
					return false;
				}
				importDeclarationHandle = graph.getHandle(importDeclaration);
				if (importDeclarationHandle == null) {
					throw new IllegalStateException("Contains said fine, but can't get handle.");
				}
				link = hg.findOne(
						graph,
						hg.and(hg.type(ImportDeclarationLink.class),
								hg.orderedLink(ontoHandle, importDeclarationHandle), 
								new SubgraphMemberCondition(ontoHandle)));
				if (link == null) {
					throw new IllegalStateException(
							"Found importDeclaration, but no link. Each Importdeclaration must have exactly one link.");
				}
				success = ontology.remove(link) 
					&& ontology.remove(importDeclarationHandle) 
					&& graph.remove(link) 
					&& graph.remove(importDeclarationHandle);
				//DBG some HG test follows:
				HGLiveHandle lh = graph.getCache().get(importDeclaration);
				if (lh != null) {
					System.out.println("Life handle but no more persistent.");	
					graph.getCache().remove(lh);
					HGLiveHandle lh2 = graph.getCache().get(importDeclaration);
					System.out.println("lh2");	
				}
				// hilpold this.importsDeclarations.remove(importDeclaration);
				return success;
			}
		});
		ontology.printGraphStats("After  RemImp");
		return success;
	}

	public Set<OWLAnnotation> getOntologyAnnotations() {
		//
		return this.getReturnSet(this.ontologyAnnotations);
	}

	public boolean addOntologyAnnotation(OWLAnnotation ann) {
		return ontologyAnnotations.add(ann);
	}

	public boolean removeOntologyAnnotation(OWLAnnotation ann) {
		return ontologyAnnotations.remove(ann);
	}

	public boolean containsAxiom(OWLAxiom axiom) {
		Set<OWLAxiom> axioms = axiomsByType.get(axiom.getAxiomType());
		return axioms != null && axioms.contains(axiom);
	}

	public int getAxiomCount() {
		int count = 0;
		for (AxiomType<?> type : AXIOM_TYPES) {
			Set<OWLAxiom> axiomSet = axiomsByType.get(type);
			if (axiomSet != null) {
				count += axiomSet.size();
			}
		}
		return count;
	}

	public Set<OWLAxiom> getAxioms() {
		Set<OWLAxiom> axioms = createSet();
		for (AxiomType<?> type : AXIOM_TYPES) {
			Set<OWLAxiom> owlAxiomSet = axiomsByType.get(type);
			if (owlAxiomSet != null) {
				axioms.addAll(owlAxiomSet);
			}
		}
		return axioms;
	}

	@SuppressWarnings("unchecked")
	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType) {
		return (Set<T>) getAxioms(axiomType, axiomsByType, false);
	}

	/**
	 * Gets the axioms which are of the specified type, possibly from the
	 * imports closure of this ontology
	 * 
	 * @param axiomType
	 *            The type of axioms to be retrived.
	 * @param includeImportsClosure
	 *            if <code>true</code> then axioms of the specified type will
	 *            also be retrieved from the imports closure of this ontology,
	 *            if <code>false</code> then axioms of the specified type will
	 *            only be retrieved from this ontology.
	 * @return A set containing the axioms which are of the specified type. The
	 *         set that is returned is a copy of the axioms in the ontology (and
	 *         its imports closure) - it will not be updated if the ontology
	 *         changes.
	 */
	public <T extends OWLAxiom> Set<T> getAxioms(AxiomType<T> axiomType,
			Collection<OWLOntology> importsClosure) {
		if (importsClosure == null || importsClosure.size() == 0) {
			return getAxioms(axiomType);
		}
		Set<T> result = createSet();
		for (OWLOntology ont : importsClosure) {
			result.addAll(ont.getAxioms(axiomType));
		}
		return result;
	}

	public <T extends OWLAxiom> int getAxiomCount(AxiomType<T> axiomType) {
		Set<OWLAxiom> axioms = axiomsByType.get(axiomType);
		if (axioms == null) {
			return 0;
		}
		return axioms.size();
	}

	public Set<OWLLogicalAxiom> getLogicalAxioms() {
		Set<OWLLogicalAxiom> axioms = createSet();
		for (AxiomType<?> type : AXIOM_TYPES) {
			if (type.isLogical()) {
				Set<OWLAxiom> axiomSet = axiomsByType.get(type);
				if (axiomSet != null) {
					for (OWLAxiom ax : axiomSet) {
						axioms.add((OWLLogicalAxiom) ax);
					}
				}
			}
		}
		return axioms;
	}

	public int getLogicalAxiomCount() {
		int count = 0;
		for (AxiomType<?> type : AXIOM_TYPES) {
			if (type.isLogical()) {
				Set<OWLAxiom> axiomSet = axiomsByType.get(type);
				if (axiomSet != null) {
					count += axiomSet.size();
				}
			}
		}
		return count;
	}

	public void addAxiomsByType(AxiomType<?> type, final OWLAxiom axiom) {
		//TODO make all types work w hypergraph.
		System.out.print("ADD Axiom: " + axiom.getClass().getSimpleName() + " E: ");
		for(OWLEntity e : axiom.getSignature()) {
			System.out.print(e + "  Etype: ");
			System.out.print(e.getEntityType() + " ");
		}
		System.out.println();
		if (type == AxiomType.DECLARATION) {
			graph.getTransactionManager().transact(new Callable<Boolean>() {
				public Boolean call() {
					ontology.printGraphStats("Before AddDeclaration");
					// hyper hyper
					HGHandle h = graph.getHandle(axiom);
					ontology.add(h);
					ontology.printGraphStats("After  AddDeclaration");
					return true;
		}});
		}
		addToIndexedSet(type, axiomsByType, axiom);
	}

	public void removeAxiomsByType(AxiomType<?> type, final OWLAxiom axiom) {
		//TODO implement more axiom types
		if (type == AxiomType.DECLARATION) {
			graph.getTransactionManager().transact(new Callable<Boolean>() {
				public Boolean call() {
					boolean removedSuccess;
					ontology.printGraphStats("Before RemoveDeclaration");
					// hyper hyper
					HGHandle h = graph.getHandle(axiom);
					ontology.remove(h);
					removedSuccess = graph.remove(h);
					ontology.printGraphStats("After  RemoveDeclaration");
					// if it pointed to an entity, entity incidence is -1
					return removedSuccess;
				}
			});
		}
		removeAxiomFromSet(type, axiomsByType, axiom, true);
	}

	public Map<OWLAxiom, Set<OWLAxiom>> getLogicalAxiom2AnnotatedAxiomMap() {
		return new HashMap<OWLAxiom, Set<OWLAxiom>>(this.logicalAxiom2AnnotatedAxiomMap);
	}

	public Set<OWLAxiom> getLogicalAxiom2AnnotatedAxiom(OWLAxiom ax) {
		return getReturnSet(logicalAxiom2AnnotatedAxiomMap.get(ax.getAxiomWithoutAnnotations()));
	}

	public void addLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax) {
		addToIndexedSet(ax.getAxiomWithoutAnnotations(), logicalAxiom2AnnotatedAxiomMap, ax);
	}

	public void removeLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax) {
		removeAxiomFromSet(ax.getAxiomWithoutAnnotations(), logicalAxiom2AnnotatedAxiomMap, ax,
				true);
	}

	public boolean containsLogicalAxiom2AnnotatedAxiomMap(OWLAxiom ax) {
		return logicalAxiom2AnnotatedAxiomMap.containsKey(ax.getAxiomWithoutAnnotations());
	}

	public Set<OWLClassAxiom> getGeneralClassAxioms() {
		return getReturnSet(this.generalClassAxioms);
	}

	public void addGeneralClassAxioms(OWLClassAxiom ax) {
		this.generalClassAxioms.add(ax);
	}

	public void removeGeneralClassAxioms(OWLClassAxiom ax) {
		this.generalClassAxioms.remove(ax);
	}

	public Set<OWLSubPropertyChainOfAxiom> getPropertyChainSubPropertyAxioms() {
		return getReturnSet(this.propertyChainSubPropertyAxioms);
	}

	public void addPropertyChainSubPropertyAxioms(OWLSubPropertyChainOfAxiom ax) {
		this.propertyChainSubPropertyAxioms.add(ax);
	}

	public void removePropertyChainSubPropertyAxioms(OWLSubPropertyChainOfAxiom ax) {
		this.propertyChainSubPropertyAxioms.remove(ax);
	}

//	public Map<OWLClass, Set<OWLAxiom>> getOwlClassReferences() {
//		return new HashMap<OWLClass, Set<OWLAxiom>>(this.owlClassReferences);
//	}

//	public void removeOwlClassReferences(OWLClass c, OWLAxiom ax) {
//		removeAxiomFromSet(c, owlClassReferences, ax, true);
//	}
//
//	public void addOwlClassReferences(OWLClass c, OWLAxiom ax) {
//		addToIndexedSet(c, owlClassReferences, ax);
//	}

//	public boolean containsOwlClassReferences(OWLClass c) {
////		return this.owlClassReferences.containsKey(c);
//	}
	

//	public Map<OWLObjectProperty, Set<OWLAxiom>> getOwlObjectPropertyReferences() {
//		return new HashMap<OWLObjectProperty, Set<OWLAxiom>>(this.owlObjectPropertyReferences);
//	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlClass(org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public boolean containsOwlClass(final OWLClass c) {
		return containsOWLEntityOntology(c.getIRI(), OWLClassHGDB.class);
	}

//	public void removeOwlObjectPropertyReferences(OWLObjectProperty p, OWLAxiom ax) {
//		removeAxiomFromSet(p, owlObjectPropertyReferences, ax, true);
//	}
//
//	public void addOwlObjectPropertyReferences(OWLObjectProperty p, OWLAxiom ax) {
//		addToIndexedSet(p, owlObjectPropertyReferences, ax);
//	}

//	public boolean containsOwlObjectPropertyReferences(OWLObjectProperty c) {
////		return this.owlObjectPropertyReferences.containsKey(c);
//	}
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlObjectProperty(org.semanticweb.owlapi.model.OWLObjectProperty)
	 */
	@Override
	public boolean containsOwlObjectProperty(final OWLObjectProperty c) {
		return containsOWLEntityOntology(c.getIRI(), OWLObjectPropertyHGDB.class);
	}

	
//	public Map<OWLDataProperty, Set<OWLAxiom>> getOwlDataPropertyReferences() {
//		return new HashMap<OWLDataProperty, Set<OWLAxiom>>(this.owlDataPropertyReferences);
//	}

//	public void removeOwlDataPropertyReferences(OWLDataProperty c, OWLAxiom ax) {
//		removeAxiomFromSet(c, owlDataPropertyReferences, ax, true);
//	}
//
//
//	public void addOwlDataPropertyReferences(OWLDataProperty c, OWLAxiom ax) {
//		addToIndexedSet(c, owlDataPropertyReferences, ax);
//	}

//	public boolean containsOwlDataPropertyReferences(OWLDataProperty c) {
////		return this.owlDataPropertyReferences.containsKey(c);
//	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlDataProperty(org.semanticweb.owlapi.model.OWLDataProperty)
	 */
	@Override
	public boolean containsOwlDataProperty(final OWLDataProperty c) {
		return containsOWLEntityOntology(c.getIRI(), OWLDataPropertyHGDB.class);
	}

	/**
	 * Contains by IRI and exact type (HGDB class).
	 * 
	 * @param iri an IRI of the Entity
	 * @param hgdbType an exact storage (HGDB) class type. 
	 * @return
	 */
	boolean containsOWLEntityOntology(final IRI iri, final Class<?> hgdbType) {
		return graph.getTransactionManager().transact(new Callable<Boolean>() {
			public Boolean call() {
				return hg.findOne(graph, hg.and(hg.type(hgdbType),
						hg.eq("IRI", iri),
						new SubgraphMemberCondition(ontoHandle))) != null;
			}
		}, HGTransactionConfig.READONLY);
		
	}
	
	
//	public Map<OWLNamedIndividual, Set<OWLAxiom>> getOwlIndividualReferences() {
//		return this.owlIndividualReferences;
//	}

//	public void removeOwlIndividualReferences(OWLNamedIndividual c, OWLAxiom ax) {
//		removeAxiomFromSet(c, owlIndividualReferences, ax, true);
//	}

//	public void addOwlIndividualReferences(OWLNamedIndividual c, OWLAxiom ax) {
//		addToIndexedSet(c, owlIndividualReferences, ax);
//	}

//hilpold	public boolean containsOwlIndividualReferences(OWLNamedIndividual c) {
//		return this.owlIndividualReferences.containsKey(c);
//	}

	public boolean containsOwlNamedIndividual(final IRI individualIRI) {
		return containsOWLEntityOntology(individualIRI, OWLNamedIndividualHGDB.class);
	}
	

	//------------------------------------------------------------------------------------
	// OWL_ENTITY BASIC QUERIES 
	//
	

	public Set<OWLAnnotationProperty> getOwlAnnotationProperties() {
		List<OWLAnnotationProperty> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLAnnotationProperty>>() {
			public List<OWLAnnotationProperty> call() {
				return hg.getAll(graph, hg.and(hg.type(OWLAnnotationPropertyHGDB.class),
						new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLClass> getOwlClasses() {
		List<OWLClass> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLClass>>() {
			public List<OWLClass> call() {
				return hg.getAll(graph, hg.and(hg.type(OWLClassHGDB.class),
						new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLDatatype> getOwlDatatypes() {
		List<OWLDatatype> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLDatatype>>() {
			public List<OWLDatatype> call() {
				return hg.getAll(graph, hg.and(hg.type(OWLDatatypeHGDB.class),
						new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#getOwlIndividuals()
	 */
	@Override
	public Set<OWLNamedIndividual> getOwlNamedIndividuals() {
		List<OWLNamedIndividual> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLNamedIndividual>>() {
			public List<OWLNamedIndividual> call() {
				return hg.getAll(graph, hg.and(hg.type(OWLNamedIndividualHGDB.class),
						new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	public Set<OWLDataProperty> getOwlDataProperties() {
		List<OWLDataProperty> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLDataProperty>>() {
			public List<OWLDataProperty> call() {
				return hg.getAll(graph, hg.and(hg.type(OWLDataPropertyHGDB.class),
						new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}
	

	public Set<OWLObjectProperty> getOwlObjectProperties() {
		List<OWLObjectProperty> l;
		l = graph.getTransactionManager().transact(new Callable<List<OWLObjectProperty>>() {
			public List<OWLObjectProperty> call() {
				return hg.getAll(graph, hg.and(hg.type(OWLObjectPropertyHGDB.class),
						new SubgraphMemberCondition(ontoHandle)));
			}
		}, HGTransactionConfig.READONLY);
		return getReturnSet(l);
	}

	//
	// END OWL_ENTITY BASIC QUERIES 
	//------------------------------------------------------------------------------------

	public Map<OWLAnonymousIndividual, Set<OWLAxiom>> getOwlAnonymousIndividualReferences() {
		return new HashMap<OWLAnonymousIndividual, Set<OWLAxiom>>(
				this.owlAnonymousIndividualReferences);
	}

	public void removeOwlAnonymousIndividualReferences(OWLAnonymousIndividual c, OWLAxiom ax) {
		removeAxiomFromSet(c, owlAnonymousIndividualReferences, ax, true);
	}

	public void addOwlAnonymousIndividualReferences(OWLAnonymousIndividual c, OWLAxiom ax) {
		addToIndexedSet(c, owlAnonymousIndividualReferences, ax);
	}

	public boolean containsOwlAnonymousIndividualReferences(OWLAnonymousIndividual c) {
		return this.owlAnonymousIndividualReferences.containsKey(c);
	}

//	public Map<OWLDatatype, Set<OWLAxiom>> getOwlDatatypeReferences() {
//		return new HashMap<OWLDatatype, Set<OWLAxiom>>(this.owlDatatypeReferences);
//	}

//	public void removeOwlDatatypeReferences(OWLDatatype c, OWLAxiom ax) {
//		removeAxiomFromSet(c, owlDatatypeReferences, ax, true);
//	}
//
//	public void addOwlDatatypeReferences(OWLDatatype c, OWLAxiom ax) {
//		addToIndexedSet(c, owlDatatypeReferences, ax);
//	}

//	public boolean containsOwlDatatypeReferences(OWLDatatype c) {
//		return this.owlDatatypeReferences.containsKey(c);
//	}

	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlDatatype(org.semanticweb.owlapi.model.OWLDatatype)
	 */
	@Override
	public boolean containsOwlDatatype(OWLDatatype c) {
		return containsOWLEntityOntology(c.getIRI(), OWLDatatypeHGDB.class);
	}
	
	
//	public Map<OWLAnnotationProperty, Set<OWLAxiom>> getOwlAnnotationPropertyReferences() {
//		return new HashMap<OWLAnnotationProperty, Set<OWLAxiom>>(
//				this.owlAnnotationPropertyReferences);
//	}



//	public void removeOwlAnnotationPropertyReferences(OWLAnnotationProperty c, OWLAxiom ax) {
//		removeAxiomFromSet(c, owlAnnotationPropertyReferences, ax, true);
//	}
//
//	public void addOwlAnnotationPropertyReferences(OWLAnnotationProperty c, OWLAxiom ax) {
//		addToIndexedSet(c, owlAnnotationPropertyReferences, ax);
//	}

//	public boolean containsOwlAnnotationPropertyReferences(OWLAnnotationProperty c) {
//		return this.owlAnnotationPropertyReferences.containsKey(c);
//	}
		
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#containsOwlAnnotationProperty(org.semanticweb.owlapi.model.OWLAnnotationProperty)
	 */
	@Override
	public boolean containsOwlAnnotationProperty(OWLAnnotationProperty c) {
		return containsOWLEntityOntology(c.getIRI(), OWLAnnotationProperty.class);
	}

	/**
	 * This is an expensive operation, because the hashmap has to be created.
	 * Maybe the hashmap should be lazy and backed by HG? (Protege never calls
	 * this.)
	 */
	public Map<OWLEntity, Set<OWLDeclarationAxiom>> getDeclarationsByEntity() {
		// return new HashMap<OWLEntity, Set<OWLDeclarationAxiom>>(
		// this.declarationsByEntity);
		// hilpold - for now.
		return null;
	}

//	public void removeDeclarationsByEntity(OWLEntity c, OWLDeclarationAxiom ax) {
		// removeAxiomFromSet(c, declarationsByEntity, ax, true);
//	}

//	public void addDeclarationsByEntity(OWLEntity c, OWLDeclarationAxiom ax) {
//		throw new IllegalArgumentException("Operation no longer supported; Interface will be changed.");
//		//addToIndexedSet(c, declarationsByEntity, ax);
//	}

//	public boolean containsDeclarationsByEntity(OWLEntity c) {
//		// return this.declarationsByEntity.containsKey(c);
//		return false;
//	}

	// ----------------------------------------------------------------------
	// HGGraphHolder HGHandleHolder Interfaces
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.hypergraphdb.HGHandleHolder#getAtomHandle()
	 */
	@Override
	public HGHandle getAtomHandle() {
		return handle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.hypergraphdb.HGHandleHolder#setAtomHandle(org.hypergraphdb.HGHandle)
	 */
	@Override
	public void setAtomHandle(HGHandle handle) {
		this.handle = handle;
	}

	/**
	 * Sets the graph and sets AtomHandle also, if graph non null.
	 */
	@Override
	public void setHyperGraph(HyperGraph graph) {
		this.graph = graph;
		if (graph != null) {
			setAtomHandle(graph.getHandle(this));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.hypergraphdb.app.owl.HGDBOntologyInternals#setOntologyHyperNode(org.hypergraphdb.app.owl.HGDBOntology)
	 */
	@Override
	public void setOntologyHyperNode(HGDBOntology ontology) {
		//TODO ugly, but we need it, because Hypernode Interface does not define convienient add)
		this.ontology = (HGDBOntologyImpl) ontology;	
		this.ontoHandle = graph.getHandle(ontology);
	}

	//
	// END HGGraphHolder HGHandleHolder Interfaces
	// ----------------------------------------------------------------------

}
