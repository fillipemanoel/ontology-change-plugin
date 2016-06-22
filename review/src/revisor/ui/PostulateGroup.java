package revisor.ui;

/**
 * @author Márcio Ribeiro and Fillipe Resina
 */

public class PostulateGroup {
	protected PostulateButton[] buttons;
	protected String title;

	PostulateGroup(String s, PostulateButton[] but){
		title  = s;
		buttons = but;
	}
	
	PostulateGroup(String s, PostulateButton but){
		title  = s;
		buttons = new PostulateButton[1];
		buttons[0] = but;
		buttons[0].setSelected(true);
    	buttons[0].setEnabled(false);
	}
	
	public int length(){
		return buttons.length;
	}
	
	public PostulateButton[] getButtons() {
		return buttons;
	}
	public void setButtons(PostulateButton[] buttons) {
		this.buttons = buttons;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
}