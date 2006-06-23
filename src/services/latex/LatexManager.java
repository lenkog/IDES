package services.latex;

import java.io.File;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.pietschy.command.ToggleCommand;

import presentation.fsa.GraphModel;

import ui.OptionsWindow;
import ui.command.OptionsCommands;

import main.Hub;

/**
 * Coordinates the LaTeX rendering.
 * 
 * @author Lenko Grigorov
 */
public class LatexManager {

	private LatexManager()
	{
	}
	
	public Object clone()
	{
	    throw new RuntimeException("Cloning of "+this.getClass().toString()+" not supported."); 
	}

	/**
	 * The LaTeX renderer to be used for rendering throughout the program.
	 */
	private static Renderer renderer=null; 
	
	/**
	 * The "Use LaTeX rendering" menu item.
	 */
	static ToggleCommand menuItem=null;
	
	/**
	 * Initializes the LaTeX rendering subsystem.
	 */
	public static void init()
	{
		Hub.registerOptionsPane(new LatexOptionsPane());
		renderer=Renderer.getRenderer(new File(getLatexPath()),new File(getGSPath()));
		menuItem=new UseLatexCommand();
		menuItem.export();
	}

	/**
	 * Returns the path to the directory of the <code>latex</code> and
	 * <code>dvips</code> executables.
	 * @return path to the directory of the <code>latex</code> and <code>dvips</code> executables
	 */
	static String getLatexPath()
	{
		return Hub.persistentData.getProperty("latexPath");
	}
	
	/**
	 * Returns the path to the GhostScript executable file.
	 * @return the path to the GhostScript executable file
	 */
	static String getGSPath()
	{
		return Hub.persistentData.getProperty("gsPath");
	}

	/**
	 * Sets the path to the directory of the <code>latex</code> and
	 * <code>dvips</code> executables.
	 * @param path path to the directory of the <code>latex</code> and <code>dvips</code> executables
	 */
	static void setLatexPath(String path)
	{
		Hub.persistentData.setProperty("latexPath",path);
		renderer=Renderer.getRenderer(new File(getLatexPath()),new File(getGSPath()));
	}
	
	/**
	 * Sets the path to the GhostScript executable file.
	 * @param path path to the GhostScript executable file
	 */
	static void setGSPath(String path)
	{
		Hub.persistentData.setProperty("gsPath",path);
		renderer=Renderer.getRenderer(new File(getLatexPath()),new File(getGSPath()));
	}
	
	/**
	 * Returns <code>true</code> if LaTeX rendering of labels is on,
	 * <code>false</code> otherwise.
	 * @return <code>true</code> if LaTeX rendering of labels is on, <code>false</code> otherwise
	 */
	public static boolean isLatexEnabled()
	{
		return Hub.persistentData.getBoolean("useLatexLabels");
	}
	
	/**
	 * A {@link Runnable} that updates the LaTeX redering settings.
	 * This is needed since the {@link LatexPrerenderer} displays its
	 * progress; thus the updating cannot be done inside the Swing
	 * event loop.
	 * @see LatexManager#setLatexEnabled(boolean)
	 * @see LatexManager#setLatexEnabledFromMenu(boolean)
	 *
	 * @author Lenko Grigorov
	 */
	private static class SetLatexUpdater implements Runnable
	{
		/**
		 * The setting that has to be effected. <code>true</code> to
		 * enable LaTeX rendering; <code>false</code> otherwise.
		 */
		private boolean setting;
		
		/**
		 * Constructs the updater object.
		 * @param b <code>true</code> to enable LaTeX rendering; <code>false</code> otherwise
		 */
		public SetLatexUpdater(boolean b)
		{
			setting=b;
		}
		
		/**
		 * Update the LaTeX rendering setting.
		 */
		public void run()
		{
			Hub.persistentData.setBoolean("useLatexLabels",setting);
			if(setting)
			{
				Iterator<GraphModel> i=Hub.getWorkspace().getGraphModels();
				while(i.hasNext()&&!new LatexPrerenderer(i.next()).wasInterrupted());
			}
			Hub.getWorkspace().notifyAllSubscribers();			
		}
	}
	
	/**
	 * Called when LaTeX rendering of labels is turned on or off from the menu.
	 * <p>A separate method is needed because {@link #setLatexEnabled(boolean)}
	 * modifies the menu item which triggers a call back from the menu item,
	 * which would lead to an infinite loop of calls. 
	 * @param b <code>true</code> to turn LaTeX rendering on, <code>false</code> to turn LaTeX rendering off
	 * @see #setLatexEnabled(boolean)
	 * @see LatexManager.SetLatexUpdater  
	 */
	protected static void setLatexEnabledFromMenu(boolean b)
	{
		SwingUtilities.invokeLater(new SetLatexUpdater(b));
	}
	
	/**
	 * Switches LaTeX rendering of labels on and off.
	 * @param b <code>true</code> to turn LaTeX rendering on, <code>false</code> to turn LaTeX rendering off
	 * @see #setLatexEnabledFromMenu(boolean)
	 * @see LatexManager.SetLatexUpdater  
	 */
	public synchronized static void setLatexEnabled(boolean b)
	{
		if(menuItem==null)
			setLatexEnabledFromMenu(b);
		else
			menuItem.setSelected(b);
	}

	/**
	 * Returns the {@link Renderer} to be used for rendering LaTeX.
	 * @return the {@link Renderer} to be used for rendering LaTeX
	 */
	public static Renderer getRenderer()
	{
		if(renderer==null)
			renderer=Renderer.getRenderer(new File(getLatexPath()),new File(getGSPath()));
		return renderer;
	}
	
	/**
	 * Handle the situation when a LaTeX rendering problem occurs. Turns off
	 * LaTeX rendering of the labels. Asks the user if they wish to verify the LaTeX settings. 
	 */
	public static void handleRenderingProblem()
	{
		setLatexEnabled(false);
		//TODO notify presentation layer of problem - or done through the command?
		SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					int choice=JOptionPane.showConfirmDialog(Hub.getMainWindow(),
							Hub.string("renderProblem"),Hub.string("renderProblemTitle"),
							JOptionPane.YES_NO_OPTION);
					if(choice==JOptionPane.YES_OPTION)
					{
						new OptionsWindow(Hub.string("latexOptionsTitle"));
					}
				}
			});
	}
}
