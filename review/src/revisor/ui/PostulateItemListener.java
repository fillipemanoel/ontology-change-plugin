package revisor.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

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

public class PostulateItemListener implements ItemListener {

	PostulateButton postulateButton;
	
	PostulateButton[] selectedButtons;
	
	PostulateButton[] disabledButtons;
	
	public PostulateItemListener(PostulateButton postBut){
		postulateButton = postBut;
		selectedButtons = postulateButton.getSelectedButtons();
		disabledButtons = postulateButton.getDisabledButtons();
	}

	public void itemStateChanged(ItemEvent e) {
		if (postulateButton.isSelected()){
			if (selectedButtons != null){
				for (PostulateButton p: selectedButtons){
					p.setSelected(true);
				}
			}
			if (disabledButtons != null){
				for ( PostulateButton p: disabledButtons){
					p.setEnabled(false);
				}
			}
		}
		else{
			if (disabledButtons != null){
				for ( PostulateButton p: disabledButtons){
					p.setEnabled(true);
				}
			}
		}
	}

}
