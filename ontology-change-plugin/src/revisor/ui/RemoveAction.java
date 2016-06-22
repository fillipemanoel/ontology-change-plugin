package revisor.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/** 
 * @author Fillipe Resina
 */
public class RemoveAction implements ActionListener{
	private RevisorAbstractView revisorView;
	
	protected RemoveAction(RevisorAbstractView revView){
		revisorView = revView;
	}
	
	public void actionPerformed(ActionEvent e) {
		revisorView.removeAxioms();
		revisorView.postulatesGUI();		
	}
}