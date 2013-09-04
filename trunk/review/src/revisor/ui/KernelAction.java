package revisor.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Set;

import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.parser.OWLParseException;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLOntologyCreationIOException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import revisor.ui.PostulateButton;
import revisor.ui.PostulateGroup;
import revisor.ui.RevisorAbstractView;

/**
 * Authors: Márcio Moretto Ribeiro and Fillipe Resina
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

		HashMap<String, String> options = new HashMap<String, String>();

		for (PostulateGroup postGroup : revisorView.postulateGroups) {
			for (PostulateButton button : postGroup.buttons) {
				if (button.isSelected()) {
					options.put(postGroup.title + "Type", button.getText());
				}
			}
		}

		String minimality = options.get("MinimalityType");
		// Prepares and Editor Parser to parse the given expression in Protege  Editor
		OWLAxiom alpha = null;
		try {
			if(editor.isWellFormed())
				alpha = editor.createObject();
			else{
				throw new OWLParseException("Ill-formed expressions: "+editor.getText());
			}
		} catch (OWLException e1) {
			e1.printStackTrace();
		}

		// Get the axioms according to the desired operation
		Set<Set<OWLAxiom>> axioms = revisorView.getAxioms(manager, ontology, alpha, options);
		// Show them to the user
		System.out.println("Hey");
		revisorView.axiomsGUI(axioms, minimality,ontology.getOntologyID().getOntologyIRI());
		revisorView.finishState(minimality);
	}
}
