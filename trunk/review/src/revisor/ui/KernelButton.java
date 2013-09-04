package revisor.ui;

import javax.swing.JCheckBox;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * Authors: Márcio Moretto Ribeiro and Fillipe Resina
 */

public class KernelButton extends JCheckBox {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OWLAxiom axiom;
	
	KernelButton(String s){
		super(s);
	}
	
	KernelButton(String s, OWLAxiom ax){
		super(s);
		axiom = ax;
	}
	
	public OWLAxiom getAxiom() {
		return axiom;
	}

	public void setAxiom(OWLAxiom axiom) {
		this.axiom = axiom;
	}
	
}
