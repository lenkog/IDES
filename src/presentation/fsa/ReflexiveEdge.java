/**
 * 
 */
package presentation.fsa;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import model.fsa.FSATransition;

import presentation.CubicParamCurve2D;
import presentation.Geometry;
import presentation.GraphicalLayout;

/**
 * A symmetric self-loop manipulated by a single control point. 
 * 
 * TODO 
 * - refactor constructors (if super constructors are a pain, don't call them).
 * - find a way to compute control points from midpoint, centre point, scalars and angles
 * 	such that the midpoint set by the user remains fixed.
 * 
 * @author Helen Bretzke
 */
public class ReflexiveEdge extends BezierEdge {	 
	/**
	 * Old size of the correspondent edge. When this variable changes the value, the refresh method
	 * call routines to recompute the curve paramethers.
	 */
	float lastNodeRadius;
	/**
	 * Index of midpoint used as handle to modify the curve position.
	 */
	public static final int MIDPOINT = 4;
				
	/**
	 * Creates a reflexive edge on <code>node</code> with the given layout and transition.
	 * @param layout
	 * @param node
	 * @param t a transition this represented by this edge
	 */
	public ReflexiveEdge(BezierLayout layout, Node node, FSATransition t)
	{
		super(node, node);
		//CHRISTIAN
		lastNodeRadius = ((CircleNodeLayout)node.getLayout()).getRadius();
		//CHRISTIAN
		addTransition(t);		
		setLayout(new ReflexiveLayout(node, this, layout));
		setHandler(new ReflexiveHandler(this));
	}
	
	/**
	 * Creates a reflexive edge on <code>node</code> representing the given transition.
	 * 
	 * @param node
	 * @param t a transition this represented by this edge
	 */
	public ReflexiveEdge(Node node, FSATransition t) {
		super(node, node);
		lastNodeRadius = ((CircleNodeLayout)node.getLayout()).getRadius();
		addTransition(t);				
		setLayout(new ReflexiveLayout(node, this));
		setHandler(new ReflexiveHandler(this));

//		// place me among any other edges adjacent to node
//		Iterator<Edge> neighbours = node.adjacentEdges();
//		if(neighbours.hasNext()){
//			Set<Edge> n = new HashSet<Edge>();
//			while(neighbours.hasNext()) {
//				n.add(neighbours.next());
//			}
//			insertAmong(n);
//		}
		((ReflexiveLayout)getLayout()).axis = ((ReflexiveLayout)getLayout()).computeBestDirection(this.getSourceNode());
		computeEdge();
	}
	/**
	 * Auto-format: Change the angle of the axis to make the initial arrow go to the
	 * most confortable position
	 */
	public void resetPosition()
	{
		((ReflexiveLayout)getLayout()).resetPosition(this.getTargetNode());
	}

	/**
	 * Searchs for enough space along circumference of node to place this edge.
	 * If not enough space, places this edge in the default position.
	 *   
	 * TODO If not enough space, looks for a layout that doesn't clobber another reflexive edge.
	 */
	public void insertAmong(Set<Edge> neighbours) {	
		double delta = Math.toRadians(2.0);
		double alpha = 0.0; 
		
		if(!BezierEdgePlacer.tooClose(this, neighbours)) {
			return;
		}
		
		/**
		 * Search for a free space using brute force and ignorance.
		 */
		while(BezierEdgePlacer.tooClose(this, neighbours) && alpha < 360) {
			((ReflexiveLayout)getLayout()).axis = Geometry.rotate(((ReflexiveLayout)getLayout()).axis, delta);			
			setMidpoint(Geometry.add(getSourceNode().getLocation(), ((ReflexiveLayout)getLayout()).axis));
			computeEdge();
			alpha ++;
		}
		
		if(alpha == 360) {
			// TODO find a spot that doesn't mask another reflexive edge
			
		}
	}
	
	
	/**
	 * Set the midpoint of the curve to <code>point</code>. 
	 * 
	 * @param point the new midpoint.
	 */
	public void setMidpoint(Point2D point) {
		((ReflexiveLayout)getLayout()).setPoint(point, MIDPOINT);
		setNeedsRefresh(true);
	}
		
	/**
	 * Returns the midpoint of the curve representing this edge. 
	 * 
	 * @return the midpoint of this edge
	 */
	public Point2D getMidpoint() {
		return ((ReflexiveLayout)getLayout()).getMidpoint();		
	}

	/**
	 * Returns true iff pointType is MIDPOINT since all other points are either
	 * fixed or computed from this point.
	 * 
	 * @return whether the given point type is movable for this edge type
	 */
	public boolean isMovable(int pointType) {
		return pointType == MIDPOINT;	
	}
		
