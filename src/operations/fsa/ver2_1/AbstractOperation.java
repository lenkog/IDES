/**
 * 
 */
package operations.fsa.ver2_1;

import ides.api.plugin.operation.Operation;

import java.util.Vector;

/**
 * @author Chris Dragert This is a helper class for the operations. All
 *         operations implement the operation methods in the same manner, except
 *         for perform, so this class exists to prevent duplication and ensure
 *         code consistency. All operations should extend this class for
 *         convenience. Alternatively, new operations can directly implement the
 *         pluggable.operation.Operation interface if the implementation here is
 *         inadequate
 */
public abstract class AbstractOperation implements Operation
{

	// member variables
	protected String NAME;

	protected String DESCRIPTION;

	// WARNING - Ensure that input type and description always match!
	protected Class<?>[] inputType;

	protected String[] inputDesc;

	// WARNING - Ensure that output type and description always match!
	protected Class<?>[] outputType;

	protected String[] outputDesc;

	// contains any warnings generated by the operation
	protected Vector<String> warnings;

	/**
	 * Name of the operation. The same string will be used to index and identify
	 * the operation as well as for display purposes.
	 * 
	 * @return the name of the operation
	 */
	public String getName()
	{
		return NAME;
	}

	/**
	 * Description of the operation. This should be approximately one sentence
	 * (i.e. 10-20 words in length) that describes the operation
	 * 
	 * @return the description of the operation
	 */
	public String getDescription()
	{
		return DESCRIPTION;
	}

	/**
	 * Number of inputs for the operation. If it can handle an unbounded number
	 * of inputs, return -1.
	 * 
	 * @return number of inputs for the operation; -1 if unbounded
	 */
	public int getNumberOfInputs()
	{
		return inputType.length;
	}

	/**
	 * Class types of the inputs. If the inputs are unbounded, the last supplied
	 * class type will be assumed for all inputs with a higher index. E.g., if {
	 * <code>Boolean</code>,<code>FSAModel</code> are supplied, the first input
	 * will be <code>Boolean</code> and all remaining inputs are assumed to be
	 * of type <code>FSAModel</code>. This type extension is applied only when
	 * the number of inputs is unbounded (i.e., when
	 * {@link #getNumberOfInputs()} returns -1).
	 * <p>
	 * Whenever possible, please use the general interfaces from the package
	 * <code>model.*</code> (such as <code>FSAModel</code>) instead of specific
	 * implementations.
	 * 
	 * @return class types of the inputs
	 * @see #getNumberOfInputs()
	 * @see #getDescriptionOfInputs()
	 */
	public Class<?>[] getTypeOfInputs()
	{
		return inputType;
	}

	/**
	 * User-readable and understandable description for each input argument. If
	 * the inputs are unbounded, the last supplied description will be assumed
	 * for all inputs with a higher index. E.g., if {<code>"String 1"</code>,
	 * <code>"String 2"</code> are supplied, the first input will be described
	 * to the user as <code>"String 1"</code> and all remaining inputs will be
	 * described as <code>"String 2"</code>. This type extension is applied only
	 * when the number of inputs is unbounded (i.e., when
	 * {@link #getNumberOfInputs()} returns -1).
	 * 
	 * @return user-readable descriptions of the inputs
	 * @see #getNumberOfInputs()
	 */
	public String[] getDescriptionOfInputs()
	{
		return inputDesc;
	}

	/**
	 * Number of outputs from the operation.
	 * 
	 * @return number of outputs from the operation
	 */
	public int getNumberOfOutputs()
	{
		return outputType.length;
	}

	/**
	 * Class types of the outputs.
	 * <p>
	 * Whenever possible, please use the general interfaces from the package
	 * <code>model.*</code> (such as <code>FSAModel</code>) instead of specific
	 * implementations.
	 * 
	 * @return class types of the inputs
	 * @see #getNumberOfOutputs()
	 * @see #getDescriptionOfOutputs()
	 */
	public Class<?>[] getTypeOfOutputs()
	{
		return outputType;
	}

	/**
	 * User-readable and understandable description for each output argument.
	 * 
	 * @return user-readable descriptions of the outputs
	 * @see #getNumberOfOutputs()
	 */
	public String[] getDescriptionOfOutputs()
	{
		return outputDesc;
	}

	/*
	 * (non-Javadoc)
	 * @see pluggable.operation.Operation#perform(java.lang.Object[])
	 */
	public abstract Object[] perform(Object[] inputs);

	public Vector<String> getWarnings()
	{
		return warnings;
	}

}
