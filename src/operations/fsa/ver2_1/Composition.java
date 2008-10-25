package operations.fsa.ver2_1;

import ides.api.core.Annotable;
import ides.api.model.fsa.FSAEvent;
import ides.api.model.fsa.FSAModel;
import ides.api.model.fsa.FSAState;
import ides.api.model.fsa.FSATransition;
import ides.api.plugin.model.ModelManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import model.fsa.ver2_1.Event;
import model.fsa.ver2_1.State;
import model.fsa.ver2_1.Transition;

/**
 * This class contains methods for composing new automata from existing
 * automata.
 * 
 * @author Kristian Edlund
 * @author Axel Gottlieb Michelsen
 * @author Lenko Grigorov
 * @author Chris Dragert
 */
public class Composition
{

	/**
	 * To be used to store the ids of pairs of states
	 */
	protected static Map<String, Long> pairIds = new TreeMap<String, Long>();

	/**
	 * Takes multiple automata and makes the product of them all
	 * 
	 * @param automata
	 *            an array of automata
	 * @param name
	 *            The name of the end product
	 * @return The result of the product
	 */
	public static FSAModel product(FSAModel[] automata, String name)
	{

		if (automata.length < 1)
		{
			return null;
		}
		else if (automata.length == 1)
		{
			return automata[0].clone();
		}

		FSAModel prevAnswer = ModelManager
				.instance().createModel(FSAModel.class, "temp");
		FSAModel newAnswer;

		product(automata[0], automata[1], prevAnswer);

		for (int i = 2; i < automata.length; i++)
		{
			newAnswer = ModelManager.instance().createModel(FSAModel.class,
					"temp");
			product(prevAnswer, automata[i], newAnswer);

			prevAnswer = newAnswer;
		}
		prevAnswer.setName(name);
		return prevAnswer;
	}

