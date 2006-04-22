package presentation.fsa;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import presentation.Glyph;

/**
 * Visual representation of the control points for the bezier curve
 * of an edge.
 * 
 * @author Helen Bretzke
 *
 */
public class EdgeHandler implements Glyph {

	private Edge edge;  // is this the parent Glyph?
	private Ellipse2D.Double[] anchors;
	private static final int RADIUS = 5;	
	
	public EdgeHandler(Edge edge) {
		this.edge = edge;
		anchors = new Ellipse2D.Double[4];		                               
		update();
	}
	
	/**
	 * Update my layout information from my edge.
	 */
	public void update() {
		// upper left corner, width and height
		int d = 2*RADIUS;
		anchors[Edge.P1] = new Ellipse2D.Double(edge.getP1().x - RADIUS, edge.getP1().y - RADIUS, d, d); 
		anchors[Edge.CTRL1] = new Ellipse2D.Double(edge.getCTRL1().x - RADIUS, edge.getCTRL1().y - RADIUS, d, d);				
		anchors[Edge.CTRL2] = new Ellipse2D.Double(edge.getCTRL2().x - RADIUS, edge.getCTRL2().y - RADIUS, d, d);
		anchors[Edge.P2] = new Ellipse2D.Double(edge.getP2().x - RADIUS, edge.getP2().y - RADIUS, d, d);
	}
	
	
	/**
	 * TODO need to know which control point was intersected.
	 */
	public boolean intersects(Point p) {
		for(int i=0; i<4; i++){
			if(anchors[i]!=null && anchors[i].contains(p)){
				return true;
			}			
		}
		return false;
	}



	public void draw(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		for(int i=0; i<4; i++){
			g2d.draw(anchors[i]);
		}
				
		// TODO set stroke to dashed line
		g2d.setColor(Color.GREEN);
		g2d.drawLine((int)(edge.getP1().x), 
				(int)(edge.getP1().y), 
				(int)(edge.getCTRL1().x), 
				(int)(edge.getCTRL1().y));
		
		g2d.drawLine((int)(edge.getP2().x), 
				(int)(edge.getP2().y), 
				(int)(edge.getCTRL2().x), 
				(int)(edge.getCTRL2().y));
		
		// TODO reset color to previous one
	}



	public Rectangle2D bounds() {
		// TODO Auto-generated method stub
		return null;
	}



	public void insert(Glyph child, int index) {}
	
	public void remove(Glyph child) {}

	public Glyph child(int index) {
		return null;
	}

	/**
	 * TODO think about this one: is the edge its parent?
	 * If so, add this to the edge's list of children and
	 */
	public Glyph parent() {		
		return edge;
	}
	
}
