package gov.miamidade.hgowl.plugin.ui.repository;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;

import javax.swing.JOptionPane;

import org.protege.editor.core.OntologyRepository;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.ui.util.JOptionPaneEx;

/**
 * VRepositoryViewPanel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 24, 2012
 */
public class VRepositoryViewPanel extends RepositoryViewPanel {

	private static final long serialVersionUID = 8762858168955963521L;

	/**
	 * @param repository
	 */
	public VRepositoryViewPanel(OntologyRepository repository) {
		super(repository);
	}
	
    public static HGOntologyRepositoryEntry showAddToVersionControlDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Add Ontology to Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		JOptionPane.showMessageDialog(null,
        	        "You did not select an ontology.",
                    "Hypergraph Versioning - None selected",
                    JOptionPane.INFORMATION_MESSAGE);
        	}
            return ore; 
        }
        return null;
    }

    public static HGOntologyRepositoryEntry showRemoveFromVersionControlDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Remove Ontology from Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		JOptionPane.showMessageDialog(null,
        	        "You did not select an ontology.",
                    "Hypergraph Versioning - None selected",
                    JOptionPane.INFORMATION_MESSAGE);
        	}
            return ore; 
        }
        return null;
    }

    public static HGOntologyRepositoryEntry showCommitDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Commit Ontology - Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		JOptionPane.showMessageDialog(null,
        	        "You did not select an ontology.",
                    "Hypergraph Versioning - None selected",
                    JOptionPane.INFORMATION_MESSAGE);
        	}
            return ore; 
        }
        return null;
    }

    public static HGOntologyRepositoryEntry showRollbackDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Rollback Ontology - Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		JOptionPane.showMessageDialog(null,
        	        "You did not select an ontology.",
                    "Hypergraph Versioning - None selected",
                    JOptionPane.INFORMATION_MESSAGE);
        	}
            return ore; 
        }
        return null;
    }

    public static HGOntologyRepositoryEntry showRevertOneDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Revert Ontology by one revision - Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
        	HGOntologyRepositoryEntry ore = panel.getTable().getSelectedEntry(); 
        	if (ore == null) {
        		JOptionPane.showMessageDialog(null,
        	        "You did not select an ontology.",
                    "Hypergraph Versioning - None selected",
                    JOptionPane.INFORMATION_MESSAGE);
        	}
            return ore; 
        }
        return null;
    }
}