	/**
	 * FIXME customize so that intersection with boundary is computed properly;
	 * parameters (sourceT and targetT) are currently being reverse.
	 */
	public void refresh() {		
		super.refresh();
	}
	
	/**
	 * This method is responsible for creating a string that contains
	 * an appropriate (depending on the type) representation of this
	 * self-loop.
	 * 
	 * TODO: Calculate a better C1 and C2 or export - there are no
	 * PSTricks self-loop options
	 *  
	 * @param selectionBox The area being selected or considered
	 * @param exportType The export format
	 * @return String The string representation
	 * 
	 * @author Sarah-Jane Whittaker
	 */
	@Override
	public String createExportString(Rectangle selectionBox, int exportType) {

		String exportString = "";
		
		Point2D.Float edgeP1 = getSourceEndPoint();
		Point2D.Float edgeP2 = getTargetEndPoint();
		Point2D.Float edgeCTRL1 = getCTRL1();
		Point2D.Float edgeCTRL2 = getCTRL2();
				
		// Make sure this node is contained within the selection box
		if (! (selectionBox.contains(edgeP1) && selectionBox.contains(edgeP2)
			&& selectionBox.contains(edgeCTRL1) && selectionBox.contains(edgeCTRL2)))
		{
			System.out.println("Self-loop " + edgeP1 + " "
				+ edgeP2 + " "
				+ edgeCTRL1 + " "
				+ edgeCTRL2 + " "
				+ " outside bounds " + selectionBox);
			return exportString;
		}
		
		// Adjust the y value for the CTRL points based on the midpoint
		
		if (exportType == GraphExporter.INT_EXPORT_TYPE_PSTRICKS)
		{
			// Draw the curve				
			exportString += "  \\psbezier[arrowsize=5pt";
			exportString += (hasUncontrollableEvent() ?
					", linestyle=dashed" : "");
			exportString += "]{->}"
				+ "(" + (edgeP1.x - selectionBox.x) + "," 
				+ (selectionBox.y + selectionBox.height - edgeP1.y) + ")(" 
				+ (edgeCTRL1.x - selectionBox.x) + "," 
				+ (selectionBox.y + selectionBox.height -edgeCTRL1.y) + ")(" 
				+ (edgeCTRL2.x - selectionBox.x) + "," 
				+ (selectionBox.y + selectionBox.height -edgeCTRL2.y) + ")(" 
				+ (edgeP2.x - selectionBox.x) + "," 
				+ (selectionBox.y + selectionBox.height - edgeP2.y) + ")\n";
			
			// Now for the label
			if ((getBezierLayout().getText() != null) && (getLabel().getText().length() > 0))
			{
				exportString += "  " 
					+ getLabel().createExportString(selectionBox, exportType);
			}
		}
		else if (exportType == GraphExporter.INT_EXPORT_TYPE_EPS)
		{	
			// LENKO!!!
		}

		return exportString;
	}
	
	/**
	 * This method returns the bounding box for the edge and its label.
	 * 
	 * @return Rectangle The bounds of the Bezier Curve and its label. 
	 */
	public Rectangle bounds() {
		return ((ReflexiveLayout)getLayout()).getCurve().getBounds().union(getLabel().bounds());		
	}
	
	public void translate(float x, float y){
		super.translate(x, y);
		//Christian(May, 17, 2007)
		//Manual midpoint translation commented.
		//Point2D midpoint = ((ReflexiveLayout)getLayout()).midpoint;
		//midpoint.setLocation(midpoint.getX() + x, midpoint.getY() + y);		
	}
	
	public void computeEdge() {
		refresh();
		((ReflexiveLayout)getLayout()).computeCurve();
	}
	
	/**
	 * Returns false since a self-loop cannot be straight. 
	 */
	public boolean isStraight()	{
		return false;
	}
		
	/**
	 * Returns false since cannot straighten a self-loop. 
	 */
	public boolean canBeStraightened() {
		return false;
	}
	
