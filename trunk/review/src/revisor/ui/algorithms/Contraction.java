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
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import org.semanticweb.owlapi.model.RemoveAxiom;

import aterm.ATermAppl;

import com.clarkparsia.pellet.owlapiv3.AxiomConverter;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/** 
 * Class used to perform contractions.
 * 
 * @author Fillipe Resina
 *
 */
public class Contraction {
	
	protected String MinimalityType = "core retainment"; 
	
	protected Set<Set<OWLAxiom>> remainderSets;
	
	protected Set<Set<OWLAxiom>> kernel;
	
	protected Set<Set<OWLAxiom>> cut;

	
	public Contraction(){
		remainderSets = new HashSet<Set<OWLAxiom>>();
		kernel = new HashSet<Set<OWLAxiom>>();
		cut = new HashSet<Set<OWLAxiom>>();
	}
	/**
	 * Constructor of the class Contraction. It receives a hash map (options) of strings.
	 * This map tells the class which postulates of contraction it must satisfy.
	 *  
	 * @param options
	 */
	public Contraction(HashMap<String, String> options){
		this();
		MinimalityType = options.get("MinimalityType");
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
	
	public Set<Set<OWLAxiom>> contraction(OWLOntology B, OWLAxiom alpha){
		try {
			kernel(B, alpha);
			if(MinimalityType == "core retainment")
				return kernel;
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
    	boolean haveToContinue = false;
    
    	Set<OWLAxiom> exp = null;
		
		// If alpha is not entailed in the ontology so the contraction is done
		if (!reasoner.isEntailed(alpha))
			return kernel;
		
//		exp = convertExplanation(factory, converter, reasoner.getKB().getExplanationSet());
		exp = B.getAxioms();

		element = this.kernelElement(exp, alpha);
		kernel.add(element);
		for(OWLAxiom axiom : element){
    		Set<OWLAxiom> set = new HashSet<OWLAxiom>();
    		set.add(axiom);
    		queue.add(set);
    	}
		//Reiter's algorithm
		while(!queue.isEmpty()) {
			hn = queue.remove();
			
			haveToContinue = false;
			for(Set<OWLAxiom> set : cut) {
				//Check if there is an element of cut that is in hn
    			if(hn.containsAll(set)) { 
    				haveToContinue = true;
    				break;
    			}
			}
    		if(haveToContinue)
    			continue;
    		
    		for(OWLAxiom axiom : hn) {
    			RemoveAxiom removeAxiom = new RemoveAxiom(B, axiom);
    			manager.applyChange(removeAxiom);
    		}
    		exp = B.getAxioms();
    		if(reasoner.isEntailed(alpha)) {
    			candidate = this.kernelElement(exp, alpha);
    			kernel.add(candidate);
    			for(OWLAxiom axiom : candidate) {
    				Set<OWLAxiom> set2 = new HashSet<OWLAxiom>();
    				set2.addAll(hn);
    				set2.add(axiom);
    				queue.add(set2);
    			}
    		}
    		else cut.add(hn);
    		
    		//Restore to the ontology the axioms removed so it can be used again
    		for(OWLAxiom axiom : hn) {
    			AddAxiom addAxiom = new AddAxiom(B, axiom);
    			manager.applyChange(addAxiom);
    		}
		}
		
		return kernel;
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
		
		// X é um elemento do kernel
		Set<OWLAxiom> X = new HashSet<OWLAxiom>(); 
			
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = manager.createOntology(IRI.create("kernel.owl"));
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
    	boolean haveToContinue = false;
		
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
			
			haveToContinue = false;
			for(Set<OWLAxiom> set : cut) {
				//Check if there is an element of cut that is in hn
    			if(hn.containsAll(set)) { 
    				haveToContinue = true;
    				break;
    			}
			}
    		if(haveToContinue)
    			continue;
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
    		else cut.add(hn);
    		
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
