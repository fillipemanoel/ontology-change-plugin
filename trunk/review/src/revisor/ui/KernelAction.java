package revisor.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.parser.ProtegeOWLEntityChecker;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * Author: Márcio Moretto Ribeiro http://www.cecm.usp.br/~marciomr
 * 
 * Universidade de São Paulo Instituto de Matemática e Estatística Date: Jul 10,
 * 2007
 * 
 * 
 * Belief Revisor (Contraction)
 */

public class KernelAction implements ActionListener {

	private ExpressionEditor<OWLClassAxiom> editor;

	private OWLModelManager manager;

	private OWLOntology ontology;

	private RevisorAbstractView revisorView;

	protected KernelAction(RevisorAbstractView revView) {
		revisorView = revView;
		manager = revisorView.manager;
		editor = revisorView.editor;
		ontology = revisorView.ontology;
	}

	public void actionPerformed(ActionEvent e) {
		String expression = editor.getText();

		HashMap<String, String> options = new HashMap<String, String>();

		for (PostulateGroup postGroup : revisorView.postulateGroups) {
			for (PostulateButton button : postGroup.buttons) {
				if (button.isSelected()) {
					options.put(postGroup.title + "Type", button.getText());
				}
			}
		}

		String minimality = options.get("MinimalityType");
		System.out.println("Hey");
		//if (editor.isWellFormed()) {
			System.out.println("Hi");
			// Prepares and Editor Parser to parse the given expression in Protege Editor
			OWLDataFactory dataFactory = manager.getOWLDataFactory();
			ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(dataFactory, expression);
			parser.setOWLEntityChecker(new ProtegeOWLEntityChecker(manager.getOWLEntityFinder()));
            parser.setBase(ontology.getOntologyID().toString());			
			OWLAxiom alpha =  null;
			try {
				alpha = parser.parseAxiom();
			} catch (ParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// Get the axioms according to the desired operation
			Set<Set<OWLAxiom>> axioms = revisorView.getAxioms(manager,
					ontology, alpha, options);
			// Show them to the user
			revisorView.axiomsGUI(axioms, minimality);
		//}
		System.out.println("Ow");
		revisorView.finishState(minimality);
	}
}

