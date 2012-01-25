package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.ui.repository.VRepositoryViewPanel;

import java.util.Collection;

import org.hypergraphdb.app.owl.PHGDBOntologyManagerImpl;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.OntologyRepositoryManager;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;


/**
 * VHGOwlEditorKit.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 24, 2012
 */
public class VHGOwlEditorKit extends HGOwlEditorKit {

	public VHGOwlEditorKit(OWLEditorKitFactory editorKitFactory) {
		super(editorKitFactory);
	}
    public boolean handleAddVersionControlRequest() throws Exception {
    	System.out.println("VHG HandleAddVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getRepository();
        if (repository == null) throw new IllegalStateException("No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        OntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showAddToVersionControlDialog(repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	success = (getVersionedRepository().addVersionControl(getOntologyBy(ontologyEntry), "Anonymous") != null);
        } else {
        	success = false;
        }
        return success;
    }

    public boolean handleRemoveVersionControlRequest() throws Exception {
    	System.out.println("VHG HandleRemoveVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        OntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showRemoveFromVersionControlDialog(repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
        	if (vo != null) {
        		getVersionedRepository().removeVersionControl(vo);
        		success = true;
        	} else {
        		success = false;
        	}
        } else {
        	success = false;
        }
        return success;
    }

    public boolean handleCommitRequest() throws Exception {
    	System.out.println("VHG HandleRemoveVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        OntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showCommitDialog(repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
        	if (vo != null) {
        		vo.commit();
        		success = true;
        	} else {
        		success = false;
        	}
        } else {
        	success = false;
        }
        return success;
    }

    public boolean handleRollbackRequest() throws Exception {
    	System.out.println("VHG HandleRemoveVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        OntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showRollbackDialog(repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
        	if (vo != null) {
        		vo.rollback();
        		success = true;
        	} else {
        		success = false;
        	}
        } else {
        	success = false;
        }
        return success;
    }

    public boolean handleRevertOneRequest() throws Exception {
    	System.out.println("VHG HandleRemoveVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        OntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showRevertOneDialog(repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
        	if (vo != null) {
        		vo.revertHeadOneRevision();
        		success = true;
        	} else {
        		success = false;
        	}
        } else {
        	success = false;
        }
        return success;
    }

    /**
     * Returns the VersionedOntologyRepository (Protege Interface)
     * @return
     */
    private VHGOwlOntologyRepository getRepository() {
        Collection<OntologyRepository> repositories = OntologyRepositoryManager.getManager().getOntologyRepositories();
        VHGOwlOntologyRepository repository = null;
        for (OntologyRepository  cur: repositories) {
        	if (cur instanceof VHGOwlOntologyRepository) {
        		//current implementation uses first one found
        		repository = (VHGOwlOntologyRepository)cur;
        		break;
        	}
        }
        return repository;
    }

    private VHGDBOntologyRepository getVersionedRepository() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		return (VHGDBOntologyRepository)hom.getOntologyRepository();
    }
    
    private OWLOntology getOntologyBy(OntologyRepositoryEntry ontologyEntry) {
		OWLOntologyID oID = ((VHGOwlOntologyRepository.HGDBRepositoryEntry)ontologyEntry).getOntologyID();
		if (oID == null) throw new IllegalStateException();		
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		return hom.getOntology(oID);
    }
    
    private VersionedOntology getVersionControlledOntologyBy(OntologyRepositoryEntry ontologyEntry) {
    	return getVersionedRepository().getVersionControlledOntology(getOntologyBy(ontologyEntry));
    }
}
