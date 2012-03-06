package revisor.ui;

import java.util.HashMap;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import revisor.ui.algorithms.Revision;


/**
 * Author: Márcio Moretto Ribeiro
 * http://www.cecm.usp.br/~marciomr
 * 
 * Universidade de São Paulo
 * Instituto de Matemática e Estatística
 * Date: Jul 10, 2007
 * 
 * 
 * Belief Revisor (Contraction)
 */

public class RevisionView extends RevisorAbstractView {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Set<Set<OWLAxiom>> getAxioms(OWLModelManager manager, OWLOntology ont, OWLAxiom a, HashMap<String, String> options) {
		Set<Set<OWLAxiom> > kernel = null;
	
		Revision revision = new Revision(manager, options);
		
		try {
			kernel = revision.revision(ont, a);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return kernel;
	}

	@Override
	protected void postulateGroupsInit(){
		
		postulateGroups = new PostulateGroup[6];
		
    	PostulateButton strongSuccessButton = new PostulateButton("strong success");
    	strongSuccessButton.setToolTipText("a belongs to K*a");
    	PostulateButton weakSuccessButton = new PostulateButton("weak success");
    	weakSuccessButton.setToolTipText("if a is consistent then a belongs to K*a");
    	PostulateButton noSuccessButton = new PostulateButton("no success");
    	
    	PostulateButton inclusionButton = new PostulateButton("inclusion");
    	inclusionButton.setToolTipText("K+a contains K*a");
    	
		PostulateButton coreRetButton = new PostulateButton("core retainment");
		PostulateButton relevanceButton = new PostulateButton ("relevance");
		PostulateButton tenacityButton = new PostulateButton("tenacity");
		PostulateButton inertnessButton = new PostulateButton("inerteness");
		
		PostulateButton strongConsistButton = new PostulateButton("strong consistence");
		strongConsistButton.setToolTipText("K*a is consistent");
		PostulateButton weakConsistButton = new PostulateButton("weak consistence");
		weakConsistButton.setToolTipText("If a is consistent then K*a is consistent");
		
		PostulateButton noUniformityButton = new PostulateButton("no uniformity");
		PostulateButton internalExchangeButton = new PostulateButton("internal exchange");
		internalExchangeButton.setToolTipText("If a and b belongs to K then K*a = K*b");
		PostulateButton weakUniformityButton = new PostulateButton("weak uniformity");
		
		PostulateButton preExpansionButton = new PostulateButton("pre-expansion");
		preExpansionButton.setToolTipText("(K+a)*a = K*a");
		
		PostulateButton[] strongSuccessSelected = {weakConsistButton, noUniformityButton};
		strongSuccessButton.initializeButtons(strongSuccessSelected, null);
		
		PostulateButton[] weakSuccessSelected = {strongConsistButton, noUniformityButton};
		weakSuccessButton.initializeButtons(weakSuccessSelected, null);
		
		PostulateButton[] noSuccessSelected = {strongConsistButton, internalExchangeButton};
		PostulateButton[] noSuccessDisabled = {noUniformityButton, weakUniformityButton};
		noSuccessButton.initializeButtons(noSuccessSelected, noSuccessDisabled);
		
		strongConsistButton.setEnabled(false);		
		weakConsistButton.setEnabled(false);
		
		internalExchangeButton.setEnabled(false);
		
		// Default Selection
		strongSuccessButton.setSelected(true); 
		coreRetButton.setSelected(true);
		
		PostulateButton[] consistButtons = {strongConsistButton, weakConsistButton};
		PostulateButton[] successButtons = {strongSuccessButton, weakSuccessButton, noSuccessButton};
		PostulateButton[] uniformityButtons = {internalExchangeButton, noUniformityButton, weakUniformityButton};
		PostulateButton[] minimalityButtons = {coreRetButton, relevanceButton, tenacityButton, inertnessButton};
		
		postulateGroups[0] = new PostulateGroup("Success", successButtons);
		postulateGroups[1] = new PostulateGroup("Inclusion", inclusionButton);
		postulateGroups[2] = new PostulateGroup("Minimality", minimalityButtons);
		postulateGroups[3] = new PostulateGroup("Consistence", consistButtons);
		postulateGroups[4] = new PostulateGroup("Uniformity", uniformityButtons);
		postulateGroups[5] = new PostulateGroup("Pre-expansion", preExpansionButton);
	}
	
	protected JPanel emptyKernelMessage(OWLAxiom a){
		JPanel panel = new JPanel();
		JLabel label = new JLabel("No inconsistences");
		panel.add(label);
		
		return panel;
	}
}
