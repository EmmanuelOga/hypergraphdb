package org.hypergraphdb.app.owl.core;

import static org.semanticweb.owlapi.model.AxiomType.AXIOM_TYPES;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hypergraphdb.HGLink;
import org.hypergraphdb.app.owl.model.axioms.OWLDeclarationAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubClassOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubDataPropertyOfAxiomHGDB;
import org.hypergraphdb.app.owl.model.axioms.OWLSubObjectPropertyOfAxiomHGDB;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * AxiomTypeMapToHGDB maps all 39 Axiom types as defined by AxiomType.class to HGDB concrete HGLink axiom classes, who's object are stored in the graph.
 * * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Oct 6, 2011
 */
public class AxiomTypeToHGDBMap {
	public final static int INITIAL_MAP_SIZE = 101;

	static {
		m = new HashMap<AxiomType<? extends OWLAxiom>, Class<? extends OWLAxiomHGDB>>(INITIAL_MAP_SIZE);
		mReverse = new HashMap<Class<? extends OWLAxiomHGDB>, AxiomType<? extends OWLAxiom>>(INITIAL_MAP_SIZE);
		logicalAxiomTypesHGDB = new HashSet<Class<? extends OWLAxiomHGDB>>();
		initializeMaps();
		initializeLogicalAxiomSet();
	}
	
	private static Map<AxiomType<? extends OWLAxiom>, Class<? extends OWLAxiomHGDB>> m;
	private static Map<Class<? extends OWLAxiomHGDB>, AxiomType<? extends OWLAxiom>> mReverse;

	private static Set<Class<? extends OWLAxiomHGDB>> logicalAxiomTypesHGDB;
	
	private AxiomTypeToHGDBMap() {
	}
	

	/**
	 * Gets a AxiomTypeHGDB class by hash lookup O(1). 
	 * 
	 * @param axiomType
	 * @return a non abstract subclass of OWLAxiomHGDB that is implementing HGLink
	 */
	public static Class<? extends OWLAxiomHGDB> getAxiomClassHGDB(AxiomType<? extends OWLAxiom> axiomType) {
		return m.get(axiomType);
	}
	
	/**
	 * Gets a AxiomType by hash lookup O(1). 
	 * 
	 * @param axiomClassHGDB
	 * @return AxiomType
	 */
	public static AxiomType<? extends OWLAxiom> getAxiomType( Class<? extends OWLAxiomHGDB> axiomClassHGDB) {
		return mReverse.get(axiomClassHGDB);
	}
	
	/**
	 * Adds both classes to the internal maps. 
	 * Only axiomTypes in AxiomType.AXIOM_TYPES should be added.
	 * 
	 * @param axiomType a non-null axiomType (usually contained in )
	 * @param axiomClassHGDB a non abstract subclass of OWLAxiomHGDB that is implementing HGLink
	 */
	public static void addToMap(AxiomType<? extends OWLAxiom> axiomType, Class<? extends OWLAxiomHGDB> axiomClassHGDB) {
		if (m.containsKey(axiomType)) throw new IllegalArgumentException("axiomType already mapped " + axiomType); 
		if (mReverse.containsKey(axiomClassHGDB)) throw new IllegalArgumentException("axiomClassHGDB already mapped " + axiomClassHGDB);
		if (axiomType == null || axiomClassHGDB == null) throw new IllegalArgumentException("null not allowed");
		if (Modifier.isAbstract(axiomClassHGDB.getModifiers())) throw new IllegalArgumentException("axiomClassHGDB must not be abstract" + axiomClassHGDB);
		if (! HGLink.class.isAssignableFrom(axiomClassHGDB)) throw new IllegalArgumentException("axiomClassHGDB must implement HGLink" + axiomClassHGDB);
		m.put(axiomType, axiomClassHGDB);
		mReverse.put(axiomClassHGDB, axiomType);
	}
	
