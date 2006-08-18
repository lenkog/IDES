/**
 * 
 */
package presentation.fsa;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D.Float;

/**
 * TODO
 * What should the layout and handler look like for this class?
 * Implement insertAmong and intersectionWithBoundary
 * Implement draw method
 * 
 * @author helen bretzke
 *
 */
public class InitialArrow extends Edge {

	private ArrowHead arrowHead = new ArrowHead();
	private GeneralPath shaft = new GeneralPath();
	
	
	
	/**
	 * Initial node pointing to the given target node with null source node. 
	 * 
	 * @param target
	 */
	public InitialArrow(Node target) {
		super(null, target);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see presentation.fsa.Edge#createExportString(java.awt.Rectangle, int)
	 */
	@Override
	public String createExportString(Rectangle selectionBox, int exportType) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see presentation.fsa.Edge#intersectionWithBoundary(presentation.fsa.Node)
	 */
	@Override
	public Float intersectionWithBoundary(Node node, int type) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see presentation.fsa.Edge#computeEdge(presentation.fsa.Node, presentation.fsa.Node)
	 */
	@Override
	public void computeEdge() {
				
	}

	/**
	 * Does nothing since initial arrows don't have events.
	 */
	@Override
	public void addEventName(String symbol) {}

	/* (non-Javadoc)
	 * @see presentation.fsa.Edge#isStraight()
	 */
	@Override
	public boolean isStraight() {		
		return true;
	}

	/* (non-Javadoc)
	 * @see presentation.fsa.Edge#getSourceEndPoint()
	 */
	@Override
	public Float getSourceEndPoint() {
		// TODO ////////////////////////////////
		return null;
	}

	/* (non-Javadoc)
	 * @see presentation.fsa.Edge#getTargetEndPoint()
	 */
	@Override
	public Float getTargetEndPoint() {		
		return arrowHead.getBasePt();
	}

}
