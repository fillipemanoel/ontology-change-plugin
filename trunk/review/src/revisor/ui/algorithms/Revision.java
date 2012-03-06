package revisor.ui.algorithms;

import java.util.HashMap;
import java.util.HashSet;
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

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

//TODO arrumar o caso do sucesso para partial meet

/**
 * Author: Márcio Moretto Ribeiro
 * http://www.cecm.usp.br/~marciomr
 * 
 * Universidade de São Paulo
 * Instituto de Matemática e Estatística
 * Date: Jul 10, 2007
 * 
 * 
 * Belief Revisor (Contraction)
 */

public class Revision {
	
	protected String UniformityType = "no uniformity";	
	protected String SuccessType = "strong success";
	protected String MinimalityType = "core retainment";
	
	protected OWLModelManager manager;

	public Revision(OWLModelManager man){
		manager = man;
	}
	
	public Revision(OWLModelManager man, HashMap<String, String> options){
		this(man);
		if (options != null){
			if(options.containsKey("SuccessType")){
				SuccessType = options.get("SuccessType");
			}
			if(options.containsKey("UniformityType")){
				UniformityType = options.get("UniformityType");
			}
			if(options.containsKey("MinimalityType")){
				MinimalityType = options.get("MinimalityType");
			}
		}
	}
	
	public void setManager(OWLModelManager manager) {
		this.manager = manager;
	}

	public Set< Set <OWLAxiom> > revision(OWLOntology B, OWLAxiom alpha) throws OWLOntologyChangeException, OWLOntologyCreationException{
		HashMap<String, String> opt = new HashMap<String, String>();
		opt.put("MinimalityType", MinimalityType);
		
		AddAxiom addAxiom = new AddAxiom(B, alpha);
		manager.applyChange(addAxiom);
		
		Contraction contraction = new Contraction(opt);
		Set<Set<OWLAxiom>> revision = contraction.mips(B);
		
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
	
}
