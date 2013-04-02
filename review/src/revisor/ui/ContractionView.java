package revisor.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLOntology;

import revisor.ui.algorithms.Contraction;

import bcontractor.api.ISet;
import bcontractor.dl.owl.pellet;
import bcontractor.kernel.operators.BlackboxKernelOperator;
import bcontractor.dl.owl.OWLSentence;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * Authors: Márcio Moretto Ribeiro and Fillipe Resina
 */

public class ContractionView extends RevisorAbstractView {
	
	private static final long serialVersionUID = -60803185188671886L;

	@Override
	protected Set<Set<OWLAxiom>> getAxioms(OWLModelManager manager, OWLOntology ontology, OWLAxiom alpha, HashMap<String, String> options) {
		Set<Set<OWLAxiom> > kernel = null;
		
		OWLPelletReasoner reasoner = new OWLPelletReasoner(); 
		BlackboxKernelOperator<OWLSentence> blackbox = new BlackboxKernelOperator<OWLSentence>(reasoner);
		Kernel<OWLSentence> kernelSet =  blackbox.eval(base, alpha);
		OWLAxiom axiom;
		kernel = new HashSet<Set<OWLAxiom>>();
		for (ISet<OWLSentence> kernels : kernelSet) {
			Set<OWLAxiom> set = new HashSet<OWLAxiom>();
			for (OWLSentence owlSentence : kernel) {
				set.add(owlSentence.getAxiom());
			}
			kernel.add(set);
		}
		//Contraction contraction = new Contraction(options);
		//kernel = contraction.contraction(ontology, alpha);
		
		return kernel;
	}

	@Override
	protected void postulateGroupsInit() {
		
    	postulateGroups = new PostulateGroup[4];
		
    	PostulateButton successButton = new PostulateButton("success");
    	successButton.setToolTipText("a does not belong to K-a");
    	
    	PostulateButton inclusionButton = new PostulateButton("inclusion");
		inclusionButton.setToolTipText("K contains K-a");
    	
		PostulateButton coreRetButton = new PostulateButton("core retainment");
		
		PostulateButton relevanceButton = new PostulateButton("relevance");
		
		PostulateButton coreIdButton = new PostulateButton("core identity");
		
		PostulateButton meetIdButton = new PostulateButton("fullness");
		
		PostulateButton weakUniformityButton = new PostulateButton("weak uniformity");
		
		PostulateButton[] minimalityButtons = {coreRetButton, relevanceButton, coreIdButton, meetIdButton};
		
		// Default Selected
		coreRetButton.setSelected(true); 
		
		postulateGroups[0] = new PostulateGroup("Success", successButton);
		postulateGroups[1] = new PostulateGroup("Inclusion", inclusionButton);
		postulateGroups[2] = new PostulateGroup("Minimality", minimalityButtons);
		postulateGroups[3] = new PostulateGroup("Uniformity", weakUniformityButton);
		
	}
	
	protected JPanel emptyKernelMessage(OWLAxiom a){
		
		JPanel panel = new JPanel();
		JLabel label = new JLabel(a + " is not implied");
		label.setForeground(Color.RED);
		panel.add(label);
		
		return panel;
	}

}

