package org.hypergraphdb.app.owl.gc;

import java.util.List;
import java.util.Set;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGIndex;
import org.hypergraphdb.HGPersistentHandle;
import org.hypergraphdb.HGRandomAccessResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.IncidenceSet;
import org.hypergraphdb.algorithms.HGDepthFirstTraversal;
import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.hypergraphdb.app.owl.core.OWLAxiomHGDB;
import org.hypergraphdb.app.owl.core.OWLObjectHGDB;
import org.hypergraphdb.app.owl.query.AnySubgraphMemberCondition;
import org.hypergraphdb.app.owl.query.OWLEntityIsBuiltIn;
import org.hypergraphdb.app.owl.type.link.AxiomAnnotatedBy;
import org.hypergraphdb.app.owl.type.link.ImportDeclarationLink;
import org.hypergraphdb.app.owl.util.StopWatch;
import org.hypergraphdb.app.owl.util.TargetSetALGenerator;
import org.hypergraphdb.atom.HGSubgraph;
import org.hypergraphdb.util.Pair;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * GarbageCollector.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Dec 20, 2011
 */
public class GarbageCollector {
	
	public StopWatch stopWatch = new StopWatch();
	
	/**
	 * A full GC run entails running:
	 *   1. MODE_DELETED_ONTOLOGIES
	 *   2. MODE_DISCONNECTED_AXIOMS
	 *   3. MODE_DISCONNECTED_ENTITIES
	 *   4. MODE_DISCONNECTED_OTHER
	 *   Those 4 modes are exclusive to each other in the following way:
	 *   The objects deleted by one mode are not deleted by any other mode.
	 */
	public static final int MODE_FULL = 0;

	/**
	 * Begins collection at all ontologies marked for deletion and garbage collects all referenced objects.
	 * Will not collect axioms that are not part of any ontology.
	 * Will not collect disconnected entities that are unreachable by traversing the ontologies.
	 */
	public static final int MODE_DELETED_ONTOLOGIES = 1;

	/**
	 *  Begins collection at all axioms that are not member in any ontology and are disconnected.
	 *  Each axiom, all reachable dependent objects, and entities with an otherwise empty incidence set will be removed.
	 *  A) they were created by DF and never added to an onto
	 *  B) they were removed from the last ontology in which they were member.
	 *  (The general case is that axioms are exclusive to an ontology; the API user however can add axioms
	 *  that exist in Onto A to Onto B, thereby reusing the axiom and it's dependent objects.
	 */
	public static final int MODE_DISCONNECTED_AXIOMS = 2;
	
	
	/**
	 * Begins collection at all disconnected OWLObjectHGDB and IRI atoms (OWLAnnotationValue), except those implementing OLWEntity or subclasses of OWLAxiomHGDB.
	 * These objects are never member in any ontology.
	 * Each object, all reachable dependent objects, and entities with an otherwise empty incidence set will be removed.
	 * Those include:
	 * - OWLClassExpressionHGDB (not CN, named Class)
	 * - (I) OWLDataRange (not R, named data prop)
	 * - OwlFacetRestrictionHGDB
	 * - OWLLiteralHGBD
	 * - OWLObjectPropertyExpression (not PN, OWLObjectPropery)
	 * - SWRLAtomHGDB
	 * - SWRLIndividualArgument
	 * - SWRLLiteralArgument
	 * - SWRLVariable
	 */
	public static final int MODE_DISCONNECTED_OTHER = 3;
	
	/**
	 *  Begins collection at entities that are not member in any ontology and are not target of any other object.
	 *  No BUILTIN entities will be removed.
	 */
	public static final int MODE_DISCONNECTED_ENTITIES = 4;

	private HyperGraph graph;
	private HGDBOntologyRepository repository;
	
		
	public GarbageCollector(HGDBOntologyRepository repository) {
		this.repository = repository;
		this.graph = repository.getHyperGraph();
	}


	/**
	 * Run full garbage collection
	 * @return
	 */
	public GarbageCollectorStatistics runGC() {
		return runGC(MODE_FULL);
	}

	public GarbageCollectorStatistics runGC(int mode) {
		return runGCInternal(mode, false);
	}

	/**
	 * Analyse what will be removed on a full garbage collection run.
	 * 
	 * @return
	 */
	public GarbageCollectorStatistics analyze() {
		return runGCInternal(MODE_FULL, true);
	}

	public GarbageCollectorStatistics analyze(int mode) {
		return runGCInternal(mode, true);
	}

