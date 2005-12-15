package com.aggressivesoftware.thirdparty.latex;

/**
 * Exception used by {@link Renderer} to announce problems pertaining to LaTeX rendering.
 * @author Lenko Grigorov
 */
public class LatexRenderException extends Exception {

	private static final long serialVersionUID = 1L;

	public LatexRenderException() {
		super();
	}

	public LatexRenderException(String arg0) {
		super(arg0);
	}

	public LatexRenderException(Throwable arg0) {
		super(arg0);
	}

	public LatexRenderException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
}
