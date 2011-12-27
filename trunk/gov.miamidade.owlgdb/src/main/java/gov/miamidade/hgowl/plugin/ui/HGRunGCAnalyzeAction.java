package gov.miamidade.hgowl.plugin.ui;

import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;
import javax.swing.JTextPane;


import org.hypergraphdb.app.owl.HGDBOntologyManager;
import org.hypergraphdb.app.owl.gc.GarbageCollector;
import org.hypergraphdb.app.owl.gc.GarbageCollectorStatistics;
import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;

/**
 * HGRunGCAction.
 * @author Thomas Hilpold (CIAO/Miami-Dade County)
 * @created Oct 11, 2011
 */
public class HGRunGCAnalyzeAction extends ProtegeOWLAction {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2085444668481360102L;

	/* (non-Javadoc)
	 * @see org.protege.editor.core.plugin.ProtegePluginInstance#initialise()
	 */
	@Override
	public void initialise() throws Exception {

	}

	/* (non-Javadoc)
	 * @see org.protege.editor.core.Disposable#dispose()
	 */
	@Override
	public void dispose() throws Exception {

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		HGOwlModelManagerImpl mm = (HGOwlModelManagerImpl) this.getOWLModelManager();
		HGDBOntologyManager om =  (HGDBOntologyManager) mm.getOWLOntologyManager();
		if (showRunGCConfirmation()) {
			//mm.getHistoryManager().clear();
			int mode = HGRunGCModeSelectionPanel.showDialog(getOWLEditorKit(), "Run Garbage Analysis Mode");
			//	this can take long:
			if (mode >= 0) {
				GarbageCollector gc = om.getOntologyRepository().getGC();
				GarbageCollectorStatistics stats = gc.analyze(mode);
				System.out.println("Total GCd atoms: " + stats.getTotalAtoms());
				showResult(stats);
			} else {
				System.out.println("GC analyze aborted by user.");
			}
		}
	}
	
	public boolean showRunGCConfirmation() {
        String message = "Please confirm that you want to run garbage analysis: \n" 
          	+ "    Undo/Redo changes will remain functional. \n"
          	+ "    The duration of this operation depends on the mode selected. \n"; 
        int userInput = JOptionPane.showConfirmDialog(getWorkspace(),
                                      message,
                                      "Confirm Garbage Collection - Full run",
                                      JOptionPane.YES_NO_OPTION);
        return (userInput == JOptionPane.YES_OPTION);
	}

	public void showResult(GarbageCollectorStatistics stats) {
		StringWriter stringWriter = new StringWriter(400);
		PrintWriter s = new PrintWriter(stringWriter);
		s.println();
		s.println("----------------------------");
		s.println("- GARBAGE ANALYSIS STATS  -");
		s.println("- Ontologies    : " + stats.getOntologies());
		s.println("- Axioms        : " + stats.getAxioms());
		s.println("- Entities      : " + stats.getEntities());
		s.println("- IRIs          : " + stats.getIris());
		s.println("- Other Objects : " + stats.getOtherObjects());
		s.println("- Total Atoms   : " + stats.getTotalAtoms());
		s.println("----------------------------");
		s.println("- Times we met an axiom contained in more than one ontology");
		s.println("-   and therefore would not be removed: " + stats.getAxiomNotRemovableCases());
		s.flush();		String message = stringWriter.toString();
		JTextPane textPane = new JTextPane();
		//textPane.setSize(500,600);
		textPane.setText(message);
		
		JOptionPaneEx.showConfirmDialog(getWorkspace(),
				"Hypergraph Garbage Analysis Statistics", 
				textPane, JOptionPane.INFORMATION_MESSAGE, 
				JOptionPane.OK_CANCEL_OPTION, 
				textPane);
	}
}
