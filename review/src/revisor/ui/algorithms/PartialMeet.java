package revisor.ui.algorithms;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

//import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/** 
 * Class used to perform contractions via a Partial Meet function.
 * 
 * @author Fillipe Resina
 *
 */
public class PartialMeet {
	
	protected Set<Set<OWLAxiom>> remainderSet;
	
	public PartialMeet(){
		remainderSet = new HashSet<Set<OWLAxiom>>();
	}
	
	public Set<Set<OWLAxiom>> getRemainderSet() {
		return remainderSet;
	}

	public void setRemainderSet(Set<Set<OWLAxiom>> remainderSet) {
		this.remainderSet = remainderSet;
	}
	
	public Set<Set<OWLAxiom>> singleContractionPM(OWLOntology B, OWLAxiom alpha){
		try {
			partialMeet(B,alpha);
			return remainderSet;	
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
	 * Method that compute all the elements of the remainder of B by alpha C using
	 * one element of the remainder (obtained by a black-box algorithm through
	 * the method remainderElement in this class) 
	 * 
	 * @param B - the ontology (belief base) on which we will apply the contraction
	 * @param alpha - the axiom (belief) by which we will contract
	 * 
	 * @return kernel
	 */
	public Set< Set<OWLAxiom> > partialMeet(OWLOntology B, OWLAxiom alpha) throws OWLOntologyCreationException, OWLOntologyChangeException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(B);
		//Reasoner reasoner = new Reasoner(B);
		manager.addOntologyChangeListener(reasoner);
				
    	Queue<Set<OWLAxiom>> queue = new LinkedList<Set<OWLAxiom>>();
    	Set<OWLAxiom> element = null;
    	Set<OWLAxiom> diff = null;
    	Set<OWLAxiom> hn = null;    
    	Set<OWLAxiom> exp = null;
		
		// If alpha is not entailed by the ontology, the contraction is done
    	if (!reasoner.isEntailed(alpha)) {
    		return remainderSet;
    	}		
		exp = B.getAxioms();
				
		element = this.remainderElement(exp,alpha,null);
		remainderSet.add(element);
		diff = new HashSet<OWLAxiom>();
		diff.addAll(exp);
		diff.removeAll(element);
		for(OWLAxiom axiom : diff){
    		Set<OWLAxiom> set = new HashSet<OWLAxiom>();
    		set.add(axiom);
    		queue.add(set);
    	}
		
		OWLOntology ont = manager.createOntology();
		reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ont);
		
		boolean entailed;
		while(!queue.isEmpty()) {
			hn = queue.remove();
			manager.addAxioms(ont,hn);
			entailed = reasoner.isEntailed(alpha);
			manager.removeAxioms(ont,hn);
			if(entailed) 
				continue;
    		
			element = this.remainderElement(exp,alpha,hn);
    		remainderSet.add(element);
    		diff.addAll(exp);
    		diff.removeAll(element);
    		for(OWLAxiom axiom : diff){
        		Set<OWLAxiom> set = new HashSet<OWLAxiom>();
        		set.addAll(hn);
        		set.add(axiom);
        		queue.add(set);
        	}
    	}
		
		return remainderSet;
	}		
		
	/**
	 * Method that computes, for contraction, one element of the remainder set of a
	 * belief set by alpha, starting from X.
	 * 
	 * @param set - the set of axioms from which we will extract one element of its remainder set
	 * @param alpha - the axiom (belief) by which we want contract
	 * @param X - a subset of the remainder element that the method will find
	 * 
	 * @return remElem - a remainder element
	 */
	private Set<OWLAxiom> remainderElement(Set<OWLAxiom> set, OWLAxiom alpha, Set<OWLAxiom> X) throws OWLOntologyCreationException, OWLOntologyChangeException{
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology();
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ont);
		//Reasoner reasoner = new Reasoner(ont);
		manager.addOntologyChangeListener(reasoner);
		
		//remElem is the element of the remainder set to be returned
		Set<OWLAxiom> remElem = new HashSet<OWLAxiom>();
		if(!(X == null)) {
			remElem.addAll(X);
			manager.addAxioms(ont,X);
		}
		
		for (OWLAxiom axiom: set){				
			manager.addAxiom(ont, axiom);
			if (reasoner.isEntailed(alpha))
				manager.removeAxiom(ont, axiom);
			else
				remElem.add(axiom);
		}
		// At this point, remElem is one element of the remainder set	
		return remElem;	
	}

}
