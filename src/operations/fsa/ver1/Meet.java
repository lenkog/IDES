/**
 * 
 */
package operations.fsa.ver1;

import model.fsa.FSAModel;
import model.fsa.ver1.Automaton;
import pluggable.operation.Operation;

/**
 * @author Lenko Grigorov
 *
 */
public class Meet implements Operation {

	public final static String NAME="meet";
	
	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getName()
	 */
	public String getName() {
		return NAME;
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getNumberOfInputs()
	 */
	public int getNumberOfInputs() {
		// TODO Auto-generated method stub
		return 2;
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getTypeOfInputs()
	 */
	public Class[] getTypeOfInputs() {
		return new Class[]{FSAModel.class,FSAModel.class};
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getDescriptionOfInputs()
	 */
	public String[] getDescriptionOfInputs() {
		return new String[]{"Finite-state automaton","Finite-state automaton"};
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getNumberOfOutputs()
	 */
	public int getNumberOfOutputs() {
		return 1;
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getTypeOfOutputs()
	 */
	public Class[] getTypeOfOutputs() {
		return new Class[]{FSAModel.class};
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#getDescriptionOfOutputs()
	 */
	public String[] getDescriptionOfOutputs() {
		return new String[]{"Meet of two automata"};
	}

	/* (non-Javadoc)
	 * @see pluggable.operation.Operation#perform(java.lang.Object[])
	 */
	public Object[] perform(Object[] inputs) {
		Automaton a=new Automaton("none");
		Composition.product((FSAModel)inputs[0],(FSAModel)inputs[1],a);
		return new Object[]{a};
	}

}
