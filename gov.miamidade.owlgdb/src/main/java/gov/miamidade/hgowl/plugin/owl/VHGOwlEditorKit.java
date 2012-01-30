package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.ui.render.VHGOwlIconProviderImpl;
import gov.miamidade.hgowl.plugin.ui.repository.VRepositoryViewPanel;

import java.text.DateFormat;
import java.util.Collection;

import javax.swing.JOptionPane;

import org.hypergraphdb.app.owl.PHGDBOntologyManagerImpl;
import org.hypergraphdb.app.owl.versioning.Revision;
import org.hypergraphdb.app.owl.versioning.VHGDBOntologyRepository;
import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.OntologyRepositoryManager;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.protege.editor.owl.model.event.EventType;
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
	
	protected void initialise(){
		super.initialise();
		getWorkspace().setOWLIconProvider(new VHGOwlIconProviderImpl(modelManager, this));
	}

    public boolean handleAddVersionControlRequest() throws Exception {
    	System.out.println("VHG HandleAddVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getProtegeRepository();
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
        OntologyRepository repository = getProtegeRepository();
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
    
    public boolean handleCommitActiveRequest() throws Exception {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		//PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		OWLOntology activeOnto = hmm.getActiveOntology();
		VHGDBOntologyRepository vor = getVersionedRepository();
		if (vor.isVersionControlled(activeOnto)) {
			VersionedOntology vo = vor.getVersionControlledOntology(activeOnto);
			int pendingChanges = vo.getHeadChangeSet().size();
			if (pendingChanges == 0) {
				// NO PENDING CHANGES OK
				System.out.println("No pending changes.");
                JOptionPane.showMessageDialog(getWorkspace(),
                        "Cannot commit: No pending changes",
                        "Hypergraph Versioning - No Changes",
                        JOptionPane.INFORMATION_MESSAGE);
			} else {
				// 	COMMIT WHAT WHO INCREMENT OK CANCEL
				if (showUserCommitDialog(vo, activeOnto)) {
					//DO IT 
					String user = System.getProperty("user.name");
					vo.commit(user, Revision.REVISION_INCREMENT);
					// NEW REVISION OK
				}
			}
		} else {
			System.out.println("Active ontology not version controlled.");
            JOptionPane.showMessageDialog(getWorkspace(),
                    "Cannot commit: Active ontology not version controlled",
                    "Hypergraph Versioning - Active not versioned",
                    JOptionPane.INFORMATION_MESSAGE);
		}
    	return true;
    	
    }
    
    public boolean showUserCommitDialog(VersionedOntology vo, OWLOntology onto) {
		int nrOfRevisions = vo.getNrOfRevisions();
		Revision headRevision = vo.getHeadRevision();
		int pendingChanges = vo.getHeadChangeSet().size();
    	String message = "Do you want to commit " + pendingChanges + " change" 
    		+ ((pendingChanges > 1)? "s" : "") + ":\n" 
          	+ "    Last Revision    : " + headRevision.getRevision() + "\n"
          	+ "    Created          : " + DateFormat.getDateTimeInstance().format(headRevision.getTimeStamp()) + "\n"
          	+ "    By               : " + headRevision.getUser() + "\n" 
    		+ "    Total Revisions  : " + nrOfRevisions + "\n" 
    		+ "    Ontology ID : " + headRevision.getOntologyID() + "\n \n"; 
        int userInput = JOptionPane.showConfirmDialog(getWorkspace(),
                                      message,
                                      "Commit Versioned HGDB Ontology - Confirm Commit",
                                      JOptionPane.YES_NO_OPTION);
        return (userInput == JOptionPane.YES_OPTION);
    }

    public boolean handleRollbackActiveRequest() throws Exception {
    	return true;
    }
    
    public boolean handleCommitRequest() throws Exception {
    	System.out.println("VHG HandleRemoveVersionControlRequest");
    	boolean success;
        // Find our Repository 
        OntologyRepository repository = getProtegeRepository();
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
        OntologyRepository repository = getProtegeRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        OntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showRollbackDialog(repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
        	if (vo != null) {
        		vo.rollback();
        		//Update Protege
        		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
        		//if (hmm.geta)
        		hmm.fireEvent(EventType.ONTOLOGY_RELOADED);
        		this.workspace.refreshComponents();
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
        OntologyRepository repository = getProtegeRepository();
        if (repository == null) throw new IllegalStateException("Cannot handle delete from repository. No HGOwlOntologyRepository registered with Protege.");
        // Open Repository delete dialog 
        OntologyRepositoryEntry ontologyEntry = VRepositoryViewPanel.showRevertOneDialog(repository);        
        if (ontologyEntry != null) {
        	// User wants to add ontology to version control.
        	VersionedOntology vo = getVersionControlledOntologyBy(ontologyEntry);
        	if (vo != null) {
        		//Update Protege
        		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
        		vo.revertHeadOneRevision();
        		hmm.fireEvent(EventType.ONTOLOGY_RELOADED);
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
     * Returns the OntologyRepository implementation of our plugin (Protege Interface).
     * Will find the versioned repository.
     * 
     * @return the found VHGOwlOntologyRepository or null.
     */
    public OntologyRepository getProtegeRepository() {
        Collection<OntologyRepository> repositories = OntologyRepositoryManager.getManager().getOntologyRepositories();
    	for (OntologyRepository  cur: repositories) {
        	if (cur instanceof VHGOwlOntologyRepository) {
        		//current implementation uses first one found
        		return cur;
        	}
        }
        return null;
    }

    public VHGDBOntologyRepository getVersionedRepository() {
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		return (VHGDBOntologyRepository)hom.getOntologyRepository();
    }
    
    public OWLOntology getOntologyBy(OntologyRepositoryEntry ontologyEntry) {
		OWLOntologyID oID = ((VHGOwlOntologyRepository.HGDBRepositoryEntry)ontologyEntry).getOntologyID();
		if (oID == null) throw new IllegalStateException();		
		HGOwlModelManagerImpl hmm  = (HGOwlModelManagerImpl) getOWLModelManager();
		PHGDBOntologyManagerImpl  hom = (PHGDBOntologyManagerImpl)hmm.getOWLOntologyManager(); 
		return hom.getOntology(oID);
    }
    
    public VersionedOntology getVersionControlledOntologyBy(OntologyRepositoryEntry ontologyEntry) {
    	return getVersionedRepository().getVersionControlledOntology(getOntologyBy(ontologyEntry));
    }


}
