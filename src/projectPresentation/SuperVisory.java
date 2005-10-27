/**
 * 
 */
package projectPresentation;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import projectModel.Automaton;
import projectModel.Event;
import projectModel.State;
import projectModel.SubElement;
import projectModel.Transition;

/**
 * @author edlund
 * 
 */
public class SuperVisory{

    public static void supC(Automaton plant, Automaton legal, Automaton result){

        // This is implemented accourding to "Introduction to discrete event
        // systems of Casandras and LaFortune.
        // Page 177

        // step 1
        
        supCProduct(plant, legal, result);

       boolean changed = true;
        
        while(changed){
            // step 2
            ListIterator<State> si = result.getStateIterator();

            while(si.hasNext()){
                changed = false;
                State s = si.next();

                State pn = plant.getState(Integer.parseInt(s.getSubElement("plantID").getChars()));
                ListIterator<Transition> pti = pn.getSourceTransitionsListIterator();
                while(pti.hasNext()){
                    Transition pt = pti.next();
                    if(!pt.getEvent().getSubElement("properties").hasSubElement("controllable")){
                        ListIterator<Transition> sti = s.getSourceTransitionsListIterator();
                        boolean hasTrans = false;                            
                        while(sti.hasNext()){
                            if(sti.next().getEvent().getId() == pt.getEvent().getId()){
                                hasTrans = true;
                                break;
                            }                                                        
                        }
                        if(!hasTrans){
                            si.remove();
                            changed = true;  
                            break;
                        }
                    }                
                }    
            }        
            Unary.trim(result);
        
            if(result.getStateCount() == 0) return;
        }
        
    }

    /**
     * Computes the accessible product of the two automata a and b.
     * 
     * @param a
     *            an automaton
     * @param b
     *            an automaton
     * @param product
     *            the accesible product of a and b.
     */
    public static void supCProduct(Automaton a, Automaton b, Automaton product){
        // Add the intersection between the eventsets as the products eventset.
        ListIterator<Event> eventsa = a.getEventIterator();
        while(eventsa.hasNext()){
            Event eventa = eventsa.next();
            ListIterator<Event> eventsb = b.getEventIterator();
            while(eventsb.hasNext()){
                Event eventb = eventsb.next();
                if(eventa.getSubElement("name").getChars().equals(
                        eventb.getSubElement("name").getChars())){
                    // is this right? Does the new event have the same
                    // properties as the old event?
                    Event event = new Event(eventa);
                    product.add(event);
                    break;
                }
            }
        }

        // find initial states, mark them as reached and add them to the que
        State[] initial = new State[2];
        int stateNumber = 0;
        LinkedList<State[]> searchList = new LinkedList<State[]>();

        Iterator<State> sia = a.getStateIterator();
        while(sia.hasNext()){
            initial[0] = sia.next();
            if(initial[0].getSubElement("properties").hasSubElement("initial")){
                Iterator<State> sib = b.getStateIterator();
                while(sib.hasNext()){
                    initial[1] = sib.next();
                    if(initial[1].getSubElement("properties").hasSubElement("initial")){
                        searchList.add(initial.clone());
                        product.add(makeState(initial, stateNumber));
                        setStateId(initial, stateNumber++);
                    }
                }
            }
        }

        // accessibility. All accessible states are added to product.
        // Transitions are only traversible if they can be traversed from both
        // states in sa
        // firing the same event, i.e., the intersection of the transitions
        // originating from the two
        // states are the transitions of state in product.
        int transitionNumber = 0;
        State[] s = new State[2];
        while(!searchList.isEmpty()){
            State[] sa = searchList.removeFirst();
            State source = product.getState(getStateId(sa));

            ListIterator<Transition> sti0 = sa[0].getSourceTransitionsListIterator();
            while(sti0.hasNext()){
                Transition t0 = sti0.next();
                ListIterator<Transition> sti1 = sa[1].getSourceTransitionsListIterator();
                while(sti1.hasNext()){
                    Transition t1 = sti1.next();
                    if((t0.getEvent() == null && t1.getEvent() == null || (t0.getEvent() != null
                            && t1.getEvent() != null && t0.getEvent().getSubElement("name")
                            .getChars().equals(t1.getEvent().getSubElement("name").getChars())))){

                        Event event = (t0.getEvent() == null) ? null : product.getEvent(t0
                                .getEvent().getId());

                        s[0] = t0.getTarget();
                        s[1] = t1.getTarget();

                        int id = getStateId(s);
                        if(id != -1){
                            product.add(new Transition(transitionNumber++, source, product
                                    .getState(id), event));
                        }
                        else{
                            State target = makeState(s, stateNumber);
                            product.add(target);
                            product.add(new Transition(transitionNumber++, source, target, event));
                            setStateId(s, stateNumber++);
                            searchList.add(s.clone());
                        }
                    }
                }
            }
        }
        // tidy up the mess I left.
        ListIterator<State> sli = a.getStateIterator();
        while(sli.hasNext()){
            sli.next().removeSubElement("searched");
        }
    }
    
    private static State makeState(State[] s, int stateNumber){
        State state = new State(stateNumber);
        SubElement name = new SubElement("name");
        name.setChars(s[0].getSubElement("name").getChars() + ", " + s[1].getSubElement("name").getChars());
        state.addSubElement(name);

        SubElement plantID = new SubElement("plantID");
        plantID.setChars(Integer.toString(s[0].getId()));
        state.addSubElement(plantID);
        
        SubElement properties = new SubElement("properties");

        if(s[0].getSubElement("properties").hasSubElement("initial") && s[1].getSubElement("properties").hasSubElement("initial")){
            SubElement initial = new SubElement("initial");
            properties.addSubElement(initial);
        }

        if(s[1].getSubElement("properties").hasSubElement("marked")){
            SubElement marked = new SubElement("marked");
            properties.addSubElement(marked);
        }
        state.addSubElement(properties);
        return state;
    }
    
    private static void setStateId(State[] s, int stateId){
        if(!s[0].hasSubElement("searched")) s[0].addSubElement(new SubElement("searched"));
        s[0].getSubElement("searched").setAttribute(Integer.toString(s[1].getId()),Integer.toString(stateId));
    }
    
    
    private static int getStateId(State[] s){
        if(s[0].hasSubElement("searched") && s[0].getSubElement("searched").hasAttribute(Integer.toString(s[1].getId()))){
            return Integer.parseInt(s[0].getSubElement("searched").getAttribute(Integer.toString(s[1].getId())));
        }
        return -1;
    }

}
