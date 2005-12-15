/*
 * Created on Jun 21, 2004
 */
package com.aggressivesoftware.ides.graphcontrol.graphparts;


import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.swt.widgets.TableItem;

import com.aggressivesoftware.geometric.Box;
import com.aggressivesoftware.geometric.Line;
import com.aggressivesoftware.geometric.Point;
import com.aggressivesoftware.geometric.UnitVector;
import com.aggressivesoftware.ides.GraphingPlatform;
import com.aggressivesoftware.ides.graphcontrol.Drawer;
import com.aggressivesoftware.ides.graphcontrol.GraphModel;
import com.aggressivesoftware.ides.graphcontrol.LatexPrinter;
import com.aggressivesoftware.ides.graphcontrol.TransitionData;

/**
 * An Edge is a Curve connecting two Nodes with and ArrowHead at one end.
 * The main job of this class is to handle more GraphModel based concepts, 
 * while the Curve class itself exist to handle more Geometric based concepts.
 * 
 * All GraphObjects are directly aware of the GraphModel.
 * An Edge may also be directly aware of its Label, its ArrowHead, and it's start and end Nodes,
 * and the EdgeGroup of which it is a part.
 * 
 * @author Michael Wood
 */
public class Edge extends GraphObject
{ 	
	/**
     * possible values for selection_state
     */
	public static final int NO_ANCHORS = 0,
							EXCLUSIVE = 1;	
	
    /**
     * region constants: possible values for last_hit_region
     */
	public static final int R_ARROWHEAD = 0,
							R_TAIL_ANCHOR = 1,
							R_TAIL_CTRL = 2,
							R_HEAD_ANCHOR = 3,
							R_HEAD_CTRL = 4,
							R_LABEL = 5,
							R_NONE = 6,
							R_LOOP = 7;	

    /**
     * location constants: possible values for options when performing hit tests
     * note: tethers refer to labels, and anchors refer to the curve.
     */
	public static final int L_NULL = 0,
							L_ALL_ANCHORS = 1,
							L_ALL_TETHERS = 2,
							L_NO_ANCHORS = 4,
							L_NO_TETHERS = 8,
							L_PADDED = 16;
		
    /**
     * the default values for the label_displacement variable
     */
	public static final int DEFAULT_LABEL_DISPLACEMENT = 5;	

	/**
     * The Node where this Edge originates.
     */
	public Node n1 = null; 
 
	/**
     * The Node where this Edge terminates.
     */
	public Node n2 = null;
	
    /**
     * The EdgeGroup that contains this Edge.
     */
	private EdgeGroup edge_group = null;
	
    /**
     * Determines edge behaviour, such as visibility, colour, etc of anchors and the tether.
     */
	public int selection_state = 0;
	
    /**
     * Visual representation of the edge.
     */	
	private Curve curve = null;	
	
    /**
     * records which region was last hit by the mouse.
     * uses region constants from Edge as valid values.
     */
	public int last_hit_region = Edge.R_NONE;

	/**
     * The label data for this Edge
     */
	private Vector label_data = null;	
	
	/**
     * Represents the displacement from the t=0.5 point of the bezier curve to the top left corner of the label
     */
	private Point label_displacement = null;	

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Edge construction //////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////		
	
    /**
     * Construct the Edge. (used at user creation)
     *
     * @param	gp			The GraphingPlatform in which this Edge will exist.
     * @param	gm			The GraphModel in which this Edge will exist.
     * @param	start_node	The Node where the Edge originates.
     * @param	end_node	The Node where the Edge terminates.
     */
	public Edge(GraphingPlatform gp, GraphModel gm, Node start_node, Node end_node)
	{ 
		super(gp, gm, GraphObject.SIMPLE); 
		constructEdge(start_node, end_node); 
		initializeLabels(DEFAULT_LABEL_DISPLACEMENT, DEFAULT_LABEL_DISPLACEMENT); 

		if (isSelfLoop()) {	curve = new Curve(n1,n2,edge_group.newUnitVector(this)); }
		else { curve = new Curve(n1,n2); }

		edge_group.recalculate();
	}

