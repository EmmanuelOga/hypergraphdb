package gov.miamidade.hgowl.plugin.ui.repository;

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
	
    public static OntologyRepositoryEntry showAddToVersionControlDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Add Ontology to Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
            return panel.getTable().getSelectedEntry();
        }
        return null;
    }

    public static OntologyRepositoryEntry showRemoveFromVersionControlDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Remove Ontology from Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
            return panel.getTable().getSelectedEntry();
        }
        return null;
    }

    public static OntologyRepositoryEntry showCommitDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Commit Ontology - Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
            return panel.getTable().getSelectedEntry();
        }
        return null;
    }

    public static OntologyRepositoryEntry showRollbackDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Rollback Ontology - Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
            return panel.getTable().getSelectedEntry();
        }
        return null;
    }

    public static OntologyRepositoryEntry showRevertOneDialog(OntologyRepository repository) {
        repository.refresh();
        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Revert Ontology by one revision - Version control (" + repository.getName() + ")", panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.getTable());
        if(ret == JOptionPane.OK_OPTION) {
            return panel.getTable().getSelectedEntry();
        }
        return null;
    }

}
