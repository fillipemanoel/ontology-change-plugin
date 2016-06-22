package revisor.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.ScrollPane;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.protege.editor.owl.ui.view.AbstractActiveOntologyViewComponent;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Márcio Ribeiro and Fillipe Resina
 */

public abstract class RevisorAbstractView extends
		AbstractActiveOntologyViewComponent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected OWLOntology ontology;
    
    protected ExpressionEditor<OWLClassAxiom> editor;  
    
    protected KernelButton[][] kernelButtons;
    
    protected OWLModelManager manager;
    
    protected JButton finishButton, previousButton;
    
    protected OWLAxiom alpha;
    
	protected PostulateGroup[] postulateGroups;
	
	protected AxiomGroup axiomGroup;
    
    protected List<JButton> mainButtons; 
    
    protected abstract void postulateGroupsInit();
    protected abstract void axiomGroupInit();
    
    protected abstract Set<Set<OWLAxiom> > getAxioms(OWLModelManager man, OWLOntology ont, HashMap<String, String> options);
    
    protected abstract JPanel emptyKernelMessage(OWLAxiom a);
    
    // create the GUI
    public void initialiseOntologyView() throws Exception {  
    	manager = getOWLModelManager();
    	ontology = manager.getActiveOntology();
    	OWLEditorKit editorKit = getOWLEditorKit();
    	
    	editor = new ExpressionEditor<OWLClassAxiom>(editorKit, manager.getOWLExpressionCheckerFactory().getClassAxiomChecker());
    	postulatesGUI();
    }
    
    // called automatically when the global selection changes
    protected void updateView(OWLOntology activeOntology) {
    	ontology = activeOntology;
    }

    // remove any listeners and perform tidyup (none required in this case)
	protected void disposeOntologyView() {
		// TODO Auto-generated method stub
	}	
	
	private JPanel buttonGroupPanel(PostulateGroup postulateGroup){
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
    	panel.setBorder(BorderFactory.createTitledBorder(postulateGroup.getTitle()));
    
  		ButtonGroup group = new ButtonGroup();    	
   		for(AbstractButton button: postulateGroup.getButtons()){
   			panel.add(button);	
   			group.add(button);
   		} 
   		 
    	return panel; 
	}
	
	private JPanel axiomGroupPanel(AxiomGroup axiomGroup){
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(0,1));
    	panel.setBorder(BorderFactory.createTitledBorder(axiomGroup.getTitle()));
    
  		ButtonGroup group = new ButtonGroup();    
  		if(axiomGroup.getButtons() != null)
  			for(AbstractButton button : axiomGroup.getButtons()) {
  				panel.add(button);	
  				group.add(button);
  			}
   		 
    	return panel; 
	}
	
	protected void postulatesGUI(){
		removeAll();
		setLayout(new BorderLayout());
		
    	JButton done = new JButton("Done");
    	KernelAction kernelAction = new KernelAction(this);
    	done.addActionListener(kernelAction);
    	
    	JButton add = new JButton("Add");
    	AxiomAction axiomAction = new AxiomAction(this);
    	add.addActionListener(axiomAction);
    	
    	JButton remove = new JButton("Remove");
    	RemoveAction removeAction = new RemoveAction(this);
    	remove.addActionListener(removeAction);
    	
    	JPanel nextPanel = new JPanel();
    	nextPanel.add(add);
    	nextPanel.add(remove);
    	nextPanel.add(done);
    	
    	JPanel postulatesAxiomsPanel = new JPanel();
    	postulatesAxiomsPanel.setLayout(new GridLayout(0,1));
    	
    	ScrollPane scrollPane = new ScrollPane();
    	scrollPane.add(postulatesAxiomsPanel);
    	
    	postulateGroupsInit();
    	axiomGroupInit();
    	
    	for (PostulateGroup postulateGroup: postulateGroups){
    		postulatesAxiomsPanel.add(buttonGroupPanel(postulateGroup));
    	}
    	postulatesAxiomsPanel.add(axiomGroupPanel(axiomGroup));
    
    	JPanel mainPanel = new JPanel();
    	mainPanel.setLayout(new BorderLayout());
    	mainPanel.add(scrollPane, BorderLayout.CENTER);
    	editor.refreshComponent();
    	mainPanel.add(editor, BorderLayout.SOUTH);
   
    	add(mainPanel, BorderLayout.CENTER);
    	add(nextPanel, BorderLayout.SOUTH);
  
    	repaint();
	}
	
	public void addAxiom(OWLAxiom alpha, String axiom) {
		axiomGroup.addAxiom(alpha,axiom);
		postulatesGUI();
	}
	
	public void removeAxioms() {
		axiomGroup.removeSelectedAxioms();
	}
	
	public void clearAxioms() {
		axiomGroup.clear();
	}
	
	public void clearKernels() {
		kernelButtons = null;
	}
	
	private JPanel kernelPanel(Set<Set <OWLAxiom> > kernel, String iri){
		JPanel kernelPanel = new JPanel();
		kernelPanel.setLayout(new GridLayout(0,1));
		int i = 0;
		
		kernelButtons = new KernelButton[kernel.size()][50];
		
		for (Set<OWLAxiom> X : kernel){
			int j = 0;
			JPanel kPanel = new JPanel();
			kPanel.setLayout(new GridLayout(0,1));
			kPanel.setBorder(BorderFactory.createTitledBorder("Kernel " + (i+1)));
			for (OWLAxiom ax : X){
				KernelButton kernelButton = new KernelButton((ax.toString().replace(iri, "")).replaceAll("<[#/:]", "<"), ax);
				kernelButton.setToolTipText(iri);
				if (X.size() == 1){
					kernelButton.setSelected(true);
					kernelButton.setEnabled(false);
				}
				kernelButton.addItemListener(new KernelItemListener(this, kernelButton));
				kernelButtons[i][j] = kernelButton;
				kPanel.add(kernelButton);
				j++;
			}
			kernelPanel.add(kPanel);
			i++;
		}
		
		return kernelPanel;
	}
	
	private JPanel remainderPanel (Set<Set<OWLAxiom>> remainderSet, String minimality){	
		JPanel remainderPanel = new JPanel();
		remainderPanel.setLayout(new GridLayout(0,1));
		int i = 0;
		
		kernelButtons = new KernelButton[50][50];
		
		mainButtons = new Vector<JButton>();
		
		for (Set<OWLAxiom> X : remainderSet){
			int j = 0;
			JPanel kPanel = new JPanel();
			kPanel.setLayout(new BorderLayout());
			kPanel.setBorder(BorderFactory.createTitledBorder("Remainder " + (i+1)));
			
			List<KernelButton> kButtons = new Vector<KernelButton>();
			
			JPanel buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new GridLayout(0,1));
			
			for (OWLAxiom ax : X){
				KernelButton remainderButton = new KernelButton(ax.toString(), ax);
				remainderButton.setEnabled(false);
		
				if (minimality == "core identity") 
					remainderButton.setSelected(true);
				
				buttonsPanel.add(remainderButton);
				kernelButtons[i][j] = remainderButton;
				kButtons.add(remainderButton);
				j++;
			}
			
			kPanel.add(buttonsPanel, BorderLayout.CENTER);
			if (minimality != "core identity"){
				JButton mainButton = new JButton("choose");
				mainButton.addActionListener(new RemainderActionListener(minimality, mainButton, kButtons, this));
			
				mainButtons.add(mainButton);
			
				JPanel mainButtonPanel = new JPanel();
				mainButtonPanel.add(mainButton);
			
				kPanel.add(mainButtonPanel, BorderLayout.SOUTH);
			}	
			
			remainderPanel.add(kPanel);
			i++;
		}
		
		return remainderPanel;
	}
	
	protected void axiomsGUI(Set<Set <OWLAxiom> > axioms, String minimality, IRI iri){
		removeAll();
		setLayout(new BorderLayout());
		
		JPanel centerPanel;
		
		if (axioms.isEmpty()){
			centerPanel = emptyKernelMessage(alpha);
		}
		else if (minimality == "core retainment"){
			centerPanel = kernelPanel(axioms, iri.toString());
		}
		else{
			centerPanel = remainderPanel(axioms, minimality);
		}
		
		add(centerPanel, BorderLayout.CENTER);
		
		PostulatesAction postAction = new PostulatesAction(this);
		
		finishButton = new JButton("Finish");
		finishButton.setActionCommand("finish");
		finishButton.addActionListener(postAction);
		
		previousButton= new JButton("Previous");
		previousButton.addActionListener(postAction);
		
    	JPanel nextPrevPanel = new JPanel();

    	nextPrevPanel.add(previousButton);
		nextPrevPanel.add(finishButton);
		
		add(nextPrevPanel, BorderLayout.SOUTH);
        repaint();		
	}			
	
	protected void finishState(String minimality){
		if (kernelButtons == null){
			finishButton.setEnabled(true);
			return;
		}
		
		if (minimality == "core retainment"){
			boolean flag = true;
			for(KernelButton[] KB: kernelButtons){
				if (KB[0] == null || !flag){
					break;
				}
				flag = false;
				for (KernelButton kb: KB){	
					if(kb != null && kb.isSelected()){
						flag = true;
						break;
					}
				}
			}
			finishButton.setEnabled(flag);
		}	
		else{
			for(KernelButton[] KB: kernelButtons){
				if (KB[0] == null){
					break;
				}
				for (KernelButton kb: KB){
					if(kb != null && kb.isSelected()){
						finishButton.setEnabled(true);
						return;
					}
				}
			}
			finishButton.setEnabled(false);
		}
	}
	
	public OWLAxiom getAlpha() {
		return alpha;
	}

	public void setAlpha(OWLAxiom alpha) {
		this.alpha = alpha;
	}
			
}
