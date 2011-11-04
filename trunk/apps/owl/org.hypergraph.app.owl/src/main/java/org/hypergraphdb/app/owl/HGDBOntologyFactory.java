package org.hypergraphdb.app.owl;

import java.util.logging.Logger;

import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFactory;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyLoaderConfiguration;
import org.semanticweb.owlapi.model.OWLOntologyManager;
/**
 * HGDBOntologyFactory.
 *
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 */
public class HGDBOntologyFactory implements OWLOntologyFactory {

    private static final Logger logger = Logger.getLogger(HGDBOntologyFactory.class.getName());

    private final static String	HGDB_SCHEME	= "hgdb";	

    private HGDBOntologyManager			manager;
    private HGDBOntologyRepository		repository;
	
	
	@Override
	public void setOWLOntologyManager(OWLOntologyManager owlOntologyManager) {
		manager = (HGDBOntologyManager)owlOntologyManager;
		repository = manager.getOntologyRepository();
	}

	@Override
	public boolean canCreateFromDocumentIRI(IRI documentIRI) {
		return HGDB_SCHEME.equals(documentIRI.getScheme());
	}

	@Override
	public boolean canLoad(OWLOntologyDocumentSource documentSource) {
		return HGDB_SCHEME.equals(documentSource.getDocumentIRI().getScheme());
	}

	@Override
	public OWLOntology createOWLOntology(OWLOntologyID ontologyID,
			IRI documentIRI, OWLOntologyCreationHandler handler)
			throws OWLOntologyCreationException {
		logger.info("HGDB createOWLOntology docIRI:" + documentIRI);
		//Check if exists by docIRI
		if (!repository.existsOntologyByDocumentIRI(documentIRI)) {
			//
			HGDBOntology ontology =  repository.createOWLOntology(ontologyID, documentIRI);
			ontology.setOWLOntologyManager(manager);
			HGDBOntologyFormat ontologyFormat = new HGDBOntologyFormat();
			ontologyFormat.setParameter(HGDBOntologyFormat.PARAMETER_IRI, documentIRI);
			handler.setOntologyFormat(ontology, new  HGDBOntologyFormat());
			handler.ontologyCreated(ontology);	
			
			return ontology;
		} else {
			logger.severe("Ontology with documentIRI" + documentIRI + " already exists.");
			return null;
		}
	}

	@Override
	public OWLOntology loadOWLOntology(
			OWLOntologyDocumentSource documentSource,
			OWLOntologyCreationHandler handler)
			throws OWLOntologyCreationException {
		logger.info("HGDB loadOWLOntology");
		if (!canCreateFromDocumentIRI(documentSource.getDocumentIRI())) {
			throw new OWLOntologyCreationException("Wrong scheme. Need:" + HGDB_SCHEME);
		}
		//try load it
		HGDBOntology o = repository.getOntologyByDocumentIRI(documentSource.getDocumentIRI());
		if (o == null) {
			throw new OWLOntologyCreationException("Not found: " + documentSource.getDocumentIRI());
		}
		//TODO Intercept creation? Set a manager in HG Type?
		o.setOWLOntologyManager(manager);
		logger.info("Loaded: Ontology" + o.getOntologyID());
		handler.ontologyCreated(o);	
		return o;
	}
	
	@Override
	public OWLOntology loadOWLOntology(
			OWLOntologyDocumentSource documentSource,
			OWLOntologyCreationHandler handler,
			OWLOntologyLoaderConfiguration configuration)
			throws OWLOntologyCreationException {
		//OWLOntologyLoaderConfiguration ignored
		logger.info("HGDB loadOWLOntology with config");
		return loadOWLOntology(documentSource, handler);
	}
}