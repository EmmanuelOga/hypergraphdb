package gov.miamidade.hgowl.plugin.owl;

import gov.miamidade.hgowl.plugin.owl.model.HGOwlModelManagerImpl;
import gov.miamidade.hgowl.plugin.ui.CreateHGOntologyWizard;

import java.net.URI;
import java.util.HashSet;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.EditorKitDescriptor;
import org.protege.editor.core.ui.wizard.Wizard;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.OWLEditorKitFactory;
import org.protege.editor.owl.ProtegeOWL;
import org.protege.editor.owl.model.SaveErrorHandler;
import org.protege.editor.owl.ui.error.OntologyLoadErrorHandlerUI;
import org.protege.editor.owl.ui.explanation.ExplanationManager;
import org.protege.editor.owl.ui.ontology.imports.missing.MissingImportHandlerUI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.VersionInfo;

/**
 * HGOwlEditorKit
 * Here, the crucial connection between Protege Editor and our OWL Api 
 * with Hypergraph backend is established (see initialize). 
 * The connection point to the Hypergraph OWL-API implementation is HGOWLModelManager.
 * 
 * @author Thomas Hilpold
 */
public class HGOwlEditorKit extends OWLEditorKit {

	private static final Logger logger = Logger.getLogger(HGOwlEditorKit.class);

    public static final String ID = "HGOwlEditorKit";

    public HGOwlEditorKit(OWLEditorKitFactory editorKitFactory) {
		super(editorKitFactory);
	}
		
    @SuppressWarnings("deprecation")
	protected void initialise(){
    	// DO NOT DO THIS: super.initialise();    	
    	// THIS SETS OUR MODEL MANAGER
    	HGOwlModelManagerImpl modelManager = new HGOwlModelManagerImpl();
    	setOWLModelManager(modelManager);
        logger.info("Using OWL API version " + VersionInfo.getVersionInfo().getVersion());
        this.newPhysicalURIs = new HashSet<URI>();
        modelManager.setExplanationManager(new ExplanationManager(this));
        modelManager.setMissingImportHandler(new MissingImportHandlerUI(this));
        modelManager.setSaveErrorHandler(new SaveErrorHandler(){
            public void handleErrorSavingOntology(OWLOntology ont, URI physicalURIForOntology, OWLOntologyStorageException e) throws Exception {
                handleSaveError(ont, physicalURIForOntology, e);
            }
        });
        loadErrorHandler = new OntologyLoadErrorHandlerUI(this);
        modelManager.setLoadErrorHandler(loadErrorHandler);
        loadIOListenerPlugins();
        registration = ProtegeOWL.getBundleContext().registerService(EditorKit.class.getCanonicalName(), this, new Properties());

    }

    protected void initialiseCompleted() {
        super.initialiseCompleted();
    }
    

    /**
     * Gets the <code>EditorKit</code> Id.  This can be used to identify
     * the type of <code>EditorKit</code>.
     * @return A <code>String</code> that represents the <code>EditorKit</code>
     *         Id.
     */
    public String getId() {
        return ID;
    }

    public boolean handleNewRequest() throws Exception {
    	boolean handleNewSuccess = false;
    	CreateHGOntologyWizard w = new CreateHGOntologyWizard(null, this);
        int result = w.showModalDialog();
        if (result == Wizard.FINISH_RETURN_CODE) {
            OWLOntologyID oid = w.getOntologyID();
            if (oid != null) {
            	HGOwlModelManagerImpl mm = (HGOwlModelManagerImpl)getOWLModelManager();
            	mm.createNewOntology(oid, w.getLocationURI());
            	//addToRecent(URI.create(prop.getProperty("hibernate.connection.url")));
            	addRecent(w.getLocationURI());
        		handleNewSuccess = true;
            }
        }
        return handleNewSuccess;
    }

    public boolean handleLoadRecentRequest(EditorKitDescriptor descriptor) throws Exception {
    	System.out.println("HG handleLoadRecentRequest");
        return super.handleLoadRecentRequest(descriptor );
    }

