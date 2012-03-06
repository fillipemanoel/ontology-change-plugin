package revisor.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.RemoveAxiom;

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

public class PostulatesAction implements ActionListener {

	private OWLOntology ontology;
	
	private KernelButton[][] kernelButtons;
	
	private OWLModelManager manager;
	
	private RevisorAbstractView revisorView;
	
	PostulatesAction(RevisorAbstractView revView){
		revisorView = revView;
		manager = revisorView.manager;
		kernelButtons = revisorView.kernelButtons;
		ontology = revisorView.ontology;
	}
	
	public void actionPerformed(ActionEvent e) {
		if("finish".equals(e.getActionCommand())){
			revisorView.postulatesGUI();
			if (kernelButtons == null)
				return;
			for(KernelButton[] KB: kernelButtons){
				for (KernelButton kb: KB){				
					if(kb != null && kb.isSelected()){
						manager.applyChange(new RemoveAxiom(ontology, kb.getAxiom()));
					}
				}
			}	
		
		}
		revisorView.postulatesGUI();		
	}
}