	/** 
	 * Sets the coordinates of <code>intersection</code> to the location where
	 * my bezier curve intersects the boundary of <code>node</code>. 
	 * 
	 * @return param t at which my bezier curve intersects <code>node</code>
	 *  
	 * @precondition node != null and intersection != null
	 */
	protected float intersectionWithBoundary(Shape nodeShape, Point2D.Float intersection, int type) {
		
		// setup curves for iterative subdivision
		CubicParamCurve2D curve = this.getBezierLayout().getCurve();
			
		CubicParamCurve2D left = new CubicParamCurve2D();
		CubicParamCurve2D right = new CubicParamCurve2D();
		
		CubicParamCurve2D temp = new CubicParamCurve2D();
		// if target, then this algorithm needs to be reversed since
		// it searches curve assuming t=0 is inside the node.		
				
		if( type == TARGET_NODE ) {
			// swap endpoints and control points		
			temp.setCurve(curve.getP2(), curve.getCtrlP2(), curve.getCtrlP1(), curve.getP1());			
		}else if( type == SOURCE_NODE ){
			temp.setCurve(curve);
		}else{
			return 0f;
		}
		
		float epsilon = 0.00001f;		
		float tPrevious = 0f;
		float t = 0.5f - 0.01f; //1f;		
		float step = t; //1f;
		
		temp.subdivide(left, right, t);		
		// the point on curve at param t
		Point2D c_t = left.getP2();
	
		while(Math.abs(t - tPrevious) > epsilon){			
			step =  Math.abs(t - tPrevious);
			tPrevious = t;
			if(nodeShape.contains(c_t)){  // inside boundary
				// search right segment
				t += step/2;
			}else{
				// search left segment
				t -= step/2;
			}
			temp.subdivide(left, right, t);					
			c_t = left.getP2();
		}		
		
		// TODO keep searching from c_t towards t=0 until we're sure we've found the first intersection.
		// Start again with step size at t.
		
		if( type == TARGET_NODE  ) 
		{
			t = 1-t;
			assert(0 <= t && t <=1);			
		}
	
		intersection.x = (float)c_t.getX();
		intersection.y = (float)c_t.getY();
			
		return t;		
	}


	/**
	 * Same data as BezierLayout (control points and label offset vector)
	 * but different algorithms and handlers specific to rendering a self-loop. 
	 * 
	 * @author Helen Bretzke
	 *
	 */
	public class ReflexiveLayout extends BezierLayout
	{
		/**
		 * The minimum length of the axis vector from the centre of the node
		 * to the midpoint of this edge.
		 * 
		 * FIXME the axis doesn't reach the *computed* midpoint.		 
		 */
		private float minAxisLength;

		// NOTE no need to store either of these variables here.		
		// vector from centre of source/target node to this point 
		// is the axis around which a symmetrical arc is drawn.		
		private Point2D axis;		
		private Point2D midpoint;
		////////////////////////////////////////////////////////
		
		/** 
		 * Default angle from centre axis vector to the tangents of the bezier curve 
		 * NOTE this looks ugly but minimizes the problem of control (mid)point drift 
		 */
		public static final double DEFAULT_ANGLE = Math.PI / 4; 
		/** Default value to scale the centre axis vector to the length of the tangents of the bezier curve */ 
		public static final float DEFAULT_SCALAR = 2f;
		
		/**
		 * Layout for a reflexive edge with vertical axis vector from centre of
		 * node to midpoint of bezier curve given by <code>bLayout</code>.
		 */
		public ReflexiveLayout(Node source, ReflexiveEdge edge, BezierLayout bLayout) {
			minAxisLength = source.bounds().height;			
			setEdge(edge);
			Point2D temp = Geometry.midpoint(bLayout.getCurve());
			setPoint(new Point2D.Float((float)temp.getX(), (float)temp.getY()), MIDPOINT);			
			setCurve(bLayout.getCurve());			
			setEventNames(bLayout.getEventNames());
			setLabelOffset(bLayout.getLabelOffset());
			initializeShape();

		}

		/**
		 * @param source
		 * @param edge
		 */
		public ReflexiveLayout(Node source, ReflexiveEdge edge) {
			minAxisLength = source.bounds().height;
			setEdge(edge);
			initializeShape();

		}
				
		/**
		 * @return the midpoint of the curve.
		 */
		public Point2D getMidpoint() {
			return midpoint;
		}

		
		/**
		 * FIXME 
		 * Problem: using fixed scalars and angles causes midpoint 
		 * of computed curve to drift away from midpoint set by user.
		 * 
		 * If the curve has already been loaded from a file,
		 * compute the correct axis, angles and scalars to 
		 * correctly reproduce the curve.
		 *  
		 */
		public void initializeShape() {
			angle1 = -DEFAULT_ANGLE;
			angle2 = DEFAULT_ANGLE;
			s1 = DEFAULT_SCALAR;
			s2 = s1;	
			
			Float centrePoint = getEdge().getSourceNode().getLocation();
			setPoint(centrePoint, P1);
			setPoint(centrePoint, P2);
			if(midpoint == null) {
				setPoint(Geometry.add(centrePoint, 
						Geometry.scale(new Point2D.Float(0, -1), 
								minAxisLength)), MIDPOINT);
			}		
		}
		
