package org.hypergraphdb.app.owl;

import org.coode.owlapi.functionalparser.OWLFunctionalSyntaxParserFactory;
import org.coode.owlapi.functionalrenderer.OWLFunctionalSyntaxOntologyStorer;
import org.coode.owlapi.latex.LatexOntologyStorer;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxParserFactory;
import org.coode.owlapi.obo.parser.OBOParserFactory;
import org.coode.owlapi.obo.renderer.OBOFlatFileOntologyStorer;
import org.coode.owlapi.owlxml.renderer.OWLXMLOntologyStorer;
import org.coode.owlapi.owlxmlparser.OWLXMLParserFactory;
import org.coode.owlapi.rdf.rdfxml.RDFXMLOntologyStorer;
import org.coode.owlapi.rdfxml.parser.RDFXMLParserFactory;
import org.coode.owlapi.turtle.TurtleOntologyStorer;
import org.hypergraphdb.app.owl.core.OWLDataFactoryHGDB;
import org.hypergraphdb.app.owl.versioning.distributed.serialize.VOWLXMLOntologyStorer;
import org.semanticweb.owlapi.io.OWLParserFactoryRegistry;
import org.semanticweb.owlapi.util.NonMappingOntologyIRIMapper;

import uk.ac.manchester.cs.owl.owlapi.EmptyInMemOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.ParsableOWLOntologyFactory;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOntologyStorer;
import uk.ac.manchester.cs.owl.owlapi.turtle.parser.TurtleOntologyParserFactory;
import de.uulm.ecs.ai.owlapi.krssparser.KRSS2OWLParserFactory;
import de.uulm.ecs.ai.owlapi.krssrenderer.KRSS2OWLSyntaxOntologyStorer;

/**
 * HGDBOWLManager.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 18, 2012
 */
public class HGDBOWLManager {

//	public static HGDBOntologyManagerImpl createOWLOntologyManager() {
//        return createOWLOntologyManager(OWLDataFactoryHGDB.getInstance());
//    }
    
	/**
	 * Create the ontology manager and add ontology factories, mappers and
	 * storers.
	 * 
	 * @param dataFactory The data factory to use
	 * @return <code>OWLDBOntologyManager</code>
	 */
	public static HGDBOntologyManagerImpl createOWLOntologyManager (
			final OWLDataFactoryHGDB dataFactory,
			HGDBOntologyRepository repository) {
		final HGDBOntologyManagerImpl ontologyManager = new HGDBOntologyManagerImpl(dataFactory, repository);
		ontologyManager.addOntologyStorer (new RDFXMLOntologyStorer());
		ontologyManager.addOntologyStorer (new OWLXMLOntologyStorer());
		ontologyManager.addOntologyStorer (new OWLFunctionalSyntaxOntologyStorer());
		ontologyManager.addOntologyStorer (new ManchesterOWLSyntaxOntologyStorer());
		ontologyManager.addOntologyStorer (new OBOFlatFileOntologyStorer());
		ontologyManager.addOntologyStorer (new KRSS2OWLSyntaxOntologyStorer());
		ontologyManager.addOntologyStorer (new TurtleOntologyStorer());
		ontologyManager.addOntologyStorer (new LatexOntologyStorer());
		ontologyManager.addOntologyStorer (new HGDBStorer ());
		ontologyManager.addOntologyStorer (new VOWLXMLOntologyStorer());

		ontologyManager.addIRIMapper (new NonMappingOntologyIRIMapper());
		// a HDBGIRI mapper cannot be added here because the repo is not known.
		ontologyManager.addOntologyFactory (new EmptyInMemOWLOntologyFactory());
		ontologyManager.addOntologyFactory (new ParsableOWLOntologyFactory());
		ontologyManager.addOntologyFactory (new HGDBOntologyFactory ());
		return ontologyManager;
	}	
	
    static {
		//2011.11.29 Parsers to load from files:		
        // Register useful parsers
        OWLParserFactoryRegistry registry = OWLParserFactoryRegistry.getInstance();
        registry.registerParserFactory(new ManchesterOWLSyntaxParserFactory());
        registry.registerParserFactory(new KRSS2OWLParserFactory());
        registry.registerParserFactory(new OBOParserFactory());
        registry.registerParserFactory(new TurtleOntologyParserFactory());
        registry.registerParserFactory(new OWLFunctionalSyntaxParserFactory());
        registry.registerParserFactory(new OWLXMLParserFactory());
        registry.registerParserFactory(new RDFXMLParserFactory());
    }
}
