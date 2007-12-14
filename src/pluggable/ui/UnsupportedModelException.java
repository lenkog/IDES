package pluggable.ui;

import model.DESModel;
import presentation.LayoutShell;

/**
 * Exception thrown when a {@link Toolset} does not support the type of
 * {@link DESModel} or {@link LayoutShell} which is passed as an argument to a
 * method of the toolset.
 * 
 * @author Lenko Grigorov
 */
public class UnsupportedModelException extends RuntimeException
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1528723636728510491L;

	public UnsupportedModelException()
	{
	}

	public UnsupportedModelException(String arg0)
	{
		super(arg0);
	}

	public UnsupportedModelException(Throwable arg0)
	{
		super(arg0);
	}

	public UnsupportedModelException(String arg0, Throwable arg1)
	{
		super(arg0, arg1);
	}
}