		/** 
		 * @return the portion of the curve that is external to the node,
		 * null if no such segment exists
		 */
		public CubicCurve2D getVisibleCurve()
		{
			// FIXME check for NaN here or in CubicCurve2Dex class
			return curve.getSegment(sourceT, targetT);		
		}
		
		/**		 
		 * Computes a symmetric reflexive bezier curve based on location of node,
		 * and angle of tangent vectors (to bezier curve) from centre axis vector.
		 * 
		 */
		public void computeCurve()
		{	
			if(getEdge() == null){
				return;
			}

			Node sourceNode = getSourceNode();
			Point2D nodeLocation = sourceNode.getLocation();

			//In case the node change its size, recalculate the midpoint position
			//by increasing value of the minAxisLength.
			float currentNodeRadius = ((CircleNodeLayout)sourceNode.getLayout()).getRadius();
			float factor = currentNodeRadius / lastNodeRadius;
			if(factor != 1)
			{
				minAxisLength *= factor;
				axis = Geometry.scale(axis, factor);
			}
			lastNodeRadius = currentNodeRadius;

			//Setting midpoint according to the axis:
			midpoint.setLocation(nodeLocation.getX()+axis.getX(), nodeLocation.getY() + axis.getY());
			setPoint(midpoint,MIDPOINT);
			//Setting the Bezier control points based on the axis:
	    	Point2D.Float v1 = Geometry.rotate(axis, angle1);
			Point2D.Float v2 = Geometry.rotate(axis, angle2);
			//Setting center of the edge
			setPoint(nodeLocation, P1);
			setPoint(nodeLocation, P2);
			//Setting the control points
			setPoint(Geometry.add(nodeLocation, Geometry.scale(v1, s1)), CTRL1);
			setPoint(Geometry.add(nodeLocation, Geometry.scale(v2, s2)), CTRL2);
			setDirty(true);

		}	
		
		/**
		 * Set the midpoint for a symmetric, reflexive bezier edge.
		 * Constraint: if midpoint is inside node, set to minimum distance from node border. 
		 * 
		 * @param point
		 * @param index
		 */
		public void setPoint(Point2D point, int index){
			float x = (float)point.getX();
			float y = (float)point.getY();		
			
			switch(index)
			{			
				case MIDPOINT:
					Float centrePoint;
					centrePoint = getEdge().getSourceNode().getLocation();
					axis = Geometry.subtract(point, centrePoint);
					double norm = Geometry.norm(axis);
					if(norm < minAxisLength){						
						// snap to arc minimum distance from border of node
						axis = Geometry.scale(axis, minAxisLength/norm);
						midpoint = Geometry.add(centrePoint, axis);
					}else{					
						midpoint = new Point2D.Float(x, y);
					}															
					//computeCurve();
					// TODO set midpoint after computing curve...
					midpoint = Geometry.midpoint(getCurve());
					setLocation((float)midpoint.getX(), (float)midpoint.getY());
//					setDirty(true);
					break;
				case P1:
					curve.x1 = x;
					curve.y1 = y;
					break;
				case P2:
					curve.x2 = x;
					curve.y2 = y;
					break;				
				case CTRL1:
					curve.ctrlx1 = x;
					curve.ctrly1 = y;
					break;
				case CTRL2:
					curve.ctrlx2 = x;
					curve.ctrly2 = y;
					break;
				default: throw new IllegalArgumentException("Invalid control point index: " + index);				
			}
			
		}
		/**
		 *@author Christian 
		 */
		private Point2D.Float computeBestDirection(Node target){
			
			Iterator<Edge> adjEdges = target.adjacentEdges();
			
			//Check the angles of the existent edges
			ArrayList<java.lang.Float> angles = new ArrayList<java.lang.Float>();
			Point2D.Float currentDirVector = new Point2D.Float();
			int number = 0;


			while(adjEdges.hasNext()){
				Edge edge = adjEdges.next();
				if(edge.getTargetNode().equals(edge.getSourceNode()))
				{
					currentDirVector = Geometry.subtract(target.getLocation(),((ReflexiveEdge)edge).getMidpoint());
					float currentAngle = (float)Geometry.angleFrom(currentDirVector,new Point2D.Float(-1,0));
					angles.add(currentAngle);
					angles.add(currentAngle+0.5f*(float)angle1);
					angles.add(currentAngle+0.5f*(float)angle2);
				}
				else
				{
					currentDirVector = Geometry.subtract(target.getLocation(),edge.getTargetEndPoint());				
					float currentAngle = (float)Geometry.angleFrom(currentDirVector,new Point2D.Float(-1,0));
					angles.add(currentAngle);
				}					

				
					
					number++;
			}
			for(int i = 0; i < angles.size();i++)
			{
				if(angles.get(i) < 0)
				{
					angles.set(i, (float)(2*Math.PI) + angles.get(i));
				}
			}
			Collections.sort(angles);
			
			//Try to fit the selfloop at its "favorite" position: 90 degrees
			boolean canFit = true; 
			//Scan from limMin->limMax and check whether all this spaces are avaliables.
			float limMin = (float)Math.toRadians(50);
			float limMax = (float)Math.toRadians(130);
			for(int i = 0; i < angles.size();i++)
			{	
				float angle = angles.get(i);
				if( angle >= limMin & angle <=limMax)
					canFit = false;
			}
			if(canFit)
				return Geometry.rotate(new Point2D.Float(1,0),Math.toRadians(-90));
			
			//If the prefered positions are not available, look for the most confortable
			//position.
			angles.add((float)(angles.get(0)+2*Math.PI));
			float maxAngle = -1;
			int bestIndex=0;
			for(int i = 0; i < angles.size()-1;i++)
			{	
				float currentAngle = angles.get(i+1)-angles.get(i);
				if(currentAngle > maxAngle)
				{
					maxAngle = currentAngle;
					bestIndex = i;
				}
			}
			float rotAngle = maxAngle/2 +angles.get(bestIndex);
			return Geometry.rotate(new Point2D.Float(1,0),-rotAngle); 
		}
		
