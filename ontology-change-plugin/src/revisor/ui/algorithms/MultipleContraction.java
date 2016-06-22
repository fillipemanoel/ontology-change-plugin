package revisor.ui.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.RemoveAxiom;

import aterm.ATermAppl;
import com.clarkparsia.pellet.owlapiv3.AxiomConverter;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/** 
 * Class with algorithms to perform multiple contractions.
 * 
 * @author Fillipe Resina
 *
 */
public class MultipleContraction {
	
	protected String MinimalityType = "core retainment"; 
	protected String MultContType = "Package";
	
	protected Set<Set<OWLAxiom>> remainderSets;
	
	protected Set<Set<OWLAxiom>> kernel;
	
	protected Set<Set<OWLAxiom>> cut;

	
	public MultipleContraction(){
		remainderSets = new HashSet<Set<OWLAxiom>>();
		kernel = new HashSet<Set<OWLAxiom>>();
		cut = new HashSet<Set<OWLAxiom>>();
	}
	/**
	 * Constructor of the class MultipleContraction. It receives a hash map (options) of strings.
	 * This map tells the class which postulates of contraction it must satisfy.
	 *  
	 * @param options
	 */
	public MultipleContraction(HashMap<String, String> options){
		this();
		MinimalityType = options.get("Minimality Type");
		MultContType = options.get("Multiple Contraction Type");
	}
	
	/**
	 * Getter method of the kernel. The kernel is a set of sets of OWLAxioms
	 * 
	 * @return kernel
	 */
	public Set<Set<OWLAxiom>> getKernel() {
		return kernel;
	}
	
	/**
	 * Setter method of the kernel.
	 * 
	 * @param kernel
	 */
	public void setKernel(Set<Set<OWLAxiom>> kernel) {
		this.kernel = kernel;
	}
	
	/**
	 * Get the MinimalityType that can be: "core retainment", "relevance"...
	 * @return MinimalityType
	 */
	public String getMinimalityType() {
		return MinimalityType;
	}
	
	/**
	 * Set the MinimalityType.
	 * @param minimalityType
	 */
	public void setMinimalityType(String minimalityType) {
		MinimalityType = minimalityType;
	}
	
	public Set<Set<OWLAxiom>> getRemainderSets() {
		return remainderSets;
	}

	public void setRemainderSets(Set<Set<OWLAxiom>> remainderSets) {
		this.remainderSets = remainderSets;
	}
	
	protected static Set<OWLAxiom> convertExplanation(OWLDataFactory factory, AxiomConverter converter,
			Set<ATermAppl> explanation) {
		if( explanation == null || explanation.isEmpty() )
			throw new OWLRuntimeException( "No explanation computed" );

		Set<OWLAxiom> result = new HashSet<OWLAxiom>();
		
		for( ATermAppl c : explanation ) {
			try{
				OWLAxiom axiom = null;
				// For some unidentified reason convert(Aterm) can not convert an individual to an axiom
				// The following lines fix this problem
				if (c.getName().equals("sub") && ((ATermAppl)c.getChildAt(0)).getName().equals("value")){
					OWLIndividual ind = factory.getOWLNamedIndividual(IRI.create(c.getChildAt(0).getChildAt(0).toString()));
					Set<OWLIndividual> set = new HashSet<OWLIndividual>();
					set.add(ind);
					OWLObjectOneOf nominal = factory.getOWLObjectOneOf(set);
					OWLClass cls = factory.getOWLClass(IRI.create(c.getChildAt(1).toString()));
					axiom = factory.getOWLSubClassOfAxiom(nominal, cls);
				}
				else{
					axiom = converter.convert( c );
				}
				
				if( axiom == null )
					throw new OWLRuntimeException( "Cannot convert: " + c );
				result.add( axiom );
			}
			catch(Exception e){
				System.out.println("Problem converting " + c + " in method convertExplanation in class Contraction");			
			}
		}

		return result;
	}
	