    public boolean handleLoadRequest() throws Exception {
    	System.out.println("HG HandleLoadRequest");
    	return super.handleLoadRequest();
//    	if(DatabaseDialogPanel.showDialog(this)){
//    		Properties prop = getDBProperty();
//    		boolean success =  handleLoadFrom(prop);
//    		if(success == true)
//    			addToRecent(URI.create(prop.getProperty("hibernate.connection.url")));
//    		return success;
//    	}
//    	else
//    		return false;
    }

    public boolean handleLoadFrom(URI uri) throws Exception {    	
        boolean success = ((HGOwlModelManagerImpl) getModelManager()).loadOntologyFromPhysicalURI(uri);
        if (success){
            addRecent(uri);
        }
        return success;
    }

    public void handleSave() throws Exception {
    	System.out.println("HG handleSave ");
    	super.handleSave();
    	//OWLOntology ont = getModelManager().getActiveOntology();
//    	OWLOntologyFormat format = getModelManager().getOWLOntologyManager().getOntologyFormat(ont);
//    	// if the format is Database, do nothing because is is already saved
//    	if(format instanceof OWLDBOntologyFormat){
//    		return;
//    	}
//    	
//        try {
//            getModelManager().save();
//            getWorkspace().save();
//            for (URI uri : newPhysicalURIs) {
//                addRecent(uri);
//            }
//            newPhysicalURIs.clear();
//        }
//        catch (OWLOntologyStorerNotFoundException e) {
//            ont = getModelManager().getActiveOntology();
//            format = getModelManager().getOWLOntologyManager().getOntologyFormat(ont);
//            String message = "Could not save ontology in the specified format (" + format + ").\n" + "Please selected 'Save As' and select another format.";
//            logger.warn(message);
//            JOptionPane.showMessageDialog(getWorkspace(),
//                                          message,
//                                          "Could not save ontology",
//                                          JOptionPane.ERROR_MESSAGE);
//        }
    }

    public void handleSaveAs() throws Exception {
    	System.out.println("HG handleSaveAs ");
    	super.handleSaveAs();
    	
//        OWLOntologyManager man = getModelManager().getOWLOntologyManager();
//        OWLOntology ont = getModelManager().getActiveOntology();
//        OWLOntologyFormat format = DBOntologyFormatPanel.showDialog(this, man.getOntologyFormat(ont));
//        if (format == null) {
//            logger.warn("Please select a valid format");
//            return;
//        }
//        if(format instanceof OWLDBOntologyFormat){
//        	if(DatabaseDialogPanel.showDialog(this)){
//        		Properties prop = getDBProperty();
//        		((HGModelManagerImpl)modelManager).saveAsDB(prop);
//        	}
//        	
//        	
//        } else {
//        	UIHelper helper = new UIHelper(this);
//            File file = helper.saveOWLFile("Please select a location in which to save: " + getModelManager().getRendering(ont));
//            if (file != null) {
//                int extensionIndex = file.toString().lastIndexOf('.');
//                if (extensionIndex == -1) {
//                    file = new File(file.toString() + ".owl");
//                }
//                else if (extensionIndex != file.toString().length() - 4) {
//                    file = new File(file.toString() + ".owl");
//                }
//            }
//            if (file != null){
//                man.setOntologyFormat(ont, format);
//                IRI documentIRI = IRI.create(file.toURI());
//                man.setOntologyDocumentIRI(ont, documentIRI);
//                getModelManager().setDirty(ont);
//                newPhysicalURIs.add(file.toURI());
//                handleSave();
//            }
//            else{
//                logger.warn("No valid file specified for the save as operation - quitting");
//            }
//        }
        
    }
    
    public void dispose() {
        super.dispose();
        //HGOwlModelManagerImpl m = (HGOwlModelManagerImpl)getOWLModelManager();
        //m.get        
    }      
}