		private void resetPosition(Node node)
		{
			 axis = computeBestDirection(node);
		}
		
		/**
		 * Returns true iff <code>o</code> is an instance of ReflexiveLayout and this layout has the same
		 * curve and label offset as <code>o</code>. 
		 * 
		 * @param o the other layout to be compared
		 * @return true iff <code>o</code> is an instance of ReflexiveLayout and this layout has the same
		 * curve and label offset as <code>o</code>. 
		 */
		/*public boolean equals(Object o)
		{
	Won't work since need to use this to compare BezierLayout instances read from file
	with ReflexiveLayouts in memory.
		
			try{
				ReflexiveLayout other = (ReflexiveLayout)o;
				return other.curve.equals(this.curve) &&
						other.getLabelOffset().equals(this.getLabelOffset());
			}catch(ClassCastException cce){
				return false;
			}
		}*/
	
	} // end Layout
	
	
	/**
	 * Visual representation of the single control point at the midpoint
	 * of a reflexive edge.  Used to modify the size and orientation a reflexive edge.
	 * 	 
	 * @author helen bretzke
	 */
	public class ReflexiveHandler extends EdgeHandler {
		
		/* Circle representing the handle to drag with the mouse */
		private Ellipse2D.Double anchor;	
		
		/* Radius of the anchor */
		private static final int RADIUS = 5;
		
		/**
		 * Creates a handler for the given edge. 
		 * 
		 * @param edge
		 */
		public ReflexiveHandler(ReflexiveEdge edge) {
			super(edge);
			refresh();
		}

		/**
		 * Refreshes the position of this handler based on location of midpoint of edge. 
		 */
		public void refresh(){
			int d = 2*RADIUS;			
			anchor = new Ellipse2D.Double(((ReflexiveEdge)getEdge()).getMidpoint().getX() - RADIUS, ((ReflexiveEdge)getEdge()).getMidpoint().getY() - d, d, d);
			setNeedsRefresh(false);
		}
		
		/**		 
		 * Returns true iff p intersects the midpoint anchor.
		 * 
		 * @return true iff p intersects the midpoint anchor. 
		 */
		public boolean intersects(Point2D p) {
			if(anchor.contains(p)){
				lastIntersected = MIDPOINT;
				return true;
			}			
			lastIntersected = NO_INTERSECTION;
			return false;		
		}
		
		/**
		 * Renders this handler in the given graphics context.
		 * 
		 *  @param g the graphics context in which to render this handler
		 */
		public void draw(Graphics g){
			if(needsRefresh()) refresh();
					
			if(!visible) return;
			
			Graphics2D g2d = (Graphics2D)g;
					
			g2d.setColor(Color.BLUE);
			g2d.setStroke(GraphicalLayout.FINE_STROKE);
			g2d.draw(anchor);
		}
	} // end Handler
}
