package gov.miamidade.hgowl.plugin.owl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.hypergraphdb.app.owl.HGDBOntology;
import org.hypergraphdb.app.owl.HGDBOntologyRepository;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.editorkit.EditorKit;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.util.OntologyIRIShortFormProvider;

/**
 * A protege repository implementation backed by a Hypergraph Ontology Repository instance.
 * This enabled Protege to show Hypergraph ontologies at startup. 
 * 
 * @author Thomas Hilpold
 */
public class HGOwlOntologyRepository implements OntologyRepository {

    private String repositoryName;

    private HGDBOntologyRepository dbRepository;

    private List<RepositoryEntry> entries;

    private OWLOntologyIRIMapper iriMapper;

    public HGOwlOntologyRepository(String repositoryName, HGDBOntologyRepository dbRepository) {
        this.repositoryName = repositoryName;
        this.dbRepository = dbRepository;
        entries = new ArrayList<RepositoryEntry>();
        iriMapper = new RepositoryIRIMapper();
    }

    public void initialise() throws Exception {
    }

    public String getName() {
        return repositoryName;
    }

    public String getLocation() {
        return "Hypergraph Repository at " + HGDBOntologyRepository.HYPERGRAPH_DB_LOCATION;
    }

    public void refresh() {
        fillRepository();
    }

    public Collection<OntologyRepositoryEntry> getEntries() {
        List<OntologyRepositoryEntry> ret = new ArrayList<OntologyRepositoryEntry>();
        ret.addAll(entries);
        return ret;
    }

    public List<Object> getMetaDataKeys() {
        return Collections.emptyList();
    }

    public void dispose() throws Exception {
    	dbRepository.dispose();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
    //
    //  Implementation details

    private void fillRepository() {
        entries.clear();
        List<HGDBOntology>  l = dbRepository.getOntologies();
        for(HGDBOntology o : l) {
            entries.add(new RepositoryEntry(o));
        }
    }

    private class RepositoryEntry implements OntologyRepositoryEntry {

        private String shortName;

        private URI ontologyURI;

        private URI physicalURI;

        public RepositoryEntry(HGDBOntology o) {
        	this.shortName = o.getOntologyID().getOntologyIRI().getFragment();
            this.ontologyURI = URI.create(o.getOntologyID().getOntologyIRI().toString());
            OntologyIRIShortFormProvider sfp = new OntologyIRIShortFormProvider();
            shortName = sfp.getShortForm(o);
            physicalURI = URI.create(o.getDocumentIRI().toString());
        }


        public String getOntologyShortName() {
            return shortName;
        }


        public URI getOntologyURI() {
            return ontologyURI;
        }


        public URI getPhysicalURI() {
            return physicalURI;
        }


        public String getEditorKitId() {
            return HGOwlEditorKitFactory.ID;
        }


        public String getMetaData(Object key) {
            return null;
        }

        public void configureEditorKit(EditorKit editorKit) {
            ((HGOwlEditorKit) editorKit).getOWLModelManager().getOWLOntologyManager().addIRIMapper(iriMapper);
        }


        public void restoreEditorKit(EditorKit editorKit) {
            ((HGOwlEditorKit) editorKit).getOWLModelManager().getOWLOntologyManager().removeIRIMapper(iriMapper);

        }
    }

    private class RepositoryIRIMapper implements OWLOntologyIRIMapper {

        public IRI getDocumentIRI(IRI iri) {
            for(RepositoryEntry entry : entries) {
                if(entry.getOntologyURI().equals(iri.toURI())) {
                    return IRI.create(entry.getPhysicalURI());
                }
            }
            return null;
        }
    }
}