    /**
     * Construct the Edge. (used at cloning)
     *
     * @param	gp					The GraphingPlatform in which this Edge will exist.
     * @param	gm					The GraphModel in which this Edge will exist.
     * @param	start_node			The node where the edge originates.
     * @param	end_node			The node where the edge terminates.
     * @param	curve				A cloned curve to be used.
     * @param	label_displacement	A cloned Point representing the displacement of the label from the midpoint of the curve.
     * @param	label_data			A cloned Vector containing the required label data objects.
     * @param	a					The attributes for this Edge.
     * @param	glyph_label			A Label to be cloned for the glyph label of this Edge.
     * @param	latex_label			A Label to be cloned for the latex label of this Edge.
     */
	private Edge(GraphingPlatform gp, GraphModel gm, Node start_node, Node end_node, Curve curve, Point label_displacement, int a, Vector label_data, GlyphLabel glyph_label, LatexLabel latex_label)
	{
		super(gp, gm, a); 
		constructEdge(start_node, end_node); 
		
		this.curve = curve;
		this.label_displacement = label_displacement;
		this.label_data = label_data;
		this.latex_label = new LatexLabel(gp, this, latex_label);
		this.glyph_label = new GlyphLabel(gp, this, glyph_label);
	}		
	
    /**
     * Construct the Edge. (used at load from file)
     *
     * @param	gp			The GraphingPlatform in which this Edge will exist.
     * @param	gm			The GraphModel in which this Edge will exist.
     * @param	start_node	The node where the edge originates.
     * @param	end_node	The node where the edge terminates.
     * @param	x1			x1 parameter for the curve object
     * @param	y1			y1 parameter for the curve object
     * @param	ctrlx1		ctrlx1 parameter for the curve object
     * @param	ctrly1		ctrly1 parameter for the curve object
     * @param	ctrlx2		ctrlx2 parameter for the curve object
     * @param	ctrly2		ctrly2 parameter for the curve object
     * @param	x2			x2 parameter for the curve object
     * @param	y2			y2 parameter for the curve object
     * @param	dx			x parameter for the direction object
     * @param	dy			y parameter for the direction object
     * @param	gtx			x parameter for the label_displacement object
     * @param	gty			y parameter for the label_displacement object
     * @param	a			The attributes for this Edge.
     */
	public Edge(GraphingPlatform gp, GraphModel gm, Node start_node, Node end_node, float x1, float y1, float ctrlx1, float ctrly1, float ctrlx2, float ctrly2, float x2, float y2, float dx, float dy, int gtx, int gty, int a)
	{
		super(gp, gm, a); 
		constructEdge(start_node, end_node); 
		initializeLabels(gtx, gty); 

		curve = new Curve(n1,n2, x1,y1,ctrlx1,ctrly1,ctrlx2,ctrly2,x2,y2, new UnitVector(dx,dy));
	}	
	
    /**
     * Set the class variables of this Edge.
     *
     * @param	start_node	The node where the edge originates.
     * @param	end_node	The node where the edge terminates.
     */
	private void constructEdge(Node start_node, Node end_node)
	{
		n1 = start_node;
		n2 = end_node;
		edge_group = n1.join(this,n2);		
	}
	
    /**
     * Set the class variables of this Edge.
     * 
     * @param	gtx		x parameter for the label_displacement object
     * @param	gty		y parameter for the label_displacement object
     */
	private void initializeLabels(int gtx, int gty)
	{
		label_displacement = new Point(gtx,gty);
		label_data = new Vector();
		latex_label = new LatexLabel(gp,this,true);
		glyph_label = new GlyphLabel(gp,this);	
	}
	
