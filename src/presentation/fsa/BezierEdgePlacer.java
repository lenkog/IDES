/**
 * 
 */
package presentation.fsa;

import java.awt.geom.CubicCurve2D;
import java.util.Set;

import presentation.GraphicalLayout;

/**
 * Algorithms for laying out a Bezier edge among a set of existing edges
 * connecting the same pair of nodes.
 * 
 * @author Helen Bretzke for DES Lab, Dept of ECE, Queen's University, Kingston
 * @date 13 August 2006
 */
public class BezierEdgePlacer {

	/**
	 * Inserts <code>edge</code> among the given set of other edges
	 * where all edges connect the same pair of nodes.
	 * Computes the layouts for each edge such that they are distributed comfortably
	 * along the circumferences of each end node.
	 * 
	 * @param edge the edge to be inserted
	 * @param edges set of all other edges connecting the same pair of nodes
	 */
	public static void insertEdgeAmong(BezierEdge edge, Set<Edge> edges)
	{
		// compute default straight edge
		edge.computeEdge();
		
		int n = edges.size();
		Edge straightEdge = containsStraightEdge(edges);
		if(straightEdge != null) {			
			// find outermost position
			BezierLayout outPos = setToOutermostFreeLayout(edge, edges);
	
			// move the straight edge to reflection of edge
			BezierLayout reflection = outPos.reflect();	
			if( straightEdge.getSource().equals(edge.getTarget())){
				// swap angles (for reversed arrow direction)
				reflection.swapAngles();				
			}
			((BezierLayout)straightEdge.getLayout()).setCurve(reflection.getCurve());
			// TODO if we only call this to update the endpts, then call a method that is named appropriately
			straightEdge.computeEdge();
			
			// unless that position is taken
			if(tooClose(straightEdge, edges))
			{				
				((BezierLayout)straightEdge.getLayout()).arcMore(false);
			}			
		}else{	// No straight edge
			if(n % 2 != 0) // Even # of neighbours			
			{
//				 Odd # of neighbours
				// LENKO: Why not use straight position?
				// For now...
				//edge.computeEdge();				
				
				// LATER
				// find edge at outermost position
				
				// place new edge symmetric to found edge
				
			} 
			// otherwise do nothing since edge is already straight by default						
		}			
	}
	
	/**
	 * Side-effect: edge is set to the outermost layout found.
	 * Either don't do this and use a temp edge, or don't return the layout.
	 * 
	 * ??? Precondition: there is already a straight edge in <code>edges</code>. 
	 * 
	 * @param edges
	 * @return the layout for the first free outermost position
	 */
	private static BezierLayout setToOutermostFreeLayout(Edge edge, Set<Edge> edges) 
	{	
		BezierLayout layout = findOutermostTakenPosition(edges);
		if(layout != null)
		{
			// if curve is 'S'-shaped
			/*if(layout.angle1 * layout.angle2 < 0)
			{
			// LENKO can we tolerate the intersection of two reflected 'S' curves?
				
			}else{
			*/
				// otherwise, try reflected position
				BezierLayout reflection = layout.reflect();
				edge.setLayout(reflection);
				edge.computeEdge();
				
				// if tooClose, arc the layout more
				// ??? or the reflection?
				if(tooClose(edge, edges))
				{                                                
					reflection.arcMore();
				}				
			//}
			return layout;
		}
		// ??? How should this be initialized ?
		return new BezierLayout();
	}


	/**
	 * NOTE Outermost is defined as having the greatest arc in the curve. 
	 * 
	 * Precondition: edges is not empty
	 * 
	 * @param edges
	 * @return the layout for the outermost non-flat, occupied edge layout among <code>edges</code> 
	 * if all edges are flat, returns a flat layout. 
	 */
	private static BezierLayout findOutermostTakenPosition(Set<Edge> edges) 
	{
		BezierLayout layout = null;
		double max = 0;
		for(Edge edge : edges)
		{
			double flatness = ((BezierLayout)edge.getLayout()).getCurve().getFlatness();
			if(flatness >= max)
			{
				max = flatness;
				layout = (BezierLayout)edge.getLayout();
			}
			
		}		
	  	return layout;		
	}
	
	/**
	 * 
	 * @param layout
	 * @param edges
	 * @return true iff <code>layout</code> is already present in <code>edges</code>
	 */
	private static boolean tooClose(Edge edge1, Set<Edge> edges)
	{
		// TODO find a nice-looking min distance
		// minimum comfortable distance between endpoints to allow margins 
		// for arrow head along node boundary
		double min = ArrowHead.SHORT_HEAD_LENGTH;
	
		for(Edge edge : edges)
		{
			assert(edge.getSourceEndPoint() != null);			
			assert(edge.getTargetEndPoint() != null);		
			
			// check if any pair of visible endpoints (intersections with node boundary) are too close
			// FIXME this may not be the best way to test for proximity...
			return (edge.getSourceEndPoint().distance(edge1.getSourceEndPoint()) > min)
					|| (edge.getSourceEndPoint().distance(edge1.getTargetEndPoint()) > min)
					|| (edge.getTargetEndPoint().distance(edge1.getTargetEndPoint()) > min)
					|| (edge.getTargetEndPoint().distance(edge1.getSourceEndPoint()) > min);			
		}
		return false;
	}
	
	/**
	 * @param edges
	 * @return the first straight edge found in <code>edges</code>
	 */
	private static Edge containsStraightEdge(Set<Edge> edges) {
		for(Edge edge : edges)
		{
			if(edge.isStraight())
			{
				return edge;
			}
		}
		return null;
	}
}