	private static void initializeMaps() {
        addToMap(AxiomType.SUBCLASS_OF, OWLSubClassOfAxiomHGDB.class);
        //02 addToMap(EQUIVALENT_CLASSES, OWLAxiomHGDB.class);
        //03 addToMap(AxiomType.DISJOINT_CLASSES, OWLAxiomHGDB.class);
        //04 addToMap(AxiomType.CLASS_ASSERTION, OWLAxiomHGDB.class);
        //05 addToMap(AxiomType.SAME_INDIVIDUAL, OWLAxiomHGDB.class);
        //06 addToMap(AxiomType.DIFFERENT_INDIVIDUALS, OWLAxiomHGDB.class);
        //07 addToMap(AxiomType.OBJECT_PROPERTY_ASSERTION, OWLAxiomHGDB.class);
        //08 addToMap(AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION, OWLAxiomHGDB.class);
        //09 addToMap(AxiomType.DATA_PROPERTY_ASSERTION, OWLAxiomHGDB.class);
        //10 addToMap(AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION, OWLAxiomHGDB.class);
        //11 addToMap(AxiomType.OBJECT_PROPERTY_DOMAIN, OWLAxiomHGDB.class);
        //12 addToMap(AxiomType.OBJECT_PROPERTY_RANGE, OWLAxiomHGDB.class);
        //13 addToMap(AxiomType.DISJOINT_OBJECT_PROPERTIES, OWLAxiomHGDB.class);
        addToMap(AxiomType.SUB_OBJECT_PROPERTY, OWLSubObjectPropertyOfAxiomHGDB.class);
        //15 addToMap(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, OWLAxiomHGDB.class);
        //16 addToMap(AxiomType.INVERSE_OBJECT_PROPERTIES, OWLAxiomHGDB.class);
        //17 addToMap(AxiomType.SUB_PROPERTY_CHAIN_OF, OWLAxiomHGDB.class);
        //18 addToMap(AxiomType.FUNCTIONAL_OBJECT_PROPERTY, OWLAxiomHGDB.class);
        //19 addToMap(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY, OWLAxiomHGDB.class);
        //20 addToMap(AxiomType.SYMMETRIC_OBJECT_PROPERTY, OWLAxiomHGDB.class);
        //21 addToMap(AxiomType.ASYMMETRIC_OBJECT_PROPERTY, OWLAxiomHGDB.class);
        //22 addToMap(AxiomType.TRANSITIVE_OBJECT_PROPERTY, OWLAxiomHGDB.class);
        //23 addToMap(AxiomType.REFLEXIVE_OBJECT_PROPERTY, OWLAxiomHGDB.class);
        //24 addToMap(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY, OWLAxiomHGDB.class);
        //25 addToMap(AxiomType.DATA_PROPERTY_DOMAIN, OWLAxiomHGDB.class);
        //26 addToMap(AxiomType.DATA_PROPERTY_RANGE, OWLAxiomHGDB.class);
        //27 addToMap(AxiomType.DISJOINT_DATA_PROPERTIES, OWLAxiomHGDB.class);
        addToMap(AxiomType.SUB_DATA_PROPERTY, OWLSubDataPropertyOfAxiomHGDB.class);
        //29 addToMap(AxiomType.EQUIVALENT_DATA_PROPERTIES, OWLAxiomHGDB.class);
        //30 addToMap(AxiomType.FUNCTIONAL_DATA_PROPERTY, OWLAxiomHGDB.class);
        //31 addToMap(AxiomType.DATATYPE_DEFINITION, OWLAxiomHGDB.class);
        //32 addToMap(AxiomType.DISJOINT_UNION, OWLAxiomHGDB.class);
        addToMap(AxiomType.DECLARATION, OWLDeclarationAxiomHGDB.class);
        //34 addToMap(AxiomType.SWRL_RULE, OWLAxiomHGDB.class);
        //35 addToMap(AxiomType.ANNOTATION_ASSERTION, OWLAxiomHGDB.class);
        //36 addToMap(AxiomType.SUB_ANNOTATION_PROPERTY_OF, OWLAxiomHGDB.class);
        //37 addToMap(AxiomType.ANNOTATION_PROPERTY_DOMAIN, OWLAxiomHGDB.class);
        //38 addToMap(AxiomType.ANNOTATION_PROPERTY_RANGE, OWLAxiomHGDB.class);
        //39 addToMap(AxiomType.HAS_KEY, OWLAxiomHGDB.class);
		System.out.println("AxiomTypeMapToHGDB Initialized: " + m.size() + " mappings defined.");
	}	
	
	/**
	 * Gets a set of concrete Axiom HGDB classes that correspond to logical Axiomtypes.
	 * 
	 * @return a unmodifiable set. 
	 */
	public static Set<Class<? extends OWLAxiomHGDB>> getLogicalAxiomTypesHGDB() {
		return logicalAxiomTypesHGDB;
	}

	/**
	 * Adds all Axiom HGDB classes to the set that correspond to locigal Axiomtypes.
	 */
	private static void initializeLogicalAxiomSet() {
		for (AxiomType<?> type : AXIOM_TYPES) {
			if (type.isLogical()) {
				System.out.println("LOGICAL AXIOM: " + type);
				if (m.containsKey(type)) {
					logicalAxiomTypesHGDB.add(m.get(type)); 
				} else {
					//not yet defined in initialize
				}
			}
		}
		//Make unmodifiable after init.
		logicalAxiomTypesHGDB = Collections.unmodifiableSet(logicalAxiomTypesHGDB);
		System.out.println("LogicalAxiomTypesHGDB Initialized, size : " + logicalAxiomTypesHGDB.size());
	}
}
