package revisor.ui.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

//TODO arrumar o caso do sucesso para partial meet

/**
 * Class used to perform revisions.
 * 
 * Author: Fillipe Resina
 */

public class Revision {
	
	protected String UniformityType = "no uniformity";	
	protected String SuccessType = "strong success";
	protected String MinimalityType = "core retainment";
	
	protected Set<Set<OWLAxiom>> kernel;
	protected Set<Set<OWLAxiom>> remainderSets;
	protected Set<Set<OWLAxiom>> cut;
	
	protected OWLModelManager manager;

	public Revision(OWLModelManager man){
		manager = man;
		remainderSets = new HashSet<Set<OWLAxiom>>();
		kernel = new HashSet<Set<OWLAxiom>>();
	}
	
	public Revision(OWLModelManager man, HashMap<String, String> options){
		this(man);
		if (options != null){
			if(options.containsKey("Success")){
				SuccessType = options.get("Success");
			}
			if(options.containsKey("Uniformity")){
				UniformityType = options.get("Uniformity");
			}
			if(options.containsKey("Minimality Type")){
				MinimalityType = options.get("Minimality Type");
			}
		}
	}
	
	public void setManager(OWLModelManager manager) {
		this.manager = manager;
	}

	public Set< Set <OWLAxiom> > revision(OWLOntology B, Set<OWLAxiom> axioms) throws OWLOntologyChangeException, OWLOntologyCreationException{
		HashMap<String, String> opt = new HashMap<String, String>();
		opt.put("MinimalityType", MinimalityType);
		
		OWLAxiom alpha = null;
		for(OWLAxiom ax:axioms)
			alpha = ax;
			
		AddAxiom addAxiom = new AddAxiom(B, alpha);
		manager.applyChange(addAxiom);
		
		Set<Set<OWLAxiom>> revision = this.mips(B);
		
		if (SuccessType == "no success")
			return revision;
		
		Set< Set <OWLAxiom> > containedAlpha = new HashSet< Set<OWLAxiom> >();
		Set< Set <OWLAxiom> > notContainedAlpha = new HashSet< Set<OWLAxiom> >();
		
		for (Set<OWLAxiom>X: revision){
			if (X.contains(alpha)){
				X.remove(alpha);  
				containedAlpha.add(X);
				
				if(X.isEmpty()){
					if (SuccessType == "strong success")
						X.add(alpha);
					else if (SuccessType == "weak success")
						revision.remove(X);
				}
			}
			else
				notContainedAlpha.add(X);
		}	
		
		
		//TODO testar essa parte
		if(UniformityType == "weak uniformity"){
			Set<OWLAxiom> toBeProtected = new HashSet<OWLAxiom>();
			for (Set<OWLAxiom>cA: containedAlpha){
				for (Set<OWLAxiom>nCA: notContainedAlpha){
					if (nCA.containsAll(cA)){
						nCA.removeAll(cA);
						if (nCA.size() == 1){
							for(OWLAxiom beta: nCA){
								OWLOntologyManager managerAlpha = OWLManager.createOWLOntologyManager();
								OWLOntologyManager managerBeta = OWLManager.createOWLOntologyManager();
								OWLOntology ontAlpha = managerAlpha.createOntology(IRI.create("alpha.owl"));					
								OWLOntology ontBeta = managerBeta.createOntology(IRI.create("beta.owl"));
						    	PelletReasoner reasonerAlpha = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontAlpha);
						    	PelletReasoner reasonerBeta = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ontBeta);
						    	managerAlpha.addOntologyChangeListener(reasonerAlpha);
						    	managerBeta.addOntologyChangeListener(reasonerBeta);

						    	AddAxiom addBeta = new AddAxiom(ontAlpha, beta);
								AddAxiom addAlpha = new AddAxiom(ontBeta, alpha);
								managerBeta.applyChange(addBeta);
								managerAlpha.applyChange(addAlpha);
								if (reasonerAlpha.isEntailed(beta) && reasonerBeta.isEntailed(alpha))
									toBeProtected.add(beta);
							}
						}		
					}
				}
			}
			for (Set<OWLAxiom>X: revision){
				for (OWLAxiom beta: toBeProtected)
					X.remove(beta);
			}
		}
		
		
		return revision;
	}
	
	public Set<Set<OWLAxiom>> mips(OWLOntology B){
		try {
			Set<Set<OWLAxiom>> mip = kernelMips(B);
			if(MinimalityType == "core retainment")
				return mip;
			else{
				this.reiter(B);
				return remainderSets;
			}
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Method that compute, for revision, all the elements of the kernel of B
	 * using one element of the kernel (obtained by a black-box algorithm through
	 * the method kernelMipsElement in this class) and applying to it an adaptation
	 * of Reiter's algorithm.
	 * 
	 * @param B - an inconsistent ontology for which we will compute its kernel
	 * 
	 * @return mips - the kernel of B
	 */	
	public Set< Set<OWLAxiom> > kernelMips(OWLOntology B) throws OWLOntologyChangeException, OWLOntologyCreationException{
		
		Set< Set<OWLAxiom> > mips = new HashSet<Set <OWLAxiom> >();		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(B);
    	manager.addOntologyChangeListener(reasoner);
		
    	Queue<Set<OWLAxiom>> queue = new LinkedList<Set<OWLAxiom>>();
    	Set<OWLAxiom> candidate = null;
    	Set<OWLAxiom> hn;
//    	boolean haveToContinue = false;
		
//    	AxiomConverter converter = new AxiomConverter( (KnowledgeBase) B, factory );
//		pellet.getKB().setDoExplanation( true );
//		set returned by the tracing
//		exp = convertExplanation( converter, pellet.getKB().getExplanationSet() );
//		
//		exp = convertExplanation( factory, converter, pellet.getKB().getExplanationSet() );
//		
		Set<OWLAxiom> exp = null;
//		exp = convertExplanation(factory, converter, reasoner.getKB().getExplanationSet() );
		exp = B.getAxioms();
		
		//Se a ontologia já é consistente não há o que calcular
		if (reasoner.isConsistent()){
			return mips;
		}		
		
		Set<OWLAxiom> X = new HashSet<OWLAxiom>();
		X = kernelMipsElement(exp);
		mips.add(X);
	
		for(OWLAxiom axiom : X){
    		Set<OWLAxiom> set = new HashSet<OWLAxiom>();
    		set.add(axiom);
    		queue.add(set);
    	}
		//Reiter's algorithm
		while(!queue.isEmpty()) {
			hn = queue.remove();
			
//			haveToContinue = false;
//			for(Set<OWLAxiom> set : cut) {
//				//Check if there is an element of cut that is in hn
//    			if(hn.containsAll(set)) { 
//    				haveToContinue = true;
//    				break;
//    			}
//			}
//    		if(haveToContinue)
//    			continue;
    		for(OWLAxiom axiom : hn) {
    			RemoveAxiom removeAxiom = new RemoveAxiom(B, axiom);
    			manager.applyChange(removeAxiom);
    		}
    		if(!reasoner.isConsistent()) {
    			exp = B.getAxioms();
    			candidate = this.kernelMipsElement(exp);
    			kernel.add(candidate);
    			for(OWLAxiom axiom : candidate) {
    				Set<OWLAxiom> set2 = new HashSet<OWLAxiom>();
    				set2.addAll(hn);
    				set2.add(axiom);
    				queue.add(set2);
    			}
    		}
//    		else cut.add(hn);
    		
    		//Restore to the ontology the axioms removed so it can be used again
    		for(OWLAxiom axiom : hn) {
    			AddAxiom addAxiom = new AddAxiom(B, axiom);
    			manager.applyChange(addAxiom);
    		}
		}
		
		return mips;	
	}	
	
	/**
	 * Method that compute, for revision, one element of the kernel of exp
	 * using the strategy expand-shrink
	 * 
	 * @param exp - a set of axioms from which we will extract one element of its kernel
	 * 
	 * @return X - a kernel element
	 */
	private Set<OWLAxiom> kernelMipsElement(Set<OWLAxiom> exp) throws OWLOntologyCreationException, OWLOntologyChangeException{
		// X é um elemento do kernel
		Set<OWLAxiom> X = new HashSet<OWLAxiom>(); 
			
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create("mips.owl"));
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ont);
    	manager.addOntologyChangeListener(reasoner);
	
		// First Part: EXPAND
		// Adicionamos os axiomas de exp na ontologia criada até que ela
		// seja inconsistente
		for (OWLAxiom axiom: exp){
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
			if (!reasoner.isConsistent()){
				break;
			}
		}
		
		// Second Part: SHRINK
		// Para cada axioma em exp, removemo-lo da ontologia ont (se contido) e
		// verificamos se ela não é mais inconsistente. Nesse caso, o axioma é 
		// necessário para gerar a inconsistência e, portanto, deve fazer parte
		// de X, que pertence ao kernel
		for (OWLAxiom axiom : exp){
			if(ont.containsAxiom(axiom)) {
				RemoveAxiom removeAxiom = new RemoveAxiom(ont, axiom);
				manager.applyChange(removeAxiom);
				if (reasoner.isConsistent()){
					X.add(axiom);
					AddAxiom addAxiom = new AddAxiom(ont, axiom);
					manager.applyChange(addAxiom);
				}	
			}
		}	
		return X;	
	}
		
	private void reiter(OWLOntology B){
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	
		try {
			for(Set<OWLAxiom> set : cut) {
				for(OWLAxiom axiom : set) {
					RemoveAxiom removeAxiom = new RemoveAxiom(B, axiom);
					manager.applyChange(removeAxiom);
				}
				remainderSets.add(B.getAxioms());
				for(OWLAxiom axiom : set) {
					AddAxiom addAxiom = new AddAxiom(B, axiom);
					manager.applyChange(addAxiom);
				}
			}
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}