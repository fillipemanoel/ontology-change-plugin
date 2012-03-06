package revisor.ui;

import javax.swing.JRadioButton;

/**
 * Authors: Márcio Moretto Ribeiro and Fillipe Resina
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
