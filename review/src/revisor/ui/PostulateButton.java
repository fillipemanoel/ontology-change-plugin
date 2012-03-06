package revisor.ui;

import javax.swing.JRadioButton;

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

public class PostulateButton extends JRadioButton {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected PostulateButton[] selectedButtons;
	
	protected PostulateButton[] disabledButtons;
	
	public PostulateButton[] getSelectedButtons() {
		return selectedButtons;
	}
	
	public PostulateButton[] getDisabledButtons(){
		return disabledButtons;
	}
	
	public void initializeButtons(PostulateButton[] selBut, PostulateButton[] disBut) {
		selectedButtons = selBut;
		disabledButtons = disBut;
		PostulateItemListener postItemSelected = new PostulateItemListener(this);
		addItemListener(postItemSelected);
	}

	public PostulateButton(String s){
		super(s);
	}
	
}
