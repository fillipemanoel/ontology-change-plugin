package revisor.ui.algorithms;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/** 
 * Class used to perform contractions via a Kernel function.
 * 
 * @author Fillipe Resina
 *
 */
public class Kernel {
	protected Set<Set<OWLAxiom>> kernelSet;
	
	public Kernel(){
		kernelSet = new HashSet<Set<OWLAxiom>>();
	}
	
	public void clear() {
		kernelSet.clear();
	}
	
	/**
	 * Method that compute all the elements of the kernel of B and alpha using
	 * one element of the kernel (obtained by a black-box algorithm through
	 * the method kernelElement in this class) and applying to it an adaptation
	 * of Reiter's algorithm.
	 * 
	 * @param B - the ontology (belief base) on which we will apply the contraction
	 * @param alpha - the axiom (belief) we will contract
	 * 
	 * @return kernel
	 */
	public Set< Set<OWLAxiom> > kernel(OWLOntology B, OWLAxiom alpha) throws OWLOntologyCreationException, OWLOntologyChangeException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(B);
    	manager.addOntologyChangeListener(reasoner);
		
    	Queue<Set<OWLAxiom>> queue = new LinkedList<Set<OWLAxiom>>();
    	Set<OWLAxiom> element = null;
    	Set<OWLAxiom> candidate = null;
    	Set<OWLAxiom> hn;
    
    	Set<OWLAxiom> exp = null;
		
		// If alpha is not entailed by the ontology, the contraction is empty
		if (!reasoner.isEntailed(alpha))
			return kernelSet;
		
		exp = B.getAxioms();

		element = this.kernelElement(exp, alpha);
		kernelSet.add(element);
		for(OWLAxiom axiom : element){
    		Set<OWLAxiom> set = new HashSet<OWLAxiom>();
    		set.add(axiom);
    		queue.add(set);
    	}
		//Reiter's algorithm
		while(!queue.isEmpty()) {
			hn = queue.remove();
			
			for(OWLAxiom axiom : hn) {
    			RemoveAxiom removeAxiom = new RemoveAxiom(B, axiom);
    			manager.applyChange(removeAxiom);
    		}
    		exp = B.getAxioms();
    		if(reasoner.isEntailed(alpha)) {
    			candidate = this.kernelElement(exp, alpha);
    			kernelSet.add(candidate);
    			for(OWLAxiom axiom : candidate) {
    				Set<OWLAxiom> set2 = new HashSet<OWLAxiom>();
    				set2.addAll(hn);
    				set2.add(axiom);
    				queue.add(set2);
    			}
    		}
    		
    		//Restore to the ontology the axioms removed so it can be used again
    		for(OWLAxiom axiom : hn) {
    			AddAxiom addAxiom = new AddAxiom(B, axiom);
    			manager.applyChange(addAxiom);
    		}
		}
		
		return kernelSet;
	}		
		
	/**
	 * Method that compute, for contraction, one element of the kernel of exp by alpha
	 * using the strategy expand-shrink
	 * 
	 * @param exp - a set of axioms from which we will extract one element of its kernel
	 * @param alpha - the axiom (belief) we want contract
	 * 
	 * @return X - a kernel element
	 */
	private Set<OWLAxiom> kernelElement(Set<OWLAxiom> exp, OWLAxiom alpha) throws OWLOntologyCreationException, OWLOntologyChangeException{
		
		// X is the element of the kernel set to be returned
		Set<OWLAxiom> X = new HashSet<OWLAxiom>(); 
			
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology();
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ont);
    	manager.addOntologyChangeListener(reasoner);
		
		// First Part: EXPAND
		// Adicionamos os axiomas de exp na ontologia criada até que alpha
		// seja inferido (esteja contido nas consequências)
		for (OWLAxiom axiom: exp){				
			AddAxiom addAxiom = new AddAxiom(ont, axiom);
			manager.applyChange(addAxiom);
			
			if(reasoner.isEntailed(alpha))
				break;
		}
		
		// Second Part: SHRINK
		// Para cada axioma em exp, removemo-lo do conjunto ont (se contido) e verificamos
		// se alpha não é mais válido. Nesse caso, o axioma é necessário para 
		// inferir alpha e então ele deve fazer parte de X que pertence ao kernel
		for (OWLAxiom axiom : exp){
			if(ont.containsAxiom(axiom)) {
				RemoveAxiom removeAxiom = new RemoveAxiom(ont, axiom);
				manager.applyChange(removeAxiom);
				
				if(!reasoner.isEntailed(alpha)){
					X.add(axiom);
					AddAxiom addAxiom = new AddAxiom(ont, axiom);
					manager.applyChange(addAxiom);
				}				
			}
		}
		// Nesse ponto X é um dos elementos do kernel	
		return X;	
	}
	
}