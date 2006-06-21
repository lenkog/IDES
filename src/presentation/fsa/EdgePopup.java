/**
 * 
 */
package presentation.fsa;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D.Float;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import main.Hub;

import presentation.fsa.NodePopup.PopupListener;

/**
 * @author Squirrel
 *
 */
public class EdgePopup extends JPopupMenu {

	private Edge edge;
	private JMenuItem miModify, miEditEvents, miStraighten, miDeleteEdge;
	private static GraphDrawingView view;
	
	// Using a singleton pattern (delayed instantiation) 
	// rather than initializing here since otherwise get java.lang.NoClassDefFoundError error
	private static EdgePopup popup;
	
	/**
	 * @param e
	 */
	protected EdgePopup(Edge e) {
		// TODO Auto-generated constructor stub
		miModify = new JMenuItem("Modify curve");
		miEditEvents = new JMenuItem("Add/Remove/Edit Events");
		miStraighten = new JMenuItem("Straighten");
		miDeleteEdge = new JMenuItem("Delete");
		addPopupMenuListener(new PopupListener());
		setEdge(e);
	}

	protected static void showPopup(GraphDrawingView context, Edge e){
		view = context;
		if(popup == null) {
			popup = new EdgePopup(e);
		}else{		
			popup.setEdge(e);
		}
		Float p = e.getLayout().getLocation();
		popup.show(context, (int)p.x, (int)p.y);
	}
			
	public void setEdge(Edge edge){
		this.edge = edge;
	}

	class MenuListener implements ActionListener {

		/* (non-Javadoc)
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent arg0) {
			Object source = arg0.getSource();
			if(source.equals(miModify)){
				edge.setSelected(true);  // Is this necessary?				
				view.setTool(view.MODIFY);
			}else if(source.equals(miEditEvents)){
				
			}else{
				Hub.displayAlert("Edge popup: " + source.toString());
			}
			
		}
		
		
	}
	
	class PopupListener implements PopupMenuListener {

		/* (non-Javadoc)
		 * @see javax.swing.event.PopupMenuListener#popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent)
		 */
		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
			view.repaint();
		}
		public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {}
		public void popupMenuCanceled(PopupMenuEvent arg0) {}
	  }	  
}
