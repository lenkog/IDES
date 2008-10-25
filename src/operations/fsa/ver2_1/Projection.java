/**
 * 
 */
package operations.fsa.ver2_1;

import ides.api.model.fsa.FSAModel;
import ides.api.plugin.model.ModelManager;

/**
 * @author Lenko Grigorov
 * @author Chris Dragert
 */
public class Projection extends AbstractOperation
{

	public Projection()
	{
		NAME = "project";
		DESCRIPTION = "Computes a projection"
				+ " of the given automaton such that all unobservable events"
				+ " have been removed.";

		// WARNING - Ensure that input type and description always match!
		inputType = new Class[] { FSAModel.class };
		inputDesc = new String[] { "Finite-state automaton" };

		// WARNING - Ensure that output type and description always match!
		outputType = new Class[] { FSAModel.class };
		outputDesc = new String[] { "Projected version of the automaton" };
	}

	/*
	 * (non-Javadoc)
	 * @see pluggable.operation.Operation#perform(java.lang.Object[])
	 */
	@Override
	public Object[] perform(Object[] inputs)
	{
		FSAModel a = ModelManager
				.instance().createModel(FSAModel.class, "none");
		Composition.observer((FSAModel)inputs[0], a);
		return new Object[] { a };
	}

}