	protected GarbageCollectorStatistics runGCInternal(int mode, boolean analyzeOnly) {
		GarbageCollectorStatistics stats = new GarbageCollectorStatistics();
		switch (mode) {
			case MODE_FULL: {
				collectRemovedOntologies(stats, analyzeOnly);
				collectAxioms(stats, analyzeOnly);
				collectOtherObjects(stats, analyzeOnly);
				collectEntities(stats, analyzeOnly);
			};break;
			case MODE_DELETED_ONTOLOGIES: {
				collectRemovedOntologies(stats, analyzeOnly);
			};break;
			case MODE_DISCONNECTED_AXIOMS: {
				collectAxioms(stats, analyzeOnly);
			};break;
			case MODE_DISCONNECTED_OTHER: {
				collectOtherObjects(stats, analyzeOnly);
			};break;
			case MODE_DISCONNECTED_ENTITIES: {
				collectEntities(stats, analyzeOnly);
			};break;
			default: {
				throw new IllegalArgumentException("runGC with unknown mode called: " + mode);
			}
		}
		return stats;
	}

	public void collectRemovedOntologies(GarbageCollectorStatistics stats, boolean analyzeOnly) {
		List<HGDBOntology> delOntos =  repository.getDeletedOntologies();
		int i = 0;
		for (HGDBOntology delOnto : delOntos) {
			i++;
			stopWatch.start();
			collectRemovedOntology(delOnto, stats, analyzeOnly);
			stopWatch.stop("Ontology collection ("+ i + " of " + delOntos.size() + "): ");
			System.out.println("Stats now: " +  stats.toString());
		}
		// 
	}

	public void collectRemovedOntology(HGDBOntology onto, GarbageCollectorStatistics stats, boolean analyzeOnly) {
		//OntologyAnnotations
		//internals.remove does remove anno from onto, NOT graph
		Set<OWLAnnotation> annos = onto.getAnnotations();
		for (OWLAnnotation anno : annos) {
			HGHandle annoHandle = graph.getHandle(anno);
			if (!analyzeOnly) {
				onto.remove(annoHandle);
				graph.remove(annoHandle);
			}
			stats.increaseOtherObjects();
			stats.increaseTotalAtoms();					
		}
		//Import declarations
		//internals.remove does remove from onto&graph: ImportDeclarationLink, ImportDeclaration
		Set<OWLImportsDeclaration> importsDeclarations = onto.getImportsDeclarations();
		for (OWLImportsDeclaration importsDeclaration : importsDeclarations) {
			HGHandle importsDeclarationHandle = graph.getHandle(importsDeclaration);
			IncidenceSet is = graph.getIncidenceSet(importsDeclarationHandle);
			if (is.size() != 1) throw new IllegalStateException();
			//remove ImportDeclarationLink
			HGHandle importDeclLinkHandle = is.first();
			ImportDeclarationLink importDeclLink = graph.get(importDeclLinkHandle);
			if (!analyzeOnly) {
				onto.remove(importDeclLinkHandle);
				onto.remove(importsDeclarationHandle);
				graph.remove(importDeclLinkHandle);
				graph.remove(importsDeclarationHandle);
			}
			stats.increaseOtherObjects();
			stats.increaseOtherObjects();
			stats.increaseTotalAtoms();		
			stats.increaseTotalAtoms();					
		}
		
		//collect Axioms
		Set<OWLAxiom> axioms = onto.getAxioms();
		for (OWLAxiom axiom : axioms) {
			HGHandle axiomHandle = graph.getHandle(axiom);
			//1. remove axiom from Subgraph, index must be zero now for removal, 
			//unless axiom is also member in other subgraphs/ontologies, which is possible dependent on how our API is used.
			if (!analyzeOnly) {
				onto.remove(axiomHandle);
			}
			//NO, following would remove axiom from graph as of 2011.12.23: onto.applyChange(new RemoveAxiom(onto, axiom));
			//2. collect enfore zero incidence set, if not analyze, because we removed our onto from axiom incidence set in step 1. 
			collectAxiomInternal(axiomHandle, stats, analyzeOnly);
		}
//		//collect Ontology
		HGHandle ontoHandle = graph.getHandle(onto);
		IncidenceSet ontoIS = graph.getIncidenceSet(ontoHandle);
		if (!ontoIS.isEmpty()) throw new IllegalStateException("GC: Ontology incidence set must be empty now, had size: " + ontoIS.size());
//		System.out.println("Onto incidence set after removing content: (expected empty)");
//		int i = 0;
//		for (HGHandle h : ontoIS) {
//			i++;
//			System.out.println(" " + i + " " + graph.get(h).toString());
//		}
//		System.out.println("Onto incidence set END");
		if (!analyzeOnly) {
			//TODO What happens to the indices in onto.
			//how do we make sure subgraph is empty.			
			graph.remove(ontoHandle);
		}
		stats.increaseOntologies();
		stats.increaseTotalAtoms();				
	}
	
