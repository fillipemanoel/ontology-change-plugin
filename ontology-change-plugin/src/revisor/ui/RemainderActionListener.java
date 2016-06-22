package revisor.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;

/** 
 * @author Marcio Ribeiro and Fillipe Resina
 */
public class RemainderActionListener implements ActionListener {

	public List<KernelButton> kernelButtons; 
	
	public JButton thisButton; 
 	
	public RevisorAbstractView revisorView;
	
 	public String minimality;
	
	
	RemainderActionListener(String min, JButton mb, List<KernelButton> kb, RevisorAbstractView rw){
		minimality = min;
		kernelButtons = kb;
		thisButton = mb;
		revisorView = rw;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		if (thisButton.getText() == "choose"){
			for(KernelButton kb: kernelButtons){
				kb.setSelected(true);
			}
			thisButton.setText("unchoose");
			
			if (minimality == "fullness"){			
				for(JButton jb: revisorView.mainButtons){
					if (jb != thisButton && jb.getText() == "unchoose")
						jb.doClick();
				}
			}
			
		}
		else{
			for(KernelButton kb: kernelButtons){
				kb.setSelected(false);
			}
			thisButton.setText("choose");
		}
		
		revisorView.finishState("tenacity");
	}

}