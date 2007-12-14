/**
 * 
 */
package operations.fsa.ver2_1;

import model.fsa.FSAModel;
import pluggable.operation.FilterOperation;

/**
 * @author Lenko Grigorov
 * @author Chris Dragert
 */
public class Coaccessible extends AbstractOperation implements FilterOperation
{

	public Coaccessible()
	{
		NAME = "coaccessible";
		DESCRIPTION = "Removes all states from which "
				+ "a marked state cannot be reached.";

		// WARNING - Ensure that input type and description always match!
		inputType = new Class[] { FSAModel.class };
		inputDesc = new String[] { "Finite-state Automaton" };

		// WARNING - Ensure that output type and description always match!
		outputType = new Class[] { FSAModel.class };
		outputDesc = new String[] { "Finite-state Automaton" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pluggable.operation.Operation#perform(java.lang.Object[])
	 */
	@Override
	public Object[] perform(Object[] inputs)
	{
		FSAModel a = ((FSAModel)inputs[0]).clone();
		Unary.buildStateCompositionOfClone(a);
		Unary.coaccessible(a);
		return new Object[] { a };
	}

	public int[] getInputOutputIndexes()
	{
		return new int[] { 0 };
	}

	public Object[] filter(Object[] inputs)
	{
		FSAModel a = (FSAModel)inputs[0];
		Unary.coaccessible(a);
		return new Object[] { a };
	}
}
