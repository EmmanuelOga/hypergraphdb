package gov.miamidade.hgowl.plugin.ui.repository;

import gov.miamidade.hgowl.plugin.owl.model.HGOntologyRepositoryEntry;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.hypergraphdb.app.owl.versioning.VersionedOntology;
import org.protege.editor.core.OntologyRepositoryEntry;

/**
 * VOntologyViewPanel.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Jan 30, 2012
 */
public class VOntologyViewPanel extends JPanel {
    
	private static final long serialVersionUID = 159528341514944079L;

	private VersionedOntology versionedOntology;

    private JTable table;

	public VOntologyViewPanel(VersionedOntology vOnto) {
        this.versionedOntology = vOnto;
        createUI();
    }

    private void createUI() {
        setLayout(new BorderLayout());
        table = new JTable(new VOntologyTableModel(versionedOntology));
        table.getColumnModel().getColumn(0).setMinWidth(20);
        table.getColumnModel().getColumn(1).setMinWidth(30);
        table.getColumnModel().getColumn(2).setMinWidth(140);
        //table.getColumnModel().getColumn(2).setWidth(130);
        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(30);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        //table.getColumnModel().getColumn(2).setWidth(130);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        //table.getColumnModel().getColumn(4).setMaxWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);
        add(new JScrollPane(table));
    }

    public Dimension getPreferredSize() {
        return new Dimension(650, 400);
    }

    public static HGOntologyRepositoryEntry showRevisionDialog(VersionedOntology vo) {
        VOntologyViewPanel panel = new VOntologyViewPanel(vo);
        JOptionPane.showMessageDialog(null, panel, "Versioned Ontology History " + vo.getHeadRevisionData().getOntologyID().getOntologyIRI(), JOptionPane.PLAIN_MESSAGE);
//        if(ret == JOptionPane.OK_OPTION) {
//        	//DO NOTHING FOR NOW
//        }
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
