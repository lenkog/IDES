package presentation.fsa;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.BasicStroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Iterator;

import presentation.GraphicalLayout;
import presentation.PresentationElement;
import presentation.Geometry;
import model.fsa.FSAState;
import model.fsa.ver1.State;
import model.fsa.ver1.Transition;

/**
 * The graphical representation of a state in a finite state automaton.
 * Child glyphs are its out edges.
 * 
 * @author helen bretzke
 *
 */
public class Node extends GraphElement {

	// the state to be represented
	private FSAState state;
		
	// TODO Change to list of labels to be displayed within the bounds of this node
	private GraphLabel label;

	// visualization objects
	private Ellipse2D circle = null;
	private Ellipse2D innerCircle = null;  // only drawn for final states	
	private ArrowHead arrow = null;  // only draw for initial states
	private Point2D.Float arrow1, arrow2;  // the arrow shaft
		
	public Node(FSAState s, NodeLayout layout){
		this.state = s;
		this.layout = layout;
		label = new GraphLabel("");
		circle = new Ellipse2D.Double();
		arrow1 = new Point2D.Float();
		arrow2 = new Point2D.Float();
		update();
	}

	public long getId(){
		return state.getId();
	}
	
	public void setLayout(NodeLayout layout){
		this.layout = layout;
		update();
	}
	
	public NodeLayout getLayout(){
		return (NodeLayout)layout;
	}
	
	// TODO change to iterate over collection of labels on a state
	// (requires change to file reading and writing, states be composed of many states)		
	public void update() {
			
		float radius = ((NodeLayout)layout).getRadius();
		Point2D.Float centre = ((NodeLayout)layout).getLocation();
		
		// upper left corner, width and height
		float d = 2*radius;
		circle = new Ellipse2D.Double(centre.x - radius, centre.y - radius, d, d);

		if(state.isMarked()){			
			float r = radius - NodeLayout.RDIF;
			d = 2*r;
			innerCircle = new Ellipse2D.Double(centre.x - r, centre.y - r, d, d);
		}
			
		if(state.isInitial()){
			// The point on the edge of the circle:
			// centre point - arrow vector
			Point2D.Float c = new Point2D.Float(centre.x, centre.y);
			Point2D.Float dir = new Point2D.Float(((NodeLayout)layout).getArrow().x, ((NodeLayout)layout).getArrow().y);			
			float offset = ((NodeLayout)layout).getRadius() + ArrowHead.SHORT_HEAD_LENGTH;
			arrow2 = Geometry.subtract(c, Geometry.scale(dir, offset));
			arrow = new ArrowHead(dir, arrow2);					
			// ??? How long should the shaft be?
			arrow1 = Geometry.subtract(arrow2, Geometry.scale(dir, ArrowHead.SHORT_HEAD_LENGTH * 2));
		}
		
		// TODO relocate based on bounds of label and expand the node size if necessary.
		label = new GraphLabel(layout.getText(), centre);
					
	}
	
	/**
	 * Draws this node and all of its out edges in the given graphics context.
	 */
	public void draw(Graphics g) {		
		//super.draw(g);	// calls draw on all edges
		// override so only calls draw on all of the outgoing edges
		Iterator c = children();
		while(c.hasNext()){
			Edge child = (Edge)c.next();
			if(child.getSource().equals(this)){
				child.draw(g);
			}
		}
		
		
		if (isSelected()){
			g.setColor(layout.getSelectionColor());
		}else if(isHighlighted()){
			g.setColor(layout.getHighlightColor());			
		}else{
			g.setColor(layout.getColor());	
		}
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setStroke(GraphicalLayout.WIDE_STROKE);		
		g2d.draw(circle);
				
		if(state.isMarked()){
			g2d.draw(innerCircle);
		}		
		
		if(state.isInitial()){
			g2d.drawLine((int)arrow1.x, (int)arrow1.y, (int)arrow2.x, (int)arrow2.y);
			g2d.setStroke(GraphicalLayout.FINE_STROKE);
			g2d.draw(arrow);
			g2d.fill(arrow);
		}				
		label.draw(g);
	}
	
	public Rectangle bounds() {		
		return circle.getBounds();
	}

	// FIXME calling RectangularShape.contains(p) which computes intersection with bounding box
	// instead of with circle.
	// Also check if initial arrow intersects with p
	/**
	 * Try with a rectangle of dimensions 1,1
	 * public boolean contains(double x,
                        double y,
                        double w,
                        double h)

       Tests if the interior of this Ellipse2D entirely contains the specified rectangular area.
       
	 */
	public boolean intersects(Point p) {		
		return circle.contains(p);
	}	

	public void setSelected(boolean b){
		this.selected = b;		
	}
	
	/**
	 * DEBUG: since we are sharing references to unique node objects, 
	 * this shouldn't be necessary.
	 *  
	 * @param n
	 * @return
	 */
	public boolean equals(Node n){
		return this.getId() == n.getId();
	}
	
	public void translate(float x, float y){
		super.translate(x,y);
		// TODO translate all INCOMING edges as well as outgoing
		update();
	}
}