	/**
	 * Create a clone of this Edge, with null as its GraphModel.
	 * If it's nodes were not previously cloned, this will return null.
	 * Note: after cloning nodes, then edges, you should null all the lastClones of the nodes.
	 * 
	 * @return	A clone of this Edge.
	 */	
	public Edge newClone()
	{
		if (n1.last_clone != null && n2.last_clone != null)
		{
			int clone_attribute = GraphObject.NULL;
			if (isSimple()) { clone_attribute = GraphObject.SIMPLE; }
			return new Edge(gp, null, n1.last_clone, n2.last_clone, curve.newClone(n1.last_clone, n2.last_clone), label_displacement.getCopy(), clone_attribute, getLabelDataVector(), glyph_label, latex_label);
		}
		else { return null; }
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Edge calculation ///////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

    /**
     * Initiate for movement by node position.
     */
	public void initiateNodeMovement(int attribute)
	{
		curve.initiateNodeMovement();
		addAttribute(attribute);
	}
	
    /**
     * Recalculate the parameters of this Edge based on a node movement, keeping the origional edge configuration.
     * 
	 * @param	n						The initiating Node.
	 * @param	origional_configuration	The configuration of the Node at the initiation of movement.
	 * @param	mouse					The current mouse position.
     */
	public void updateNodeMovement(Node n, Configuration origional_configuration, Point mouse)
	{
		if (isSimple()) 
		{ 
			autoConfigureCurve();
		}
		else
		{
			if (isSelfLoop()) { curve.recalculateSelfLoop(); }
			else if (n == n1) { curve.updateNodeMovement(origional_configuration,mouse,n2,n1); }
			else              { curve.updateNodeMovement(origional_configuration,mouse,n1,n2); }		
		}
		selectedLabel().setAnchor(label_displacement.plus(curve.calculateBezierPoint((float)0.5)), Label.CORNER);
	}

    /**
     * Terminate movement by node position.
     */
	public void terminateNodeMovement(int attribute)
	{
		removeAttribute(attribute);
	}
	
    /**
     * Recalculate the parameters of this Edge using an automatic algorithm.
     */
	public void autoConfigureCurve()
	{
		if (isSelfLoop()) { curve.recalculateSelfLoop(); }
		else
		{
			int edge_position = edge_group.indexOf(this);
			int edge_group_levels = edge_group.levels();
			boolean odd_number_in_group = edge_group.hasOddEdges();
			boolean intersects_node = gm.findNode(n1,n2);
			boolean against_group_direction = edge_group.isStartNode(n2);
			int level = 0;
			float angle = 120 / (edge_group_levels+1);
			if (odd_number_in_group && !intersects_node)
			{
				if (edge_position == 0)
				{
					curve.calculateCurve(0,0); // strait edge
				}
				else
				{
					level = (int)Math.ceil(edge_position/2.0);
					if (edge_position % 2 == 0) { level = -level; }
					// swap side if our assumption of direction was false
					if (against_group_direction) { level = -level; }
					curve.calculateCurve(level*8,level*angle);				
				}
			}
			else
			{
				level = (int)Math.ceil((edge_position+1)/2.0);
				if (intersects_node) 
				{ 
					level = level + 2; 
					angle = 120 / (edge_group_levels+3);
				}
				if (edge_position % 2 == 1) { level = -level; }
				// swap side if our assumption of direction was false
				if (against_group_direction) { level = -level; }
				float adjust = angle/2; // because there is no center edge, we want all angles to be less by one half increment
				if (level < 0) { adjust = -adjust; } // because we want a decrease in magnitude
				curve.calculateCurve(level*8,level*angle-adjust);
			}
		}
	}
	
	public void autoStraightenCurve()
	{
		curve.calculateCurve(0,0); // strait edge
		removeAttribute(GraphObject.SIMPLE);
	}
	
	public void autoArcMore()
	{
		float angle = (float)Math.toDegrees(curve.headAnchorAngle());
		// force angle to -180 ... 180 (it represents the angle from the bisector)
		if (angle > 180) { angle = 360 - angle; }
		if (angle < -180) { angle = 360 + angle; }
		// increase the size to a maximum of +-90
		if (angle >= 0 && angle < 90) { angle = angle + 10; }
		else if (angle < 0 && angle > -90) { angle = angle - 10; }
		// fix the angle for convention of calculateCurve
		angle = angle * -1;

		// compute and increase the rise
		Line bisector = new Line(n1.origin(),n2.origin());
		float rise = bisector.perpendicularDistance(curve.headCtrl());
		rise = (float)(rise * 1.1);
		// fix the rise for convention of calculateCurve
		if (angle < 0) { rise = rise * -1; }
		
		curve.calculateCurve(rise,angle);
		removeAttribute(GraphObject.SIMPLE);
	}
	
	public void autoArcLess()
	{
		float angle = (float)Math.toDegrees(curve.headAnchorAngle());
		// force angle to -180 ... 180 (it represents the angle from the bisector)
		if (angle > 180) { angle = 360 - angle; }
		if (angle < -180) { angle = 360 + angle; }
		// decrease the size
		if (angle > 0) { angle = angle - 10; }
		else { angle = angle + 10; }
		// fix the angle for convention of calculateCurve
		angle = angle * -1;

		// compute and decrease the rise
		Line bisector = new Line(n1.origin(),n2.origin());
		float rise = bisector.perpendicularDistance(curve.headCtrl());
		float factor = (float)((100 - Math.abs(angle))/100);
		rise = rise - (float)(rise * factor);
		// fix the rise for convention of calculateCurve
		if (angle < 0) { rise = rise * -1; }
		
		curve.calculateCurve(rise,angle);
		removeAttribute(GraphObject.SIMPLE);
	}
		
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Edge drawing ///////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
    /**
     * Draw this edge.  
     * It's style is determined here by testing it's properties, and it's edgegroup.
     *
     * @param	drawer		The Drawer that will handle the drawing.
     * @param	all_anchors	True if all edges in the graph should draw their anchors (which define the curves).
     * @param	all_tethers	True if all edges in the graph should draw their tethers (which define the label positions).
     */
	public void draw(Drawer drawer, boolean all_anchors, boolean all_tethers)
	{		
		if (isGrouped())     { drawer.setColor(GraphModel.GROUPED); }
		if (isTraceObject()) { drawer.setColor(GraphModel.TRACE); }
		if (isHotSelected()) { drawer.setColor(GraphModel.HOT_SELECTED); }
		
		if (hasUncontrollableLabel()) { curve.drawCurve(drawer,Drawer.DASHED); } 
		else { curve.drawCurve(drawer,Drawer.SOLID); } 
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
		if (!labelDataIsNull()) 
		{ 					
			selectedLabel().drawData(drawer, label_data);
			
			if (all_tethers || selection_state == Edge.EXCLUSIVE)
			{
				Point destination = curve.calculateBezierPoint((float)0.5);
				if (isHotSelected() && last_hit_region == Edge.R_LABEL) { drawer.setColor(GraphModel.CUSTOM); }	
				else { drawer.setColor(GraphModel.TETHERS); }
				selectedLabel().drawBox(drawer);
				selectedLabel().drawTether(drawer,destination);
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		if (all_anchors || selection_state == Edge.EXCLUSIVE)
		{
			drawer.setColor(GraphModel.ANCHORS); 
			if (isSelfLoop())
			{
				if (isHotSelected() && last_hit_region == Edge.R_LOOP) { drawer.setColor(GraphModel.CUSTOM); }	
				curve.drawSelfLoopAnchor(drawer);
			}
			else 
			{
				if (isHotSelected() && (last_hit_region == Edge.R_TAIL_ANCHOR || last_hit_region == Edge.R_TAIL_CTRL))
				{
					curve.drawHeadAnchors(drawer);				
					drawer.setColor(GraphModel.CUSTOM);
					curve.drawTailAnchors(drawer);				
				}
				else if (isHotSelected() && (last_hit_region == Edge.R_HEAD_ANCHOR || last_hit_region == Edge.R_HEAD_CTRL || last_hit_region == Edge.R_ARROWHEAD))
				{
					curve.drawTailAnchors(drawer);				
					drawer.setColor(GraphModel.CUSTOM);
					curve.drawHeadAnchors(drawer);				
				}	
				else
				{
					curve.drawTailAnchors(drawer);				
					curve.drawHeadAnchors(drawer);				
				}
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		drawer.setColor(GraphModel.NORMAL); 
	}
		
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Edge selection /////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////	

    /**
     * Used to determine if a mouse-click should select this edge or its label.
     * This first checks if the click falls into any of the edges anchors, or on the arrowhead.
     * Then it checks if it falls in the bounding box of the label, if one exists.
     * For the options variable use the locations constants from the Edge class (start with L_).
     * It is allowed to OR multiple constants.
     *
     * @param	x			x co-ordinate of the mouse.
     * @param	y			y co-ordinate of the mouse. 
     * @param	options		selection options regarding anchors and tethers. Use the locations constants from the Edge class (start with L_). It is allowed to OR multiple constants.
     * @return	true if this edge should be selected by this mouse click.
     */
	public boolean isLocated(int x, int y, int options)
	{
		boolean all_anchors = ((options & Edge.L_ALL_ANCHORS) > 0);
		boolean padded = ((options & Edge.L_PADDED) > 0);
		Point mouse = new Point(x,y);

		// anchors
		if ((options & Edge.L_NO_ANCHORS) == 0) 
		{
			if (all_anchors || selection_state == Edge.EXCLUSIVE)
			{
				if (isSelfLoop())
				{
					last_hit_region = curve.isLocatedSelfLoop(mouse,padded);
					if (last_hit_region != Edge.R_NONE) { return true; }
				}
				else 
				{
					last_hit_region = curve.isLocatedAnchors(mouse,padded);
					if (last_hit_region != Edge.R_NONE) { return true; }
				}
			}			
		}

		// arrowhead
		if ((options & Edge.L_ALL_TETHERS) == 0)
		{
	  		last_hit_region = curve.isLocatedArrowhead(mouse,padded);
			if (last_hit_region != Edge.R_NONE) { return true; }
		}
		
		// tethers
		if (!labelDataIsNull() && ((options & Edge.L_NO_TETHERS) == 0))
		{
			// vary the selection area based on whether or not the bounding box is being displayed.
			int adjustment = Label.BOUNDING_BOX_FACTOR;
			if (padded) { adjustment = 2*adjustment; }
			if (((options & Edge.L_ALL_TETHERS) > 0) || selection_state == Edge.EXCLUSIVE) { adjustment = 0; }
			
			if (selectedLabel().isLocated(new Point(x,y)))
			{
				last_hit_region = Edge.R_LABEL;
				return true;
			}
		}

		last_hit_region = Edge.R_NONE; 
		return false; 
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Edge movement //////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void initiateMovement(Point mouse, int attribute, int state_mask)
	{
		removeAttribute(GraphObject.SIMPLE);
		
		Point origin = null;
		if (last_hit_region == Edge.R_TAIL_ANCHOR || last_hit_region == Edge.R_TAIL_CTRL) { origin = n1.origin(); }
		else { origin = n2.origin(); }
		
		Point selection_target = null;
		if (isSelfLoop()) { selection_target = curve.selfLoopAnchor(); }
		else if (last_hit_region == Edge.R_TAIL_ANCHOR) { selection_target = curve.tailAnchor(); }
		else if (last_hit_region == Edge.R_TAIL_CTRL) { selection_target = curve.tailCtrl(); }
		else if (last_hit_region == Edge.R_HEAD_CTRL) { selection_target = curve.headCtrl(); }
		else { selection_target = curve.headAnchor(); }
		
		origional_configuration = new Configuration(origin,curve.tailAnchor(),curve.tailCtrl(),curve.headCtrl(),curve.headAnchor(),label_displacement,mouse,selection_target,state_mask);
		addAttribute(attribute);		
	}

	public void updateMovement(Point mouse)
	{
		if (isSelfLoop() && (last_hit_region == Edge.R_LOOP || last_hit_region == Edge.R_ARROWHEAD))
		{
			curve.moveSelfLoop(origional_configuration,mouse); 
		}
		else if (last_hit_region == Edge.R_TAIL_ANCHOR || last_hit_region == Edge.R_TAIL_CTRL || last_hit_region == Edge.R_HEAD_CTRL || last_hit_region == Edge.R_HEAD_ANCHOR || last_hit_region == Edge.R_ARROWHEAD)
		{
			UnitVector bisector = new UnitVector(n1.origin(),n2.origin());
			if (last_hit_region == Edge.R_TAIL_ANCHOR) 
			{
				curve.moveTailAnchor(origional_configuration,mouse);
			}
			else if (last_hit_region == Edge.R_TAIL_CTRL) 
			{ 
				curve.moveTailCtrl(origional_configuration,mouse);
			}
			else if (last_hit_region == Edge.R_HEAD_CTRL) 
			{ 
				bisector.reverse();
				curve.moveHeadCtrl(origional_configuration,mouse);
			}
			else if (last_hit_region == Edge.R_HEAD_ANCHOR || last_hit_region == Edge.R_ARROWHEAD) 
			{
				bisector.reverse();
				curve.moveHeadAnchor(origional_configuration,mouse);
			}
		}
		else if (last_hit_region == Edge.R_LABEL)
		{
			label_displacement.x = origional_configuration.label_displacement.x + (mouse.x - origional_configuration.movement_origin.x);
			label_displacement.y = origional_configuration.label_displacement.y + (mouse.y - origional_configuration.movement_origin.y);

		}

		selectedLabel().setAnchor(label_displacement.plus(curve.calculateBezierPoint((float)0.5)), Label.CORNER);
	}

	public void terminateMovement(int attribute)
	{
		origional_configuration = null;	
		removeAttribute(attribute);
		last_hit_region = Edge.R_NONE;	
	}
	
	/**
     * Adjust settings of this GraphObject to take into consideration recent changes to it's label.
     */
	public void accomodateLabel() 
	{
		selectedLabel().setAnchor(label_displacement.plus(curve.calculateBezierPoint((float)0.5)), Label.CORNER);

		String representation = getLabelDataString();
		if (!selectedLabel().string_representation.equals(representation))
		{
			// the label has changed we must render it.
			selectedLabel().string_representation = representation;
			selectedLabel().render();
		}
		
		selectedLabel().renderIfNeeded();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Labels /////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
	/**
	 * Add a label to this Edge
	 * 
	 * @param	new_label_data		The label to be added to this Edge
	 */	
	public void addLabel(TableItem new_label_data)
	{
		label_data.add(new_label_data);
	}

	/**
	 * Remove a label from this Edge
	 * 
	 * @param	new_label_data		The label to be removed from this Edge
	 */	
	public void removeLabel(TableItem new_label_data)
	{
		label_data.remove(new_label_data);
	}
	
	/**
	 * Tests the label of this Edge
	 * 
	 * @param	label	A test value.
	 * @return	true if the test value matches the text in the label_data.
	 */	
	public boolean checkLabel(Object label)
	{
		if (!labelDataIsNull()) 
		{ 
			if (label_data.contains(label)) { return true; }
			else { return false; }
		}
		else { return false; }
	}
	
	/**
	 * Tests if the label_data object should be null
	 * 
	 * @return	true if the label_data object should be null
	 */	
	public boolean labelDataIsNull()
	{
		int i=0;
		while (i<label_data.size())
		{
			if (((TableItem)label_data.elementAt(i)).isDisposed()) { label_data.removeElementAt(i); }
			else { i++; }
		}
		return (label_data.size() == 0);
	}
	
	/**
	 * Calculates the string to be displayed from the array of associated TableItems
	 * 
	 * @return	The string to be displayed.
	 */	
	private String getLabelDataString()
	{
		if (!labelDataIsNull())
		{
			int column = TransitionData.SPEC_SYMBOL;
			if (selectedLabel().isLatexLabel()) { column = TransitionData.SPEC_LATEX; }
			
			String representation = ((TableItem)label_data.elementAt(0)).getText(column);
			for (int i=1; i<label_data.size(); i++) { representation = representation + ", " + ((TableItem)label_data.elementAt(i)).getText(column); }
			return representation;
		}
		else { return ""; }
	}

	/**
	 * Delivers a copy of the label_data Vector of this Edge.
	 * 
	 * @return	A copy of the label_data Vector of this Edge.
	 */	
	public Vector getLabelDataVector()
	{
		Vector ldv = new Vector();
		if (!labelDataIsNull())
		{
			for (int i=0; i<label_data.size(); i++) 
			{ ldv.add(label_data.elementAt(i)); }			
		}
		return ldv;
	}
		
	/**
	 * Test if this edge has any labels that contain the given machine code, providing this edge starts at the given start node
	 * 
	 * @param	machine_code	The machine_code that will identify the edge
	 * @param	start_node		The node from which the edge should start
	 * @return	true if this edge bears the given machine code	
	 */	
	public boolean hasMachineCode(int machine_code, Node start_node)
	{
		if (n1 != start_node) { return false; }
		if (labelDataIsNull()) { return false; }
		else
		{
			for (int i=0; i<label_data.size(); i++)
			{
				if (((TableItem)label_data.elementAt(i)).getText(TransitionData.SPEC_MACHINE_CODE).equals(""+machine_code))
				{ return true; }
			}
		}
		return false;
	}	
	
	/**
	 * Test if this edge has any labels that are uncontrollable transitions.
	 * 
	 * @return	true if this edge bears any uncontrollable transitions.
	 */	
	private boolean hasUncontrollableLabel()
	{
		if (labelDataIsNull()) { return false; }
		else
		{
			for (int i=0; i<label_data.size(); i++)
			{
				if (((TableItem)label_data.elementAt(i)).getText(TransitionData.SPEC_CONTROLLABLE).equals(TransitionData.BOOLEAN_COMBO_FALSE))
				{ return true; }
			}
		}
		return false;
	}
	
	public Point getLabelDisplacement()
	{ return label_displacement; }

	public void setLabelDisplacement(Point label_displacement)
	{ this.label_displacement = label_displacement; }
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// miscellaneous //////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Access a copy of the midpoint of the curve.
	 * 
	 * @return	A copy of the midpoint of the curve.
	 */
	public Point midpoint()
	{
		return curve.midpoint();
	}
	
    /**
     * Put the arrowhead on the other end.
     */
	public void reverseDirection()
	{
		if (!isSelfLoop())
		{
			Node n = n1;
			n1 = n2;
			n2 = n;
			if (isSimple()) 
			{ 
				curve.reverseDirection(false); 
				autoConfigureCurve(); 
			}
			else 
			{ 
				curve.reverseDirection(true); 
			}
		}
	}
	
    /**
     * Delete this Edge from it's EdgeGroup
     */
	public void delete()
	{
		n1 = null;
		n2 = null;
		curve.dispose();
		curve = null;
		label_data = null;
		glyph_label = null;
		latex_label = null;

		edge_group.removeEdge(this);
		edge_group = null;
		
		gm.safeNull(this);
		gm.removeEdge(this);
		gm = null;
	}
	
    /**
     * If n1 == n2 then this is a self loop edge.
     *
     * @return	true if it is a self loop edge.
     */
	public boolean isSelfLoop()
	{ return (n1 == n2); }
	
	/**
	 * Translate all variables.
	 * 
	 * @param	x	Translation in the x direction.
	 * @param	y	Translation in the y direction.
	 */	
	public void translateAll(int x, int y)
	{
		curve.translateAll(x,y);
		selectedLabel().setAnchor(label_displacement.plus(curve.calculateBezierPoint((float)0.5)), Label.CORNER);
	}		
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// File Access ////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////			

	/**
	 * Given proper data in a BufferedReader, create a new Node from a file.
	 * This requires that all Nodes are allready input into the GraphModel.
	 * 
	 * @param	gp		The GraphingPlatform to which the new Edge should belong.
	 * @param	gm		The GraphModel to which the new Edge should belong.
	 * @param	in		An initialized BufferedReader for inputing the data from file.
	 * @param	td		The TransitionData associated with the given GraphModel.
	 * @return 	true If the operation was successful.
	 */	
	public static boolean loadFromFile(GraphingPlatform gp, GraphModel gm, BufferedReader in, TransitionData td)
	{	
		try
		{
			float x1=0, y1=0, ctrlx1=0, ctrly1=0, ctrlx2=0, ctrly2=0, x2=0, y2=0, dx=0, dy=0;
			int gtx=Edge.DEFAULT_LABEL_DISPLACEMENT, gty=Edge.DEFAULT_LABEL_DISPLACEMENT, a=0;
			Node source=null, target=null;
			boolean X1=false, Y1=false, CTRLX1=false, CTRLY1=false, CTRLX2=false, CTRLY2=false, X2=false, Y2=false, SOURCE=false, TARGET=false;
			String transition_data_indicies = "";

			StringTokenizer tdi_tokenizer = null;
			
			String last_token = "";
	    	String this_token = null;
		    String this_line = in.readLine();
		    while (this_line != null) 
		    {
		    	StringTokenizer st = new StringTokenizer(this_line);
		        while (st.hasMoreTokens()) 
		        {
		        	this_token = st.nextToken();
		            if (this_token.equals("]")) 
		            { 
		            	if (X1 && Y1 && CTRLX1 && CTRLY1 && CTRLX2 && CTRLY2 && X2 && Y2 && SOURCE && TARGET) 
		            	{ 
		            		Edge e = new Edge(gp,gm,source,target,x1,y1,ctrlx1,ctrly1,ctrlx2,ctrly2,x2,y2,dx,dy,gtx,gty,a);
		            		gm.addEdge(e);
		            		if (transition_data_indicies.length() > 0) 
		            		{ 
		            			tdi_tokenizer = new StringTokenizer(transition_data_indicies,",");
		            			while (tdi_tokenizer.hasMoreTokens())
		            			{
			            			e.addLabel(td.edges_table.getItem(Integer.parseInt(tdi_tokenizer.nextToken()))); 		            				
		            			}
		            		}
		            		return true; 
		            	}  
		            	else { return false; }
		            }
		            else if (last_token.equals("x1")) { x1 = Float.parseFloat(this_token); X1 = true; }
		            else if (last_token.equals("y1")) { y1 = Float.parseFloat(this_token); Y1 = true; }
		            else if (last_token.equals("ctrlx1")) { ctrlx1 = Float.parseFloat(this_token); CTRLX1 = true; }
		            else if (last_token.equals("ctrly1")) { ctrly1 = Float.parseFloat(this_token); CTRLY1 = true; }
		            else if (last_token.equals("ctrlx2")) { ctrlx2 = Float.parseFloat(this_token); CTRLX2 = true; }
		            else if (last_token.equals("ctrly2")) { ctrly2 = Float.parseFloat(this_token); CTRLY2 = true; }
		            else if (last_token.equals("x2")) { x2 = Float.parseFloat(this_token); X2 = true; }
		            else if (last_token.equals("y2")) { y2 = Float.parseFloat(this_token); Y2 = true; }
		            else if (last_token.equals("tdi")) { transition_data_indicies = this_token; }
		            else if (last_token.equals("gtx")) { gtx = Integer.parseInt(this_token); }
		            else if (last_token.equals("gty")) { gty = Integer.parseInt(this_token); }
		            else if (last_token.equals("a")) { a = Integer.parseInt(this_token); }
		            else if (last_token.equals("source"))
		            {
		            	source = gm.getNodeById(Integer.parseInt(this_token));
		            	if (source != null) { SOURCE = true; }
		            }
		            else if (last_token.equals("target"))
		            {
		            	target = gm.getNodeById(Integer.parseInt(this_token));
		            	if (target != null) { TARGET = true; }
		            }
		            last_token = this_token;
		        }
		    	this_line = in.readLine();
		    }
		}
	    catch (Exception e)	{ throw new RuntimeException(e); } 
	    return false;
	}
	
	/**
	 * Output a representation of this Edge to file via the given PrintWriter.
	 * 
	 * @param	out		An initialized PrintWriter for outputing the data to file.
	 */	
	public void saveToFile(PrintWriter out)
	{
		String label_data_indicies_string = "";
		if (!labelDataIsNull())
		{
			label_data_indicies_string = "        tdi " + ((TableItem)label_data.elementAt(0)).getParent().indexOf((TableItem)label_data.elementAt(0));
			for (int i=1; i<label_data.size(); i++) { label_data_indicies_string = label_data_indicies_string + "," + ((TableItem)label_data.elementAt(i)).getParent().indexOf((TableItem)label_data.elementAt(i)); }
		}
		
		out.println("    edge [");
        out.println("        source " + gm.getId(n1));
        out.println("        target " + gm.getId(n2));

        curve.printWriterOut(out);

        out.println(label_data_indicies_string);
        out.println("        gtx " + label_displacement.x);
        out.println("        gty " + label_displacement.y);
        out.println("        a " + printAttributes());     
        out.println("    ]");
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Export to custom formats ///////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////			

	/**
     * Export the selection area to latex.
     * 
     * @param	box				A Rectangle representation of the SelectionArea.
     * @param	latex_printer	The object that stores and orders the generated output.
     */
	public void exportLatex(Box box, LatexPrinter latex_printer)
	{
		if (n1.x() > box.x1() && n1.x() < box.x2() && n1.y() > box.y1() && n1.y() < box.y2() && n2.x() > box.x1() && n2.x() < box.x2() && n2.y() > box.y1() && n2.y() < box.y2())
		{
			curve.exportLatex(box,latex_printer,gp.sv.use_pstricks,hasUncontrollableLabel());

			if (!labelDataIsNull())
			{
				// glyph_bounds x,y is top-left corner of text area, but has been expanded by LABEL_BOUND_FACTOR for drawing purposes
				// latex needs bottom-left corner, hence the adjustments.
				selectedLabel().exportLatex(latex_printer,box.x1(),box.y2());
			}
		}
	}
}