	/**
	 * Computes the shuffle product of the two automata a and b.
	 * 
	 * @param a
	 *            an automaton
	 * @param b
	 *            an automaton
	 * @param shuffle
	 *            the accesible product of a and b.
	 */
	public static void shuffle(FSAModel a, FSAModel b, FSAModel shuffle)
	{
		shuffle.setAnnotation(Annotable.COMPOSED_OF, new String[] {
				a.getName(), b.getName() });

		// the event set for the shuffle is the union of the two event sets
		// get all the events from 'a' first.
		ListIterator<FSAEvent> eventIterator = a.getEventIterator();
		while (eventIterator.hasNext())
		{
			// add the current event from 'a' to shuffle
			shuffle.add(new Event(eventIterator.next()));
		}

		// now get the events from b
		eventIterator = b.getEventIterator();
		while (eventIterator.hasNext())
		{
			// need to test each event to prevent duplications
			FSAEvent testEvent = eventIterator.next();
			// add the event if it isn't already in the event list
			if (!shuffle.getEventSet().contains(testEvent))
			{
				shuffle.add(new Event(testEvent));
			}
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
	public static void product(FSAModel a, FSAModel b, FSAModel product)
	{

		product.setAnnotation(Annotable.COMPOSED_OF, new String[] {
				a.getName(), b.getName() });

		// long eventid = 0;

		// Add the intersection between the eventsets as the products eventset.
		ListIterator<FSAEvent> eventsa = a.getEventIterator();
		while (eventsa.hasNext())
		{
			FSAEvent eventa = eventsa.next();
			ListIterator<FSAEvent> eventsb = b.getEventIterator();
			while (eventsb.hasNext())
			{
				FSAEvent eventb = eventsb.next();
				if (eventa.equals(eventb))
				{
					// TODO: is this right? Does the new event have the same
					// properties as the old event?
					FSAEvent event = new Event(eventa);
					// event.setId(eventid++);
					product.add(event);
					break;
				}
			}
		}

		// find initial states, mark them as reached and add them to the que
		FSAState[] initial = new FSAState[2];
		long stateNumber = 0;
		LinkedList<FSAState[]> searchList = new LinkedList<FSAState[]>();

		Iterator<FSAState> sia = a.getStateIterator();
		while (sia.hasNext())
		{
			initial[0] = sia.next();
			if (initial[0].isInitial())
			{
				Iterator<FSAState> sib = b.getStateIterator();
				while (sib.hasNext())
				{
					initial[1] = sib.next();
					if (initial[1].isInitial())
					{
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
		long transitionNumber = 0;
		FSAState[] s = new FSAState[2];
		while (!searchList.isEmpty())
		{
			FSAState[] sa = searchList.removeFirst();
			FSAState source = product.getState(getStateId(sa));

			ListIterator<FSATransition> sti0 = sa[0]
					.getOutgoingTransitionsListIterator();
			while (sti0.hasNext())
			{
				FSATransition t0 = sti0.next();
				ListIterator<FSATransition> sti1 = sa[1]
						.getOutgoingTransitionsListIterator();
				while (sti1.hasNext())
				{
					FSATransition t1 = sti1.next();
					if ((t0.getEvent() == null && t1.getEvent() == null)
							|| (t0.getEvent() != null && t1.getEvent() != null && t0
									.getEvent().equals(t1.getEvent())))
					{

						FSAEvent event = (t0.getEvent() == null) ? null
								: product.getEvent(t0.getEvent().getId());

						s[0] = t0.getTarget();
						s[1] = t1.getTarget();

						long id = getStateId(s);
						if (id != -1)
						{
							product.add(new Transition(
									transitionNumber++,
									source,
									product.getState(id),
									event));
						}
						else
						{
							FSAState target = makeState(s, stateNumber);
							product.add(target);
							product.add(new Transition(
									transitionNumber++,
									source,
									target,
									event));
							setStateId(s, stateNumber++);
							searchList.add(s.clone());
						}
					}
				}
			}
		}

		pairIds.clear();
	}

	/**
	 * Takes multiple automata and makes the product of them all
	 * 
	 * @param automata
	 *            an array of automata
	 * @param name
	 *            The name of the end product of the parallel composition
	 * @return The result of the parallel composition
	 */
	public static FSAModel parallel(FSAModel[] automata, String name)
	{

		if (automata.length < 1)
		{
			return null;
		}
		else if (automata.length == 1)
		{
			return automata[0].clone();
		}

		FSAModel prevAnswer = ModelManager
				.instance().createModel(FSAModel.class, "temp");
		FSAModel newAnswer;

		parallel(automata[0], automata[1], prevAnswer);

		for (int i = 2; i < automata.length; i++)
		{
			newAnswer = ModelManager.instance().createModel(FSAModel.class,
					"temp");
			parallel(prevAnswer, automata[i], newAnswer);
			prevAnswer = newAnswer;
		}
		prevAnswer.setName(name);
		return prevAnswer;
	}

	/**
	 * Computes the accessible parallel composition of the two automata a and b.
	 * 
	 * @param a
	 *            an automaton
	 * @param b
	 *            an automaton
	 * @param parallel
	 *            a pointer to the result for the accesible parallel product of
	 *            a and b.
	 */
	public static void parallel(FSAModel a, FSAModel b, FSAModel parallel)
	{

		parallel.setAnnotation(Annotable.COMPOSED_OF, new String[] {
				a.getName(), b.getName() });

		// Add the union of the eventsets as the parallel compositions eventset.
		// mark all events in the intersection as being in the intersection.
		long eventid = 0;

		// key=event from original automata,value=correpsponding new event in
		// result
		HashMap<FSAEvent, FSAEvent> events = new HashMap<FSAEvent, FSAEvent>();
		// key=new event,value=the two original events that intersect
		HashSet<FSAEvent> intersection = new HashSet<FSAEvent>();

		ListIterator<FSAEvent> it = a.getEventIterator();
		while (it.hasNext())
		{
			FSAEvent event = it.next();
			FSAEvent temp = new Event(event);
			temp.setId(eventid++);
			parallel.add(temp);
			events.put(event, temp);
		}

		it = b.getEventIterator();
		while (it.hasNext())
		{
			FSAEvent eventb = it.next();
			FSAEvent eventa = getId(eventb, a);
			if (eventa == null)
			{
				FSAEvent temp = new Event(eventb);
				temp.setId(eventid++);
				parallel.add(temp);
				events.put(eventb, temp);
			}
			else
			{
				FSAEvent e = events.get(eventa);
				intersection.add(e);
				events.put(eventb, e);
			}
		}

		// find initial states, mark them as reached and add them to the que
		FSAState[] initial = new FSAState[2];
		long stateNumber = 0;
		LinkedList<FSAState[]> searchList = new LinkedList<FSAState[]>();

		Iterator<FSAState> sia = a.getStateIterator();
		while (sia.hasNext())
		{
			initial[0] = sia.next();
			if (initial[0].isInitial())
			{
				Iterator<FSAState> sib = b.getStateIterator();
				while (sib.hasNext())
				{
					initial[1] = sib.next();
					if (initial[1].isInitial())
					{
						searchList.add(initial.clone());
						parallel.add(makeState(initial, stateNumber));
						setStateId(initial, stateNumber++);
					}
				}
			}
		}

		// accessibility. All accessible states are added to parallel.
		// Transitions are traversible if they can be traversed from both
		// states in sa firing the same event, i.e., the intersection of the
		// transitions
		// originating from the two
		// states are the transitions of state in product, or if the event
		// firing the transition isn't in the intersection between E_a and E_b.
		long transitionNumber = 0;
		FSAState[] s = new FSAState[2];
		while (!searchList.isEmpty())
		{
			FSAState[] sa = searchList.removeFirst();
			FSAState source = parallel.getState(getStateId(sa));

			// add all transitions in sa[0] and sa[1] that
			// aren't in the intersection between E_a and E_b
			for (int i = 0; i < 2; i++)
			{
				ListIterator<FSATransition> stli = sa[i]
						.getOutgoingTransitionsListIterator();
				while (stli.hasNext())
				{
					FSATransition t = stli.next();
					if (t.getEvent() == null
							|| !intersection.contains(events.get(t.getEvent())))
					{
						FSAEvent event = (t.getEvent() == null) ? null : events
								.get(t.getEvent());

						s[(i + 1) % 2] = sa[(i + 1) % 2];
						s[i] = t.getTarget();

						long id = getStateId(s);
						if (id != -1)
						{
							parallel.add(new Transition(
									transitionNumber++,
									source,
									parallel.getState(id),
									event));
						}
						else
						{
							FSAState target = makeState(s, stateNumber);
							parallel.add(target);
							parallel.add(new Transition(
									transitionNumber++,
									source,
									target,
									event));
							setStateId(s, stateNumber++);
							searchList.add(s.clone());
						}
					}
				}
			}

			ListIterator<FSATransition> sti0 = sa[0]
					.getOutgoingTransitionsListIterator();
			while (sti0.hasNext())
			{
				FSATransition t0 = sti0.next();
				if (t0.getEvent() != null
						&& !intersection.contains(events.get(t0.getEvent())))
				{
					continue;
				}
				ListIterator<FSATransition> sti1 = sa[1]
						.getOutgoingTransitionsListIterator();
				while (sti1.hasNext())
				{
					FSATransition t1 = sti1.next();
					if (t1.getEvent() != null
							&& !intersection
									.contains(events.get(t1.getEvent())))
					{
						continue;
					}
					// System.out.println(""+t0.getEvent()+", "+t1.getEvent()+".
					// "+)
					if ((t0.getEvent() == null && t1.getEvent() == null)
							|| (t0.getEvent() != null && t1.getEvent() != null && t0
									.getEvent().equals(t1.getEvent())))
					{

						FSAEvent event = (t0.getEvent() == null) ? null
								: events.get(t0.getEvent());

						s[0] = t0.getTarget();
						s[1] = t1.getTarget();

						long id = getStateId(s);
						if (id != -1)
						{
							parallel.add(new Transition(
									transitionNumber++,
									source,
									parallel.getState(id),
									event));
						}
						else
						{
							FSAState target = makeState(s, stateNumber);
							parallel.add(target);
							parallel.add(new Transition(
									transitionNumber++,
									source,
									target,
									event));
							setStateId(s, stateNumber++);
							searchList.add(s.clone());
						}
					}
				}
			}
		}

		pairIds.clear();
	}

	/**
	 * Computes an observer for the non-deterministic automaton P(a), i.e., the
	 * automaton where all unobservable events have been replaced with the empty
	 * event, epsilon.
	 * 
	 * @param a
	 *            a non-deterministic automaton
	 * @param observer
	 *            the output, a deterministic observer of the automaton a.
	 */
	public static void observer(FSAModel a, FSAModel observer)
	{

		observer.setAnnotation(Annotable.COMPOSED_OF, new String[] { a
				.getName() });

		// long eventid=0;

		ListIterator<FSAEvent> eli = a.getEventIterator();
		while (eli.hasNext())
		{
			FSAEvent e = eli.next();
			if (e.isObservable())
			{
				FSAEvent event = new Event(e);
				// event.setId(eventid++);
				observer.add(event);
			}
		}

		LinkedList<LinkedList<FSAState>> searchList = new LinkedList<LinkedList<FSAState>>();
		long id = 0, transitionid = 0;

		// find initial states, mark them as reached and add them to the
		// searchlist
		LinkedList<FSAState> states = new LinkedList<FSAState>();
		Iterator<FSAState> sia = a.getStateIterator();
		while (sia.hasNext())
		{
			FSAState initial = sia.next();
			if (initial.isInitial())
			{
				states.add(initial);
			}
		}
		if (states.size() == 0)
		{
			return;
		}
		unobservableReach(states);
		sort(states);
		FSAState rState = makeState(states, id, true);
		setIn(states, id++);

		observer.add(rState);
		searchList.add(states);
		FSAState target, source;

		states = new LinkedList<FSAState>();

		// find the accesible states in the observer.
		while (!searchList.isEmpty())
		{
			LinkedList<FSAState> sourceList = searchList.remove();
			source = observer.getState(isIn(sourceList));
			eli = a.getEventIterator();
			while (eli.hasNext())
			{
				FSAEvent event = eli.next();
				if (!event.isObservable())
				{
					continue;
				}
				ListIterator<FSAState> sli = sourceList.listIterator();
				while (sli.hasNext())
				{
					FSAState s = sli.next();
					ListIterator<FSATransition> tli = s
							.getOutgoingTransitionsListIterator();
					while (tli.hasNext())
					{
						FSATransition t = tli.next();
						if (t.getEvent().equals(event)
								&& !states.contains(t.getTarget()))
						{
							states.add(t.getTarget());
						}
					}
				}
				if (!states.isEmpty())
				{
					unobservableReach(states);
					sort(states);
					long stateid = isIn(states);
					if (stateid < 0)
					{
						target = makeState(states, id, false);
						setIn(states, id++);
						observer.add(target);
						searchList.add(states);
					}
					else
					{
						target = observer.getState(stateid);
					}
					event = (event == null) ? null : observer.getEvent(event
							.getId());
					FSATransition t = new Transition(
							transitionid++,
							source,
							target,
							event);
					observer.add(t);
					states = new LinkedList<FSAState>();
				}
			}
		}

		pairIds.clear();
	}

	/**
	 * Private function to check if a stateset is in the observer
	 * 
	 * @param sll
	 *            the stateset to check
	 * @return null if it is not in, else returns the id of the observer state
	 */
	private static long isIn(LinkedList<FSAState> sll)
	{
		Long id = pairIds.get(id(sll));
		if (id == null)
		{
			return -1;
		}
		else
		{
			return id.longValue();
		}
	}

	/**
	 * private function for setting an in subelement a state
	 * 
	 * @param sll
	 *            the stateset to set a new id in
	 * @param n
	 *            the new id
	 */
	private static void setIn(LinkedList<FSAState> sll, long n)
	{
		pairIds.put(id(sll), new Long(n));
	}

	/**
	 * Makes a string of the ides in a stateset
	 * 
	 * @param sll
	 *            the stateset to compile a string from
	 * @return the id string . seperated
	 */
	private static String id(LinkedList<FSAState> sll)
	{
		ListIterator<FSAState> sli = sll.listIterator();
		String name = "";
		while (sli.hasNext())
		{
			name += sli.next().getId() + ".";
		}
		return name;
	}

	/**
	 * Private function for making a new state from a stateset
	 * 
	 * @param sll
	 *            the state set to make a new state from
	 * @param id
	 *            the id it should use for the new state
	 * @param initial
	 *            sets the state as initial if needed
	 * @return the newly created state
	 */
	private static FSAState makeState(LinkedList<FSAState> sll, long id,
			boolean initial)
	{
		ListIterator<FSAState> sli = sll.listIterator();
		FSAState s;
		FSAState rs;
		boolean marked = false;
		int cId = 0;
		long[] compositionIds = new long[sll.size()];

		while (sli.hasNext())
		{
			s = sli.next();
			marked |= s.isMarked();
			compositionIds[cId++] = s.getId();
		}

		rs = new State(id);
		rs.setMarked(marked);
		rs.setInitial(initial);
		rs.setAnnotation(Annotable.COMPOSED_OF, compositionIds);
		return rs;
	}

	/**
	 * Sorting algorithm for sorting a list of states after id.
	 * 
	 * @param sll
	 *            the list of states to sort
	 */
	private static void sort(LinkedList<FSAState> sll)
	{
		Collections.sort(sll, new Comparator<FSAState>()
		{
			public int compare(FSAState s1, FSAState s2)
			{
				return (int)Math.signum(s1.getId() - s2.getId());
			}
		});
	}

	/**
	 * calculates the unoberservable reach for a stateset
	 * 
	 * @param sll
	 *            the stateset to use.
	 */
	private static void unobservableReach(LinkedList<FSAState> sll)
	{
		ListIterator<FSAState> sli = sll.listIterator();
		HashSet<FSAState> reached = new HashSet<FSAState>();
		while (sli.hasNext())
		{
			reached.add(sli.next());
		}
		sli = sll.listIterator();
		while (sli.hasNext())
		{
			FSAState s = sli.next();
			ListIterator<FSATransition> stli = s
					.getOutgoingTransitionsListIterator();
			while (stli.hasNext())
			{
				FSATransition t = stli.next();
				if ((t.getEvent() == null || !t.getEvent().isObservable())
						&& !reached.contains(t.getTarget()))
				{
					reached.add(t.getTarget());
					sli.add(t.getTarget());
					sli.previous();
				}
			}
		}
	}

	/**
	 * Gets the id for an event in another automaton. It is used for finding an
	 * event id based on the event name.
	 * 
	 * @param e
	 *            the event to search for
	 * @param a
	 *            the automaton to search in
	 * @return returns -1 if it couldn't find the event, else returns the id of
	 *         the event in automaton a
	 */
	private static FSAEvent getId(FSAEvent e, FSAModel a)
	{
		ListIterator<FSAEvent> eli = a.getEventIterator();
		while (eli.hasNext())
		{
			FSAEvent temp = (FSAEvent)eli.next();
			if (temp.equals(e))
			{
				return temp;
			}
		}
		return null;
	}

	/**
	 * Private function for making a new state from a stateset
	 * 
	 * @param s
	 *            the stateset to make a new state from
	 * @param stateNumber
	 *            the id of the new state
	 * @return the newly created state
	 */
	private static FSAState makeState(FSAState[] s, long stateNumber)
	{
		FSAState state = new State(stateNumber);
		// SubElement name = new SubElement("name");
		// name.setChars("(" + s[0].getSubElement("name").getChars() + ", "
		// + s[1].getSubElement("name").getChars() + ")");
		// state.addSubElement(name);

		// SubElement properties = new SubElement("properties");

		// state.setName("{" + s[0].getName() + "," + s[1].getName() + "}");
		state.setAnnotation(Annotable.COMPOSED_OF, new long[] { s[0].getId(),
				s[1].getId() });

		if (s[0].isInitial() && s[1].isInitial())
		{
			state.setInitial(true);
		}

		if (s[0].isMarked() && s[1].isMarked())
		{
			state.setMarked(true);
		}
		return state;
	}

	/**
	 * set the stateid for a set of states
	 * 
	 * @param s
	 *            the stateset
	 * @param stateId
	 *            the id to set
	 */
	private static void setStateId(FSAState[] s, long stateId)
	{
		pairIds.put("" + s[0].getId() + "," + s[1].getId(), new Long(stateId));
	}

	/**
	 * Gets the id from a set of states
	 * 
	 * @param s
	 *            the stateset
	 * @return the id of the stateset
	 */
	private static long getStateId(FSAState[] s)
	{
		String key = "" + s[0].getId() + "," + s[1].getId();
		if (pairIds.containsKey(key))
		{
			return pairIds.get(key).longValue();
		}
		return -1;
	}
}
