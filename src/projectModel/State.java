package projectModel;

import java.util.*;

/**
 * 
 * @author Axel Gottlieb Michelsen
 *
 */
public class State extends AutomatonElement{
	private LinkedList sourceT, targetT;

	public State(){
		sourceT = new LinkedList<Transition>();
		targetT = new LinkedList<Transition>();
	}
	
	public void addSourceTransition(Transition t){
		sourceT.add(t);
	}
	public void removeSourceTransition(Transition t){
		sourceT.remove(t);
	}
	public ListIterator<Transition> getSourceTransitionsListIterator(){
		return sourceT.listIterator();
	}
	
	public void addTargetTransition(Transition t){
		targetT.add(t);
	}
	public void removeTargetTransition(Transition t){
		targetT.remove(t);
	}
	public ListIterator<Transition> getTargetTransitionListIterator(){
		return targetT.listIterator();
	}

}
