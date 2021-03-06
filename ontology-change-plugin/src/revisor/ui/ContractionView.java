package revisor.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import revisor.ui.algorithms.MultipleContraction;

import bcontractor.api.ISet;
import bcontractor.base.ISets;
import bcontractor.dl.owl.OWLSentence;
import bcontractor.dl.owl.hermit.OWLHermitReasoner;
import bcontractor.kernel.operators.BlackboxKernelOperator;
import bcontractor.kernel.Kernel;

/**
 * @author Márcio Ribeiro and Fillipe Resina
 */

public class ContractionView extends RevisorAbstractView {
	
	private static final long serialVersionUID = -60803185188671886L;

	@Override
	protected Set<Set<OWLAxiom>> getAxioms(OWLModelManager manager, OWLOntology ontology, HashMap<String, String> options) {
		Set<Set<OWLAxiom> > kernel = null;
		
		/*OWLHermitReasoner reasoner = new OWLHermitReasoner(); 
		BlackboxKernelOperator<OWLSentence> blackbox = new BlackboxKernelOperator<OWLSentence>(reasoner);*/
		
		/*ISet<OWLSentence> base = ISets.empty();
		for (OWLAxiom axiom : ontology.getTBoxAxioms(false)) {
			base = base.union(new OWLSentence(axiom));
		}
		for (OWLAxiom axiom : ontology.getABoxAxioms(false)) {
			base = base.union(new OWLSentence(axiom));
		}*/
		MultipleContraction multiCont = new MultipleContraction(options);
		Set<OWLAxiom> removed = new HashSet<OWLAxiom>();
		for(AxiomButton ab : axiomGroup.getButtons())
			removed.add(ab.getAxiom());
		kernel = multiCont.contraction(ontology, removed);
		
		/*Kernel<OWLSentence> kernelSet =  multiCont.contraction(ontology, removed);
		kernel = new HashSet<Set<OWLAxiom>>();
		for (ISet<OWLSentence> kernels : kernelSet) {
			Set<OWLAxiom> set = new HashSet<OWLAxiom>();
			for (OWLSentence owlSentence : kernels) {
				set.add(owlSentence.getAxiom());
			}
			kernel.add(set);
		}*/
		
		return kernel;
	}

	@Override
	protected void axiomGroupInit() {		
    	if(axiomGroup == null)
    		axiomGroup = new AxiomGroup("Axioms");
	}
	
	@Override
	protected void postulateGroupsInit() {
		
    	postulateGroups = new PostulateGroup[5];
		
    	PostulateButton successButton = new PostulateButton("success");
    	successButton.setToolTipText("a does not belong to K-a");
    	
    	PostulateButton inclusionButton = new PostulateButton("inclusion");
		inclusionButton.setToolTipText("K contains K-a");
    	
		PostulateButton coreRetButton = new PostulateButton("core retainment");
		
		PostulateButton relevanceButton = new PostulateButton("relevance");
		
//		PostulateButton coreIdButton = new PostulateButton("core identity");
//		
//		PostulateButton meetIdButton = new PostulateButton("fullness");
		
		PostulateButton weakUniformityButton = new PostulateButton("weak uniformity");
		
		PostulateButton packageButton = new PostulateButton("Package");
		PostulateButton choiceButton = new PostulateButton("Choice");
		
		PostulateButton[] minimalityButtons = {coreRetButton, relevanceButton};
		PostulateButton[] multipleTypeButtons = {packageButton,choiceButton};	
		
		// Default Selected
		coreRetButton.setSelected(true); 
		
		postulateGroups[0] = new PostulateGroup("Success", successButton);
		postulateGroups[1] = new PostulateGroup("Inclusion", inclusionButton);
		postulateGroups[2] = new PostulateGroup("Minimality Type", minimalityButtons);
		postulateGroups[3] = new PostulateGroup("Uniformity", weakUniformityButton);
		postulateGroups[4] = new PostulateGroup("Multiple Contraction Type", multipleTypeButtons);
	}
	
	protected JPanel emptyKernelMessage(OWLAxiom a){
		
		JPanel panel = new JPanel();
		JLabel label = new JLabel("None is implied");
		label.setForeground(Color.RED);
		panel.add(label);
		
		return panel;
	}

}
