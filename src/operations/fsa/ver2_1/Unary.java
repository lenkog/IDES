/**
 * Contains all operations defined as unary operations
 */
package operations.fsa.ver2_1;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import model.fsa.FSAModel;
import model.fsa.FSAState;
import model.fsa.FSATransition;
import model.fsa.ver2_1.Automaton;
import model.fsa.ver2_1.State;


/**
 * This class contatins methods for unary operations on automata.
 * 
 * @author Kristian Edlund
 * @author Axel Gottlieb Michelsen
 * @author Lenko Grigorov
 */
public class Unary{
    /**
     * Finds the accessible part of an automaton
     * The function does not return a new automaton, so if the original automaton should not be changed,
     *  make sure to copy it first.
     * @param automaton The automaton which to find the accessible part of
     */    
    public static void accessible(FSAModel automaton){
    	HashSet<FSAState> accessible=new HashSet<FSAState>();

        LinkedList<FSAState> searchList = new LinkedList<FSAState>();
        // find initial states, mark them as reached and add them to the que
        Iterator<FSAState> stateIterator = automaton.getStateIterator();
        while(stateIterator.hasNext()){
            FSAState state = stateIterator.next();
            if(state.isInitial()){
                searchList.addFirst(state);
                accessible.add(state);
            }
        }
        // for all accesible states
        while(!searchList.isEmpty()){
            FSAState state = searchList.removeFirst();
            // mark all states that are accesible from this state as accesible
            // if they have not previously been marked as accesible.
            Iterator<FSATransition> transitionIterator = state.getSourceTransitionsListIterator();
            while(transitionIterator.hasNext()){
                FSATransition transition = transitionIterator.next();
                if(!accessible.contains(transition.getTarget())){
                	accessible.add(transition.getTarget());
                    searchList.addFirst(transition.getTarget());
                }
            }
        }
        
        // remove all states that aren't accessible.
        stateIterator = automaton.getStateIterator();
        while(stateIterator.hasNext()){
            FSAState state = stateIterator.next();
            if(!accessible.contains(state)) stateIterator.remove();
        }
    }
    
    /**
     * Finds the coaccessible part of an automaton
     * The function does not return a new automaton, so if the original automaton should not be changed,
     * make sure to copy it first.
     * @param automaton The automaton which to find the coaccessible part of
     */  
    public static  void coaccessible(FSAModel automaton){
    	HashSet<FSAState> coaccessible=new HashSet<FSAState>();

    	LinkedList<FSAState> searchList = new LinkedList<FSAState>();
        ListIterator<FSAState> states = automaton.getStateIterator();
        // mark all marked states as coaccessible and add them to the list.
        while(states.hasNext()){
            FSAState s = states.next();
            if(s.isMarked()){
                coaccessible.add(s);
                searchList.add(s);
            }
        }
        // for all states in the list mark all states that can access this state
        // as coaccessible and add it to the list (if it isn't allready marked as
        // coaccessible.)
        while(!searchList.isEmpty()){
            FSAState s = searchList.removeFirst();
            ListIterator<FSATransition> tli = s.getTargetTransitionListIterator();
            while(tli.hasNext()){
                FSAState source = tli.next().getSource();
                if(!coaccessible.contains(source)){
                    coaccessible.add(source);
                    searchList.addFirst(source);
                }
            }
        }
        // Remove all states that aren't coaccessible.
        states = automaton.getStateIterator();
        while(states.hasNext()){
            State s = (State) states.next();
            if(!coaccessible.contains(s)) states.remove();
        }
    }
    
    /**
     * Finds the trim part of an automaton
     * The function does not return a new automaton, so if the original automaton should not be changed,
     * make sure to copy it first.
     * @param automaton The automaton which to trim.
     */   
    public static void trim(FSAModel automaton){
        accessible(automaton);
        coaccessible(automaton);
    }
    
    /**
     * Makes an automaton prefix closed
     * The function does not return a new automaton, so if the original automaton should not be changed,
     * make sure to copy it first.
     * @param automaton The automaton to prefix close
     */    
    public static void prefixClosure(FSAModel automaton){
        LinkedList<FSAState> searchList = new LinkedList<FSAState>();
        ListIterator<FSAState> states = automaton.getStateIterator();
        // add all marked states to the list.
        while(states.hasNext()){
            FSAState s = states.next();
            if(s.isMarked()){
                searchList.add(s);
            }
        }
        // for all states in the list mark all states that can access this state
        // as marked and add it to the list (if it isn't allready marked.)
        while(!searchList.isEmpty()){
            FSAState s = searchList.removeFirst();
            ListIterator<FSATransition> tli = s.getTargetTransitionListIterator();
            while(tli.hasNext()){
                FSAState source = tli.next().getSource();
                if(!source.isMarked()){
                    source.setMarked(true);
                    searchList.addFirst(source);
                }
            }
        }
    }
    
    public static void buildStateCompositionOfClone(FSAModel clone)
    {
    	for(Iterator<FSAState> i=clone.getStateIterator();i.hasNext();)
    	{
    		State s=(State)i.next();
    		s.setStateCompositionList(new long[]{s.getId()});
    	}
    }
}