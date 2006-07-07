package ui.tools;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import main.Hub;

import presentation.fsa.EdgeLabellingDialog;
import presentation.fsa.GraphDrawingView;
import presentation.fsa.GraphElement;
import ui.tools.DrawingTool;
import ui.command.GraphCommands;

public class TextTool extends DrawingTool {
		
	private GraphDrawingView context;
	
	public TextTool(GraphDrawingView context){
		this.context = context;		
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		cursor = toolkit.createCustomCursor(toolkit.createImage(Hub.getResource("images/cursors/text.gif")), new Point(0,0), "MAKE_LABELS");		
	}
	
	@Override
	public void handleMouseClicked(MouseEvent me) {
		if(me.getClickCount() != 2){
			context.setTool(GraphDrawingView.SELECT);
			context.getCurrentTool().handleMouseClicked(me);
			return;
		}
		// get current selection
		if(context.updateCurrentSelection(me.getPoint())){			
			new GraphCommands.TextCommand(context, (GraphElement)context.getCurrentSelection().child(0)).execute();			
		}else{
			// if nothing selected
			// TODO create a free label ...			
	
			// For now, open an input dialog and set location at mouse click
			// TODO set location of dialog close to the selected element or click location
			// Set any existing text in the dialog before showing
			// Extract text from dismissed dialog
			String inputValue = JOptionPane.showInputDialog("Enter label text: ");		
			System.out.println(inputValue);
		}
		context.clearCurrentSelection();
	}

	@Override
	public void handleKeyTyped(KeyEvent ke) {}
	
	@Override
	public void handleMouseDragged(MouseEvent me) {}

	@Override
	public void handleMouseMoved(MouseEvent me) {}

	@Override
	public void handleMousePressed(MouseEvent me) {}

	@Override
	public void handleMouseReleased(MouseEvent me) {}

	@Override
	public void handleKeyPressed(KeyEvent ke) {}

	@Override
	public void handleKeyReleased(KeyEvent ke) {}
}
