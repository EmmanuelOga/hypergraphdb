package gov.miamidade.hgowl.plugin.ui.repository;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.protege.editor.core.OntologyRepositoryEntry;
import org.protege.editor.core.ui.util.JOptionPaneEx;

/**
 * VOntologyViewPanel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 30, 2012
 */
public class VOntologyViewPanel extends JPanel {
    

	private VersionedOntology versionedOntology;

    private JTable table;

	public VOntologyViewPanel(VersionedOntology vOnto) {
        this.versionedOntology = vOnto;
        createUI();
    }

    private void createUI() {
        setLayout(new BorderLayout());
        table = new JTable(new VOntologyTableModel(versionedOntology));
        table.getColumnModel().getColumn(0).setMaxWidth(100);
        table.getColumnModel().getColumn(1).setMaxWidth(100);
        table.getColumnModel().getColumn(4).setMaxWidth(100);
        add(new JScrollPane(table));
    }

    public Dimension getPreferredSize() {
        return new Dimension(650, 400);
    }

    public static OntologyRepositoryEntry showRevisionDialog(VersionedOntology vo) {
        VOntologyViewPanel panel = new VOntologyViewPanel(vo);
        int ret = JOptionPaneEx.showConfirmDialog(null, "Versioned Ontology History" + vo.getHeadRevisionData().getOntologyID().getOntologyIRI(), panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.table);
        if(ret == JOptionPane.OK_OPTION) {
        	//DO NOTHING FOR NOW
        }
        return null;
    }

//    public static OntologyRepositoryEntry showDeleteDialog(OntologyRepository repository) {
//        repository.refresh();
//        RepositoryViewPanel panel = new RepositoryViewPanel(repository);
//        int ret = JOptionPaneEx.showConfirmDialog(null, "Delete Ontology from " + repository.getName(), panel, JOptionPane.WARNING_MESSAGE, JOptionPane.OK_CANCEL_OPTION, panel.table);
//        if(ret == JOptionPane.OK_OPTION) {
//            return panel.table.getSelectedEntry();
//        }
//        return null;
//    }

	
}