	public Set<Set<OWLAxiom>> contraction(OWLOntology B, Set<OWLAxiom> C){
		try {
			if(MultContType == "Package") {
				kernelPackage(B,C);
				return kernel;
			}
			else{
				kernelChoice(B,C);
				return kernel;	
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
	 * Method that compute all the elements of the kernel set of B by C using
	 * one element of the kernel (obtained by a black-box algorithm through
	 * the method kernelPackageElement in this class) via package contraction  
	 *  
	 * @param B - the ontology (belief base) on which we will apply the contraction
	 * @param C - the set of axioms (beliefs) by which we will contract
	 * 
	 * @return kernel
	 */
	public Set< Set<OWLAxiom> > kernelPackage(OWLOntology B, Set<OWLAxiom> C) throws OWLOntologyCreationException, OWLOntologyChangeException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(B);
		//Reasoner reasoner = new Reasoner(B);
		manager.addOntologyChangeListener(reasoner);
				
    	Queue<Set<OWLAxiom>> queue = new LinkedList<Set<OWLAxiom>>();
    	Set<OWLAxiom> element = null;
    	Set<OWLAxiom> candidate = null;
    	Set<OWLAxiom> hn;
    	boolean entails = false;
    	boolean necessary = false;
    
    	Set<OWLAxiom> exp = null;
		
		// If none of C is entailed by the ontology the contraction is done
    	for(OWLAxiom axiom : C)
    		if (reasoner.isEntailed(axiom)) {
    			entails = true;
    			break;
    		}
    	if(!entails) return kernel;    		
		
		exp = B.getAxioms();

		element = this.kernelPackageElement(exp,C);
		kernel.add(element);
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
    		necessary = true;
    		for(Set<OWLAxiom> aKernel : kernel)
    			if(exp.containsAll(aKernel)){
    				necessary = false;
    				break;
    			}
    		if(!necessary) {
    			for(OWLAxiom axiom : hn) {
        			AddAxiom addAxiom = new AddAxiom(B, axiom);
        			manager.applyChange(addAxiom);
        		}
    			continue;
    		}
    		entails = false;
    		for(OWLAxiom axiom : C) {
    			if (reasoner.isEntailed(axiom)) {
        			entails = true;
        			break;
        		}
    		}
    		if(entails) {
    			candidate = this.kernelPackageElement(exp, C);
    			kernel.add(candidate);
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
		
		return kernel;
	}		
		
	/**
	 * Method that compute, for contraction, one element of the kernel of exp by C
	 * using the strategy expand-shrink (for package contraction)
	 * 
	 * @param exp - a set of axioms from which we will extract one element of its kernel
	 * @param C - the set of axioms (beliefs) by which we want contract
	 * 
	 * @return X - a kernel element
	 */
	private Set<OWLAxiom> kernelPackageElement(Set<OWLAxiom> exp, Set<OWLAxiom> C) throws OWLOntologyCreationException, OWLOntologyChangeException{
		
		// X é um elemento do kernel
		Set<OWLAxiom> X = new HashSet<OWLAxiom>(); 
			
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology();
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ont);
		//Reasoner reasoner = new Reasoner(ont);
		manager.addOntologyChangeListener(reasoner);
		
		// First Part: EXPAND
		// Adicionamos os axiomas de exp na ontologia criada até que algo de C
		// seja inferido (esteja contido nas consequências)
		for (OWLAxiom axiom: exp){				
			manager.addAxiom(ont, axiom);
			
			boolean entails = false;
			for(OWLAxiom alpha : C) {
				if (reasoner.isEntailed(alpha)) {
	    			entails = true;
	    			break;
	    		}
			}
			if(entails)	break;
		}
		// Second Part: SHRINK
		// Para cada axioma em ont, removemo-lo e verificamos
		// se C não é mais implicado. Nesse caso, o axioma é necessário para 
		// inferir C e então ele deve fazer parte de X que pertence ao kernel
		for (OWLAxiom axiom : ont.getAxioms()){
			RemoveAxiom removeAxiom = new RemoveAxiom(ont, axiom);
			manager.applyChange(removeAxiom);
			boolean entails = false;
			for(OWLAxiom alpha : C) {
				if (reasoner.isEntailed(alpha)) {
	    			entails = true;
	    			break;
	    		}
			}
			if(!entails) {
				X.add(axiom);
				AddAxiom addAxiom = new AddAxiom(ont, axiom);
				manager.applyChange(addAxiom);
			}
		}
		// Nesse ponto X é um dos elementos do kernel	
		return X;	
	}
	
	/**
	 * Method that compute all the elements of the kernel of B and C using
	 * one element of the kernel (obtained by a black-box algorithm through
	 * the method kernelChoiceElement in this class) via choice contraction 
	 * 
	 * @param B - the ontology (belief base) on which we will apply the contraction
	 * @param C - the set of axioms (beliefs) by which we will contract
	 * 
	 * @return kernel
	 */
	public Set< Set<OWLAxiom> > kernelChoice(OWLOntology B, Set<OWLAxiom> C) throws OWLOntologyCreationException, OWLOntologyChangeException{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(B);
		//Reasoner reasoner = new Reasoner(B);
		manager.addOntologyChangeListener(reasoner);
				
    	Queue<Set<OWLAxiom>> queue = new LinkedList<Set<OWLAxiom>>();
    	Set<OWLAxiom> element = null;
    	Set<OWLAxiom> candidate = null;
    	Set<OWLAxiom> hn;
    	boolean entails = true;
//    	boolean necessary = false;
    
    	Set<OWLAxiom> exp = null;
		
		// If something of C is not entailed by the ontology, the contraction is empty
    	for(OWLAxiom axiom : C)
    		if (!reasoner.isEntailed(axiom)) {
    			entails = false;
    			break;
    		}
    	if(!entails) return kernel;    		
		
		exp = B.getAxioms();

		element = this.kernelChoiceElement(exp,C);
		kernel.add(element);
		for(OWLAxiom axiom : element){
    		Set<OWLAxiom> set = new HashSet<OWLAxiom>();
    		set.add(axiom);
    		queue.add(set);
    	}

		while(!queue.isEmpty()) {
			hn = queue.remove();
    		manager.removeAxioms(B,hn);    		
    		exp = B.getAxioms();
    		
//    		necessary = true;
//    		for(Set<OWLAxiom> aKernel : kernel)
//    			if(exp.containsAll(aKernel)){
//    				necessary = false;
//    				break;
//    			}
//    		if(!necessary) {
//    			for(OWLAxiom axiom : hn) {
//        			AddAxiom addAxiom = new AddAxiom(B, axiom);
//        			manager.applyChange(addAxiom);
//        		}
//    			continue;
//    		}
    		
    		if(reasoner.isEntailed(C)) {
    			candidate = this.kernelChoiceElement(exp, C);
    			kernel.add(candidate);
    			for(OWLAxiom axiom : candidate) {
    				Set<OWLAxiom> set2 = new HashSet<OWLAxiom>();
    				set2.addAll(hn);
    				set2.add(axiom);
    				queue.add(set2);
    			}
    		}
    		//Restore to the ontology the axioms removed so it can be used again
    		manager.addAxioms(B,hn);
		}
		
		return kernel;
	}		
		
	/**
	 * Method that compute, for contraction, one element of the kernel of exp by C
	 * using the strategy expand-shrink
	 * 
	 * @param exp - a set of axioms from which we will extract one element of its kernel
	 * @param C - the set of axioms (beliefs) by which we want contract
	 * 
	 * @return X - a kernel element
	 */
	private Set<OWLAxiom> kernelChoiceElement(Set<OWLAxiom> exp, Set<OWLAxiom> C) throws OWLOntologyCreationException, OWLOntologyChangeException{
		
		// X é um elemento do kernel
		Set<OWLAxiom> X = new HashSet<OWLAxiom>(); 
			
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology();
		PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(ont);
		//Reasoner reasoner = new Reasoner(ont);
		manager.addOntologyChangeListener(reasoner);
		
		// First Part: EXPAND
		// Adicionamos os axiomas de exp na ontologia criada até que todo o C
		// seja inferido (esteja contido nas consequências)
		for (OWLAxiom axiom: exp){				
			manager.addAxiom(ont, axiom);
			if (reasoner.isEntailed(C))
	    		break;
		}
		// Second Part: SHRINK
		// Para cada axioma em ont, removemo-lo e verificamos
		// se C não é mais implicado. Nesse caso, o axioma é necessário para 
		// inferir C e então ele deve fazer parte de X que pertence ao kernel
		for (OWLAxiom axiom : ont.getAxioms()){
			manager.removeAxiom(ont, axiom);
			if (!reasoner.isEntailed(C)) {
				X.add(axiom);
				manager.addAxiom(ont,axiom);
			}
		}
		// Nesse ponto X é um dos elementos do kernel	
		return X;	
	}
	
}