	/**
	 * Collects and removes all axioms that do not belong to any ontology.
	 * ie. are not members in any subgraph.
	 */
	public void collectAxioms(GarbageCollectorStatistics stats, boolean analyzeOnly) {
		stopWatch.start();		
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
					hg.typePlus(OWLAxiomHGDB.class),
					hg.disconnected(),
					hg.not(new AnySubgraphMemberCondition(graph)))
				);
		stopWatch.stop("Disconnected Axiom query time: Found: " + handlesToRemove.size() + " Duration:");
		for (HGHandle h: handlesToRemove) {
			collectAxiomInternal(h, stats, analyzeOnly);
		}
		stopWatch.stop("Disconnected Axiom collection time: ");
		System.out.println("Stats now: " + stats.toString());
	}
	
	/**
	 * Removes one axiom and all reachable objects if possible.
	 * If you are deleting an ontology, make sure you remove the axiom from the ontology before calling this method,
	 * as this method expects the axiom not to be a member in any subgraph.
	 * 
	 * @param axiomHandle
	 * @param stats
	 * @param enforceDisconnected causes an exception, if axiom is not disconnected.
	 * @param analyzeOnly
	 */
	protected void collectAxiomInternal(HGHandle axiomHandle, GarbageCollectorStatistics stats, boolean analyzeOnly) {
		int subgraphCount = countSubgraphsWhereAtomIsMember(axiomHandle);
		int maxAllowedSubgraphCount = analyzeOnly? 1 : 0;
		if (subgraphCount > maxAllowedSubgraphCount) {
			//
			stats.increaseAxiomNotRemovableCases();
		} else {
			// Remove axiom annotation links and deep remove Annotations!
			List<HGHandle> annoLinkHandles = hg.findAll(graph,
					hg.and(hg.type(AxiomAnnotatedBy.class), hg.incident(axiomHandle)));
			for (HGHandle annoLinkHandle : annoLinkHandles) {
				AxiomAnnotatedBy axAb = graph.get(annoLinkHandle);
				HGHandle annotationHandle = axAb.getTargetAt(1);						
				//Deep remove annotation (tree)
				collectOWLObjectsByDFSInternal(annotationHandle, stats, analyzeOnly);
				if (!analyzeOnly) {
					//remove annotation link (tree)		
					graph.remove(annoLinkHandle);
				}
				stats.increaseOtherObjects();
			}
			
			//Deep remove axiom (tree)
			collectOWLObjectsByDFSInternal(axiomHandle, stats, analyzeOnly);
			// stats updated by DFS
		}
	}
	
	/**
	 * Everything with an otherwise empty incidence set will be removed.
	 * A 
	 * @param linkHandle
	 * @param stats
	 */
	protected void collectOWLObjectsByDFSInternal(HGHandle linkHandle, GarbageCollectorStatistics stats, boolean analyzeOnly) {
		TargetSetALGenerator tsAlg = new TargetSetALGenerator(graph);
		HGDepthFirstTraversal dfs = new HGDepthFirstTraversal(linkHandle, tsAlg);
		boolean linkHandleReturned = false;
		while (dfs.hasNext()) {
			Pair<HGHandle, HGHandle> p = dfs.next();
			HGHandle targetHandle = p.getSecond();
			if (canRemoveAnalyze(targetHandle, p.getFirst(), stats)) {
				if (!analyzeOnly) {
					graph.remove(targetHandle);				
				}
				//stats were already updated on canRemoveAnalyze
			}
			if (targetHandle.getPersistent().equals(linkHandle.getPersistent())) linkHandleReturned = true;
		}
		if (linkHandleReturned) System.out.println("I GOT THE LINK HANDLE FROM DFS");// throw new IllegalStateException("Error during traversal");
		//DFS does not return linkHandle!
		if (canRemoveAnalyze(linkHandle, null, stats)) {
			if (!analyzeOnly) {
				graph.remove(linkHandle);
			}
		}
	}
	
	protected boolean canRemoveAnalyze(HGHandle atomHandle, HGHandle parent, GarbageCollectorStatistics stats) {
		Object atom = graph.get(atomHandle);
		IncidenceSet is = graph.getIncidenceSet(atomHandle);
		//empty, if we deleted parent already, or only parent => safe to delete		
		boolean canRemove = (is.isEmpty() || (is.size() == 1 && (is.first().equals(parent)) || parent == null));
		if (atom == null) {
			System.out.println("GC: Atom null for handle: " + atomHandle);
			canRemove = false;
		}
		if (canRemove) {
			if (atom instanceof OWLOntology) {
				stats.increaseTotalAtoms();
				stats.increaseOntologies();
			} else if (atom instanceof OWLAxiomHGDB) {	
				stats.increaseTotalAtoms();
				stats.increaseAxioms();
			} else if (atom instanceof OWLEntity) {
				OWLEntity entity = (OWLEntity) atom;
				if (entity.isBuiltIn()) {
					//Don't remove built in entities.
					canRemove = false;
					System.out.println("GC: Encountered builtin entity during DFS: " + entity + " Class: " + entity.getClass());
				} else {
					stats.increaseEntities();
					stats.increaseTotalAtoms();
				}
			} else if (atom instanceof IRI) {
				System.out.println("GC: Encountered IRI during DFS: " + atom);
				//we'll encounter those as linked to by Annotations and AnnotationAxioms as
				//an OWLAnnotationValue can be an IRI.
				stats.increaseTotalAtoms();
				stats.increaseIris();
			} else if (atom instanceof OWLObjectHGDB) {
				stats.increaseTotalAtoms();
				stats.increaseOtherObjects();
			} else {
				System.err.println("GC: Encountered unknown atom during DFS GC: " +  atom.getClass() + " Object: " + atom);
			}
		}
		return canRemove;		
	}	

	/**
	 * Counts the number of subgraphs a given atom is a member in. 
	 * Uses Subgraph.reverseIndex.
	 * 
	 * @param atomHandle
	 * @return >=0
	 */
	public int countSubgraphsWhereAtomIsMember(HGHandle atomHandle) {
		HGPersistentHandle axiomPersHandle = graph.getPersistentHandle(atomHandle);
		if (axiomPersHandle == null) throw new IllegalStateException("Null persistent handle");
		HGIndex<HGPersistentHandle,HGPersistentHandle> indexAxiomToOntologies = HGSubgraph.getReverseIndex(graph);
		HGRandomAccessResult<HGPersistentHandle> rs = indexAxiomToOntologies.find(axiomPersHandle);
		int i = 0;
		try {
			while (rs.hasNext()) {
				rs.next();
				i++;
			}
		} finally {
			rs.close();			
		}
		return i;
	}
	
	//TODO SHOW BORIS more efficient query?
	/**
	 * Collects other OWL objects that are disconnected (subclasses of OWLObjectHGDB, not Entities, not AxiomsHGDB) and reachable objects.
	 * 
	 * @param stats
	 */
	public void collectOtherObjects(GarbageCollectorStatistics stats, boolean analyzeOnly) {
		stopWatch.start();
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
				hg.disconnected(),
				hg.typePlus(OWLObjectHGDB.class),
				hg.not(hg.typePlus(OWLEntity.class)),
				hg.not(hg.typePlus(OWLAxiomHGDB.class)))
			);
		stopWatch.stop("Disconnected Others query time: Found: " + handlesToRemove.size() + " Duration:");
		for (HGHandle h: handlesToRemove) {
			Object o = graph.get(h); 
			if (o instanceof IRI) {
				System.out.println("Should not have found IRI (check query types): " + o);
			} 
			collectOWLObjectsByDFSInternal(h, stats, analyzeOnly);
		}
		stopWatch.stop("Disconnected Others collection time: ");
		System.out.println("Stats now: " + stats.toString());		
	}
	
	/**
	 * Collects and removes disconnected entities.
	 * 
	 * @param stats
	 */
	public void collectEntities(GarbageCollectorStatistics stats, boolean analyzeOnly) {
		stopWatch.start();
		List<HGHandle> handlesToRemove = hg.findAll(graph, hg.and(
					hg.typePlus(OWLEntity.class),
					hg.disconnected(),
					hg.not(new OWLEntityIsBuiltIn()),
					hg.not(new AnySubgraphMemberCondition(graph)))
					);
		stopWatch.stop("Disconnected Entities query time: Found: " + handlesToRemove.size() + " Duration:");
		if (!analyzeOnly) {
			int successRemoveCounter = 0;
			for (HGHandle h: handlesToRemove) {
				if (graph.remove(h)) {
					successRemoveCounter ++;
				}
			}
			if (successRemoveCounter != handlesToRemove.size()) throw new IllegalStateException("successRemoveCounter != handles.size()");
			stats.setEntities(stats.getEntities() + successRemoveCounter);
			stats.setTotalAtoms(stats.getTotalAtoms() + successRemoveCounter);
			stopWatch.stop("Disconnected Entities collection time: ");
		} else {
			stats.setEntities(stats.getEntities() + handlesToRemove.size());
			stats.setTotalAtoms(stats.getTotalAtoms() + handlesToRemove.size());
		}
	}	
		
	/**
	 * @return the graph
	 */
	public HyperGraph getGraph() {
		return graph;
	}

	/**
	 * @return the repository
	 */
	public HGDBOntologyRepository getRepository() {
		return repository;
	}
}
