package revisor.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class KernelItemListener implements ItemListener {
	
	RevisorAbstractView revisor;
	KernelButton kernelButton;
	
	public KernelItemListener(RevisorAbstractView rev, KernelButton kb){
		revisor = rev;
		kernelButton = kb;
	}
	
	public void itemStateChanged(ItemEvent e) {
		revisor.finishState("core retainment");
		for(KernelButton[] KB: revisor.kernelButtons){
			if (KB == null)
				break;
			for(KernelButton kb: KB){
				if(kb != null){
					if (kb.getAxiom().toString().equals(kernelButton.getAxiom().toString()))					
						kb.setSelected(kernelButton.isSelected());
				}
			}
		}
	}

}
