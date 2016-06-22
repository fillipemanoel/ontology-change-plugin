package revisor.ui;

import javax.swing.JCheckBox;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author Fillipe Resina
 */

public class AxiomButton extends JCheckBox {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private OWLAxiom axiom;
	
	AxiomButton(String s){
		super(s);
	}
	
	AxiomButton(String s, OWLAxiom ax){
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