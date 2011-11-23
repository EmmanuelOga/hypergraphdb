package gov.miamidade.hgowl.plugin.owl;

import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryFactory;

/**
 * HGOwlOntologyRepositoryFactory.
 * A factory for Hypergraph backed Protege OntologyRepositories.
 * 
 * @author Thomas Hilpold (GIC/Miami-Dade County)
 * @created Sep 26, 2011
 */
public class HGOwlOntologyRepositoryFactory extends OntologyRepositoryFactory {

	HGDBOntologyRepository dbRepository;
	
	@Override
	public void initialise() throws Exception {
		dbRepository = new HGDBOntologyRepository();
	}

	@Override
	public void dispose() throws Exception {
		dbRepository.dispose();
		dbRepository = null;
	}

	@Override
	public OntologyRepository createRepository() {
		if (dbRepository == null) {
			dbRepository = new HGDBOntologyRepository();
		}
		return new HGOwlOntologyRepository("Hypergraph", dbRepository);
	}
}