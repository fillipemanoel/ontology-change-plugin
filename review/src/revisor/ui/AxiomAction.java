package revisor.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.protege.editor.owl.model.parser.OWLParseException;
import org.protege.editor.owl.ui.clsdescriptioneditor.ExpressionEditor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAxiom;
import org.semanticweb.owlapi.model.OWLException;

/**
 * @author Fillipe Resina
 */
public class AxiomAction implements ActionListener{
	private ExpressionEditor<OWLClassAxiom> editor;
	private RevisorAbstractView revisorView;

	protected AxiomAction(RevisorAbstractView revView) {
		revisorView = revView;
		editor = revisorView.editor;
	}

	public void actionPerformed(ActionEvent e) {
		// Prepares and Editor Parser to parse the given expression in Protege  Editor
		OWLAxiom alpha = null;
		try {
			if(editor.isWellFormed()) {
				alpha = editor.createObject();
				revisorView.addAxiom(alpha,editor.getText());
			}
			else{
				throw new OWLParseException("Ill-formed expressions: "+editor.getText());
			}
		} catch (OWLException e1) {
			e1.printStackTrace();
		}
		
	}
}
