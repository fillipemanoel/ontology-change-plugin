package revisor.ui;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

/** 
 * @author Fillipe Resina
 */
public class AxiomGroup {
	protected Set<AxiomButton> buttons;
	protected String title;

	AxiomGroup(String s){
		title  = s;
		buttons = null;
	}
	
	/*AxiomGroup(String s, AxiomButton[] but){
		title  = s;
		buttons = but;
	}*/
	
	public void addAxiom(OWLAxiom alpha, String axiom){
		AxiomButton button = new AxiomButton(axiom,alpha);
		if(buttons == null)
			buttons = new HashSet<AxiomButton>();
		buttons.add(button);
	}
	
	public int length(){
		return buttons.size();
	}
	
	public Set<AxiomButton> getButtons() {
		return buttons;
	}
	public void setButtons(Set<AxiomButton> buttons) {
		this.buttons = buttons;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public void removeSelectedAxioms() {
		if(buttons == null)
			return;
		Set<AxiomButton> removed = new HashSet<AxiomButton>();
		for (AxiomButton ab : buttons){				
			if(ab != null && ab.isSelected()){
				removed.add(ab);
			}
		}
		buttons.removeAll(removed);
	}

	public void clear() {
		buttons.clear();
	}
}