package revisor.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import bcontractor.api.ISet;
import bcontractor.base.ISets;
import bcontractor.dl.owl.OWLSentence;
import bcontractor.dl.owl.hermit.OWLHermitReasoner;
import bcontractor.kernel.Kernel;
import bcontractor.kernel.operators.BlackboxKernelOperator;


/**
 * Authors: Márcio Moretto Ribeiro and Fillipe Resina
 */

public class RevisionView extends RevisorAbstractView {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 804938953979712136L;

	/**
	 * 
	 */

	@Override
	protected Set<Set<OWLAxiom>> getAxioms(OWLModelManager manager, OWLOntology ont, OWLAxiom alpha, HashMap<String, String> options) {
		Set<Set<OWLAxiom> > kernel = null;
	
		OWLHermitReasoner reasoner = new OWLHermitReasoner(); 
		BlackboxKernelOperator<OWLSentence> blackbox = new BlackboxKernelOperator<OWLSentence>(reasoner);
		
		ISet<OWLSentence> base = ISets.empty();
		for (OWLAxiom axiom : ontology.getTBoxAxioms(false)) {
			base = base.union(new OWLSentence(axiom));
		}
		for (OWLAxiom axiom : ontology.getABoxAxioms(false)) {
			base = base.union(new OWLSentence(axiom));
		}		
		base = base.union(new OWLSentence(alpha));
		
		Kernel<OWLSentence> kernelSet =  blackbox.eval(base);
		System.out.println("Number of Kernels: "+kernelSet.getAlphaKernelCount());
		kernel = new HashSet<Set<OWLAxiom>>();
		for (ISet<OWLSentence> kernels : kernelSet) {
			Set<OWLAxiom> set = new HashSet<OWLAxiom>();
			for (OWLSentence owlSentence : kernels) {
				set.add(owlSentence.getAxiom());
			}
			kernel.add(set);
		}
		return kernel;
		
	}

	protected void axiomGroupInit() {		
    	if(axiomGroup == null)
    		axiomGroup = new AxiomGroup("Axioms");
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
