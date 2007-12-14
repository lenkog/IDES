/**
 * 
 */
package pluggable.layout.jung;

import java.util.Hashtable;

import presentation.fsa.Node;
import edu.uci.ics.jung.graph.Vertex;

/**
 * @author Lenko Grigorov
 */
public class BridgeMapper
{

	public static Hashtable<Node, Vertex> nodeMap = new Hashtable<Node, Vertex>();

	public static Hashtable<Vertex, Node> nodeMapInverse = new Hashtable<Vertex, Node>();
}
